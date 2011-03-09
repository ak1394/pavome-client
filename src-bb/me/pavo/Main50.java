package me.pavo;

import java.io.IOException;

import javax.microedition.io.SocketConnection;

import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;

public class Main50 extends Main {
    public static void main(String[] args) {
    	new Main50().start();
    }
    
	public SocketConnection openConnection(String server) throws IOException {
		ConnectionFactory cf = new ConnectionFactory();
		ConnectionDescriptor descr = cf.getConnection("socket://" + server);
		if(descr != null) {
			SocketConnection connection = (SocketConnection) descr.getConnection();
			return connection;
		} else {
			return null;
		}
	}
    
}
