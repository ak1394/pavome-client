package me.pavo.ui;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import me.pavo.Main;
import me.pavo.PavoException;
import me.pavo.Post;
import me.pavo.logic.Showable;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;

public class VideoView extends javax.microedition.lcdui.Canvas implements  CommandListener, FutureCallback, Showable {
	private Player player;
	private VideoControl vidc;
	private Form form;
	private Waiting waiting;	

	public VideoView(Form form, String kind, String name, Post post) {
		this.form = form;
		waiting = new Waiting("Connecting");
		waiting.show();
	    addCommand(new Command("Stop", Command.OK, 1));
	    setCommandListener(this);
		Connection.getInstance().getAttachment(kind, name, post.getId(), Post.ATTACHMENT_VIDEO).addCallback(this);
	}
	
	public void commandAction(Command c, Displayable d)
	{
		stop();
	}

	private synchronized void play(String url) {
		try {
			player = Manager.createPlayer(url);
			player.prefetch();
			player.realize();
    		
			javax.microedition.lcdui.Display.getDisplay(Main.INSTANCE).setCurrent(this);
			
    		vidc = (VideoControl) ((Player) player).getControl("VideoControl");
    		vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
    		vidc.setDisplayFullScreen(true);
    		vidc.setVisible(true);
            
    		player.start();
		} catch (Exception e) {
			Connection.sendException(this, new PavoException("play", e));
		}
	}
	
	private synchronized void stop() {
		if(player != null) {
			vidc.setVisible(false);
			try { player.stop(); } catch (Exception e) { }
			try { player.close(); } catch (Exception e) { }
			try { player.deallocate(); } catch (Exception e) { }
			player = null;
		}
		
		Display.getInstance().callSerially(new Runnable() {
			public void run() {
				form.show();
			}
		});
	}
	
	public void callbackFired(Future future) {
		String location = (String) ((Hashtable) future.getResult()).get("video");
			play(location);
		}

	protected void paint(Graphics arg0) {
	}
	
	public void show() {
		javax.microedition.lcdui.Display.getDisplay(Main.INSTANCE).setCurrent(this);
	}
	
}