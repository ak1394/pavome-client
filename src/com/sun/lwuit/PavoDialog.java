package com.sun.lwuit;

import me.pavo.Main;

public class PavoDialog extends Dialog {
	int i = 0;
	public void keyReleased(int keyCode) {
		super.keyReleased(Main.remapKey(keyCode));
	}

	public void keyPressed(int keyCode) {
		super.keyPressed(Main.remapKey(keyCode));
	}
	
	public void keyRepeated(int keyCode) {
		super.keyRepeated(Main.remapKey(keyCode));
	}
}
