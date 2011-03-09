package me.pavo.ui;

import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.server.FutureErrback;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.PavoForm;

public abstract class FutureForm extends PavoForm implements FutureCallback, FutureErrback {
	Form back;
	Connection connection;

	FutureForm(Future future, Connection connection, String message, Form back) {
		this.back = back;
		this.connection = connection;
		Waiting waiting = new Waiting("Loading profile..."); 
		waiting.show();
		future.addCallback(this);		
		future.addErrback(this);		
	}
	
	public abstract void build(Object result);
	
	public void callbackFired(final Future future) {
		Display.getInstance().callSerially(new Runnable(){
			public void run() {
				FutureForm.this.build(future.getResult());
				FutureForm.this.show();
			}});
	}

	public void errbackFired(final Future future) {
		if(future.getResult() != null) {
			UI.error((String) future.result);
		} else {
			UI.error("Request failed");
		}
		back.show();
	}
	
	
}
