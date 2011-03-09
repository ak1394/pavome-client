package me.pavo.ui;

import me.pavo.Post;
import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.layouts.BorderLayout;

public class PhotoView extends Form implements FutureCallback {
	private Label iconLabel;

	public PhotoView(String kind, String name, Post post, final Form back) {
		super();
		setLayout(new BorderLayout());

		iconLabel = new Label("Loading...");
		iconLabel.setIcon(UI.getImage(UI.WAIT_ICON));
		iconLabel.setAlignment(Label.CENTER);
		iconLabel.setTextPosition(Label.BOTTOM);
		
		addComponent(BorderLayout.CENTER, iconLabel);
		addCommand(new Command("Back") {
            public void actionPerformed(ActionEvent ev) {
            	back.show();
            }
		});
		
		Connection.getInstance().getAttachment(kind, name, post.getId(), Post.ATTACHMENT_IMAGE).addCallback(this);
	}
	
	protected void onShowCompleted() {
		Cache.removeAll();
	}

	public void callbackFired(final Future future) {
		Display.getInstance().callSerially(new Runnable(){
			public void run() {
				Params photo = (Params) future.getResult();
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
			}});
	}
}
