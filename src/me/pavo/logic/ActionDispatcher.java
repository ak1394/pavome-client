package me.pavo.logic;

import me.pavo.server.Params;

public interface ActionDispatcher {
	
	public static final int TOPLEVEL = 1000;
	public static final int SCREEN = 2000;
	public static final int TWEETS = 3000;
	public static final int DM = 4000;
	
	public void dispatch(Params action);
}
