package me.pavo.ui;

import java.util.Hashtable;
import java.util.Stack;

import me.pavo.server.Settings;

public class Cache {
	
	private static Stack order = new Stack();
	private static Hashtable cache = new Hashtable();
	private static int MAX_SIZE;
	
	
	public Cache() {
		MAX_SIZE = Settings.getInt(Settings.IMAGE_CACHE_SIZE);
	}
	
	public static boolean has(Object key) {
		return cache.containsKey(key);
	}
	
	public static Object get(Object key) {
		if(cache.containsKey(key)) {
			order.removeElement(key);
			order.push(key);
			return cache.get(key);
		}
		return null;
	}
	
	public static void removeAll() {
		order.removeAllElements();
		cache.clear();
	}
	
	public static void put(Object key, Object value) {
		if(cache.containsKey(key)) {
			order.removeElement(key);
			order.push(key);
		} else {
			cache.put(key, value);
			order.push(key);
			if(order.size() > MAX_SIZE) {
				cache.remove(order.firstElement());
				order.removeElement(order.firstElement());
			}
		}
	}
	
}
