package me.pavo.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class Packet {
	
	public static final byte OK = 0x0a;
	public static final byte ERROR = 0x0b;
	public static final byte ERROR_MESSAGE = 0x0c;
	public static final byte POST = 0x0d;
	public static final byte LOGIN = 0x0e;
	public static final byte PARAMS = 0x0f;
	public static final byte ACK = 0x10;
	public static final byte SETTINGS = 0x11;
	public static final byte AVATAR_REQUEST = 0x12;
	public static final byte GET_ATTACHMENT2 = 0x13;
	public static final byte PING = 0x14;
	public static final byte PONG = 0x15;
	public static final byte IMAGE = 0x16;
	public static final byte PREVIEW_IMAGE = 0x17;
	public static final byte RESUME = 0x18;
	public static final byte TOPIC_PAGE = 0x19;
	public static final byte TOPIC_MESSAGE = 0x1a;
	public static final byte TOPIC_CLOSE = 0x1b;
//	public static final byte GET_ATTACHMENT = 0x1c;
	public static final byte DELETE_PREVIEW = 0x1d;
	public static final byte CHECK_REQUEST = 0x1e;
	public static final byte CHECK_RESULT = 0x1f;
//	public static final byte TOPIC_PAGE_BEFORE = 0x20;
//	public static final byte TOPIC_PAGE_AFTER = 0x21;
	public static final byte BLUETOOTH_DEVICES = 0x22;
	public static final byte BLUETOOTH_LOCAL = 0x23;
	public static final byte EXCEPTION = 0x24;
	public static final byte STREAM_CHUNK = 0x25;
	public static final byte STREAM_END = 0x26;
//	public static final byte DELETE = 0x27;
	public static final byte USER_PROFILE = 0x28;
	public static final byte FOLLOW = 0x29;
	public static final byte UNFOLLOW = 0x30;
	public static final byte RE_TWEET = 0x3a;
	public static final byte TOPIC_OPEN2 = 0x3b;
	public static final byte TOPIC_PAGE_BEFORE2 = 0x3c;
	public static final byte TOPIC_PAGE_AFTER2 = 0x3d;
	public static final byte DELETE2 = 0x3e;
	public static final byte CREATE_FAVORITE = 0x3f;
	public static final byte DESTROY_FAVORITE = 0x40;
	public static final byte PREVIEW = 0x41;
	public static final byte STATUS_REQUEST2 = 0x42;
	public static final byte TWEETPHOTO = 0x43;
	public static final byte REQUEST_TOKEN = 0x44;
	public static final byte REQUEST_URL = 0x45;
	public static final byte DISCONNECT = 0x46;

	public static final byte PARAM_INT = 0x0a;
	public static final byte PARAM_STR = 0x0b;
	public static final byte PARAM_BIN = 0x0c;
	public static final byte PARAM_IMG = 0x0d;
	public static final byte PARAM_INT_LIST = 0x0e;
	public static final byte PARAM_UNDEFINED = 0x0f;
	public static final byte PARAM_STR_LIST = 0x10;
	public static final byte PARAM_BIN_LIST = 0x11;
	public static final byte PARAM_LONG = 0x12;
	public static final byte PARAM_LONG_LIST = 0x13;
	public static final byte PARAM_DATETIME = 0x14;
	public static final byte PARAM_DATETIME_LIST = 0x15;
	public static final byte PARAM_BOOL = 0x16;
	public static final byte PARAM_BOOL_LIST = 0x17;
	public static final byte PARAM_PARAMS = 0x18;

	public static final byte TEST_SYSTEM_PROPERTY = 0x1;
	public static final byte TEST_MIDLET_PROPERTY = 0x2;
	public static final byte TEST_CLASS  = 0x3;
	public static final byte TEST_PERMISSION  = 0x4;
	public static final byte TEST_MEMORY  = 0x5;
	public static final byte TEST_LWUIT  = 0x6;
	public static final byte TEST_PLATFORM_REQUEST  = 0x7;
	public static final byte TEST_PLATFORM_REQUEST_FATAL  = 0x8;
	public static final byte TEST_DIALOG_INFO  = 0x9;
	public static final byte TEST_DIALOG_CONFIRM  = 0xa;
	public static final byte TEST_PLATFORM_PROPERTY = 0xb;

	private static int counter = 0;
	private static final Object lock = new Object();
	
	Integer id;
	private ByteArrayOutputStream baos;
	private DataOutputStream dos;
	private Object attachment;
	private boolean keepAttachment;
	
	public Packet(byte type) {
		synchronized (lock) {
			counter++;
			this.id = new Integer(counter);
		}
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		try {
			dos.writeByte(type);
			dos.writeInt(id.intValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(byte[] v) {
		try {
			dos.write(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void attach(Object v) {
		attachment = v;
	}
	
	public boolean hasAttachment() {
		return attachment != null;
	}
	
	public Object getAttachment() {
		return attachment;
	}
	
	public void setKeepAttachment(boolean keepAttachment) {
		this.keepAttachment = keepAttachment;
	}

	public boolean shouldKeepAttachment() {
		return keepAttachment;
	}
	
	public void writeByte(int v) {
		try {
			dos.writeByte(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeInt(int v) {
		try {
			dos.writeInt(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeLong(long v) {
		try {
			dos.writeLong(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeIntList(int v[]) {
		try {
			dos.writeByte(Packet.PARAM_INT_LIST);
			dos.writeInt(v.length);
			for(int i=0; i<v.length; i++) {
				dos.writeInt(v[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeUTFList(String v[]) {
		try {
			dos.writeByte(Packet.PARAM_STR_LIST);
			dos.writeInt(v.length);
			for(int i=0; i<v.length; i++) {
				dos.writeUTF(v[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeIntParam(String key, int v) {
		try {
			dos.writeUTF(key);
			dos.writeByte(Packet.PARAM_INT);
			dos.writeInt(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeIntListParam(String key, Vector v) {
		try {
			dos.writeUTF(key);
			dos.writeByte(Packet.PARAM_INT_LIST);
			dos.writeInt(v.size());
			for(int i=0; i<v.size(); i++) {
				Hashtable h = (Hashtable) v.elementAt(i);
				dos.writeInt(((Integer)h.get(key)).intValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void writeUTFListParam(String key, Vector v) {
		try {
			dos.writeUTF(key);
			dos.writeByte(Packet.PARAM_STR_LIST);
			dos.writeInt(v.size());
			for(int i=0; i<v.size(); i++) {
				Hashtable h = (Hashtable) v.elementAt(i);
				if(h.get(key) != null) {
					dos.writeUTF((String)h.get(key));
				} else {
					dos.writeUTF("");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeUTF(String v) {
		try {
			dos.writeUTF(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] toByteArray() {
		return baos.toByteArray();
	}
	
	public Integer getId() {
		return id;
	}
}
