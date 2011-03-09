package me.pavo;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class Main43 extends Main {
    public static void main(String[] args) {
    	new Main43().start();
    }
    
	public SocketConnection openConnection(String server) throws IOException {
		SocketConnection connection = (SocketConnection) Connector.open("socket://"	+ server + getConnectionSuffix());
		return connection;
	}
    
}
