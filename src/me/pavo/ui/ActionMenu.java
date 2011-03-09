package me.pavo.ui;

import java.util.Enumeration;
import java.util.Hashtable;

import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.logic.ToplevelDispatcher;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.PavoForm;
import com.sun.lwuit.SnapshotForm;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.GroupLayout;

public class ActionMenu extends PavoForm implements FocusListener, ActionListener {
	
	public static final int TOP = 1;
	public static final int BOTTOM = 7;
	public static final int LEFT = 3;
	public static final int RIGHT = 5;
	public static final int CENTER = 4;
	
	private Hashtable buttons;
	private Command select;
	private ActionList actions;
	private Button current;
	
	public ActionMenu(ActionList actions) {
		this.actions = actions;
		getStyle().setBgTransparency(0);
		
		buttons = new Hashtable();
		for(Enumeration e = actions.getCommands().elements(); e.hasMoreElements();) {
			Command c = (Command) e.nextElement();
			if(Settings.getBool(Settings.SMALLSCREEN) || Settings.getBool(Settings.SHORTSCREEN)) {
				Params action = actions.getAction(c);
				String name = action.getString("icon_name") + "48";
				c = new Command(c.getCommandName(), UI.getImage(name), c.getId());
			}
			Button b = new Button(c);
			b.setTickerEnabled(false);
			b.setText("");
			b.setUIID("ExMenuButton");
			b.addFocusListener(this);
			UI.updateSelectionStyle(b, 0x77);
			buttons.put(actions.getAction(c).get("position"), b);
		}
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutocreateGaps(false);
		layout.setAutocreateContainerGaps(false);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.add(0, 1, 1000)
				.add(button(TOP))
				.add(layout.createParallelGroup().add(button(LEFT)).add(button(CENTER)).add(button(RIGHT)))
				.add(button(BOTTOM))
				.add(0, 1, 1000)
		);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.add(0, 1, 1000)
				.add(button(LEFT))
				.add(layout.createParallelGroup().add(button(TOP)).add(button(CENTER)).add(button(BOTTOM)))
				.add(button(RIGHT))
				.add(0, 1, 1000)
			);
		
		setLayout(layout);
		setFocused(button(CENTER));
		
		Command back = actions.append(ToplevelDispatcher.BACK_TO_CURRENT, UI.localize("back"), null);
		select = new Command(UI.localize("select") + " ");
		addCommand(select);
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(back);
		}
		setBackCommand(back);
		addCommandListener(this);
		addCommandListener(actions);
	}
	
	public void showMenu() {
		new SnapshotForm(Display.getInstance().getCurrent()).show();
		showModal(0, 1, 0, 0, false, false, false);
	}

	private Component button(int position) {
		Integer p = new Integer(position);
		if(!buttons.containsKey(p)) {
			buttons.put(p, new Label());
		}
		return (Component) buttons.get(p);
	}
	
	public void focusGained(Component cmp) {
		if(cmp instanceof Button) {
			setTitle(((Button)cmp).getCommand().getCommandName());
			current = (Button) cmp;
		}
	}
	
	public void focusLost(Component cmp) {
	}

	public void actionPerformed(ActionEvent evt) {
		if(evt.getCommand() != null && evt.getCommand().equals(select)) {
			evt.consume();
			actions.actionPerformed(new ActionEvent(current.getCommand()));
		}
	}	
}