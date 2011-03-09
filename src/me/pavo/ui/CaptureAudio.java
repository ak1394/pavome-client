package me.pavo.ui;

import me.pavo.PavoException;
import me.pavo.UI;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Button;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.MediaComponent;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

public class CaptureAudio extends Form implements ActionListener, WorkflowElement, Runnable {
	
	Button button;
	MediaComponent media;
	Capture capture;
	private Handler handler;
	
	public static final int OK = 1;
	public static final int CANCEL = 2;
	
	public CaptureAudio() {
		setTitle(UI.localize("record_audio"));
		capture = new Capture(this);
		setScrollable(false);
		setLayout(new BorderLayout());
		button = new Button("Starting");
		button.setIcon(UI.getImage(UI.BIG_RECORD));
		button.setAlignment(CENTER);
		button.setTextPosition(BOTTOM);
		button.addActionListener(this);
		addComponent(BorderLayout.CENTER, button);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void onShow() {
		new Thread(new Runnable() {
			public void run() {
				Display.getInstance().callSerially(CaptureAudio.this);
			}}).start();
	}
	
	public void run() {
		try {
			capture.initPlayer(Settings.getString(Settings.DEVICE_AUDIO));
			capture.initRecordControl(Settings.getString(Settings.AUDIO_CAPTURE_DIR) + "pavotmp.amr");
			capture.setRecordSizeLimit(Settings.getInt(Settings.AUDIO_CAPTURE_SIZE_LIMIT));
			try { capture.player.start(); } catch (Exception e) { throw new PavoException("playerStart", e); }
			button.setText("Ready");
			capture.state = Capture.STARTING;
		} catch (final PavoException e) {
			try { capture.abortRecording();	} catch (Exception ee) { }
			handle(e);
		}
	}
	
	public synchronized void actionPerformed(ActionEvent ev) {
		try {
			switch (capture.state) {
			case Capture.STARTING:
				button.setText("Recording");
		        capture.recordControl.startRecord();
		        capture.state = Capture.CAPTURING;
				break;
			case Capture.CAPTURING:
				capture.commitRecording();
			case Capture.COMMITED:
				button.setText("Stop");
				capture.stopPlayer();
				capture.state = Capture.FINISHED;
				handle(new Params().set("result", Handler.OK).set("content-type", capture.getContentType()).set("file", capture.getResult()));
				break;
			}
		} catch (PavoException e) {
			if(capture.state == Capture.CAPTURING || capture.state == Capture.COMMITED) {
				try { capture.abortRecording();	} catch (Exception ee) { }
			}
			UI.error("Capture failed at " + e.getLocation(), e);
			handle(e);		
		}
	}
	
	private void handle(Params result) {
		if(handler != null) {
			handler.handle(result);
		}
	}
	
	private void handle(PavoException e) {
		Connection.sendException(this, e);
		handle(new Params().set("result", Handler.ERROR).set("exception", e));
	}
}