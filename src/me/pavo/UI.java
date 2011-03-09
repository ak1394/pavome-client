package me.pavo;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Vector;

import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.Params;
import me.pavo.server.Settings;
import me.pavo.ui.Cache;
import me.pavo.ui.DirectMessages;
import me.pavo.ui.Notifier;
import me.pavo.ui.Screen;
import me.pavo.ui.StackedTweets;
import me.pavo.ui.Tweets;
import me.pavo.ui.Waiting;

import com.sun.lwuit.Button;
import com.sun.lwuit.CheckBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.RGBImage;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class UI implements Runnable {
	
	private Screen current;
	
	private Connection connection;	
	private static Resources resources;
	
	public static final String SMALL_PHOTO = "photo32";
	public static final String SMALL_VIDEO = "video32";
	public static final String SMALL_AUDIO = "audio32";
	public static final String SMALL_SEARCH = "smallSearch";
	public static final String SMALL_AVATAR = "avatar_small";
	public static final String SMALL_FAVORITE = "star16";
	public static final String SMALL_FOLDER = "folder32";
	public static final String SMALL_EMPTY = "blank32";

	public static final String BIG_MORE = "more";
	public static final String BIG_CHANNELS = "channels";
	public static final String BIG_PHOTO = "photo";
	public static final String BIG_VIDEO = "video";
	public static final String BIG_POST = "text";
	public static final String BIG_RECORD = "record";
	public static final String BIG_DM = "mail";
	public static final String BIG_ARROW_UP = "arrow-up";

	public static final String MEDIUM_DM = "mail32";
	
	public static final String BIG_REPLY = "mention";
	public static final String BIG_YES = "check";
	public static final String BIG_NO = "close";
	public static final String BIG_ROTATE = "rotate";
	public static final String BIG_TRASH = "trash";
	public static final String BIG_OPEN_LINK = "http_link";
	public static final String BIG_RE_TWEET = "retweet";
	public static final String BIG_AVATAR = "avatar";
	public static final String BIG_FOLLOW = "follow";
	public static final String BIG_UNFOLLOW = "unfollow";
	public static final String BIG_EXIT = "exit";
	public static final String BIG_SETTINGS = "settings";
	public static final String BIG_HOME = "home";
	public static final String BIG_VIEW = "view";
	public static final String BIG_RE_TWEET_NEW = "retweet-new";
	public static final String BIG_FAVORITE = "favorite";
	public static final String BIG_UNFAVORITE = "unfavorite";
	public static final String BIG_SEARCH = "search";
	public static final String BIG_ATTACH = "attach";
	public static final String BIG_IRT = "irt";
	public static final String BIG_UPVOTE = "upvote";
	public static final String BIG_DOWNVOTE = "downvote";

	public static final String MESSAGE_ARRIVED = "messageArrived";
	public static final String WAIT_ICON = "wait";
	public static final String NOTIFICATION_ICON = "notification";
	public static final String BACK_ICON = "back";
	public static final String RETWEETED_ICON = "retweeted16";
	public static final String IRT_ICON = "irt16";
	public static final String PHOTO_ICON = "photo16";
	public static final String VIDEO_ICON = "video16";
	public static final String AUDIO_ICON = "sound16";
	public static final String UPVOTE_ICON = "upvote16";
	public static final String DOWNVOTE_ICON = "downvote16";
	public static final String VIEWS_ICON = "view16";
	public static final String KEYPAD_ICON = "keypad16";

	public static final String MEDIUM_TWEET = "tweet32";
	public static final String MEDIUM_PROFILE = "profile32";
	public static final String MEDIUM_HOME = "home32";
	public static final String MEDIUM_REPLY = "mention32";
	public static final String MEDIUM_AVATAR = "avatar32";
	public static final String MEDIUM_IRT = "irt32";
	public static final String MEDIUM_SEARCH = "search32";

	private Notifier navigation;
	private Timer timer;
	
	private static UI instance;
	private Vector screens;
	
	public UI() {
		instance = this;
	}
	
	public static UI getInstance() {
		return instance;
	}
	
	public void init(String token, String version) throws Exception {
		Settings.set(Settings.SMALLSCREEN, Display.getInstance().getDisplayWidth() < 240);
		Settings.set(Settings.SHORTSCREEN, Display.getInstance().getDisplayHeight() <= 240);
		Display.getInstance().setThirdSoftButton(true);
		Display.getInstance().setShowDuringEditBehavior(Display.SHOW_DURING_EDIT_IGNORE);
		if(Settings.has(Settings.QWERTY)) {
			TextField.setQwertyDevice(Settings.getBool(Settings.QWERTY));
		}
		
		if(Settings.getBool(Settings.VKB_ONLY)) {
			TextField.setT9Text("");
		}
		
		new Cache();
		timer = new Timer();
		connection = Connection.getInstance();
		
		if(!loadResources()) {
			Main.INSTANCE.stopApplication();
			return;
		}
		
		if(Settings.getBool(Settings.TOUCHSCREEN)) {
            Style style = UIManager.getInstance().getComponentStyle("SoftButton");
			style.setPadding(Component.TOP, style.getPadding(Component.TOP) + 8);
			style.setPadding(Component.BOTTOM, style.getPadding(Component.BOTTOM) + 8);
			UIManager.getInstance().setComponentStyle("SoftButton", style);
		}
		
		displayLoading();
		
		if(token == null && Main.INSTANCE.getSavedConfig().size() > 0) {
			token = (String) Main.INSTANCE.getSavedConfig().get("token");
		}
		
		if(token == null) {
			while(true) {
				Hashtable credentials = UI.loginDialog();
				if(credentials == null) {
					continue;
				}
				
				Future result = connection.requestToken(credentials);
				if(result.isSuccess()) {
					token = ((Params) result.getResult()).getString("token");
					Hashtable config = new Hashtable();
					config.put("token", token);
					Main.INSTANCE.saveConfig(config);
					break;
				} else {
					UI.error("Failed to login");
				}
			}
		}
		
		Future result = connection.login(version, token); 
		if(!result.isSuccess()) {
			if (result.getResult() != null && result.getResult() instanceof String) {
				UI.error("Failed to login: " + result.getResult());
			} else {
				UI.error("Failed to login");
			}
			Main.INSTANCE.stopApplication();
			return;
		}
		
		new Thread(this).start();
	}
	
	public static void error(final String text) {
		if(Display.getInstance().isEdt()) {
			Dialog.show("Error", text, Dialog.TYPE_ERROR, null, "Ok", null, 0);
		} else {
			Display.getInstance().callSeriallyAndWait(new Runnable() {
				public void run() {
					Dialog.show("Error", text, Dialog.TYPE_ERROR, null, "Ok", null, 0);
				}});
		}
	}

	public static void error(String text, Exception e) {
		Dialog.show("Error", text + " " + e.toString(), Dialog.TYPE_ERROR, null, "Ok", null, 0);
	}
	
	public void run() {
		AvatarStore.initInstance(connection);
		new Contacts().init();
		navigation = new Notifier();
		
		screens = new Vector();
		screens.addElement(new Tweets("default", "home"));
		screens.addElement(new Tweets("default", "mentions", 2500));
		screens.addElement(new DirectMessages(3500));
		current = (Screen) screens.elementAt(0);
		current.show();
		connection.addMessageListener(navigation);
	}
	
	private void displayLoading() {
		try {
			Resources r = Resources.open("/progress-anim.res");
			Image image = Image.createImage("/peacock46x48.png");
			Waiting waiting = new Waiting("Connecting...", image, r.getImage("progress"));
			waiting.getStyle().setBgColor(0xFFFFFF);
			waiting.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Image getImage(String id) {
		if(id.equals(WAIT_ICON)) {
			try {	return Resources.open("/busy-anim.res").getImage(WAIT_ICON); } catch (IOException e) {}
			return null;
		} else {
			Image image = resources.getImage(id);
			if(image == null) {
				try { image = Image.createImage("/" + id + ".png"); } catch (IOException e) {}
			}
			return image; 
		}
	}
	
	public static Image getIconForScreenMedium(String kind, String name) {
		if(kind.equals("default")) {
			if(name.equals("home")) {
				return UI.getImage(UI.MEDIUM_HOME); 
			} else if(name.equals("mentions")) {
				return UI.getImage(UI.MEDIUM_REPLY);
			} else if(name.equals("dm")) {
				return UI.getImage(UI.MEDIUM_DM);
			}
		} else if(kind.equals("user")) {
			return AvatarStore.getInstance().getAvatar(name);
		} else if(kind.equals("tweet")) {
			return UI.getImage(UI.MEDIUM_IRT);
		} else if(kind.equals("search")) {
			return UI.getImage(UI.MEDIUM_SEARCH);
		}
		return UI.getImage(UI.BIG_NO);
	}

	public static Image getIconForScreenBig(String kind, String name) {
		if(kind.equals("default")) {
			if(name.equals("home")) {
				return UI.getImage(UI.BIG_HOME); 
			} else if(name.equals("mentions")) {
				return UI.getImage(UI.BIG_REPLY);
			} else if(name.equals("dm")) {
				return UI.getImage(UI.BIG_DM);
			}
		} else if(kind.equals("user")) {
			return AvatarStore.getInstance().getAvatar(name);
		} else if(kind.equals("search")) {
			return UI.getImage(UI.BIG_SEARCH);
		}
		return UI.getImage(UI.BIG_NO);
	}
	
	public static void updateSelectionStyle(Button button, int blend) {
		Image icon = button.getIcon();
		
		if(!Settings.getBool(Settings.TOUCHSCREEN)) {
			int[] argb = icon.getRGB();
			int a, r, g, b;
			
			for(int i=0; i<argb.length; i++) {
				int px = argb[i];
		        a = px >> 24 & 0xff;
		        if(a != 0) {
			        r = (px >> 16 & 0xff) + blend >> 1;
			        g = (px >> 8 & 0xff) + blend >> 1;
			        b = (px & 0xff) + blend >> 1;
			        argb[i] = ((a << 24) & 0xff000000) | ((r << 16) & 0xff0000) | ((g << 8) & 0xff00) | (b & 0xff);
		        }
			}
			
			Image unselectedIcon = new RGBImage(argb,icon.getWidth(), icon.getHeight());
			button.setIcon(unselectedIcon);
		} else {
			button.setIcon(icon);
		}
		button.setRolloverIcon(icon);
		button.getStyle().setBgTransparency(0);
		button.getSelectedStyle().setBgTransparency(0);
		button.getPressedStyle().setBgTransparency(0);
	}
	
	public static Font font(String id) {
		return resources.getFont(id);
	}
	
	public static String localize(String id) {
		Hashtable l = resources.getL10N("localize", Settings.getString(Settings.LOCALE));
		if(l.containsKey(id)) {
			return (String) l.get(id);
		} else {
			return id;
		}
	}
	
	public static boolean confirm(String text, String confirm, String cancel) {
		boolean result;
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			Display.getInstance().setThirdSoftButton(false);
			result = Dialog.show(null, text, Dialog.TYPE_CONFIRMATION, null, confirm, cancel);
			Display.getInstance().setThirdSoftButton(true);
		} else {
			result = Dialog.show(null, text, Dialog.TYPE_CONFIRMATION, null, confirm, cancel);
		}

		return result;
	}
	
	public static boolean notify(String text) {
		return Dialog.show(null, text, Dialog.TYPE_INFO, null, "Ok", null);
	}

	public static Hashtable loginDialog() throws Exception {
		
		Label usernameLabel = new Label("Username");
		TextArea username = TextField.create();
		if(username instanceof TextField) {
			((TextField)username).setInputMode("abc");
		}
		
		Label passwordLabel  = new Label("Password");
		TextArea password = TextField.create();

		if(password instanceof TextField) {
			((TextField)password).setInputMode("abc");
		}
		
		Command login = new Command("Login");
		Command exit = new Command("Exit");

		Dialog d = new Dialog();
		d.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		
		Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		
		container.addComponent(usernameLabel);
		container.addComponent(username);
		
		container.addComponent(passwordLabel);
		container.addComponent(password);

		
		CheckBox follow = new CheckBox("Follow @PavoMe");
		follow.setSelected(true);
		container.addComponent(follow);
		
		Container buttons = new Container();
		
		buttons.addComponent(new Button(login));
		buttons.addComponent(new Button(exit));

		d.addComponent(container);
		d.addComponent(buttons);
		
		Command c = d.show(0, 0, 0, 0, true, true);
		
		if(c.equals(login) && username.getText() != null & username.getText().length() > 0 && password.getText() != null & password.getText().length() > 0) {
			Hashtable result = new Hashtable();
			result.put("username", username.getText());
			result.put("password", password.getText());
			result.put("follow", new Boolean(follow.isSelected()));
			return result;
		} else if(c.equals(exit)) {
			throw new Exception("Exiting...");
		} else {
			return null;
		}
	}
	
	
    public static void openURL(String url) {
		try {
			if(Settings.getBool(Settings.URL_OPEN_DIE)) {
				if(UI.confirm("Opening the URL will close the midlet?", "Ok", "Cancel")) {
					Main.INSTANCE.openURL(url);
					Main.INSTANCE.stopApplication();
				}
			} else {
				Main.INSTANCE.openURL(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	private boolean loadResources() {
		try {
			resources = Resources.open("/default.res");
			int smallFontHeight = com.sun.lwuit.Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL).getHeight();
			if(smallFontHeight > 15) {
				UIManager.getInstance().setThemeProps(resources.getTheme("Default"));			
			} else {
				UIManager.getInstance().setThemeProps(resources.getTheme("Default"));
			}
			return true;
		} catch (Exception e) {
			error("Failed to load resources " + e);
			return false;
		}
	}
	
	public static Connection getConnection() {
		return instance.connection;
	}
	
	public Screen showCurrent() {
		current.show();
		return current;
	}
	
	private void showScreen(Screen screen) {
		current = screen;
		current.show();
	}

	public void closeScreen(Screen screen) {
		screens.removeElement(screen);
		current = (Screen) screens.elementAt(0);
		current.show();
	}
	
	public void showLeft() {
		if(screens.indexOf(current) == 0) {
			current = (Screen) screens.elementAt(screens.size()-1);
		} else {
			current = (Screen) screens.elementAt(screens.indexOf(current)-1);
		}
		current.setTransitionInAnimator(CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, true, 500));
		current.show();
		current.setTransitionInAnimator(CommonTransitions.createEmpty());
	}
	
	public void showRight() {
		if(screens.indexOf(current) == screens.size()-1) {
			current = (Screen) screens.elementAt(0);
		} else {
			current = (Screen) screens.elementAt(screens.indexOf(current)+1);
		}
		current.setTransitionInAnimator(CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, false, 500));
		current.show();
		current.setTransitionInAnimator(CommonTransitions.createEmpty());
	}

	public Screen getCurrent() {
		return current;
	}
	
	public void reloadCurrent() {
		current.reload();
	}
	
	public static Command command(String text, String icon, int id) {
		return new Command(text, UI.getImage(icon), id);
	}
	
	public Timer getTimer() {
		return timer;
	}

	public void showHashtagScreen(String tag) {
		if(current instanceof StackedTweets) {
			((StackedTweets)current).push("search", tag);
		} else {
			StackedTweets search = new StackedTweets("search", tag);
			screens.addElement(search);
			showScreen(search);
		}
	}
	
	public void showSearchScreen(String query) {
		StackedTweets search = new StackedTweets("search", query);
		screens.addElement(search);
		showScreen(search);
	}

	public void showUserScreen(String username) {
		if(current instanceof StackedTweets) {
			((StackedTweets)current).push("user", username);
		} else {
			StackedTweets user = new StackedTweets("user", username);
			screens.addElement(user);
			showScreen(user);
		}
	}

	public void showHomeScreen() {
		showScreen((Screen) screens.elementAt(0));
	}

	public void showTweetScreen(String inReplyTo) {
		if(current instanceof StackedTweets) {
			((StackedTweets)current).push("tweet", inReplyTo);
		} else {
			StackedTweets user = new StackedTweets("tweet", inReplyTo);
			screens.addElement(user);
			showScreen(user);
		}
	}

	public void showMyTweetsScreen() {
		StackedTweets my = new StackedTweets("default", "user");
		screens.addElement(my);
		showScreen(my);
	}

	public void showMentionsScreen() {
		showScreen((Screen) screens.elementAt(1));
	}

	public void showFavoritesScreen() {
		StackedTweets favorites = new StackedTweets("default", "favorites");
		screens.addElement(favorites);
		current = favorites;
		current.show();
	}

	public void showDmScreen() {
		showScreen((Screen) screens.elementAt(2));
	}
}
