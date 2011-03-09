package me.pavo.logic;

import me.pavo.Main;
import me.pavo.UI;
import me.pavo.server.Params;
import me.pavo.ui.SearchForm;
import me.pavo.ui.SettingsForm;
import me.pavo.ui.TiledMenu;

public class ToplevelDispatcher implements ActionDispatcher {
	
	public static final int HOME = ActionDispatcher.TOPLEVEL + 1;
	public static final int OWN_TWEETS = ActionDispatcher.TOPLEVEL + 2;
	public static final int MENTIONS = ActionDispatcher.TOPLEVEL + 3;
	public static final int DM = ActionDispatcher.TOPLEVEL + 4;
	public static final int USER_TWEETS = ActionDispatcher.TOPLEVEL + 5;
	public static final int BACK_TO_CURRENT = ActionDispatcher.TOPLEVEL + 6;
	public static final int EXIT = ActionDispatcher.TOPLEVEL + 7;
	public static final int SETTINGS = ActionDispatcher.TOPLEVEL + 8;
	public static final int AUDIO_TWEET = ActionDispatcher.TOPLEVEL + 9;
	public static final int TEXT = ActionDispatcher.TOPLEVEL + 10;
	public static final int PHOTO = ActionDispatcher.TOPLEVEL + 11;
	public static final int VIDEO = ActionDispatcher.TOPLEVEL + 12;
	public static final int MORE = ActionDispatcher.TOPLEVEL + 13;
	public static final int CHANNELS = ActionDispatcher.TOPLEVEL + 14;
	public static final int NEW_DM = ActionDispatcher.TOPLEVEL + 15;
	public static final int FAVORITES = ActionDispatcher.TOPLEVEL + 16;
	public static final int PASS = ActionDispatcher.TOPLEVEL + 17;
	public static final int FILE_TWEET = ActionDispatcher.TOPLEVEL + 18;
	public static final int SEARCH = ActionDispatcher.TOPLEVEL + 19;
	public static final int GO_TOP = ActionDispatcher.TOPLEVEL + 20;
	
	public void dispatch(Params action) {
		UI ui = UI.getInstance();
		switch (action.getInt("action")) {
		case HOME:
			ui.showHomeScreen();
			break;
		case OWN_TWEETS:
			ui.showMyTweetsScreen();
			break;
		case MENTIONS:
			ui.showMentionsScreen();
			break;
		case DM:
			ui.showDmScreen();
			break;
		case FAVORITES:
			ui.showFavoritesScreen();
			break;
		case USER_TWEETS:
			ui.showUserScreen(action.getString("user"));
			break;
		case BACK_TO_CURRENT:
			ui.showCurrent();
			break;
		case EXIT:
			if(UI.confirm("Exit PavoMe?", "Yes", "No")) {
				Main.INSTANCE.stopApplication();
			}
			break;
		case GO_TOP:
			ui.getCurrent().reload();
			ui.getCurrent().show();
			break;
		case SETTINGS:
			new SettingsForm().show();
			break;
		case AUDIO_TWEET:
			new CaptureAudioWF().start();
			break;
		case FILE_TWEET:
			new AttachFileWF().start();
			break;
		case TEXT:
			new TextTweetWF().start();
			break;
		case PHOTO:
			new TakePhotoWF().start();			
			break;
		case VIDEO:
			new CaptureVideoWF(UI.getConnection()).start();		
			break;
		case CHANNELS:
			new TiledMenu(ui.getCurrent().getChannelActions()).show();
			break;
		case MORE:
			new TiledMenu(ui.getCurrent().getMoreActions()).show();
			break;
		case NEW_DM:
			new NewDmWF().start();
			break;
		case SEARCH:
			new SearchForm().show();
			break;
		default:
			break;
		}
	}
}


