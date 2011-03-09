package me.pavo.server;


public class Future {
	private boolean arrived = false;
	private boolean callbackFired = false;
	private boolean errbackFired = false;
	private boolean success;
	public Integer id;
	public Object result;
	private FutureCallback callback;
	private FutureErrback errback;
	public Object callbackParams;
	

	public Future(Integer id) {
		this.id = id;
	}

	public synchronized Future addCallback(FutureCallback callback) {
		this.callback = callback;
		if(arrived) {
			fireCallbacks();
		}
		return this;
	}
	
	
	public synchronized Future addErrback(FutureErrback errback) {
		this.errback = errback;
		if(arrived) {
			fireErrbacks();
		}
		return this;
	}
	
	public synchronized Future addCallback(FutureCallback callback, Object callbackParams) {
		this.callback = callback;
		this.callbackParams = callbackParams;
		if(arrived) {
			fireCallbacks();
		}
		return this;
	}
	
	private void fireCallbacks() {
		if(!callbackFired && success && callback != null) {
			callback.callbackFired(this);
			callbackFired = true;
		}
	}

	private void fireErrbacks() {
		if(!errbackFired && !success && errback != null) {
			errback.errbackFired(this);
			errbackFired = true;
		}
	}
	
	public synchronized void block() {
		while (!arrived) {
			try {
				this.wait(500);
			} catch (InterruptedException err) {
			}
		}
	}

	public boolean isSuccess() {
		if (!arrived)
			block();
		return success;
	}

	public Object getResult() {
		if (!arrived)
			block();
		return result;
	}

	public synchronized void arrived(Message message) {
		if(message == null || (message.packet != Packet.ERROR && message.packet != Packet.ERROR_MESSAGE)) {
			success = true;
		} else {
			success = false;
		}
		
		if(message != null) {
			result = message.result;
		}
		
		arrived = true;
		notify();
		fireCallbacks();
		fireErrbacks();
	}
}
