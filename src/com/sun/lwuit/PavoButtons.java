package com.sun.lwuit;

import java.util.Enumeration;
import java.util.Vector;

import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.layouts.FlowLayout;

public class PavoButtons extends Container implements FocusListener, Painter, ActionListener {
	private ActionList actions;
	private Button current;
	private PavoDialog d;
	private int top;
	private Form form;
	private SnapshotForm snapshot;
	private Params shortcuts;

	public PavoButtons(ActionList actions, FocusListener focusListener, boolean expanded) {
		this(actions, focusListener, expanded, new Params());
	}	
	
	public PavoButtons(ActionList actions, FocusListener focusListener, boolean expanded, Params keyShortcuts) {
		this.actions = actions;
		this.shortcuts = keyShortcuts;
		Vector commands = actions.getCommands();
		float ratio = expanded ? 1.2f : 1.1f;
		int maxheight = 0;
		int maxwidth = 0;
		
		Vector buttons = new Vector();
		for(Enumeration e = commands.elements(); e.hasMoreElements();) {
			Button b = new Button((Command) e.nextElement());
			b.setPreferredW((int) (b.getIcon().getWidth() * ratio));
			b.setPreferredH((int) (b.getIcon().getHeight() * ratio) + b.getStyle().getFont().getHeight());
			b.setTickerEnabled(false);
			b.setAlignment(CENTER);
			b.setTextPosition(BOTTOM);
			b.setUIID("ExMenuButton");
			if(focusListener != null) {
				b.addFocusListener(focusListener);
			}
			UI.updateSelectionStyle(b, 0xbb);			
			buttons.addElement(b);
			maxheight = Math.max(b.getPreferredH(), maxheight);
			maxwidth = Math.max(b.getPreferredW(), maxwidth);
		}
		
		int columns = Display.getInstance().getDisplayWidth() / maxwidth;
		int rows = ((commands.size() + (columns-1))  / columns);
		
		int new_width = Math.max(maxwidth, (Display.getInstance().getDisplayWidth() / columns) - 2);
		
		for(Enumeration e = buttons.elements(); e.hasMoreElements();) {
			Button b = (Button) e.nextElement();
			b.setWidth(new_width);
			addComponent(b);
		}
		
		if(expanded) {
			setScrollableY(true);
			setScrollableX(false);
			setPreferredH((maxheight * rows) + 10); // some problem with scrolling, adding 10 px as a fix
			setPreferredW(Display.getInstance().getDisplayWidth());
		} else {
			setPreferredH(maxheight);
			setScrollableX(true);
			setScrollableY(false);
			setIsScrollVisible(false);
		}
	}
	
	public Params showDialog() {
		
		form = Display.getInstance().getCurrent();
		
		top = Display.getInstance().getDisplayHeight()
		- this.getPreferredH() - Display.getInstance().getCurrent().getTitleComponent().getPreferredH() 
		- Display.getInstance().getCurrent().getMenuBar().getPreferredH();		
		
		snapshot = new SnapshotForm(Display.getInstance().getCurrent());
		snapshot.show();
		
		d = new PavoDialog();
        d.setLayout(new FlowLayout());
        d.setScrollableY(false);
        d.setScrollableX(true);
        d.getContentPane().setIsScrollVisible(false);
        setScrollable(false);
        
		Command cancel = new Command(UI.localize("cancel"));
		Command select = new Command(UI.localize("select"));
		Command select1 = new Command(UI.localize("select") + " ");
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			d.addCommand(select);
			d.addCommand(select1);
			d.addCommand(cancel);
		} else {
			d.addCommand(select);
			d.addCommand(select1);
		}
        d.setBackCommand(cancel);
        d.addComponent(this);
        d.addFocusListener(this);

        if(!Settings.getBool(Settings.TOUCHSCREEN)) {
            d.setGlassPane(this);
        }
        
        d.addCommandListener(this);
        
		Command c = d.show(top, 0, 0, 0, false, true);
		
		form.show();
		
		if(c != cancel) {
			return actions.getAction(current.getCommand());
		} else {
			return null;
		}
	}

	public void focusGained(Component cmp) {
		if(cmp instanceof Button) {
			current = (Button)cmp;
		}
	} 

	public void focusLost(Component cmp) {
	}

	public void paint(Graphics g, Rectangle rect) {
		int top_offset = 2;
		int side_offset = 67;
		int padding = 2; 
		int width = 36;
		int height = 20;
		int arc = 10;
		
		Font font = UI.font(Settings.getString(Settings.MAIN_FONT));		
		g.setClip(0, 0, Display.getInstance().getDisplayWidth(), Display.getInstance().getDisplayHeight());
		g.setColor(0xffffff);
		g.fillRoundRect(Display.getInstance().getDisplayWidth() - side_offset, top_offset, width, height, arc, arc);
		g.drawImage(UI.getImage(UI.KEYPAD_ICON), Display.getInstance().getDisplayWidth() - side_offset + padding, top_offset + padding);
		g.setColor(0x000000);
		g.drawRoundRect(Display.getInstance().getDisplayWidth() - side_offset, top_offset, width, height, arc, arc);
		
		if(shortcuts.containsKey(actions.getAction(current.getCommand()).get("action"))) {
			char c = (char) shortcuts.getInt(actions.getAction(current.getCommand()).get("action"));
			if(c != 0) {
				g.setColor(0x000000);
				g.setFont(font);		
				g.drawChar(c, Display.getInstance().getDisplayWidth() - side_offset +  padding + padding + 16, top_offset + ((height - font.getHeight()) / 2));
			}
		}
		
		Display.getInstance().getImplementation().flushGraphics(Display.getInstance().getDisplayWidth() - side_offset, top_offset, width, height);
	}

	public void actionPerformed(ActionEvent evt) {
		snapshot.allowRepaint();
	}
}