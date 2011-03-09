package me.pavo.ui;

import me.pavo.server.Params;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

public class KeyboardDispatcher implements ActionListener {
	private Tweets tweets;
	
	public KeyboardDispatcher(Tweets tweets) {
		this.tweets = tweets;
	}

	public void actionPerformed(ActionEvent evt) {
		tweets.dispatch(new Params().set("action", Tweets.CONTEXT_REPLY));
	}

}
