package me.pavo.logic;

import me.pavo.NewPost;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.Confirmation;
import me.pavo.ui.EditText;

public class TextTweetWF extends Workflow {

	private Connection connection;
	private int softlimit;
	private int hardlimit;	
	private String reference;
	private String text;
	
	public TextTweetWF() {
		this("", null, 140, 140);
	}
	public TextTweetWF(String text, String reference, int softlimit, int hardlimit) {
		this.connection = Connection.getInstance();
		this.text = text;
		this.reference = reference;
		this.softlimit = softlimit;
		this.hardlimit = hardlimit;
	}
	
	public void run() {
		Params result = show(new EditText(text, softlimit));
		if(isOk(result)) {
			String text = result.getString("text");
			if(text.length() > hardlimit) {
				int extra_length = text.length() - hardlimit;
				String question = "Your tweet is " + extra_length + " chars too long. Would you like to trim it, or re-edit?";
				if(isOk(show(new Confirmation(question, "Trim", "Edit")))) {
					text = text.substring(0, hardlimit);
				} else {
					new TextTweetWF(text, reference, softlimit, hardlimit).start();
					return;
				}
			}
			
			NewPost newPost = new NewPost();
		    newPost.set(NewPost.BODY, text);
		    if(reference != null) { 
		    	newPost.set(NewPost.IRT, reference);
		    }
		    connection.sendPost(newPost);
		}
		showCurrentScreen();
	}
}
