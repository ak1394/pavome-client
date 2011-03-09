package me.pavo.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import me.pavo.Main;
import me.pavo.PavoException;
import me.pavo.logic.Showable;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

public class CapturePhoto extends javax.microedition.lcdui.Canvas implements CommandListener, WorkflowElement, ActionListener, Showable {
	
	public static final int OK = 1;
	public static final int CANCEL = 2;
	
	private Capture capture;
	private Handler handler;
	private VideoControl vidc;
	private byte[] snapshot;
	private Command cmCancel;
	private Command cmOk;
	
	public CapturePhoto() {
		capture = new Capture(this);
	    cmOk = new Command("Ok", Command.OK, 1);
	    cmCancel = new Command("Cancel", Command.CANCEL, 3);
	    addCommand(cmOk);
	    addCommand(cmCancel);
	    setCommandListener(this);	    
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	protected void showNotify() {
		Cache.removeAll();
		try {
			if(capture.state == Capture.INIT) {
				capture.initPlayer(Settings.getString(Settings.DEVICE_PHOTO));
				vidc = (VideoControl) ((Player) capture.player).getControl("VideoControl");
				vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
				vidc.setVisible(true);
				try {vidc.setDisplayFullScreen(Settings.getBool(Settings.PHOTO_CAPTURE_FULLSCREEN));} catch (Exception e) { throw new PavoException("setFullscreen", e); }
				try { capture.player.start(); } catch (Exception e) { throw new PavoException("playerStart", e); }
				capture.state = Capture.STARTING;
			}
		} catch (PavoException e) {
				handle(e);
		}
	}
	
	public void commandAction(Command c, Displayable d)  {
		try {
			if (c == cmOk) {
				snapshot = vidc.getSnapshot(Settings	.getString(Settings.PHOTO_SNAPSHOT_PARAM));
				capture.stopPlayer();
				handle(new Params().set("result", Handler.OK).set("snapshot", snapshot));
			} else if (c == cmCancel) {
				capture.abortRecording();
				capture.state = Capture.FINISHED;
				capture.stopPlayer();
				handle(new Params().set("result", Handler.CANCEL));
			}
		} catch (Exception e) {
			capture.stopPlayer();
			handle(new PavoException("getSnapshot", e));
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

	public void actionPerformed(ActionEvent evt) {
	}

	protected void paint(Graphics arg0) {
	}
	
	public void show() {
		javax.microedition.lcdui.Display.getDisplay(Main.INSTANCE).setCurrent(this);
	}
}
