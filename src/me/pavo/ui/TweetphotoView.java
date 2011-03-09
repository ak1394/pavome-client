package me.pavo.ui;

import me.pavo.Post;
import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.PavoButtons;
import com.sun.lwuit.PavoForm;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.FlowLayout;

public class TweetphotoView extends PavoForm implements FutureCallback, ActionListener, Handler {
	private Label iconLabel;
	private Params photo;
	private boolean favored = false;
	private boolean voted = false;
	private int id;
	
	private static final int FAVORITE = 1;
	private static final int UNFAVORITE = 2;
	private static final int VOTE_PLUS = 3;
	private static final int VOTE_MINUS = 4;
	private static final int COMMENT = 5;
	
	private static final int COMMAND_PHOTO = 1;
	private static final int COMMAND_BACK = 2;
	
	public TweetphotoView(String kind, String name, Post post) {
		super();
		setTitle("TweetPhoto");
		setLayout(new BorderLayout());

		iconLabel = new Label("Loading...");
		iconLabel.setIcon(UI.getImage(UI.WAIT_ICON));
		iconLabel.setAlignment(Label.CENTER);
		iconLabel.setTextPosition(Label.BOTTOM);
		
		addComponent(BorderLayout.CENTER, iconLabel);
		
		Command back = new Command(UI.localize("back"), COMMAND_BACK);
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(back);
		}
		setBackCommand(back);
		
		addCommandListener(this);
		Connection.getInstance().getAttachment(kind, name, post.getId(), Post.ATTACHMENT_IMAGE).addCallback(this);
	}
	
	protected void onShowCompleted() {
		Cache.removeAll();
	}
	
	public String getUIID() {
		return "TweetPhoto";
	}

	public void actionPerformed(ActionEvent ev) {
		if(ev.getCommand() != null) {
			switch(ev.getCommand().getId()) {
			case COMMAND_PHOTO:
				ActionList actions = new ActionList();
				actions.append(COMMENT, "comment", UI.BIG_POST);
				if(!favored) {
					actions.append(FAVORITE, "favorite", UI.BIG_FAVORITE);
				} else {
					actions.append(UNFAVORITE, "unfavorite", UI.BIG_UNFAVORITE);
				}
				if(!voted) {
					actions.append(VOTE_PLUS, "upvote", UI.BIG_UPVOTE);
					actions.append(VOTE_MINUS, "downvote", UI.BIG_DOWNVOTE);
				}
				PavoButtons buttons = new PavoButtons(actions, null, Settings.getBool(Settings.TOUCHSCREEN));
				Params result = buttons.showDialog();
				if(result != null) {
					dispatch(result);
				}
				break;
			case COMMAND_BACK:
				UI.getInstance().showCurrent();
				break;
			}
		}
	}

	private void dispatch(Params action) {
		switch (action.getInt("action")) {
		case FAVORITE:
			Connection.getInstance().sendTweetphoto(new Params().set("id", id).set("action", FAVORITE));
			favored = true;
			break;
		case UNFAVORITE:
			Connection.getInstance().sendTweetphoto(new Params().set("id", id).set("action", UNFAVORITE));
			favored = false;
			break;
		case VOTE_PLUS:
			Connection.getInstance().sendTweetphoto(new Params().set("id", id).set("action", VOTE_PLUS));
			voted = true;
			break;
		case VOTE_MINUS:
			Connection.getInstance().sendTweetphoto(new Params().set("id", id).set("action", VOTE_MINUS));
			voted = true;
			break;
		case COMMENT:
			EditText editText = new EditText("", 100);
			editText.setHandler(this);
			editText.show();
			break;
		}
		
	}

	public void callbackFired(final Future future) {
		Display.getInstance().callSerially(new Runnable(){
			public void run() {
				photo = (Params) future.getResult();
				if(!photo.has("image")) {
					iconLabel.setIcon(null);
					iconLabel.setText(UI.localize("notfound"));
				} else {
					Image image = photo.getImage("image");
					if(Settings.getBool(Settings.SCALE_PHOTOS)) {
						image = image.scaled(iconLabel.getWidth() - (iconLabel.getWidth() / 8), -1);
					}
					iconLabel.setText("");
					iconLabel.setIcon(image);
				}
				
				if(photo.has("views")) { // must be from not-deleted photo
					favored = photo.getBoolean("favored");
					voted = photo.getInt("voted") != 0;
					id = photo.getInt("id");
					removeAllCommands();

					Command back = new Command(UI.localize("back"), COMMAND_BACK);
					Command action = new Command(UI.localize("photo"), COMMAND_PHOTO);
					Command action1 = new Command(UI.localize("photo") + " ", COMMAND_PHOTO);
					
					if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
						addCommand(action);
						addCommand(action1);
						addCommand(back);
					} else {
						addCommand(action);
						addCommand(action1);
					}
					
					setBackCommand(back);
					
					Container stats = new Container(new FlowLayout(CENTER));
					Label views = new Label(Integer.toString(photo.getInt("views")));
					views.setIcon(UI.getImage(UI.VIEWS_ICON));
					
					Label downvotes = new Label(Integer.toString(photo.getInt("downvotes")));
					downvotes.setIcon(UI.getImage(UI.DOWNVOTE_ICON));

					Label upvotes = new Label(Integer.toString(photo.getInt("upvotes")));
					upvotes.setIcon(UI.getImage(UI.UPVOTE_ICON));
					
					stats.addComponent(views);
					stats.addComponent(upvotes);
					stats.addComponent(downvotes);
					
					addComponent(BorderLayout.SOUTH, stats);
				}
				
			}});
	}

	public void handle(Params result) {
		if(result.getInt("result") == Handler.OK) {
			String comment = result.getString("text");
			Connection.getInstance().sendTweetphoto(new Params().set("id", id).set("action", COMMENT).set("text", comment));
		}
	}
}
