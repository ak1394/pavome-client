package me.pavo.ui;

import me.pavo.UI;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Button;
import com.sun.lwuit.CheckBox;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BoxLayout;

public class SettingsForm extends Form {
	private Command ok;
	private Command cancel;
	
	private ComboBox tweetsOrderBox;
	private String[] tweetsOrderOptions = { "At the Top", "At the Bottom" };

	private ComboBox photoService;
	private String[] photoServiceOptions = { "TweetPhoto", "PavoMe" };
	
	private ComboBox fontBox;
	private String[] fontOptions = { "Medium", "Small", "International Small", "International Medium", "International Large"};

	private ComboBox vibrateBox;
	private String[] vibrateOptions = { "All new tweets", "Mentions and DMs", "Mentions", "DMs", "Disable"};
	
	private CheckBox notificationAnimation;

	private ComboBox swapControlsBox;
	private String[] swapControlsOptions = { "Tweet, Action", "Action, Tweet"};

	private ComboBox avatarsBox;
	private String[] avatarsOptions = { "Load", "Don't load"};
	
	public SettingsForm() {
		setUIID("Settings");
		setTitle("Settings");
		
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		{
			Container settings = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			settings.addComponent(new Label("Show new tweets"));
			tweetsOrderBox = new ComboBox(tweetsOrderOptions);
			if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
				tweetsOrderBox.setSelectedIndex(1);
			} else {
				tweetsOrderBox.setSelectedIndex(0);
			}
			settings.addComponent(tweetsOrderBox);
			addComponent(settings);
		}

		{
			Container settings = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			settings.addComponent(new Label("Photo sharing"));
			photoService = new ComboBox(photoServiceOptions);
			if(Settings.getString(Settings.PHOTO_SERVICE).equals("tweetphoto")) {
				photoService.setSelectedIndex(0);
			} else {
				photoService.setSelectedIndex(1);
			}
			settings.addComponent(photoService);
			addComponent(settings);
		}

		{
			Container settings = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			settings.addComponent(new Label("Profile Pictures"));
			avatarsBox = new ComboBox(avatarsOptions);
			if(Settings.getBool(Settings.LOAD_AVATARS)) {
				avatarsBox.setSelectedIndex(0);
			} else {
				avatarsBox.setSelectedIndex(1);
			}
			settings.addComponent(avatarsBox);
			addComponent(settings);
		}
		
		Container font = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		font.addComponent(new Label("Font"));
		fontBox = new ComboBox(fontOptions);
		if(Settings.getString(Settings.MAIN_FONT).equals("sys-large")) {
			fontBox.setSelectedIndex(4);
		} else if(Settings.getString(Settings.MAIN_FONT).equals("sys-medium")) {
			fontBox.setSelectedIndex(3);
		} else if(Settings.getString(Settings.MAIN_FONT).equals("sys-main")) {
			fontBox.setSelectedIndex(2);
		}  else if(Settings.getString(Settings.MAIN_FONT).equals("tiny")) {
			fontBox.setSelectedIndex(1);
		} else {
			fontBox.setSelectedIndex(0);	
		}
		
		font.addComponent(fontBox);
		addComponent(font);

		{
			Container settings = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			settings.addComponent(new Label("Vibrate for new:"));
			vibrateBox = new ComboBox(vibrateOptions);
			if(Settings.getBool(Settings.NOTIFICATION_VIBRATE)) {
				vibrateBox.setSelectedIndex(0);
			} else if(Settings.getBool(Settings.NOTIFICATION_VIBRATE_MENTIONS) && Settings.getBool(Settings.NOTIFICATION_VIBRATE_DM)) {
					vibrateBox.setSelectedIndex(1);
			} else if(Settings.getBool(Settings.NOTIFICATION_VIBRATE_MENTIONS)) {
				vibrateBox.setSelectedIndex(2);
			} else if(Settings.getBool(Settings.NOTIFICATION_VIBRATE_DM)) {
				vibrateBox.setSelectedIndex(3);
			} else {
				vibrateBox.setSelectedIndex(4);
			}
			settings.addComponent(vibrateBox);
			addComponent(settings);
		}
		
		Container notifications = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		notificationAnimation = new CheckBox("Animation for new tweets");
		notificationAnimation.setSelected(Settings.getBool(Settings.NOTIFICATION_ANIMATION));
		notifications.addComponent(notificationAnimation);
		
		addComponent(notifications);

		if(!Settings.getBool(Settings.TOUCHSCREEN)) {
			Container settings = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			settings.addComponent(new Label("Softbuttons order"));
			swapControlsBox = new ComboBox(swapControlsOptions);
			if(Settings.getBool(Settings.SWAP_CONTROLS)) {
				swapControlsBox.setSelectedIndex(1);
			} else {
				swapControlsBox.setSelectedIndex(0);
			}
			settings.addComponent(swapControlsBox);
			addComponent(settings);
		}
		
