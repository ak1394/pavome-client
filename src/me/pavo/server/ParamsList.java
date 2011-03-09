package me.pavo.server;

import java.util.Enumeration;
import java.util.Vector;

public class ParamsList extends Vector {
	
	public Params get(int index) {
		return (Params) super.elementAt(index);
	}

	public Params find(Object key, Object value) {
		for(int i=0; i<size(); i++) {
			Params params = get(i);
			if(params.containsKey(key) && params.get(key).equals(value)) {
				return params;
			}
		}
		return null;
	}
	
	public void append(ParamsList paramsList) {
		for(Enumeration e = paramsList.elements(); e.hasMoreElements();) {
			super.addElement(e.nextElement());
		}
	}
	
	public void prepend(ParamsList paramsList) {
		for(int i = paramsList.size()-1; i>=0;i++) {
			super.insertElementAt(paramsList.get(i), 0);
		}
	}
	
	public int append(Params params) {
		super.addElement(params);
		return size()-1;
	}

	public void prepend(Params params) {
		super.insertElementAt(params, 0);
	}
		
	public Params pop() {
		Params result = null;
		if(size() > 0) {
			result = (Params) lastElement();
			removeElementAt(size() - 1);
		}
		return result;
	}
	
	public boolean contains(String name, int value) {
		return contains(name, new Integer(value));
	}
	
	public boolean contains(String name, Object value) {
		for(int i=0; i<size(); i++) {
			if(get(i).has(name) && get(i).get(name).equals(value)) { 
				return true;
			}
		}
		return false;
	}
	
	public void delete(String name, int value) {
		delete(name, new Integer(value));
	}

	public void delete(String name, Object value) {
		for(Enumeration e = elements(); e.hasMoreElements();) {
			Params current = (Params) e.nextElement();
			if(current.has(name) && current.get(name).equals(value)) {
				removeElement(current);
			}
		}
	}
}
