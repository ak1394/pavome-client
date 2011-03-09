package me.pavo.ui;

import java.util.Stack;
import java.util.Vector;

import me.pavo.AvatarStore;
import me.pavo.Post;
import me.pavo.UI;
import me.pavo.server.Settings;
import me.pavo.text.Hyphenator;
import me.pavo.text.Parser;
import me.pavo.text.Text;

import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;

public class AvatarText extends Component {
	private Vector rendered;
	private int avatarPadding = 2;
	private int width;
	private int height;
	private Font font;
	private Font font_bold;
	private Post post;
	private int lineHeight;
	private int textColor;
	private int usernameColor;
	private int linkColor;
	private int hashtagColor;
	private Object cachedImageKey = new Object();
	private Object cachedSelectedImageKey = new Object();
	
	public AvatarText(Post post) {
		this.post = post;
		textColor = UIManager.getInstance().getComponentStyle("TweetText").getFgColor();
		usernameColor = UIManager.getInstance().getComponentStyle("TweetUsername").getFgColor();
		linkColor = UIManager.getInstance().getComponentStyle("TweetLink").getFgColor();
		hashtagColor = UIManager.getInstance().getComponentStyle("TweetHashtag").getFgColor();
		font = UI.font(Settings.getString(Settings.MAIN_FONT));
		font_bold = UI.font(Settings.getString(Settings.MAIN_FONT_BOLD));
		lineHeight = font.getHeight();
	}

	protected Dimension calcPreferredSize() {
		if(rendered == null) {
			render();
		}
		return new Dimension(width, height);
	}

	public String getUIID() {
		return "AvatarText";
	}
	
	public void repaint() {
	}

	public void paint(Graphics g) {
		long ts = System.currentTimeMillis();
		Style style = getStyle();
		
		if(rendered == null) {
			render();
		}

		if(hasFocus() || Settings.getBool(Settings.TOUCHSCREEN)) {
			Image cacheSelected;
			if(!Cache.has(cachedSelectedImageKey)) {
				cacheSelected = Image.createImage(getWidth(), getHeight());
				Graphics g1 = cacheSelected.getGraphics();
				g1.setColor(0xffffff);
				g1.fillRect(0, 0, getWidth(), getHeight());
				g1.translate(-getX(), -getY());
				for(int i=0; i<rendered.size(); i++) {
					Text text = (Text) rendered.elementAt(i);
					drawText(g1, text, getX() + text.x, getY() + text.y, text.width);
				}
				Cache.put(cachedSelectedImageKey, cacheSelected);
			}
			g.drawImage((Image)Cache.get(cachedSelectedImageKey), getX(), getY());
		} else {
			Image cache;
			if(!Cache.has(cachedImageKey)) {
				cache = Image.createImage(getWidth(), getHeight());
				Graphics g1 = cache.getGraphics();
				g1.setColor(0xbbbbbb);
				g1.fillRect(0, 0, getWidth(), getHeight());
				g1.translate(-getX(), -getY());
				for(int i=0; i<rendered.size(); i++) {
					Text text = (Text) rendered.elementAt(i);
					drawText(g1, text, getX() + text.x, getY() + text.y, text.width);
				}
				Cache.put(cachedImageKey, cache);
			}
			g.drawImage((Image)Cache.get(cachedImageKey), getX(), getY());
		}
		Image avatar = AvatarStore.getInstance().getAvatar(post);
		g.drawImage(avatar, getX() + getStyle().getPadding(LEFT), getY() + style.getPadding(TOP) + avatarPadding);
	}
	
	public void render() {
		Style style = getStyle();
		int avatarWidth = 32;
		int avatarHeight = 32;
		rendered = new Vector();
		width = Display.getInstance().getDisplayWidth() - style.getPadding(LEFT) - style.getPadding(RIGHT);
		
		Stack content = Parser.parse(post.getBody());
		content.push(new Text(" ", Text.SPACE));		
		content.push(new Text(post.getAuthor(), Text.USERNAME));
		
		Hyphenator hyphenator = new Hyphenator(font, content);
		
		int line = 0;
		while(line * lineHeight < avatarHeight + avatarPadding + avatarPadding && !content.empty()) {
			int x = style.getPadding(LEFT) + avatarWidth + avatarPadding;
			int y = line * lineHeight;
			renderLine(hyphenator, content,  x, y, width - avatarWidth - avatarPadding);
			line++;
		}
		
		while(!content.empty()) {
			int x = style.getPadding(LEFT);
			int y = style.getPadding(TOP) + line * lineHeight;
			renderLine(hyphenator, content,  x, y, width);
			line++;
		}
		
		height = style.getPadding(TOP) + (line * lineHeight) + style.getPadding(BOTTOM);
		
		int minSize = avatarHeight + avatarPadding + avatarPadding + lineHeight; 
		height = height < minSize ? minSize : height;  
	}
	
	
	private void renderLine(Hyphenator hyphenator, Stack content, int x, int y, int maxWidth) {
		boolean forceHyphenation = true;
		while(!content.empty()) {
			Text text = (Text)content.pop();
			int width = getFont(text).stringWidth(text.toString());
			if(width > maxWidth) {
				text = hyphenator.split(getFont(text), text, maxWidth, forceHyphenation);
				text.width = getFont(text).stringWidth(text.toString());
				text.x = x;
				text.y = y;
				rendered.addElement(text);
				return;
			}
			text.width = width;
			text.x = x;
			text.y = y;
			rendered.addElement(text);			
			x = x + width;
			maxWidth = maxWidth - width;
			forceHyphenation = false;
		}
	}

	private void drawText(Graphics g, Text text, int x, int y, int width) {
		switch(text.getType()) {
		case Text.HASHTAG:
			g.setFont(getFont(text));
			g.setColor(hashtagColor);
			g.drawString(text.toString(), x, y);
			break;
		case Text.USERNAME:
			g.setFont(getFont(text));
			g.setColor(usernameColor);
			g.drawString(text.toString(), x, y);
			break;
		case Text.URL:
			g.setFont(getFont(text));
			g.setColor(linkColor);
			g.drawString(text.toString(), x, y);
			g.drawLine(x, y+lineHeight-1, x+width, y+lineHeight-1);
			break;
		default:
			g.setFont(getFont(text));
			g.setColor(textColor);
			g.setColor(0xff000000);
			g.drawString(text.toString(), x, y);
		}
	}
	
	private Font getFont(Text text) {
		switch(text.getType()) {
		case Text.USERNAME:
			return font_bold;
		default:
			return font;
		}
	}
}