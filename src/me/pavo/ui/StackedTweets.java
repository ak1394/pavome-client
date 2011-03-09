package me.pavo.ui;

import java.util.Stack;

import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.events.ActionEvent;

public class StackedTweets extends Tweets {
	private Stack kinds;
	private Stack names;
	private Command back;
	private Command close;
	
	public StackedTweets(String kind, String name) {
		super(kind, name);
		kinds = new Stack();
		names = new Stack();
		back = new Command("Back", BACK);
		close = new Command("Close", BACK);
		if(Settings.getBool(Settings.TOUCHSCREEN)) {
			removeCommand(new Command("   >>   ", RIGHT));
		}
		addCommand(close);
	}
	
	public void push(String kind, String name) {
		kinds.push(this.kind);
		names.push(this.name);
		this.kind = kind;
		this.name = name;
		setTitle(name);
		removeCommand(close);
		addCommand(back);
		topicManager.close();
		reload();
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getCommand() != null) {
			switch(ev.getCommand().getId()) {
			case BACK:
				back();
				return;
			}
		}
		super.actionPerformed(ev);
	}
	
	private void back() {
		if(names.size() > 0) {
			this.kind = (String) kinds.pop();
			this.name = (String) names.pop();
			if(names.size() == 0) {
				removeCommand(back);
				addCommand(close);
			}
			setTitle(name);
			topicManager.close();
			reload();
		} else {
			topicManager.close();
			UI.getInstance().closeScreen(this);
		}
	}
	
	public void close() {
		// can be called from Screen/run if topic has failed top open
		back();
	}
}
