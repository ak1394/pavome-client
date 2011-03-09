package me.pavo.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import com.sun.lwuit.Image;

import me.pavo.Main;
import me.pavo.Post;
import me.pavo.UI;

public class ConnectionReader implements Runnable {

	DataInputStream is;
	private boolean running;
	private Connection connection;

	public ConnectionReader(Connection connection, DataInputStream is) {
		this.is = is;
		this.running = true;
		this.connection = connection;
	}

	public void run() {
		while (running) {
			try {
				int size = is.readInt();
				byte[] buffer = new byte[size];
				is.readFully(buffer, 0, size);
				Message message = unpack(buffer, size);
				connection.receiveMessage(message);
			} catch (IOException e) {
				connection.receiveMessage(this);
				running = false;
			}
		}
	}

	private Message unpack(byte[] buffer, int size) throws IOException {
		DataInputStream packet = new DataInputStream(new ByteArrayInputStream(buffer, 1, size - 1));
		int id = packet.readInt();
		switch (buffer[0]) {
		case Packet.OK:
			return new Message(Packet.OK, id, null);
		case Packet.ACK:
			int ack = packet.readInt();
			return new Message(Packet.ACK, id, new Integer(ack));
		case Packet.ERROR:
			return new Message(Packet.ERROR, id, null);
		case Packet.ERROR_MESSAGE:
			return new Message(Packet.ERROR_MESSAGE, id, unpackErrorMessage(packet));
		case Packet.PARAMS:
			return new Message(Packet.PARAMS, id, unpackParams(packet, new Params()));
		case Packet.TOPIC_PAGE:
			return new Message(Packet.TOPIC_PAGE, id, unpackPage(packet));
		case Packet.TOPIC_MESSAGE:
			return new Message(Packet.TOPIC_MESSAGE, id, unpackParams(packet, new Params()));
		case Packet.PING:
			return new Message(Packet.PING, id, null);
		case Packet.CHECK_REQUEST:
			return new Message(Packet.CHECK_REQUEST, id, unpackParams(packet, new Params()));
		case Packet.SETTINGS:
			return new Message(Packet.SETTINGS, id, unpackParams(packet, new Params()));
		default:
			System.out.println("unknown packet");
		}
		return null;
	}

	private Object unpackErrorMessage(DataInputStream packet) throws IOException {
		String message = packet.readUTF();
		return message;
	}
	
	private Params unpackParams(DataInputStream packet, Params result) throws IOException {
		int count = packet.readByte();
		for(int i = 0; i<count; i++) {
			String k = packet.readUTF();
			int type = packet.readByte();
			switch(type) {
			case Packet.PARAM_PARAMS:
				result.set(k, unpackParams(packet, new Params()));
				break;
			case Packet.PARAM_UNDEFINED:
				break;
			case Packet.PARAM_BOOL:
				result.set(k, packet.readByte() == 1);
				break;
			case Packet.PARAM_INT:
				result.set(k, packet.readInt());
				break;
			case Packet.PARAM_LONG:
				result.set(k, packet.readLong());
				break;
			case Packet.PARAM_STR:
				result.set(k, packet.readUTF());
				break;
			case Packet.PARAM_BIN:
				{
					int size = packet.readInt();
					byte[] v = new byte[size];
					packet.readFully(v);
					result.put(k, v);
				}
				break;
			case Packet.PARAM_IMG:
				{
					int size = packet.readInt();
					byte[] buf = new byte[size];
					packet.readFully(buf);
					Image v = Image.createImage(new ByteArrayInputStream(buf));
					result.put(k, v);
				}
				break;
			case Packet.PARAM_BOOL_LIST:
			{
				int size = packet.readInt();
				boolean[] v = new boolean[size]; 
				for(int j=0; j<size; j++) {
					v[j] = packet.readBoolean();
				}
				result.put(k, v);
			}
			break;
			case Packet.PARAM_INT_LIST:
				{
					int size = packet.readInt();
					int[] v = new int[size]; 
					for(int j=0; j<size; j++) {
						v[j] = packet.readInt();
					}
					result.put(k, v);
				}
				break;
			case Packet.PARAM_LONG_LIST:
				{
					int size = packet.readInt();
					long[] v = new long[size]; 
					for(int j=0; j<v.length; j++) {
						v[j] = packet.readLong();
					}
					result.put(k, v);
				}
				break;
			case Packet.PARAM_DATETIME_LIST:
				{
					int size = packet.readInt();
					Date[] v = new Date[size]; 
					for(int j=0; j<v.length; j++) {
						v[j] = new Date(packet.readLong());
					}
					result.put(k, v);
				}
				break;
			case Packet.PARAM_STR_LIST:
				{
					int size = packet.readInt();
					String[] v = new String[size]; 
					for(int j=0; j<size; j++) {
						v[j] = packet.readUTF();
					}
					result.put(k, v);
				}
				break;
			case Packet.PARAM_BIN_LIST:
				{
					int size = packet.readInt();
					byte[][] v = new byte[size][]; 
					for(int j=0; j<size; j++) {
						int bufSize = packet.readInt();
						if(bufSize > 0) {
							byte[] binBuf = new byte[bufSize];
							packet.readFully(binBuf);
							v[j] = binBuf;
						} else {
							v[j] = null;
						}
					}
					result.put(k, v);
				}
				break;
			}
		}
		return result;
	}
	
	private Vector unpackPage(DataInputStream packet) throws IOException {
		Params page = unpackParams(packet, new Params());
		if(page.size() > 0) {
			long[] id = (long[]) page.get("id");
			int[] origin = (int[]) page.get("origin");
		    String[] reference = (String[]) page.get("reference");
		    String[] author = (String[]) page.get("author");
		    String[] author_id = (String[]) page.get("author_id");
		    String[] forwarded_by = (String[]) page.get("forwarded_by");
		    String[] body = (String[]) page.get("body");
		    Date[] posted = (Date[]) page.get("posted");
		    int[] attached = (int[]) page.get("attached");
		    boolean[] favorited = (boolean[]) page.get("favorited");
		    String[] in_reply_to = (String[]) page.get("irt");
		    
		    Vector result  = new Vector();
		    for(int i=0; i<id.length; i++) {
		    	result.addElement(new Post(id[i], origin[i], reference[i], author[i], author_id[i], forwarded_by[i], body[i],
		    							               posted[i], attached[i], favorited[i], in_reply_to[i]));
		    }
		    return result;
		}
		return null;
	}
	
}
