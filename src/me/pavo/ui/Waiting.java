package me.pavo.ui;

import me.pavo.UI;

import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BoxLayout;

public class Waiting extends Form {
	
	private Label label;
	private Label progressLabel;

	public Waiting(String message) {
		this(message, null, null);
	}

	public Waiting(String message, Image image, Image indicator) {
		super();
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		setScrollable(false);
		label = new Label(message);
		if(image != null) {
			label.setIcon(image);
		}
		label.setAlignment(Label.CENTER);
		label.setTextPosition(Label.BOTTOM);
		
		progressLabel = new Label("");
		if(indicator != null) {
			progressLabel.setIcon(indicator);
		} else {
			progressLabel.setIcon(UI.getImage(UI.WAIT_ICON));
		}
		progressLabel.setAlignment(Label.CENTER);
		
		label.getStyle().setPadding(Component.TOP, Display.getInstance().getDisplayHeight() / 3);
		progressLabel.getStyle().setPadding(Component.TOP, Display.getInstance().getDisplayHeight() / 4);
		
		addComponent(label);
		addComponent(progressLabel);
		
	}
}
