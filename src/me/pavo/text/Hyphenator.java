package me.pavo.text;

import java.util.Stack;

import com.sun.lwuit.Font;

public class Hyphenator {
	private Stack elements; 
	
	public Hyphenator(Font font, Stack elements) {
		this.elements = elements;
	}
	
	public Text split(Font font, Text text, int maxwidth, boolean forceHyphenation) {
		String string = text.toString();
		int width = 0;
		for(int i = 0;  i < string.length(); i++) {
			int charWidth = font.charWidth(string.charAt(i));
			if(width + charWidth > maxwidth) {
				String left = string.substring(0, i);
				String right = string.substring(i);
				Text leftText;
				if(forceHyphenation || (left.length() >= 3 && right.length() >= 2)) {
					Text rightText = new Text(right, text.getType());
					leftText = new Text(left, text.getType());
					elements.push(rightText);
				} else {
					leftText = new Text("", text.getType());
					elements.push(text);
				}
				return leftText; 
			} else {
				width = width + charWidth;
			}
		}
		return text;
	}
}
