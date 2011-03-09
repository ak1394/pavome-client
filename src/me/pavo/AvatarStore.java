package me.pavo;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.server.Settings;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;

public class AvatarStore {
	private static AvatarStore instance = null;
	private Hashtable avatars;
	private Connection connection;
	private int avatarCacheSize;
	private Vector avatarCache;
	private int fetchTime;
	private Vector avatarRequests;
	private int avatarFetchSize;
	private boolean isTimerSet = false;
	private Vector avatarRequestTimes;
	private Image unknownAvatar;
	
	private AvatarStore(Connection connection) {
		this.connection = connection;
		avatarCacheSize = Settings.getInt(Settings.AVATAR_CACHE_SIZE);
		fetchTime = Settings.getInt(Settings.AVATAR_FETCH_TIME);
		avatarFetchSize = Settings.getInt(Settings.AVATAR_FETCH_SIZE);
		avatars = new Hashtable();
		avatarRequests = new Vector();
		avatarRequestTimes = new Vector();
		avatarCache = new Vector();
		unknownAvatar = UI.getImage(UI.SMALL_AVATAR);
	}

	public synchronized static void initInstance(Connection connection) {
		instance = new AvatarStore(connection);
	}

	public synchronized static AvatarStore getInstance() {
		return instance;
	}
	
	public synchronized Image getAvatar(Post post) {
		return getAvatar(post.getAuthor());
	}

	public synchronized Image getAvatar(String name) {
		name = name.toLowerCase();
		
		if(!Settings.getBool(Settings.LOAD_AVATARS)) {
			return unknownAvatar;
		}
		
		if(avatars.containsKey(name)) {
			avatarCache.removeElement(name);
			avatarCache.insertElementAt(name, 0);
			return (Image) avatars.get(name);
		} else if(name.equals("~#renderproto#~")) {
			return unknownAvatar;
		}
		
		if(avatarRequests.contains(name)) {
			int index = avatarRequests.indexOf(name);
			Long ts = (Long) avatarRequestTimes.elementAt(index);
			
			avatarRequests.removeElementAt(index);
			avatarRequestTimes.removeElementAt(index);
			
			avatarRequests.insertElementAt(name, 0);
			avatarRequestTimes.insertElementAt(ts, 0);
			
		} else {
			avatarRequests.insertElementAt(name, 0);
			avatarRequestTimes.insertElementAt(new Long(System.currentTimeMillis()), 0);
		}
		
		if(!isTimerSet) {
			UI.getInstance().getTimer().schedule(new AvatarTimerTask(), fetchTime);
			isTimerSet = true;
		}
		
		return unknownAvatar;
	}

	public synchronized void putAvatar(String name, byte[] avatar) {
		Image avatarImage = null;
		try { avatarImage = Image.createImage(new ByteArrayInputStream(avatar)); } catch (Exception e) {};
		if(avatarImage != null) {
			avatars.put(name, avatarImage);
			avatarCache.insertElementAt(name, 0);

			if(avatarCache.size() > avatarCacheSize) {
				avatars.remove(avatarCache.elementAt(avatarCacheSize));
				avatarCache.removeElementAt(avatarCacheSize);
			}
			
			if(avatarRequests.contains(name)) {
				int i = avatarRequests.lastIndexOf(name);
				avatarRequests.removeElementAt(i);
				avatarRequestTimes.removeElementAt(i);
			}
			
			Display.getInstance().callSerially(new Runnable() {
				public void run() {
					Form form = Display.getInstance().getCurrent();
					if (form != null) {
						form.repaint();
					}
				}
			});
		}
	}
	
	public synchronized void requestAvatars(AvatarTimerTask task) {
		for(int i=0; i<avatarRequests.size() && i < avatarFetchSize; i++) {
			String name = (String) avatarRequests.elementAt(i);
			Long ts = (Long) avatarRequestTimes.elementAt(i);
			if(System.currentTimeMillis() - ts.longValue() >= 1000) {
				connection.getAvatar(name).addCallback(task);
			}
		}
		avatarRequests = new Vector();
		avatarRequestTimes = new Vector();
		isTimerSet = false;
	}

	class AvatarTimerTask extends TimerTask implements FutureCallback {
		public void run() {
			requestAvatars(this);
		}

		public void callbackFired(Future future) {
			Hashtable result = (Hashtable) future.getResult();
			String name = ((String) result.get("username")).toLowerCase();
			Object avatar = result.get("avatar");
			putAvatar(name, (byte[]) avatar);
		}
	}
}
