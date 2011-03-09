package me.pavo.ui;

import java.util.Calendar;
import java.util.Date;

import me.pavo.Post;
import me.pavo.UI;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.UIManager;

public class Tweet extends Container {
	
	private TopicManager paintListener;
	private AvatarText avatarText;
	private Component footer;
	private Post post;
	private Dimension cachedSize = null;
	
	public Tweet prev;
	public Tweet next;
	
	private long last_click = 0;
	
	public Tweet(Post post) {
		this.post = post;
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		
		int v_padding = Math.min(10, (Display.getInstance().getDisplayHeight() / 40) - 1);
		
		getStyle().setPadding(v_padding, v_padding, 0, 0);
		getSelectedStyle().setPadding(v_padding, v_padding, 0, 0);
		
		if(post.getOrigin() == Post.ORIGIN_TWITTER) {
			avatarText = new AvatarText(post);
			avatarText.setFocusable(false);
			footer = footer(post);
			addComponent(avatarText);
			addComponent(footer);
		} else if(post.getOrigin() == Post.ORIGIN_SYSTEM) {
			TextArea message = new TextArea(post.getBody());
			message.setFocusable(false);
			message.getStyle().setBorder(Border.createEmpty());
			message.setEditable(false);
			message.setPreferredH(80);
			addComponent(message);
		}
        setFocusable(true);
	}
	
	public String getUIID() {
		return "Tweet";
	}
	
	public Post getPost() {
		return post;
	}

	public void setPrev(Tweet prev) {
		if(this.prev != null) {
			this.prev.next = null;
		}
		this.prev = prev;
		if(prev != null) {
			prev.next = this;
		}
	}
	
	public void setNext(Tweet next) {
		if(this.next != null) {
			this.next.prev = null;
		}
		this.next = next;
		if(next != null) {
			next.prev = this;
		}
	}
	
	public Dimension getPreferredSize() {
		if(cachedSize == null) {
			cachedSize = super.getPreferredSize();
		}
		return cachedSize;
	}
	
	public void paint(Graphics g) {
		if(hasFocus() || Settings.getBool(Settings.TOUCHSCREEN)) {
			g.setColor(0xffffff);
			g.fillRect(getX(), getY(), getWidth(), getHeight());
		}
		paintListener.painted(this, getAbsoluteX(), getAbsoluteY());
		if(avatarText != null) {
			avatarText.setFocus(hasFocus());
		}
		super.paint(g);
		
		g.setColor(0xff000000);
		int dashw = 10;
		for(int x = getX(); x < getWidth(); x = x + dashw) {
			g.drawLine(x , getY() + getHeight() - 1, x + (dashw/2), getY() + getHeight() - 1);
		}
	}
	
	private Component footer(Post post) {
		Container footer = new Container(new FlowLayout(RIGHT));
		if(post.getForwardedBy().length() > 0) {
			Label forwarededBy = new Label(UI.getImage(UI.RETWEETED_ICON));
			forwarededBy.setText(post.getForwardedBy());
			forwarededBy.getStyle().setFont(UI.font(Settings.getString(Settings.MAIN_FONT_BOLD)));
			forwarededBy.getStyle().setFgColor(UIManager.getInstance().getComponentStyle("TweetUsername").getFgColor());
			footer.addComponent(forwarededBy);
		}
		
		if(post.isFavorited()) {
			footer.addComponent(new Label(UI.getImage(UI.SMALL_FAVORITE)));
		}
		
		if(!post.getInReplyTo().equals("")) {
			footer.addComponent(new Label(UI.getImage(UI.IRT_ICON)));
		}

		if(post.getAttached() != Post.ATTACHMENT_NONE) {
			Label attachment = new Label("");
			switch(post.getAttached()) {
			case Post.ATTACHMENT_IMAGE:
				attachment.setIcon(UI.getImage(UI.PHOTO_ICON));
				break;
			case Post.ATTACHMENT_VIDEO:
				attachment.setIcon(UI.getImage(UI.VIDEO_ICON));
				break;
			case Post.ATTACHMENT_AUDIO:
				attachment.setIcon(UI.getImage(UI.AUDIO_ICON));
				break;
			}
			footer.addComponent(attachment);
		}
		
		Label posted = new Label(dateToString(post.getPosted()));
		posted.getStyle().setFont(UI.font(Settings.getString(Settings.MAIN_FONT)));
		footer.addComponent(posted);
		footer.setPreferredW(Display.getInstance().getDisplayWidth());
		
		return footer;
	}
	
	 public static String dateToString(Date date) {
		 Date now = new Date();
		 Calendar cThen = Calendar.getInstance();
		 cThen.setTime(date);
		 long diff = (now.getTime() - date.getTime()) / 1000;
		 if(diff > 86400) {
				int y = cThen.get(Calendar.YEAR);
				int m = cThen.get(Calendar.MONTH) + 1;
				int d = cThen.get(Calendar.DATE);
				int h = cThen.get(Calendar.HOUR_OF_DAY);
				int mn = cThen.get(Calendar.MINUTE);
				String t = (y < 10 ? "0" : "") + y + "-" + (m < 10 ? "0" : "") + m
						+ "-" + (d < 10 ? "0" : "") + d + " " + (h < 10 ? "0" : "") + h + ":" + (mn < 10 ? "0" : "") + mn; 
				return t;
		 } else {
			 if(diff > 3600) {
				 return (diff / 3600) + " hr ago"; 
			 } else if(diff > 60) {
				 return (diff / 60) + " min ago";
			 } else {
				 return "few seconds ago";
			 }
		 }
	}

	public void setPaintListener(TopicManager listener) {
		this.paintListener = listener;
	}

	public void pointerReleased(int x, int y) {
		super.pointerReleased(x, y);
		if(last_click == 0) {
			last_click = System.currentTimeMillis();
		} else if(System.currentTimeMillis() - last_click < 700) {
			Command command = new Command("", Screen.SB_TWEET);
			getComponentForm().dispatchCommand(command, new ActionEvent(command));
		} else {
			last_click = System.currentTimeMillis();
		}
	}

	public void rebuild() {
		Component newFooter = footer(post);
		replace(footer, newFooter, null);
		footer = newFooter;
	}
}
