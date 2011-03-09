package me.pavo.logic;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;

import me.pavo.UI;
import me.pavo.server.Params;
import me.pavo.ui.Handler;

public class Workflow extends Thread implements Handler {	
	private static final Params NO_RESULT = new Params();
	private static final Object CURRENT = new Object();
	private Params result;

	public synchronized Params show(Showable showable) {
		return showObject(showable);
	}
	
	public synchronized Params show(Form form) {
		return showObject(form);
	}

	public synchronized Params show(Runnable runnable) {
		return showObject(runnable);
	}

	private synchronized Params showObject(Object wrapped) {
		result = NO_RESULT;
		Display.getInstance().callSerially(new RunnableWrapper(wrapped));
		return waitForResult(wrapped);
	}
	
	public synchronized void showCurrentScreen() {
		Display.getInstance().callSerially(new RunnableWrapper(CURRENT));
	}
	
	public synchronized Params waitForResult(Object workflowElement) {
		if(workflowElement instanceof WorkflowElement) {
			while (result == NO_RESULT) {
				try {	this.wait(100); } catch (InterruptedException err) { }
			}
			return result;
		} else {
			return null;
		}
	}
	
	public synchronized void handle(Params result) {
		if(this.result == NO_RESULT) {
			this.result = result;
			this.notify();
		}
	}
	
	public boolean isOk(Params result) {
		return result.getInt("result") == OK;
	}

	public boolean isCancel(Params result) {
		return result.getInt("result") == CANCEL;
	}
	
	class RunnableWrapper implements Runnable {
		private Object wrapped;

		public RunnableWrapper(Object wrapped) {
			this.wrapped = wrapped;
		}

		public void run() {
			if(wrapped instanceof WorkflowElement) {
				((WorkflowElement) wrapped).setHandler(Workflow.this);
			}

			if(wrapped instanceof Form) {
				((Form)wrapped).show();
			} else if(wrapped instanceof Showable) {
				((Showable)wrapped).show();
			} else if(wrapped instanceof Runnable) {
				((Runnable)wrapped).run();
			}
			else if(wrapped == CURRENT) {
				UI.getInstance().showCurrent();
			}
		}
	}
}
