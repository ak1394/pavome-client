package me.pavo.ui;

import me.pavo.server.Params;

public interface Handler {
	public static final int OK = 1;
	public static final int CANCEL = 2;
	public static final int ERROR = 3;
	public void handle(Params result);
}
