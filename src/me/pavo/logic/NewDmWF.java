package me.pavo.logic;

import me.pavo.NewPost;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.EditText;
import me.pavo.ui.GetUsername;

public class NewDmWF extends Workflow {
	
	public NewDmWF() {
	}
	
	public void run() {
		Params username = show(new GetUsername());
		if(isOk(username)) {
			Params text = show(new EditText("", 140));
			if(isOk(text)) {
				NewPost newPost = new NewPost();
	            newPost.set(NewPost.BODY, text.getString("text"));
	            newPost.set(NewPost.DM, username.getString("username"));
			    Connection.getInstance().sendPost(newPost);
			}
		}
		showCurrentScreen();
	}
}