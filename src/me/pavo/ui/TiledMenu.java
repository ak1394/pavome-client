package me.pavo.ui;

import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.logic.ToplevelDispatcher;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Painter;
import com.sun.lwuit.PavoButtons;
import com.sun.lwuit.PavoForm;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.geom.Rectangle;

public class TiledMenu extends PavoForm implements FocusListener, ActionListener, Painter {
	private ActionList actions;
	private Command select;
	private Button current;
	private Params shortcuts;
	
	public TiledMenu(ActionList actions) {
		this(actions, null);
	}

	public TiledMenu(ActionList actions, Params keyShortcuts) {
		this.actions = actions;
		this.shortcuts = keyShortcuts;
		PavoButtons buttons = new PavoButtons(actions, this, true);
		addComponent(buttons);
		Command back = actions.append(ToplevelDispatcher.BACK_TO_CURRENT, UI.localize("back"), null);
		select = new Command(UI.localize("select") + " ");
		addCommand(select);
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(back);
		}
		setBackCommand(back);
		addCommandListener(this);
		addCommandListener(actions);
		if(shortcuts != null && !Settings.getBool(Settings.TOUCHSCREEN)) {
			setGlassPane(this);
		}
		setScrollable(false);
	}

	public void actionPerformed(ActionEvent evt) {
		if(evt.getCommand() != null && evt.getCommand().equals(select)) {
			evt.consume();
			actions.actionPerformed(new ActionEvent(current.getCommand()));
		}
	}	
	
	public void focusGained(Component cmp) {
		if(cmp instanceof Button) {
			setTitle(((Button)cmp).getText());
			current = (Button) cmp;
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
			g.setColor(0x000000);
			g.setFont(font);		
			g.drawChar(c, Display.getInstance().getDisplayWidth() - side_offset +  padding + padding + 16, top_offset + ((height - font.getHeight()) / 2));
		}
		Display.getInstance().getImplementation().flushGraphics(Display.getInstance().getDisplayWidth() - side_offset, top_offset, width, height);
	}
	
}