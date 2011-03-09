package me.pavo.ui;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import me.pavo.AvatarStore;
import me.pavo.Contacts;
import me.pavo.Main;
import me.pavo.Post;
import me.pavo.UI;
import me.pavo.logic.ActionDispatcher;
import me.pavo.logic.ActionList;
import me.pavo.logic.ToplevelDispatcher;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.server.Settings;
import me.pavo.text.Parser;
import me.pavo.text.Text;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.ListSelectDialog;
import com.sun.lwuit.PavoButtons;
import com.sun.lwuit.PavoForm;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

public abstract class Screen extends PavoForm implements ActionListener, Runnable {
	protected String name;
	protected String kind;
	private Params reverseKeyShortcuts;
	protected TopicManager topicManager;
	private TweetContainer tweetContainer;
	protected int startDelay;
	
	public static final int SB_TWEET = 1;
	public static final int SB_ACTION = 2;
	protected static final int BACK = 4;
	protected static final int LEFT = 5;
	protected static final int RIGHT = 6;
	
	protected static final int CONTEXT_DELETE = ActionDispatcher.SCREEN + 1;
	protected static final int CONTEXT_VIEW_AUDIO = ActionDispatcher.SCREEN + 3;
	protected static final int CONTEXT_VIEW_VIDEO = ActionDispatcher.SCREEN + 4;
	protected static final int CONTEXT_VIEW_IMAGE = ActionDispatcher.SCREEN + 5;
	protected static final int CONTEXT_OPEN_LINK = ActionDispatcher.SCREEN + 6;
	protected static final int CONTEXT_USER = ActionDispatcher.SCREEN + 7;
	protected static final int CONTEXT_HASHTAG = ActionDispatcher.SCREEN + 8;
	
	protected static final int USER_SHOW_PROFILE = 1;
	protected static final int USER_SHOW_TWEETS = 2;
	
	public Screen(String kind, String name) {
		super();
		this.kind = kind;
		this.name = name;
		
		if(name.equals("user")) {
			setTitle(UI.localize("own_tweets"));
		} else {
			setTitle(UI.localize(name));
		}

	    setScrollable(false);
		setLayout(new BorderLayout());
	    setCyclicFocus(false);
	    setIsScrollVisible(false);
	    getStyle().setPadding(0,0,0,0);
	    getStyle().setMargin(0, 0, 0, 0);
		
		setBackCommand(new Command("", BACK));

		Command first; 
		Command second;

		if(Settings.getBool(Settings.SWAP_CONTROLS)) {
			first = new Command(UI.localize("tweet"), SB_TWEET);
			second = new Command(UI.localize("action"), SB_ACTION); 
		} else {
			first = new Command(UI.localize("action"), SB_ACTION); 
			second = new Command(UI.localize("tweet"), SB_TWEET);
		}
		
		if(!Settings.getBool(Settings.TOUCHSCREEN)) {
			addCommand(first);
			addCommand(second);
		} else {
			addCommand(new Command(UI.localize("action"), SB_ACTION));
			addCommand(new Command("   <<   ", LEFT));
			addCommand(new Command("   >>   ", RIGHT));
		}
		
		addCommandListener(this);
		
		Params keyShortcuts = getKeyShortcuts();
		reverseKeyShortcuts = new Params();
		for(Enumeration e = keyShortcuts.keys(); e.hasMoreElements();) {
			Integer commandId = (Integer) e.nextElement();
			Integer keyCode = keyShortcuts.getInteger(commandId);
			reverseKeyShortcuts.put(keyCode, commandId);
			if(keyCode.intValue() != 0) {
				addKeyListener(keyCode.intValue(), this);
			}
		}
	}
	
	public void reload() {
		Label w = new Label(UI.getImage(UI.WAIT_ICON));
		w.setAlignment(Component.CENTER);
		addComponent(BorderLayout.CENTER, w);
		new Thread(this).start();
		revalidate();
	}
	
	public Params getKeyShortcuts() {
		Params shortcuts = new Params();
		Params settings = Settings.getParams(Settings.SHORTCUTS);
		shortcuts.set(CONTEXT_DELETE, settings.getInt("delete", 0));
		shortcuts.set(CONTEXT_USER, settings.getInt("user", 0));
		shortcuts.set(CONTEXT_HASHTAG, settings.getInt("hashtag", 0));
		return shortcuts;
	}
	
	protected ActionList getPavoActions() {
		ActionList actions = new ActionList(new ToplevelDispatcher());
		actions.append(actions.make(ToplevelDispatcher.GO_TOP, "go_top", UI.BIG_ARROW_UP).set("position", ActionMenu.LEFT));
		actions.append(actions.make(ToplevelDispatcher.MORE, "more", UI.BIG_MORE).set("position", ActionMenu.BOTTOM));
		actions.append(actions.make(ToplevelDispatcher.CHANNELS, "screens", UI.BIG_CHANNELS).set("position", ActionMenu.RIGHT));
		return actions;
	}
	
