package me.pavo.server;

import java.util.Enumeration;
import java.util.Hashtable;

import me.pavo.Main;
import me.pavo.UI;

import com.sun.lwuit.Display;

public class Service implements MessageListener {
	private Connection connection;
	
	public Service(Connection connection) {
		this.connection = connection;
	}
	
	public void messageArrived(final Message message) {
		switch(message.packet) {
		case Packet.CHECK_REQUEST:
			new Thread(new Runnable() {
				public void run() {
					Hashtable result = performTests((Hashtable) message.result);
					connection.sendCheckResult(result);
				}
			}).start();
			break;
		case Packet.PING:
			connection.sendPong();
			break;
		case Packet.SETTINGS:
			Settings.update((Hashtable) message.result);
			break;
		case Packet.ERROR_MESSAGE:
			if(message.id == 0) {
				UI.error((String) message.result);
			}
			break;
		}
	}
	
	private Hashtable performTests(Hashtable tests) {
		Enumeration keys = tests.keys();
		Hashtable result = new Hashtable();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			try {
				switch(((Integer)tests.get(key)).intValue()) {
				case Packet.TEST_SYSTEM_PROPERTY:
					testSystem(result, key);
					break;
				case Packet.TEST_MIDLET_PROPERTY:
					testMidlet(result, key);
					break;
				case Packet.TEST_PLATFORM_PROPERTY:
					testPlatform(result, key);
					break;
				case Packet.TEST_PERMISSION:
					testPermission(result, key);
					break;
				case Packet.TEST_CLASS:
					testClass(result, key);
					break;
				case Packet.TEST_MEMORY:
					testMemory(result, key);
					break;
				case Packet.TEST_LWUIT:
					testLWUIT(result, key);
					break;
				case Packet.TEST_PLATFORM_REQUEST:
					testPlatformRequest(result, key);
					break;
				case Packet.TEST_PLATFORM_REQUEST_FATAL:
					testPlatformRequestFatal(result, key);
					break;
				case Packet.TEST_DIALOG_INFO:
					testDialogInfo(result, key);
					break;
				case Packet.TEST_DIALOG_CONFIRM:
					testDialogConfirm(result, key);
					break;
				}
			} catch(Error e) {
				saveResult("error", result, key, e.getClass().toString()+ ":" + e.getMessage());
			}
		}	
		return result;
	}
	
	private void testSystem(Hashtable result, String key) {
		saveResult("system", result, key, Main.INSTANCE.getSystemProperty(key));
	}

	private void testMidlet(Hashtable result, String key) {
		saveResult("midlet", result, key, Main.INSTANCE.getProperty(key));
	}

	private void testPlatform(Hashtable result, String key) {
		saveResult("platform", result, key, Main.INSTANCE.getPlatformProperty(key));
	}
	
	private void testPermission(Hashtable result, String key) {
		saveResult("permission", result, key, new Integer(Main.INSTANCE.checkPermission(key)));
	}
	
	private void testClass(Hashtable result, String key) {
		try {
			Class.forName(key);
			saveResult("class", result, key, "true");
		} catch (ClassNotFoundException e) {
			saveResult("class", result, key, "false");
		} catch(Error e) {
			saveResult("class", result, key, "error");
		}
	}

	private void testMemory(Hashtable result, String key) {
		if(key.equals("total"))	saveResult("memory", result, key, Long.toString((Runtime.getRuntime().totalMemory())));
		else if(key.equals("free")) saveResult("memory", result, key, Long.toString((Runtime.getRuntime().freeMemory())));
	}
	
	private void testLWUIT(Hashtable result, String key) {
		Display display = Display.getInstance();
		if(key.equals("width")) saveResult("lwuit", result, key, new Integer(display.getDisplayWidth()));
		else if (key.equals("height")) saveResult("lwuit", result, key, new Integer(display.getDisplayHeight()));
		else if (key.equals("colors")) saveResult("lwuit", result, key, new Integer(display.numColors()));
		else if (key.equals("alpha")) saveResult("lwuit", result, key, new Integer(display.numAlphaLevels()));
		else if (key.equals("touchscreen")) saveResult("lwuit", result, key, new Boolean(display.isTouchScreenDevice()));
		else if (key.equals("multitouch")) saveResult("lwuit", result, key, new Boolean(display.isMultiTouch()));
	}
	
	private void testPlatformRequest(Hashtable result, String key) {
		try {
			saveResult("result", result, key, new Boolean(Main.INSTANCE.openURL(key))); 
		} catch(Exception e) {
			saveResult("result", result, key, e.toString());
		}
	}

	private void testPlatformRequestFatal(Hashtable result, String key) {
		try {
			saveResult("result", result, key, new Boolean(Main.INSTANCE.openURL(key)));
			new Thread(new Runnable() {
				public void run() {
					try { Thread.sleep(1000); } catch (InterruptedException e) {}
					Main.INSTANCE.stopApplication();
				}}).start();
		} catch(Exception e) {
			saveResult("result", result, key, e.toString());
		}
	}
	
	private void saveResult(String type, Hashtable result, String key, Object value)
	{
		if(value != null) {
			result.put(type + ":" + key, value);
		}
	}
	
	private void testDialogInfo(Hashtable result, String key) {
		saveResult("result", result, key, new Boolean(UI.notify(key)));
	}
	
	private void testDialogConfirm(Hashtable result, String key) {
		saveResult("result", result, key, new Boolean(UI.confirm(key, "Yes", "No")));
	}
}
