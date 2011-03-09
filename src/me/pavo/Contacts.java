package me.pavo;

import me.pavo.server.Connection;
import me.pavo.server.Params;

public class Contacts {
	private static Contacts instance;
	private String[] ids;
	
	public Contacts() {
		Contacts.instance = this;
	}
	
	public void init() {
		Params result = (Params) Connection.getInstance().getContacts().getResult();
		ids = (String[]) result.get("id");
	}
	
	public static Contacts getInstance() {
		return instance;
	}
	
	public boolean isSelf(String id) {
		for(int i=0; i<ids.length;i++) {
			if(ids[i].equals(id)) {
				return true;
			}
		}
		return false;
	}
}

