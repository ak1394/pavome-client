package me.pavo.text;

import java.util.Stack;

public class Parser {
	static String spaceChars = " ";
	static String sepChars = "¬!\"$%^&*()[]<>{};:,.'\\/~|";
	
	public Parser() {
		
	}

	public static Stack parse(String string) {
		Stack result = new Stack();
		StringBuffer current = new StringBuffer();
		for(int i = 0;  i < string.length(); i++) {
			char c = string.charAt(i); 
			if(spaceChars.indexOf(c) == -1) {
				// not a whitespace char
				current.append(c);
			} else {
				// whitespace char
				if(current.length() > 0) {
					insert(result, current.toString());
					current = new StringBuffer();
				}
				if(!result.empty() && ((Text)result.peek()).getType() != Text.SPACE) {
					insert(result, new Text(" ", Text.SPACE));
				}
			}
		}
		
		if(current.length() > 0) {
			insert(result, current.toString());
		}
		
		Stack reversed = new Stack();
		while(!result.empty()) {
			reversed.push(result.pop());
		}

		return reversed;
	}
	
	public static Stack filter(Stack result, int textType) {
		Stack filtered = new Stack();
		while(!result.isEmpty()) {
			Text text = (Text) result.pop();
			if(text.getType() == textType) {
				filtered.push(text.toString());
			}
		}
		return filtered;
	}
	
	private static void insert(Stack result, String value) {
		if(value.startsWith("http://") || value.startsWith("https://")) {
			result.push(new Text(value, Text.URL));
		} else if(value.startsWith("@")) {
			parseUsernameOrHashtag(result, value, Text.USERNAME);
		} else if(value.startsWith("#")) {
			parseUsernameOrHashtag(result, value, Text.HASHTAG);
		} else  {
			result.push(new Text(value, Text.TEXT));
		}
	}

	private static void insert(Stack result, Text value) {
		result.push(value);
	}
	
	private static void parseUsernameOrHashtag(Stack result, String value, int what) {
		StringBuffer current = new StringBuffer();
		for(int i = 0;  i < value.length(); i++) {
			char c = value.charAt(i); 
			if(sepChars.indexOf(c) == -1) {
				// not separator char
				current.append(c);
			} else {
				// separator char
				if(current.length() > 1) {
					result.push(new Text(current.toString(), what));
					result.push(new Text(value.substring(i), Text.TEXT));
				} else {
					result.push(new Text(current.toString(), Text.TEXT));
					result.push(new Text(value.substring(i), Text.TEXT));
				}
				return;
			}
		}
		// no separator chars found
		if(value.length() == 1) {
			// alone @ char
			result.push(new Text(value, Text.TEXT));
		} else {
			result.push(new Text(value, what));
		}
	}
}