	public ActionList getChannelActions() {
		ActionList actions = new ActionList(new ToplevelDispatcher());
		actions.append(ToplevelDispatcher.HOME, "home", UI.BIG_HOME);
		actions.append(ToplevelDispatcher.OWN_TWEETS, "own_tweets", UI.BIG_AVATAR);
		actions.append(ToplevelDispatcher.MENTIONS, "mentions", UI.BIG_REPLY);
		actions.append(ToplevelDispatcher.DM, "dm", UI.BIG_DM);
		actions.append(ToplevelDispatcher.FAVORITES, "favorites", UI.BIG_FAVORITE);
		actions.append(ToplevelDispatcher.SEARCH, "search", UI.BIG_SEARCH);
		return actions;
	}
	
	public ActionList getMoreActions() {
		ActionList actions = new ActionList(new ToplevelDispatcher());
		actions.append(ToplevelDispatcher.SETTINGS, "settings", UI.BIG_SETTINGS);
		actions.append(ToplevelDispatcher.EXIT, "exit", UI.BIG_EXIT);
		return actions;
	}

	protected ActionList getTweetActions(Post current) {
		ActionList actions = new ActionList();
		switch(current.getAttached()) {
		case Post.ATTACHMENT_IMAGE:
			actions.append(CONTEXT_VIEW_IMAGE, "view_photo", UI.BIG_VIEW);
			break;
		case Post.ATTACHMENT_VIDEO:
			actions.append(CONTEXT_VIEW_VIDEO, "view_video", UI.BIG_VIEW);
			break;
		case Post.ATTACHMENT_AUDIO:
			actions.append(CONTEXT_VIEW_AUDIO, "view_audio", UI.BIG_VIEW);
			break;
		case Post.ATTACHMENT_NONE:
			break;
		}
		
		if(current.getBody().indexOf("http://") != -1 || current.getBody().indexOf("https://") != -1) {
			actions.append(CONTEXT_OPEN_LINK, "open", UI.BIG_OPEN_LINK);
		}

		if(current.getBody().indexOf("#") != -1) {
			actions.append(CONTEXT_HASHTAG, "hashtag", UI.BIG_SEARCH);
		}
		
		actions.append(CONTEXT_USER, "user", UI.BIG_AVATAR);

		if(Contacts.getInstance().isSelf(current.getAuthorId())) {
			actions.append(CONTEXT_DELETE, "delete", UI.BIG_TRASH);
		}
		
		return actions;
	}
	
	public void dispatch(Params action) {
		switch (action.getInt("action")) {
		case CONTEXT_VIEW_IMAGE:
			if(getCurrentPost().getBody().indexOf("http://tweetphoto.com") != -1) {
				new TweetphotoView(kind, name, getCurrentPost()).show();
			} else {
				new PhotoView(kind, name, getCurrentPost(), this).show();
			}
			break;
		case CONTEXT_VIEW_VIDEO:
			new VideoView(this, kind, name, getCurrentPost());
			break;
		case CONTEXT_VIEW_AUDIO:
			new AudioView(this, kind, name, getCurrentPost());
			break;
		case CONTEXT_DELETE:
			if(UI.confirm("Are you sure you want to delete this post?", "Yes", "No")) {
				Post post = getCurrentPost();
				Connection.getInstance().deletePost(kind, name, post.getReference());
				topicManager.delete(post);
			}
			break;
		case CONTEXT_OPEN_LINK:
			Stack urls = Parser.filter(Parser.parse(getCurrentPost().getBody()), Text.URL);
			String url = (String) ListSelectDialog.select("Open URL", urls);
			if(url != null) {
				UI.openURL(url);
			}
			break;
		case CONTEXT_HASHTAG:
			Stack tags = Parser.filter(Parser.parse(getCurrentPost().getBody()), Text.HASHTAG);
			Vector tagCommands = new Stack();
			for(int i=0; i<tags.size(); i++) {
				String tag = ((String) tags.elementAt(i));
				tagCommands.addElement(new Command(tag));
			}
			Command tag = (Command) ListSelectDialog.select("search", tagCommands);
			if(tag != null) {
				UI.getInstance().showHashtagScreen(tag.getCommandName());
			}
			break;
		case CONTEXT_USER:
		{
			Stack users = Parser.filter(Parser.parse(getCurrentPost().getBody()), Text.USERNAME);
			String currentUser = "@" + getCurrentPost().getAuthor();
			if(!users.contains(currentUser)) {
				users.push(currentUser);
			}
			String forwardedBy = getCurrentPost().getForwardedBy(); 
			if(!forwardedBy.equals("") && !users.contains("@" + forwardedBy)) {
				users.push("@" + forwardedBy);
			}
			Vector usersCommands = new Vector();
			for(int i=0; i<users.size(); i++) {
				String username = ((String) users.elementAt(i)).substring(1);
				usersCommands.addElement(new Command(username, AvatarStore.getInstance().getAvatar(username)));
			}
			Command r = (Command) ListSelectDialog.select("", usersCommands);
			if(r != null) {
				String username = r.getCommandName();
				Vector commands = new Vector();
				commands.addElement(UI.command(UI.localize("view_profile"), UI.MEDIUM_PROFILE, USER_SHOW_PROFILE));
				commands.addElement(UI.command(UI.localize("view_tweets"), UI.MEDIUM_TWEET, USER_SHOW_TWEETS));
				Command rr = (Command) ListSelectDialog.select(username, commands);
				if(rr != null) {
					switch(rr.getId()) {
					case USER_SHOW_PROFILE:
						new ViewUserProfile(Connection.getInstance().getUserProfile(username), Connection.getInstance(), this);
						break;
					case USER_SHOW_TWEETS:
						UI.getInstance().showUserScreen(username);
						break;
					}
				}
			}
		}
			break;
		default:
			// TODO fire dialog, warn unknown command
			break;
		}
	}
	
