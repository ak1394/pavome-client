package me.pavo.ui;

import me.pavo.UI;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;

public class Confirmation implements Runnable, WorkflowElement {

	private Handler handler;
	private String decline;
	private String confirm;
	private String text;
	
	public Confirmation(String text, String confirm, String decline) {
		this.text = text;
		this.confirm = confirm;
		this.decline = decline;
	}

	public void run() {
		if(UI.confirm(text, confirm, decline)) {
			handle(new Params().set("result", Handler.OK));
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