		ok = new Command(UI.localize("ok"));
		cancel = new Command(UI.localize("cancel"));
		setBackCommand(cancel);
		Button okButton = new Button(ok);
		okButton.setAlignment(CENTER);
		Button cancelButton = new Button(cancel);
		cancelButton.setAlignment(CENTER);
		addComponent(okButton);
		addComponent(cancelButton);
	}

	public void actionCommand(Command source) {
		if(source == ok) {
			if(tweetsOrderBox.getSelectedIndex() == 0) {
				// top
				Settings.set(Settings.TWEET_LIST_REVERSED, false);
			} else {
				// bottom
				Settings.set(Settings.TWEET_LIST_REVERSED, true);
			}

			switch(photoService.getSelectedIndex()) {
			case 0:
				Settings.set(Settings.PHOTO_SERVICE, "tweetphoto");
				break;
			case 1:
				Settings.set(Settings.PHOTO_SERVICE, "pavome");
				break;
			}

			switch(avatarsBox.getSelectedIndex()) {
			case 0:
				Settings.set(Settings.LOAD_AVATARS, true);
				break;
			case 1:
				Settings.set(Settings.LOAD_AVATARS, false);
				break;
			}
			
			switch(fontBox.getSelectedIndex()) {
			case 0:
				// medium
				Settings.set(Settings.MAIN_FONT, "main");
				Settings.set(Settings.MAIN_FONT_BOLD, "main-bold");
				break;
			case 1:
				// small
				Settings.set(Settings.MAIN_FONT, "tiny");
				Settings.set(Settings.MAIN_FONT_BOLD, "tiny-bold");
				break;
			case 2:
				// system small
				Settings.set(Settings.MAIN_FONT, "sys-main");
				Settings.set(Settings.MAIN_FONT_BOLD, "sys-main-bold");
				break;
			case 3:
				// system medium
				Settings.set(Settings.MAIN_FONT, "sys-medium");
				Settings.set(Settings.MAIN_FONT_BOLD, "sys-medium-bold");
				break;
			case 4:
				// system large
				Settings.set(Settings.MAIN_FONT, "sys-large");
				Settings.set(Settings.MAIN_FONT_BOLD, "sys-large-bold");
				break;
			}

			switch(vibrateBox.getSelectedIndex()) {
			case 0:
				Settings.set(Settings.NOTIFICATION_VIBRATE, true);
				Settings.set(Settings.NOTIFICATION_VIBRATE_DM, true);
				Settings.set(Settings.NOTIFICATION_VIBRATE_MENTIONS, true);
				break;
			case 1:
				Settings.set(Settings.NOTIFICATION_VIBRATE, false);
				Settings.set(Settings.NOTIFICATION_VIBRATE_DM, true);
				Settings.set(Settings.NOTIFICATION_VIBRATE_MENTIONS, true);
				break;
			case 2:
				Settings.set(Settings.NOTIFICATION_VIBRATE, false);
				Settings.set(Settings.NOTIFICATION_VIBRATE_DM, false);
				Settings.set(Settings.NOTIFICATION_VIBRATE_MENTIONS, true);
				break;
			case 3:
				Settings.set(Settings.NOTIFICATION_VIBRATE, false);
				Settings.set(Settings.NOTIFICATION_VIBRATE_DM, true);
				Settings.set(Settings.NOTIFICATION_VIBRATE_MENTIONS, false);
				break;
			case 4:
				Settings.set(Settings.NOTIFICATION_VIBRATE, false);
				Settings.set(Settings.NOTIFICATION_VIBRATE_DM, false);
				Settings.set(Settings.NOTIFICATION_VIBRATE_MENTIONS, false);
				break;
			}
			
			Settings.set(Settings.NOTIFICATION_ANIMATION, notificationAnimation.isSelected());
			if(!Settings.getBool(Settings.TOUCHSCREEN)) {
				Settings.set(Settings.SWAP_CONTROLS, swapControlsBox.getSelectedIndex() == 1);
			}
			
			Params settings = new Params();
			settings.set(Settings.TWEET_LIST_REVERSED, Settings.getBool(Settings.TWEET_LIST_REVERSED));
			settings.set(Settings.MAIN_FONT, Settings.getString(Settings.MAIN_FONT));
			settings.set(Settings.MAIN_FONT_BOLD, Settings.getString(Settings.MAIN_FONT_BOLD));
			settings.set(Settings.NOTIFICATION_VIBRATE, Settings.getBool(Settings.NOTIFICATION_VIBRATE));
			settings.set(Settings.NOTIFICATION_VIBRATE_MENTIONS, Settings.getBool(Settings.NOTIFICATION_VIBRATE_MENTIONS));
			settings.set(Settings.NOTIFICATION_VIBRATE_DM, Settings.getBool(Settings.NOTIFICATION_VIBRATE_DM));
			settings.set(Settings.NOTIFICATION_ANIMATION, Settings.getBool(Settings.NOTIFICATION_ANIMATION));
			settings.set(Settings.PHOTO_SERVICE, Settings.getString(Settings.PHOTO_SERVICE));
			settings.set(Settings.SWAP_CONTROLS, Settings.getBool(Settings.SWAP_CONTROLS));
			settings.set(Settings.LOAD_AVATARS, Settings.getBool(Settings.LOAD_AVATARS));
			
		    UI.getConnection().sendSettings(settings);
		    UI.getInstance().getCurrent().reload();
		    UI.getInstance().showCurrent();
		    
		} else {
			UI.getInstance().showCurrent();
		}
	}
}