	public void close() {
		topicManager.close();
		UI.getInstance().showHomeScreen();
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getCommand() != null) {
			switch(ev.getCommand().getId()) {
			case SB_TWEET:
				Post current = getCurrentPost();
				if(current != null && current.getOrigin() == Post.ORIGIN_TWITTER) {
					showTweetMenu(getCurrentPost());
				}
				break;
			case SB_ACTION:
				new me.pavo.ui.ActionMenu(getPavoActions()).showMenu();
				break;
			case BACK:
				Display.getInstance().minimizeApplication();
				break;
			case LEFT:
				UI.getInstance().showLeft();
				break;
			case RIGHT:
				UI.getInstance().showRight();
				break;
			}
		} else if(ev.getSource().equals(null)) { // FIXME for touchscreen touching the tweet should bring up the menu
			showTweetMenu(getCurrentPost());			
			ev.consume();
		} else if(ev.getSource().equals(this)) {
			Integer keyCode = new Integer(ev.getKeyEvent());
			if(reverseKeyShortcuts.containsKey(keyCode)) {
				ev.consume();
				Integer actionId = reverseKeyShortcuts.getInteger(keyCode);
				ActionList actions = getTweetActions(getCurrentPost());
				Params action = actions.find(actionId);
				if(action != null) {
					dispatch(action);
				}
			}
		}
	}
	
	private void showTweetMenu(Post tweet) {
		ActionList actions = getTweetActions(tweet);
		PavoButtons buttons = new PavoButtons(actions, null, Settings.getBool(Settings.TOUCHSCREEN), getKeyShortcuts());
		Params result = buttons.showDialog();
		if(result != null) {
			dispatch(result);
		}
	}
	
	public String getKind() {
		return kind;
	}

	public String getName() {
		return name;
	}
	
	public void relayout() {
		setShouldCalcPreferredSize(true);
		layoutContainer();
	}
	
	public void run() {
		if(startDelay > 0) {
			try {	
			System.out.println("before sleep");	
				Thread.sleep(startDelay);
			System.out.println("after sleep");
			} catch (InterruptedException e) {	}
			startDelay = 0;
		}
		
		topicManager = new TopicManager(kind, name);
		final boolean success = topicManager.open();
		Display.getInstance().callSerially(new Runnable() {
			public void run() {
				if(success) {
					Display.getInstance().callSerially(new Runnable() {
						public void run() {
							tweetContainer = new TweetContainer();
							tweetContainer.setManager(topicManager);
							topicManager.init(Screen.this, tweetContainer);
							addComponent(BorderLayout.CENTER, tweetContainer);
							repaint();
						}
					});
				} else {
					UI.error("Error: " + topicManager.getError());
				}
			}
		});
	}

	protected Post getCurrentPost() {
		if(getFocused() != null) {
			return ((Tweet)getFocused()).getPost(); 
		}
		return null;
	}
	
	public void keyPressed(int keyCode) {
        int game = Display.getInstance().getGameAction(Main.remapKey(keyCode));
        if(game == Display.GAME_LEFT) {
    		UI.getInstance().showLeft();
        } else if(game == Display.GAME_RIGHT) {
    		UI.getInstance().showRight();
        } else {
    		super.keyPressed(keyCode);
        }
	}
	
	public void keyRepeated(int keyCode) {
        int game = Display.getInstance().getGameAction(Main.remapKey(keyCode));
        if(game != Display.GAME_LEFT && game != Display.GAME_RIGHT) {
    		super.keyRepeated(keyCode);
        }
	}
	
	public void keyReleased(int keyCode) {
        int game = Display.getInstance().getGameAction(Main.remapKey(keyCode));
        if(game != Display.GAME_LEFT && game != Display.GAME_RIGHT) {
    		super.keyReleased(keyCode);
        }
	}
}
