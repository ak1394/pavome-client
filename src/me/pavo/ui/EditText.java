package me.pavo.ui;

import me.pavo.UI;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.T9TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.GridLayout;

public class EditText extends Form implements WorkflowElement, ActionListener {

	private Handler handler;
	private T9TextArea textArea;
	private TextField textField;
	private boolean fired;
	private Command cancel;
	private Command ok;
	
	public EditText() {
		this("");
	}
	
	public EditText(String text) {
		this(text, 140);
	}
	
	public EditText(String text, int limit) {
		setLayout(new BorderLayout());
		ok = new Command(UI.localize("ok"));
		cancel = new Command(UI.localize("cancel"));
		if(Settings.getBool(Settings.VKB_ONLY)) {
			textField = new TextField(limit);
			Container buttons = new Container(new GridLayout(1, 2));
			Button okButton = new Button(ok);
			okButton.setAlignment(CENTER);
			buttons.addComponent(okButton);
			Button cancelButton = new Button(cancel);
			cancelButton.setAlignment(CENTER);
			buttons.addComponent(cancelButton);
			addComponent(BorderLayout.CENTER, textField);
			addComponent(BorderLayout.SOUTH, buttons);
		} else {
			textArea = new T9TextArea(text, limit);
			addComponent(BorderLayout.CENTER, textArea);
		}
		if(!Settings.getBool(Settings.VKB_ONLY) && !Settings.getBool(Settings.INVISIBLE_T9)) {
			textArea.addActionListener(this);
		}
		addCommandListener(this);
	}
	
    public void show() {
		if(Settings.getBool(Settings.INVISIBLE_T9) && !Settings.getBool(Settings.VKB_ONLY)) {
			textArea.editText();
			handle(textArea.getT9Text());
		} else {
			super.show();
		}
    }
    
    protected void onShowCompleted() {
    	if(!fired) {
    		fired = true;
    		if(!Settings.getBool(Settings.VKB_ONLY)) {
        		textArea.editText();
    		}
    	}
    }
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	private void handle(String text) {
		if(handler != null) {
			if(text == null) {
				handler.handle(new Params().set("result", Handler.CANCEL));
			} else {
				handler.handle(new Params().set("result", Handler.OK).set("text", text));
			}
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		if(Settings.getBool(Settings.VKB_ONLY)) {
			if(evt.getCommand().equals(ok)) {
				handle(textField.getText());
			} else {
				handle(null);
			}
		} else {
			handle(textArea.getT9Text()); 
		}
	}
}