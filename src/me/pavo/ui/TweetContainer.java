package me.pavo.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.UIManager;

public class TweetContainer extends Container {
	
	private TopicManager manager;

	public TweetContainer() {
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		setScrollable(true);
	}
	
	public void paint(Graphics g) {
		manager.startedPainting();
		super.paint(g);
		manager.finishedPainting();
	}
	
	protected void paintScrollbars(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawVerticalScroll(g, this, manager.scrollOffset, manager.scrollBlock);
	}

	public void setManager(TopicManager manager) {
		this.manager = manager;
	}
	
	public void setScrollY(int scrollY) {
		super.setScrollY(scrollY);
	}
}
