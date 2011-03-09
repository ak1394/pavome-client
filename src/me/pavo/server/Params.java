package me.pavo.server;

import java.util.Hashtable;

import javax.microedition.io.file.FileConnection;

import com.sun.lwuit.Image;

public class Params extends Hashtable {
	
	public Params() {
		super();
	}

	public boolean hasString(String key) {
		return containsKey(key) && ((String) get(key)).length() > 0;
	}

	public boolean has(String key) {
		return containsKey(key);
	}
	
	public String getString(String key) {
		return (String) get(key);
	}

	public int getInt(Object key) {
		return ((Integer) get(key)).intValue();
	}

	public int getInt(Object key, int defaultValue) {
		if(containsKey(key)) {
			return ((Integer) get(key)).intValue();
		} else {
			return defaultValue;
		}
	}
	
	public Integer getInteger(Object key) {
		return (Integer) get(key);
	}
	
	public long getLong(String key) {
		return ((Long) get(key)).longValue();
	}

	public FileConnection getFile(String key) {
		return (FileConnection) get(key);
	}
	
	public Image getImage(String key) {
		return (Image) get(key);
	}
	
	public boolean getBoolean(String key) {
		return ((Boolean)get(key)).booleanValue();
	}	

	public Params set(String key, int v) {
		put(key, new Integer(v));
		return this;
	}

	public Params set(String key, long v) {
		put(key, new Long(v));
		return this;
	}
	
	public Params set(String key, boolean v) {
		put(key, new Boolean(v));
		return this;
	}

	public Params set(int key, int value) {
		put(new Integer(key), new Integer(value));
		return this;
	}
	
	public Params set(String key, Object v) {
		if(key != null && v != null) {
			put(key, v);
		}
		return this;
	}
}
