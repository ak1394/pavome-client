package com.sun.lwuit;

public class T9TextArea extends TextArea {
	
	private String t9Text; 
	
	public T9TextArea(String initialText, int maxsize) {
		super(initialText, maxsize);
	}
	
	public String editText() {
		t9Text = null;
		super.editString();
		return t9Text;
	}

	void onEditComplete(String text) {
		super.onEditComplete(text);
		t9Text = text;
	}
	
	public String getT9Text() {
		return t9Text;
	}
}
