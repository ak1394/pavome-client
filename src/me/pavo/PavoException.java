package me.pavo;

public class PavoException extends Exception {
	String location;
	
	public PavoException(String location, Exception e) {
		super(e.toString());
		this.location = location;
	}

	public PavoException(String location, String message) {
		super(message);
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
}
