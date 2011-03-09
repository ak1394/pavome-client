package me.pavo.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import me.pavo.DummyCanvas;
import me.pavo.Main;
import me.pavo.PavoException;
import me.pavo.logic.Showable;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

public class CaptureVideo extends javax.microedition.lcdui.Canvas implements CommandListener, WorkflowElement, ActionListener, Showable {
	private Capture capture;
	private VideoControl vidc;
	private Command cmStart;
	private Command cmStop;
	private Command cmCancel;
	private Handler handler;

	public static final int OK = 1;
	public static final int CANCEL = 2;
	
	public CaptureVideo() {
		capture = new Capture(this);
	    cmStart = new Command("Start", Command.OK, 1);
	    cmStop = new Command("Stop", Command.OK, 2);
	    cmCancel = new Command("Cancel", Command.CANCEL, 3);
	    addCommand(cmStart);
	    addCommand(cmCancel);
	    setCommandListener(this);	    
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	protected void showNotify() {
		try {
			if (capture.state == Capture.INIT) {
				initPlayer();
				if (!Settings.getBool(Settings.VIDEO_CAPTURE_RECORD_BEFORE_PLAY)) {
					initRecording();
				}
				startPlayer();
				capture.state = Capture.STARTING;
			}
		} catch (PavoException e) {
			handle(e);
		}
	}
	
	public void commandAction(Command c, Displayable d) {
		if (c == cmStart) {
			removeCommand(cmStart);
			addCommand(cmStop);
			actionPerformed(new ActionEvent(cmStart));
		} else if (c == cmStop) {
			actionPerformed(new ActionEvent(cmStop));
		} else if (c == cmCancel) {
			capture.abortRecording();
			capture.state = Capture.FINISHED;
			handle(new Params().set("result", Handler.CANCEL));
		}
	}
	
	public synchronized void actionPerformed(ActionEvent ev) {
		try {
			switch (capture.state) {
			case Capture.STARTING:
				if(Settings.getBool(Settings.VIDEO_CAPTURE_RECORD_BEFORE_PLAY)) {
					teardownPlayer();
					initPlayer();
					initRecording();
					startRecording();
					startPlayer();
				} else {
					startRecording();
				}
		        capture.state = Capture.CAPTURING;
				break;
			case Capture.CAPTURING:
				finishRecording();
				break;
			case Capture.COMMITED:
				finishRecording();
				break;
			}
		} catch (PavoException e) {
			if(capture.state == Capture.CAPTURING || capture.state == Capture.COMMITED) {
				try { capture.abortRecording();	} catch (Exception ee) { }
			}
			handle(e);
		}
	}

	void initPlayer() throws PavoException {
		capture.initPlayer(Settings.getString(Settings.DEVICE_VIDEO));
		vidc = (VideoControl) ((Player) capture.player).getControl("VideoControl");
		vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
		try {vidc.setDisplayFullScreen(Settings.getBool(Settings.VIDEO_CAPTURE_FULLSCREEN));} catch (Exception e) { throw new PavoException("setFullscreen", e); }
		vidc.setVisible(true);
	}
	
	void initRecording() throws PavoException {
		capture.initRecordControl(Settings.getString(Settings.VIDEO_CAPTURE_DIR) + "pavotmp.3gp");
		capture.setRecordSizeLimit(Settings.getInt(Settings.VIDEO_CAPTURE_SIZE_LIMIT));
	}
	
	void startPlayer() throws PavoException {
		try { capture.player.start(); } catch (Exception e) { throw new PavoException("playerStart", e); }
	}
	
	void startRecording() throws PavoException {
        capture.recordControl.startRecord();
	}
		
	void teardownPlayer() {
		vidc.setVisible(false);
		capture.stopPlayer();
		vidc = null;
	}
	
	void finishRecording() throws PavoException {
		if(capture.state == Capture.CAPTURING) {
			capture.commitRecording();	
		}
		teardownPlayer();
		capture.state = Capture.FINISHED;
		// doing this or will get Prefetch error -14 on nokia n95
		javax.microedition.lcdui.Display.getDisplay(Main.INSTANCE).setCurrent(new DummyCanvas());
		handle(new Params().set("result", Handler.OK).set("content-type", capture.getContentType()).set("file", capture.getResult()));
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

	protected void paint(Graphics arg0) {
	}
	
	public void show() {
		javax.microedition.lcdui.Display.getDisplay(Main.INSTANCE).setCurrent(this);
	}
	
}
