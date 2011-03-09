package me.pavo.logic;

import me.pavo.NewPost;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.EditText;

public class ReplyToDmWF extends Workflow {

	private int softlimit;
	private int hardlimit;	
	private String recipient;
	private String text;
	
	public ReplyToDmWF(String text, String recipient, int softlimit, int hardlimit) {
		this.text = text;
		this.recipient = recipient;
		this.softlimit = softlimit;
		this.hardlimit = hardlimit;
	}
	
	public void run() {
		Params result = show(new EditText(text, softlimit));
		if(isOk(result)) {
			NewPost newPost = new NewPost();
            newPost.set(NewPost.BODY, result.getString("text"));
            newPost.set(NewPost.DM, recipient);
		    Connection.getInstance().sendPost(newPost);
		}
		showCurrentScreen();
	}
}