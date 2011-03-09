package me.pavo.ui;

import me.pavo.Contacts;
import me.pavo.Post;
import me.pavo.UI;
import me.pavo.logic.ActionDispatcher;
import me.pavo.logic.ActionList;
import me.pavo.logic.TextTweetWF;
import me.pavo.logic.ToplevelDispatcher;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.server.Settings;

public class Tweets extends Screen {
	
	static final int CONTEXT_REPLY = ActionDispatcher.TWEETS + 1;
	static final int CONTEXT_RE_TWEET = ActionDispatcher.TWEETS + 2;
	static final int CONTEXT_RE_TWEET_NEW = ActionDispatcher.TWEETS + 3;
	static final int CONTEXT_TOGGLE_FAVORITE = ActionDispatcher.TWEETS + 4;
	static final int CONTEXT_VIEW_IRT = ActionDispatcher.TWEETS + 6;

	public Tweets(String kind, String name, int startDelay) {
		super(kind, name);
		this.startDelay = startDelay;
		reload();
	}
	
	public Tweets(String kind, String name) {
		super(kind, name);
		reload();
	}

	public Params getKeyShortcuts() {
		Params shortcuts = super.getKeyShortcuts();
		Params settings = Settings.getParams(Settings.SHORTCUTS);
		shortcuts.set(CONTEXT_REPLY, settings.getInt("reply", 0));
		shortcuts.set(CONTEXT_RE_TWEET, settings.getInt("retweet_old", 0));
		shortcuts.set(CONTEXT_RE_TWEET_NEW, settings.getInt("retweet_new", 0));
		shortcuts.set(CONTEXT_TOGGLE_FAVORITE, settings.getInt("favorite", 0));
		shortcuts.set(CONTEXT_VIEW_IRT, settings.getInt("irt", 0));
		return shortcuts;
	}
	protected ActionList getPavoActions() {
		ActionList actions = super.getPavoActions();
		actions.append(actions.make(ToplevelDispatcher.PHOTO, "photo", UI.BIG_PHOTO).set("position", ActionMenu.TOP));
		actions.append(actions.make(ToplevelDispatcher.TEXT, "text", UI.BIG_POST).set("position", ActionMenu.CENTER));
		return actions;
	}
		
	protected ActionList getTweetActions(Post current) {
		ActionList actions = super.getTweetActions(current);
		if(!Contacts.getInstance().isSelf(current.getAuthorId())) {
			actions.append(CONTEXT_RE_TWEET, "retweet", UI.BIG_RE_TWEET);
			actions.append(CONTEXT_RE_TWEET_NEW, "retweet", UI.BIG_RE_TWEET_NEW);
			actions.append(CONTEXT_REPLY, "reply", UI.BIG_REPLY);
		}
		if(current.isFavorited()) {
			actions.append(CONTEXT_TOGGLE_FAVORITE, "unfavorite", UI.BIG_UNFAVORITE);
		} else {
			actions.append(CONTEXT_TOGGLE_FAVORITE, "favorite", UI.BIG_FAVORITE);
		}
		
		if(!current.getInReplyTo().equals("")) {
			actions.append(CONTEXT_VIEW_IRT, "in_reply_to", UI.BIG_IRT);
		}
		
		return actions;
	}
	
	public ActionList getMoreActions() {
		ActionList actions = super.getMoreActions();
		actions.prepend(ToplevelDispatcher.FILE_TWEET, "attach_file", UI.BIG_ATTACH);
		actions.prepend(ToplevelDispatcher.VIDEO, "video", UI.BIG_VIDEO);
		actions.prepend(ToplevelDispatcher.AUDIO_TWEET, "record_audio", UI.BIG_RECORD);
		return actions;
	}
	
	public void dispatch(Params action) {
		switch(action.getInt("action")) {
		case CONTEXT_REPLY:
		{
			Post post = getCurrentPost();
			String text = "@" + post.getAuthor() + " ";
			new TextTweetWF(text, post.getReference(), 140, 140).start();			
		}
			break;
		case CONTEXT_RE_TWEET:
		{
			Post post = getCurrentPost();
			String text = "RT @" + post.getAuthor() + " " + post.getBody();
			new TextTweetWF(text, post.getReference(), 280, 140).start();
		}
			break;
		case CONTEXT_RE_TWEET_NEW:
			Post post = getCurrentPost();
			Tweet tweet = topicManager.getTweetByPost(post);
			post.setForwardedBy(UI.localize("me"));			
			Connection.getInstance().reTweetPost(getCurrentPost().getOrigin(), getCurrentPost().getReference());
			tweet.rebuild();
			repaint();
			break;
		case CONTEXT_TOGGLE_FAVORITE:
			if(getCurrentPost().isFavorited()) {
				getCurrentPost().setFavorited(false);
				Connection.getInstance().unfavoritePost(getCurrentPost().getReference());
			} else {
				getCurrentPost().setFavorited(true);
				Connection.getInstance().favoritePost(getCurrentPost().getReference());
			}
			break;
		case CONTEXT_VIEW_IRT:
			UI.getInstance().showTweetScreen(getCurrentPost().getInReplyTo());
			break;
		default:
			super.dispatch(action);
			break;
		}
	}
}
