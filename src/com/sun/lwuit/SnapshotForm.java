package com.sun.lwuit;

public class SnapshotForm extends Form {
	private Form previous;
	private boolean painted = false;
	private boolean tinted = true;

	public SnapshotForm(Form previous) {
		this.previous = previous;
	}
	
	public void paint(Graphics g) {
		if(!painted) {
			previous.paint(g);
			if(tinted) {
				g.setColor(0x000000);
				g.fillRect(0, 0, previous.getWidth(), previous.getHeight(), (byte)128);
			}
			painted = true;
		}
	}

	public void allowRepaint() {
		painted = false;
		tinted = false;
	}

}
