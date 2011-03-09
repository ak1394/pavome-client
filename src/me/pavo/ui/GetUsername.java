package me.pavo.ui;

import me.pavo.UI;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;

import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;

public class GetUsername extends Form implements ActionListener, WorkflowElement {
	private TextArea username;
	private Button ok;
	private Button cancel;
	private Handler handler;
	
	public GetUsername() {
		setTitle("New Direct Message");
		username = TextField.create();
		username.setText("@");
		
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		addComponent(new Label("To:"));
		addComponent(username);
		
		
		Container buttons = new Container(new FlowLayout(Component.CENTER));
		ok = new Button(UI.localize("ok"));
		cancel = new Button(UI.localize("cancel"));
		ok.addActionListener(this);
		cancel.addActionListener(this);
		buttons.addComponent(ok);
		buttons.addComponent(cancel);
		addComponent(buttons);
	}
	
	public String getUIID() {
		return "GetUsername";
	}

	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == ok && username.getText().length() > 1) {
			handle(new Params().set("result", Handler.OK).set("username", username.getText()));
		} else {
			handle(new Params().set("result", Handler.CANCEL));
		}
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	private void handle(Params result) {
		if(handler != null) {
			handler.handle(result);
		}
	}
}
