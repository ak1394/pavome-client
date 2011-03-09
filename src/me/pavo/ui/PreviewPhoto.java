package me.pavo.ui;

import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.PavoButtons;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;

public class PreviewPhoto  extends Form implements ActionListener, WorkflowElement, Handler  {
	public static final int OK = 1;
	public static final int CANCEL = 2;
	private static final int TEXT = 3;	
	private static final int ROTATE = 5;
	private static final int PASS = 6;
	
	private int rotation = 0; 
	private Label previewLabel;
	private Image preview;
	private Handler handler;
	private ActionList actions;
	private String text;
	
	public PreviewPhoto(Image preview) {
		this.preview = preview;
		setTitle(UI.localize("preview_photo"));

		actions = new ActionList();
		actions.append(TEXT, "add_text", UI.BIG_POST);
		actions.append(OK, "ok", UI.BIG_YES);
		actions.append(ROTATE, "rotate", UI.BIG_ROTATE);

		previewLabel = new Label(preview);
		previewLabel.setAlignment(CENTER);
		
		PavoButtons buttons = new PavoButtons(actions, null, Settings.getBool(Settings.TOUCHSCREEN) || Settings.getBool(Settings.SMALLSCREEN));
		
		if(!Settings.getBool(Settings.SMALLSCREEN)) {
			setLayout(new BorderLayout());
			addComponent(BorderLayout.CENTER, previewLabel);
			addComponent(BorderLayout.SOUTH, buttons);
			setScrollable(false);
		} else {
			setLayout(new BoxLayout(BoxLayout.Y_AXIS));
			addComponent(previewLabel);
			addComponent(buttons);
			setScrollable(true);
		}

		Command  cancel = actions.append(CANCEL, "Cancel", null); 
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(actions.append(PASS, "", null));
			addCommand(cancel);
		}
		setBackCommand(cancel);
		addCommandListener(this);
		
	}

	public void actionPerformed(ActionEvent evt) {
		switch (actions.getAction(evt.getCommand()).getInt("action")) {
		case OK:
			if(handler != null) {
				handler.handle(new Params().set("result", Handler.OK).set("text", text != null ? text : "").set("rotation", rotation + 360));
			}
			break;
		case TEXT:
			EditText editText = new EditText(text, 100);
			editText.setHandler(this);
			editText.show();
			break;
		case CANCEL:
			if(handler != null) {
				handler.handle(new Params().set("result", Handler.CANCEL));
			}
			break;
		case ROTATE:
			preview = preview.rotate(-90);
			previewLabel.setIcon(preview);
			rotation = rotation - 90;
			if(rotation == - 360) {
				rotation = 0;
			}
			repaint();
			break;
		}
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void handle(Params result) {
		if(result.getInt("result") == Handler.OK) {
			text = result.getString("text");
		}
		show();
	}
}
