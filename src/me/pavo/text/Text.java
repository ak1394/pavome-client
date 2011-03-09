package me.pavo.text;

public class Text {
	public static final int TEXT = 0;
	public static final int SPACE = 1;
	public static final int URL = 2;
	public static final int USERNAME = 3;
	public static final int HASHTAG = 4;
	
	private String text;
	private int type;
	public int width;
	public int x;
	public int y;
	
	public Text(String text, int type) {
		this.text = text;
		this.type = type;
	}
	
	public String toString() {
		return text;
	}
	
	public int getType() {
		return type;
	}
	
}
