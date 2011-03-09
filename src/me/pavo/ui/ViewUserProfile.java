package me.pavo.ui;

import me.pavo.Contacts;
import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.PavoButtons;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.GridLayout;

public class ViewUserProfile extends FutureForm implements ActionListener {
	
	private static final int COMMAND_BACK = 0;
	private static final int COMMAND_ACTION = 1;
	private static final int COMMAND_FOLLOW = 2;
	private static final int COMMAND_UNFOLLOW = 3;
	private static final int COMMAND_OPEN_LINK = 4;
	private Params profile;

	public ViewUserProfile(Future future, Connection connection, Form back) {
		super(future, connection, "Loading profile...", back);
	}

	public void build(Object result) {
		this.profile = (Params) result;
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		Command action = new Command(UI.localize("action"), COMMAND_ACTION); 
		Command action1 = new Command(UI.localize("action") + " ", COMMAND_ACTION); 
		Command back = new Command("Back", COMMAND_BACK);
		
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(action);
			addCommand(action1);
			addCommand(back);
		} else {
			addCommand(action);
			addCommand(action1);
		}
		
		setBackCommand(back);
		
		addCommandListener(this);
		
		setTitle(profile.getString("username"));
		setScrollableY(true);
		Container top = new Container(new BorderLayout());
		TextArea name = new TextArea(profile.getString("name"));
		name.setEditable(false);
		name.setFocusable(false);
		name.getStyle().setBorder(null);
		
		top.addComponent(BorderLayout.CENTER, name);
		top.addComponent(BorderLayout.EAST, new Label(profile.getImage("image")));
		top.setFocusable(true);
		addComponent(top);

		if(profile.hasString("url")) {
			Label urlLabel = new Label("Web");
			urlLabel.setAlignment(CENTER);
			urlLabel.setUIID("BoldLabel");
			addComponent(urlLabel);
			Label url = new Label(profile.getString("url"));		
			addComponent(url);
		}
		
		if(profile.hasString("location")) {
			Label locationLabel = new Label("Location");
			locationLabel.setAlignment(CENTER);
			locationLabel.setUIID("BoldLabel");
			addComponent(locationLabel);
			Label location = new Label(profile.getString("location"));		
			addComponent(location);
		}
		
		if(profile.hasString("description")) {
			Label bioLabel = new Label("Bio");
			bioLabel.setUIID("BoldLabel");
			bioLabel.setAlignment(CENTER);
			addComponent(bioLabel);
			TextArea description = new TextArea(profile.getString("description"));
			description.setEditable(false);
			description.setFocusable(false);
			description.getStyle().setBorder(null);
			addComponent(description);
		}
		
		addComponent(new Label(" "));
		
		Container f = new Container(new GridLayout(3, 2));
		
		f.addComponent(label("following"));
		f.addComponent(label(profile.get("friends_count")));
		
		f.addComponent(label("followers"));
		f.addComponent(label(profile.get("followers_count")));
		
		f.addComponent(label("statuses"));
		f.addComponent(label(profile.get("statuses_count")));
		
		f.setFocusable(true);
		
		
		addComponent(f);
	}
	
	Label label(Object str) {
		Label l = new Label((String)str);
		l.getStyle().setBgTransparency(0);
		l.setFocusable(false);
		l.setAlignment(CENTER);
		return l;
	}

	public void callbackFired(final Future future) {
		Display.getInstance().callSerially(new Runnable(){
			public void run() {
				ViewUserProfile.this.build(future.getResult());
				ViewUserProfile.this.show();
			}});
	}

	public String getUIID() {
		return "UserProfile";
	}

	public void actionPerformed(ActionEvent ev) {
		switch(ev.getCommand().getId()) {
		case COMMAND_BACK:
			back.show();
			break;
		case COMMAND_ACTION:
			ActionList actions = new ActionList();
			if(!Contacts.getInstance().isSelf(profile.getString("id"))) {
				if(profile.getString("following").equals("true")) {
					actions.append(COMMAND_UNFOLLOW, "unfollow", UI.BIG_UNFOLLOW);
				} else {
					actions.append(COMMAND_FOLLOW, "follow", UI.BIG_FOLLOW);
				}
			}
			if(profile.hasString("url")) {
				actions.append(COMMAND_OPEN_LINK, "homepage", UI.BIG_OPEN_LINK);
			}
			if(actions.size() > 0) {
				PavoButtons buttons = new PavoButtons(actions, null, Settings.getBool(Settings.TOUCHSCREEN));
				Params result = buttons.showDialog();
				if(result != null) {
					dispatch(result);
				}
			}
			break;
		}
	}
	
	public void dispatch(Params action) {
		switch (action.getInt("action")) {
		case COMMAND_FOLLOW:
			connection.follow(profile.getString("username"));
			back.show();
			break;
		case COMMAND_UNFOLLOW:
			connection.unfollow(profile.getString("username"));
			back.show();
			break;
		case COMMAND_OPEN_LINK:
			UI.openURL(profile.getString("url"));
			back.show();
			break;
		}
	}
}