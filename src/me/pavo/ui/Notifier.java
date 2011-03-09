package me.pavo.ui;

import java.util.TimerTask;

import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.server.Message;
import me.pavo.server.MessageListener;
import me.pavo.server.Packet;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Display;

public class Notifier implements MessageListener {
	
	public void messageArrived(Message message) {
		if(message.packet == Packet.TOPIC_MESSAGE) {
			Params params = (Params) message.result;
			if((params.getString("kind").equals("default") || params.getString("kind").equals("search")) && !params.getBoolean("hush")) {
				Future paused = Connection.getInstance().pause();
				paused.addCallback(new Notifier0(params, paused));
			}
		}
	}
	
	class Unpauser  extends TimerTask {
		final Future paused;
		
		public Unpauser(Future paused) {
			this.paused = paused;
		}
		
		public void run() {
			Connection.getInstance().unpause(paused);
		}
	}
	
	class Notifier0 implements Runnable, FutureCallback {
		private Params params;
		private Future paused;
		
		Notifier0(Params params, Future paused) {
			this.params = params;
			this.paused = paused;
		}

		public void callbackFired(Future future) {
			Display.getInstance().callSerially(this);
		}
		
		public void run() {
			Screen current = UI.getInstance().getCurrent();
			String kind = params.getString("kind");
			String name = params.getString("name");
			int count = params.getInt("count");
			
			if(Settings.getBool(Settings.NOTIFICATION_ANIMATION) && current != null && current == Display.getInstance().getCurrent()) {
				NotificationAnimation anim = new NotificationAnimation(kind, name, count);
				anim.start(current);
			}
			
			if(Settings.getBool(Settings.NOTIFICATION_VIBRATE)) {
				Display.getInstance().vibrate(Settings.getInt(Settings.VIBRATION_DURATION));
			} else if(kind.equals("default") && name.equals("mentions") && (Settings.getBool(Settings.NOTIFICATION_VIBRATE_MENTIONS))) {
				Display.getInstance().vibrate(Settings.getInt(Settings.VIBRATION_DURATION));
			} else if(kind.equals("default") && name.equals("dm") && (Settings.getBool(Settings.NOTIFICATION_VIBRATE_DM))) {
				Display.getInstance().vibrate(Settings.getInt(Settings.VIBRATION_DURATION));
			}
			
			UI.getInstance().getTimer().schedule(new Unpauser(paused), 650);
		}
	}
}
