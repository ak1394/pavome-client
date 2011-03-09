package me.pavo.ui;

import me.pavo.UI;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;

public class SearchForm extends Form implements ActionListener {
	private TextArea query;
	private Command back;
	
	public SearchForm() {
		setTitle("Search");
		back = new Command(UI.localize("back"));
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		
		query = TextField.create();
		addComponent(query);
		Container container = new Container(new FlowLayout(CENTER));
		container.addComponent(new Button(new Command("Search")));
		addComponent(container);
		addCommandListener(this);
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(back);
		}
		setBackCommand(back);
	}

	public String getUIID() {
		return "Search";
	}
	
	public void actionPerformed(ActionEvent evt) {
		if(evt.getCommand() != null && evt.getCommand().equals(back)) {
			UI.getInstance().showCurrent();
		} else if(query.getText().length() > 0) {
			UI.getInstance().showSearchScreen(query.getText());
		}
	}
}
