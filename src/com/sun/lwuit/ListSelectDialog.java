package com.sun.lwuit;

import java.util.Vector;

import me.pavo.UI;

import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.List;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.list.ListModel;
import com.sun.lwuit.plaf.Border;

public class ListSelectDialog extends Container {
	public static Object select(String title, Vector options) {
		return select(title, options, BorderLayout.CENTER, true);
	}
	
	public static Object select(String title, Vector options, String position, boolean silent) {
		if(options.size() == 1 && silent) {
			return options.elementAt(0);
		} else if(options.size() > 0) {
			ListModel model = new DefaultListModel(options);
			List list = new UnselectableList(model);
			DefaultListCellRenderer renderer = new DefaultListCellRenderer(false);
			renderer.getStyle().setPadding(5, 5, 5, 5);
			renderer.getSelectedStyle().setPadding(5, 5, 5, 5);
			renderer.getSelectedStyle().setBorder(Border.createLineBorder(1, 0x00000000));
			list.setListCellRenderer(renderer);
			list.setPreferredW((int)Math.max(list.getPreferredW(), Display.getInstance().getDisplayWidth() * 0.7f));
			list.setCommandList(true);
			final Dialog d = new Dialog();
			d.setTitle(title);
	        d.setLayout(new BorderLayout());
			d.setMenu(true);

			Command cancel = new Command(UI.localize("cancel"));
			Command select = new Command(UI.localize("select"));
			Command select1 = new Command(UI.localize("select") + " ");
			
			if(Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
				d.addCommand(select);
				d.addCommand(select1);
				d.addCommand(cancel);
			} else {
				d.addCommand(select);
				d.addCommand(select1);
			}
			
	        d.setBackCommand(cancel);
	        
			d.addComponent(BorderLayout.CENTER, list);
			
			Command result = d.showPacked(position, true);
			
	        d.disposeImpl();
	        if (result != cancel) {
	        	return list.getSelectedItem();
	        }
		}
		return null;
	}
}
