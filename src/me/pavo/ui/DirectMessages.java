package me.pavo.ui;

import me.pavo.Contacts;
import me.pavo.Post;
import me.pavo.UI;
import me.pavo.logic.ActionDispatcher;
import me.pavo.logic.ActionList;
import me.pavo.logic.ReplyToDmWF;
import me.pavo.logic.ToplevelDispatcher;
import me.pavo.server.Params;
import me.pavo.server.Settings;

public class DirectMessages extends Screen {
	public static final int DM_REPLY = ActionDispatcher.DM + 1;
	
	public DirectMessages(int startDelay) {
		super("default", "dm");
		this.startDelay = startDelay;
		reload();
	}
	
	public DirectMessages() {
		super("default", "dm");
		reload();
	}
	
	protected ActionList getPavoActions() {
		ActionList actions = super.getPavoActions();
		actions.append(actions.make(ToplevelDispatcher.NEW_DM, "new_dm", UI.BIG_DM).set("position", ActionMenu.CENTER));
		return actions;
	}
	
	protected ActionList getTweetActions(Post current) {
		ActionList actions = super.getTweetActions(current);
		Params delete = actions.make(Screen.CONTEXT_DELETE, "delete", UI.BIG_TRASH);		
		if(!actions.contains("action", Screen.CONTEXT_DELETE)) {
			actions.append(delete);
		}
		
		if(!Contacts.getInstance().isSelf(current.getAuthorId())) {
			actions.append(DM_REPLY, "reply", UI.BIG_DM);
		}
		
		return actions;
	}
	
	public Params getKeyShortcuts() {
		Params shortcuts = new Params();
		Params settings = Settings.getParams(Settings.SHORTCUTS);
		shortcuts.set(DM_REPLY, settings.getInt("reply", 0));
		return shortcuts;
	}
	
	public void dispatch(Params action) {
		switch(action.getInt("action")) {
		case DM_REPLY:
			new ReplyToDmWF("", getCurrentPost().getAuthor(), 140, 140).start();
		default:
			super.dispatch(action);
			break;
		}
	}
}
