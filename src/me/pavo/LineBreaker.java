package me.pavo;

import java.util.Vector;

import com.sun.lwuit.Font;

public class LineBreaker {
	char widestChar = 'W';
	int widestCharWidth;
	Font font;
	
	char[] chars;
	int start;
	Vector resultVector;

	public LineBreaker(Font font) {
		resultVector = new Vector();
		this.font = font;
		widestCharWidth = font.charWidth(widestChar);
	}
	
	public void start(String text) {
		start = 0;
		chars = text.toCharArray();
		resultVector.removeAllElements();
	}
	
	public boolean nextChunk(int width) {
		if(start >= chars.length) {
			return false;
		}
		
		int minCharactersInRow = Math.max(1, width / widestCharWidth);
		int i = start + minCharactersInRow;
		int lastBreak = -1;
		
		// proceed forward starting with substring of minCharactersInRow looking for space
		for(;i < chars.length && fastWidthCheck(i, width); i++) {
			if(chars[i] == ' ') {
				lastBreak = i;
			}
		}

		if(i >= chars.length) {
			// reached the end of chars
			resultVector.addElement(new String(chars, start, chars.length - start));
			start = i;
			return true;
		} else if(lastBreak == -1) {
			// no line break found just cut the string for now
			resultVector.addElement(new String(chars, start, minCharactersInRow));
			start = start + minCharactersInRow;
			return true;
		} else {
			// found line break 
			resultVector.addElement(new String(chars, start, lastBreak - start));
			start = lastBreak + 1;
			return true;
		}
	}
	
	public String[] result() {
		String r[] = new String[resultVector.size()];
		for(int i=0; i<resultVector.size(); i++) {
			r[i] = (String) resultVector.elementAt(i);
		}
		return r;
	}

	private boolean fastWidthCheck(int current, int width) {
		int length = current - start;
        if(length * widestCharWidth < width) {
            return true;
        }
        return font.charsWidth(chars, start, length) < width;
    }

}
