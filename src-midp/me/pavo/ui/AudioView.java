package me.pavo.ui;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import me.pavo.Main;
import me.pavo.PavoException;
import me.pavo.Post;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.logic.Showable;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;

public class AudioView extends javax.microedition.lcdui.Canvas implements  CommandListener, FutureCallback, Showable {
	private Player player;
	private Form form;
	private Waiting waiting;	

	public AudioView(Form form, String kind, String name, Post post) {
		this.form = form;
		waiting = new Waiting("Connecting");
		waiting.show();
	    addCommand(new Command("Stop", Command.OK, 1));
	    setCommandListener(this);
		Connection.getInstance().getAttachment(kind, name, post.getId(), Post.ATTACHMENT_AUDIO).addCallback(this);
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
    		player.start();
		} catch (Exception e) {
			Connection.sendException(this, new PavoException("play", e));
		}
	}
	
	private synchronized void stop() {
		if(player != null) {
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
		String location = (String) ((Hashtable) future.getResult()).get("audio");
			play(location);
	}

	protected void paint(Graphics g) {
		int width = getWidth();
	    int height = getHeight();
	    g.setColor(0, 0, 0);
	    g.drawString("Playing", width / 2, height / 2, Graphics.BASELINE | Graphics.HCENTER);
	 }

	public void show() {
		javax.microedition.lcdui.Display.getDisplay(Main.INSTANCE).setCurrent(this);
	}
}