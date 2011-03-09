package me.pavo;

import com.sun.lwuit.Image;

public class User {
	public String username;
	public String fullname;
	public Image avatar;

	User(String username, String fullname, Image avatar) {
		this.username = username;
		this.fullname = fullname;
		this.avatar = avatar;
	}

}
