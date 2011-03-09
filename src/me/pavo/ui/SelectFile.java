package me.pavo.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import me.pavo.UI;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.DefaultListCellRenderer;

public class SelectFile extends Form implements ActionListener, WorkflowElement  {
	List list;
	Vector roots;
	
	static final int UNKNOWN = 0;
	static final int DIRECTORY = 1;
	static final int IMAGE = 2;
	static final int VIDEO = 3;
	static final int AUDIO = 4;

	public static final int OK = 1;
	public static final int CANCEL = 2;
	
	private int level = 0;
	
	FileConnection current = null;
	Command back;
	private Handler handler;

	public SelectFile() {
		setScrollable(false);
		try {
			setTitle(UI.localize("select_file"));
			list = list();
			addComponent(list);
			show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		back = new Command(UI.localize("back"));
		if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
			addCommand(new Command(""));
			addCommand(back);
		}
		setBackCommand(back);
		addCommandListener(this);
		
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	List list() throws IOException {
		Vector filenameList = new Vector();
		if(current != null) {
			filenameList.addElement(new Command("..", UI.getImage(UI.SMALL_FOLDER)));
		}

		if(current != null) {
			for(Enumeration e = current.list(); e.hasMoreElements();) {
				String name = (String) e.nextElement();
				filenameList.addElement(new Command(name, getIcon(name)));
			}
		} else {
			roots = new Vector();
			for(Enumeration e = FileSystemRegistry.listRoots(); e.hasMoreElements();) {
				roots.addElement(e.nextElement());
			}
			for(Enumeration e = roots.elements(); e.hasMoreElements();) {
				filenameList.addElement(new Command((String) e.nextElement(), UI.getImage(UI.SMALL_FOLDER)));
			}
		}

		List list = new List(filenameList);
		DefaultListCellRenderer renderer = new DefaultListCellRenderer(false);
		list.setListCellRenderer(renderer);
		list.addActionListener(this);
		list.setPreferredW(getWidth());
		list.setFixedSelection(List.FIXED_NONE_CYCLIC);
		return list;
		
	}

	void refresh() throws IOException {
		List newList = list();
		replace(list, newList, null);
		list = newList;
		if(current == null) {
			setTitle(UI.localize("select_file"));
		} else {
			setTitle(current.getURL().substring("file:///".length()));
		}
		repaint();
	}
	
	public void actionPerformed(ActionEvent evt) {
		try {
			if (evt.getSource() == back) {
				if(level > 0) {
					if (current.getName().equals("")) {
						current = null;
					} else {
						current = (FileConnection) Connector.open("file://" + current.getPath());
					}
					level--;
					refresh();
				} else {
					handle(new Params().set("result", Handler.CANCEL));
				}
				return;
			}

			Command c = (Command) list.getSelectedItem();
			String name = c.getCommandName();

			if (name.equals("..")) {
				if (current.getName().equals("")) {
					current = null;
				} else {
					current = (FileConnection) Connector.open("file://" + current.getPath());
				}
				level--;
				refresh();
			} else if (name.endsWith("/")) {
				if (current == null) {
					current = (FileConnection) Connector.open("file:///" + name);
				} else {
					current = (FileConnection) Connector.open(current.getURL() + name);
				}
				level++;
				refresh();
			} else {
				Params result = new Params().set("result", Handler.OK);
				switch (getType(name)) {
				case IMAGE:
					result.set("content-type", "image/jpeg");
					result.set("attachment", "image");
					break;
				case AUDIO:
					result.set("content-type", "audio/amr");
					result.set("attachment", "audio");
					break;
				case VIDEO:
					result.set("content-type", "video/3gpp");
					result.set("attachment", "video");
					break;
				}
				if (result.has("attachment")) {
					result.set("file", (FileConnection) Connector.open(current.getURL() + name));
					handle(result);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			handle(e);
		}
	}
	
	Image getIcon(String filename) {
		switch(getType(filename)) {
		case DIRECTORY:
			return UI.getImage(UI.SMALL_FOLDER);
		case IMAGE:
			return UI.getImage(UI.SMALL_PHOTO);
		case VIDEO:
			return UI.getImage(UI.SMALL_VIDEO);
		case AUDIO:
			return UI.getImage(UI.SMALL_AUDIO);
		default:
			return UI.getImage(UI.SMALL_EMPTY);
		}
	}
	
	int getType(String filename) {
		filename = filename.toLowerCase();
		if(filename.endsWith("/")) {
			return DIRECTORY;
		} else if(filename.endsWith(".jpg")) {
			return IMAGE;
		} else if(filename.endsWith(".3gp")) {
			return VIDEO;
		} else if(filename.endsWith(".amr")) {
			return AUDIO;
		} else {
			return UNKNOWN;
		}
	}
	
	public String getUIID() {
		return "SelectFile";
	}

	private void handle(Params result) {
		if(handler != null) {
			handler.handle(result);
		}
	}
	
	private void handle(Exception e) {
		handle(new Params().set("result", Handler.ERROR).set("exception", e));
	}
	
}