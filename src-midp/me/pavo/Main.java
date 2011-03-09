package me.pavo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import me.pavo.server.Connection;
import me.pavo.server.Settings;

import com.sun.lwuit.Display;
import com.sun.lwuit.impl.midp.VKBImplementationFactory;

public class Main extends javax.microedition.midlet.MIDlet {
	public static Main INSTANCE;
	
	public Main() throws IOException {
	}

	public void stopApplication() {
		notifyDestroyed();
	}

    public String getSystemProperty(String property) {
    	return System.getProperty(property);
    }
	
    public String getProperty(String property) {
    	return getAppProperty(property);
    }
	
    public String getPlatformProperty(String property) {
    	return null;
    }
    
    public static int remapKey(int keyCode) {
		return keyCode;
    }
    
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		if (INSTANCE == null) {
			INSTANCE = this;
			
			Settings.init();
			Connection connection = new Connection(getProperty("Pavo-Server") == null ? "delta.pavo.me:2222" : getProperty("Pavo-Server"));
			
			try {
				connection.start();
			} catch (Exception e) {
				Display.init(this);
				UI.error("Unable to connect to server: " + e);
				stopApplication();
				return;
			}
			
			if(Settings.getBool(Settings.VKB)) {
				VKBImplementationFactory.init();			
			}
			
			Display.init(this);
			
			Settings.postDisplayInit();
			
			UI ui = new UI();
			try {
				ui.init(getProperty("Pavo-Token"), getProperty("MIDlet-Version") == null ? "1.1.1" : getProperty("MIDlet-Version"));
			} catch (Exception e) {
				notifyDestroyed();
			}
		}
	}

	public void saveConfig(Hashtable config) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(1);
			dos.writeInt(config.size());
			for (Enumeration e = config.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				dos.writeUTF(key);
				dos.writeUTF(config.get(key).toString());
			}

			byte[] bytes = baos.toByteArray();

			RecordStore rs = RecordStore.openRecordStore("pavome", true);
			if (rs.getNumRecords() == 0) {
				rs.addRecord(bytes, 0, bytes.length);
			} else {
				RecordEnumeration e = rs.enumerateRecords(null, null, false);
				int id = e.nextRecordId();
				rs.setRecord(id, bytes, 0, bytes.length);
			}
			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Hashtable getSavedConfig() {
		Hashtable result = new Hashtable();
		try {
			RecordStore rs = RecordStore.openRecordStore("pavome", true);
			if (rs.getNumRecords() > 0) {
				RecordEnumeration e = rs.enumerateRecords(null, null, false);
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(e.nextRecord()));
				int version = dis.readInt();
				int count = dis.readInt();
				for (int i = 0; i < count; i++) {
					String key = dis.readUTF();
					String value = dis.readUTF();
					result.put(key, value);
				}
			}
			rs.closeRecordStore();
		} catch (Exception e) {
			return result;
		}
		return result;
	}

	public boolean openURL(String url) {
		try {
			return platformRequest(url);
		} catch (ConnectionNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public  SocketConnection openConnection(String server) throws IOException {
		SocketConnection connection = (SocketConnection) Connector.open("socket://"	+ server);
		return connection;
	}
}
