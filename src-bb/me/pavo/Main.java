package me.pavo;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.SocketConnection;

import me.pavo.server.Connection;
import me.pavo.server.Settings;
import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.UiApplication;

import com.sun.lwuit.Display;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.util.EventDispatcher;

public abstract class Main extends UiApplication {
	public static Main INSTANCE;
	public static String token;
	public static String server;
	public static String version;
	private static long PERSISTED_KEY = 22;
	public static Object foregoundEvent = new Object();
	public static Object backgroundEvent = new Object();
	
	private static EventDispatcher foregroundDispatcher;
	private static EventDispatcher backgroundDispatcher;	
	
	
	public Main()  {
		foregroundDispatcher = new EventDispatcher();
		backgroundDispatcher = new EventDispatcher();
	}
	
	public static void addForegroundEventListener(ActionListener listener) {
		foregroundDispatcher.addListener(listener);
	}

	public static void addBackgroundEventListener(ActionListener listener) {
		backgroundDispatcher.addListener(listener);
	}

	
	public static void removeForegroundEventListener(ActionListener listener) {
		foregroundDispatcher.removeListener(listener);
	}

	public static void removeBackgroundEventListener(ActionListener listener) {
		backgroundDispatcher.removeListener(listener);
	}

    public boolean openURL(String url) {
    	Browser.getDefaultSession().displayPage(url);
    	return true;
    }

    public int checkPermission(String permission) {
    	return 0;
    }
    
    public String getSystemProperty(String property) {
    	return null;
    }
    
    public void activate() {
    	foregroundDispatcher.fireActionEvent(new ActionEvent(foregoundEvent));
    }
    
    public void deactivate() {
    	backgroundDispatcher.fireActionEvent(new ActionEvent(backgroundEvent));
    }
    
    public String getPlatformProperty(String property) {
    	if(property.equals("device_name")) {
    		return DeviceInfo.getDeviceName();
    	} else if(property.equals("platform_version")) {
    		return DeviceInfo.getPlatformVersion();
    	} else if(property.equals("software_version")) {
    		return DeviceInfo.getSoftwareVersion();
    	} else if(property.equals("has_camera")) {
    		return String.valueOf(DeviceInfo.hasCamera());
    	} else if(property.equals("is_simulator")) {
    		return String.valueOf(DeviceInfo.isSimulator());
    	} else if(property.equals("battery_level")) {
    		return String.valueOf(DeviceInfo.getBatteryLevel());
    	} else if(property.equals("locale_country")) {
    		return Locale.getDefaultForSystem().getCountry();
    	} else if(property.equals("locale_language")) {
    		return Locale.getDefaultForSystem().getLanguage();
    	}
    	return null;
    }
    
    public static int remapKey(int keyCode) {
		switch(keyCode) {
		case 'e': // up
		case 'E': // up
			return Display.getInstance().getKeyCode(Display.GAME_UP);
		case 'x': // down
		case 'X': // down
			return Display.getInstance().getKeyCode(Display.GAME_DOWN);
		case 's': // left
		case 'S': // left
			return Display.getInstance().getKeyCode(Display.GAME_LEFT);
		case 'f': // right
		case 'F': // right
			return Display.getInstance().getKeyCode(Display.GAME_RIGHT);
		case 'd': // fire
		case 'D': // fire
			return Display.getInstance().getKeyCode(Display.GAME_FIRE);
		default:
			return keyCode;
		}
    }
    
    public String getProperty(String property) {
    	if(property.equals("Pavo-Token") && token != null) return token;
    	if(property.equals("Pavo-Server") && server != null) return server;
    	if(property.equals("MIDlet-Version") && version != null) return version;
    	
    	int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle(); 
    	String moduleName = CodeModuleManager.getModuleName(moduleHandle);
    	CodeModuleGroup[] allGroups = CodeModuleGroupManager.loadAll();
    	CodeModuleGroup myGroup = null;
    	for(int i=0; i < allGroups.length; i++)
    	        { 
    	           if(allGroups[i].containsModule(moduleName)) 
    	             { myGroup = allGroups[i];
    		       break;
    		     }
    		}
    	if(myGroup != null) {
        	return myGroup.getProperty(property);    	
    	} else {
    		return null;
    	}
    }

    public void stopApplication() {
        System.exit(0);    	
    }
    
	protected void start() {
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
			
			Display.init(this);
			
			Settings.postDisplayInit();
			
			UI ui = new UI();
			try {
				ui.init(getProperty("Pavo-Token"), getProperty("MIDlet-Version") == null ? "1.1.2" : getProperty("MIDlet-Version"));
			} catch (Exception e) {
				stopApplication();
			}
		}
	}
	
	public void saveConfig(Hashtable config) {
        PersistentObject persistent = PersistentStore.getPersistentObject(PERSISTED_KEY);
        persistent.setContents(config);
        persistent.commit();
	}

	public Hashtable getSavedConfig() {
        PersistentObject config = PersistentStore.getPersistentObject(PERSISTED_KEY);
        if(config.getContents() == null) {
        	return new Hashtable();
        } else {
    		return	(Hashtable) config.getContents();
        }
	}
	
	public abstract SocketConnection openConnection(String server) throws IOException;
	
	public String getConnectionSuffix() {
		String connSuffix = "";

		if (DeviceInfo.isSimulator()) {
			connSuffix = ";deviceside=true";
		} else if ((WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED) && RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN)) {
			connSuffix = ";interface=wifi";
		} else {
			String uid = null;
			ServiceBook sb = ServiceBook.getSB();
			ServiceRecord[] records = sb.findRecordsByCid("WPTCP");
			for (int i = 0; i < records.length; i++) {
				if (records[i].isValid() && !records[i].isDisabled()) {
					if (records[i].getUid() != null
							&& records[i].getUid().length() != 0) {
						if ((records[i].getCid().toLowerCase().indexOf("wptcp") != -1)
								&& (records[i].getUid().toLowerCase().indexOf(
										"wifi") == -1)
								&& (records[i].getUid().toLowerCase().indexOf(
										"mms") == -1)) {
							uid = records[i].getUid();
							break;
						}
					}
				}
			}
			if (uid != null) {
				// WAP2 Connection
				connSuffix = ";ConnectionUID=" + uid;
			} else {
				connSuffix = ";deviceside=true";
			}
		}
		return connSuffix;
	}
}
