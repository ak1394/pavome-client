package me.pavo.ui;

import me.pavo.UI;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.animations.Animation;
import com.sun.lwuit.animations.Motion;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.painter.PainterChain;

public class NotificationAnimation implements Animation, Painter {
	private Form form;
	private Motion motion;
	private int displayWidth;
	private Image icon;
	
	public NotificationAnimation(String kind, String name, int count) {
		icon = UI.getIconForScreenBig(kind, name);
	}
	
	public void start(Form form) {
		this.form = form;
		displayWidth = Display.getInstance().getDisplayWidth();
		motion = Motion.createSplineMotion(0, Display.getInstance().getDisplayHeight() - icon.getHeight(), 650);
		motion.start();
		PainterChain.installGlassPane(form, this);
		form.registerAnimated(this);
	}
	
	public boolean animate() {
		boolean finished = motion.isFinished();
		if(finished) {
			form.deregisterAnimated(this);
			PainterChain.removeGlassPane(form, this);
		}
		return !finished;
	}

	public void paint(Graphics g) {
		form.repaint();
	}

	public void paint(Graphics g, Rectangle rect) {
		int v = motion.getValue();
		g.drawImage(icon, displayWidth - icon.getWidth() , v);
	}
}
