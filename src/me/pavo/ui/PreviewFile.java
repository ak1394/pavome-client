package me.pavo.ui;

import java.io.IOException;

import javax.microedition.io.file.FileConnection;

import me.pavo.UI;
import me.pavo.logic.ActionList;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;
import me.pavo.server.Settings;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.PavoButtons;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.plaf.Border;

public class PreviewFile extends Form implements ActionListener, WorkflowElement, Handler {
	public static final int OK = 1;
	public static final int CANCEL = 2;
	private static final int TEXT = 3;
	private static final int PASS = 4;

	private Handler handler;
	private ActionList actions;
	private String text;

	public PreviewFile(String title, FileConnection file) {
		
		long fileSize = -1;
		try { fileSize = file.fileSize(); } catch (IOException e) {}

		setLayout(new BorderLayout());
		
		TextArea previewLabel = new TextArea(3, 3);
		previewLabel.setText("Captured file size is " + (1 + (int) fileSize / 1024) + "k");
		previewLabel.setEditable(false);
		previewLabel.setFocusable(false);
		previewLabel.getStyle().setBorder(Border.createEmpty());
		
		actions = new ActionList();
		actions.append(TEXT, "add_text", UI.BIG_POST);
		actions.append(OK, "ok", UI.BIG_YES);
		
		addComponent(BorderLayout.CENTER, previewLabel);
		addComponent(BorderLayout.SOUTH, new PavoButtons(actions, null, Settings.getBool(Settings.TOUCHSCREEN) || Settings.getBool(Settings.SMALLSCREEN)));

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
				handler.handle(new Params().set("result", Handler.OK).set("text", text != null ? text : ""));
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
		}
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void handle(Params result) {
		if(result.getInt("result") == Handler.OK) {
			text = result.getString("text");
		}
		Display.getInstance().callSerially(new Runnable() {
			public void run() {
				PreviewFile.this.show();
			}
		});
	}
}