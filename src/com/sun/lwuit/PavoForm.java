package com.sun.lwuit;

import me.pavo.Main;

public class PavoForm extends Form {
	Image buffer;
	boolean fixed;
	
	public void keyReleased(int keyCode) {
		super.keyReleased(Main.remapKey(keyCode));
	}

	public void keyPressed(int keyCode) {
		super.keyPressed(Main.remapKey(keyCode));
	}
	
	public void keyRepeated(int keyCode) {
		super.keyRepeated(Main.remapKey(keyCode));
	}
	
	public void showModal(int top, int bottom, int left, int right,
			boolean includeTitle, boolean modal, boolean reverse) {
		super.showModal(top, bottom, left, right, includeTitle, modal, reverse);
	}
}
