package me.pavo.logic;

import java.util.Vector;

import me.pavo.UI;
import me.pavo.server.Params;
import me.pavo.server.ParamsList;

import com.sun.lwuit.Command;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

public class ActionList implements ActionListener {
	
	private ParamsList actions;
	private ActionDispatcher dispatcher;

	public ActionList() {
		this(null);
	}

	public ActionList(ActionDispatcher dispatcher) {
		this.actions = new ParamsList();
		this.dispatcher = dispatcher;
	}

	public Command append(int action, String name, String icon_name) {
		return append(make(action, name, icon_name));
	}

	public void prepend(int action, String name, String icon_name) {
		prepend(make(action, name, icon_name));
	}
	
	public Command append(Params action) {
		int id = actions.append(action);
		return makeCommand(action, id);
	}

	public void prepend(ParamsList actions) {
		this.actions.prepend(actions);
	}

	public void append(ParamsList actions) {
		this.actions.append(actions);
	}
	
	public void prepend(Params action) {
		actions.prepend(action);
	}

	public Params make(int action, String name, String icon_name) {
		Params a = new Params();
		a.set("action", action);
		a.set("name", name);
		a.set("icon_name", icon_name);
		return a;
	}
	
	public Command makeCommand(Params action, int id) {
		String name = UI.localize(action.getString("name"));
		Image icon = action.has("icon_name") ? UI.getImage(action.getString("icon_name")) : null; 
		return new Command(name, icon, id);
	}
	
	public boolean contains(String name, int value) {
		return actions.contains(name, value);
	}
	
	public boolean contains(String name, Object value) {
		return actions.contains(name, value);
	}
	
	public void delete(String name, int value) {
		actions.delete(name, value);
	}

	public void delete(String name, Object value) {
		actions.delete(name, value);
	}
	
	public Vector getCommands() {
		Vector commands = new Vector();
		for(int i=0; i<actions.size(); i++) {
			commands.addElement(makeCommand(actions.get(i), i));
		}
		return commands;
	}
	
	public Params getAction(Command c) {
		return actions.get(c.getId());
	}

	public Params getAction(Integer commandId) {
		return actions.get(commandId.intValue());
	}
	
	public void actionPerformed(ActionEvent evt) {
		dispatcher.dispatch(getAction(evt.getCommand()));
	}
	
	public int size() {
		return actions.size();
	}
	
	public Params action(int index) {
		return actions.get(index);
	}
	
	public Params find(Integer action) {
		return actions.find("action", action);
	}
}
