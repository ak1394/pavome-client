package me.pavo.ui;

import com.sun.lwuit.Component;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.plaf.Border;

public class DashedBorder extends Border {
	public void paint(Graphics g, Component c) {
		int w = c.getWidth();
		for(int x = c.getX(); x < w; x = x + 10) {
			g.drawLine(x , c.getY(), x + (10/2), c.getY());
		}
	}
}
