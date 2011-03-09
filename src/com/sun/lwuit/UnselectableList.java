package com.sun.lwuit;

import com.sun.lwuit.list.ListModel;

public class UnselectableList extends List {
	public UnselectableList(ListModel model) {
		super(model);
	}

	protected boolean isSelectableInteraction() {
		return false;
	}
}
