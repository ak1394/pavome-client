package me.pavo.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.SocketConnection;
import javax.microedition.io.file.FileConnection;

import me.pavo.Main;
import me.pavo.NewPost;
import me.pavo.PavoException;
import me.pavo.Post;
import me.pavo.UI;

public class Connection implements Runnable {
	private static Vector in = new Vector();
	private static Vector out = new Vector();
	private static Vector outInternal = new Vector();
	private static Vector inInternal = new Vector();
	private static Hashtable futures = new Hashtable();
	private static MessageDispatcher dispatcher = new MessageDispatcher();
	private static final Object PAUSE = new Object();
	
	private static final int CONNECTED = 1;
	private static final int DISCONNECTING = 2;
	private static final int DISCONNECTED = 3;
	private int state = CONNECTED; 
	
	private static final long MAX_IDLE_TIME = 15000;
	private static final long MAX_DISCONNECTED_TIME = 15000;
	
	private Future paused;
	private String server;
	
	static Connection instance;
	
	public Object lock = new Object();

	DataInputStream inPacket;
	ByteArrayInputStream inPacketBytes;
	
	private SocketConnection socketConnection;
	private DataInputStream is;
	private DataOutputStream os;
	private ConnectionReader reader;
	
	private long lastActivityTs;
	private long lastDisconnectTs;
	private String token;
	private int pendingDisconnectOkId;
	private Object sessionId;
	
	
	public Connection(String server) {
		this.server = server;
		dispatcher.addListener(new Service(this));
		instance = this;
		paused = null;
	}
	
	public static Connection getInstance() {
		return instance;
	}
	
	public void start() throws IOException {
		openConnection();
		new Thread(this).start();
	}
	
	public void addMessageListener(MessageListener l) {
		dispatcher.addListener(l);
	}

	public void removeMessageListener(MessageListener l) {
		dispatcher.removeListener(l);
	}

	public Future login(String clientRevision, String clientId) {
		this.token = clientId;
		Hashtable params = new Hashtable();
		params.put("token", clientId);
		params.put("revision", clientRevision);
		params.put("platform", System.getProperty("microedition.platform"));
		params.put("configuration", System.getProperty("microedition.configuration"));
		params.put("profiles", System.getProperty("microedition.profiles"));
		Packet packet = new Packet(Packet.LOGIN);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}

	public Future requestToken(Hashtable credentials) {
		Packet packet = new Packet(Packet.REQUEST_TOKEN);
		writeHashtable(packet, credentials);
		return sendPacket(packet);
	}
	
	public Future sendPost(NewPost post) {
		Packet packet = new Packet(Packet.POST);
		writeHashtable(packet, post);
		packet.attach(post.attachment);
		packet.setKeepAttachment(post.shouldKeepAttachment());
		return sendPacket(packet);
	}

	public Future sendImageForPreview(byte[] image) {
		Packet packet = new Packet(Packet.PREVIEW_IMAGE);
		packet.writeInt(image.length);
		packet.write(image);
		return sendPacket(packet);
	}

	public Future sendFileForPreview(Params params) {
		FileConnection file = params.getFile("file");
		params.remove("file");
		Packet packet = new Packet(Packet.PREVIEW);
		writeHashtable(packet, params);
		packet.attach(file);
		packet.setKeepAttachment(true);
		return sendPacket(packet);
	}
	
	public Future topicOpen(String kind, String name) {
		Packet packet = new Packet(Packet.TOPIC_OPEN2);
		writeHashtable(packet, new Params().set("kind", kind).set("name", name));
		return sendPacket(packet);
	}

	public Future topicClose(String kind, String name) {
		Packet packet = new Packet(Packet.TOPIC_CLOSE);
		writeHashtable(packet, new Params().set("kind", kind).set("name", name));
		return sendPacket(packet);
	}

	public Future topicPageBefore(String kind, String name, long message_id) {
		Packet packet = new Packet(Packet.TOPIC_PAGE_BEFORE2);
		writeHashtable(packet, new Params().set("kind", kind).set("name", name).set("message_id", message_id));
		return sendPacket(packet);
	}

	public Future topicPageAfter(String kind, String name, long message_id) {
		Packet packet = new Packet(Packet.TOPIC_PAGE_AFTER2);
		writeHashtable(packet, new Params().set("kind", kind).set("name", name).set("message_id", message_id));
		return sendPacket(packet);
	}
	
	public Future getAvatar(String username) {
		Packet packet = new Packet(Packet.AVATAR_REQUEST);
		packet.writeInt(Post.ORIGIN_TWITTER);		
		packet.writeUTF(username);
		return sendPacket(packet);
	}

	public Future getContacts() {
		Packet packet = new Packet(Packet.STATUS_REQUEST2);
		return sendPacket(packet);
	}

	public Future getAttachment(String topicKind, String topicName, long message_id, int attachmentType) {
		Packet packet = new Packet(Packet.GET_ATTACHMENT2);
		writeHashtable(packet, new Params().set("kind", topicKind).set("name", topicName).set("message_id", message_id).set("type", attachmentType));
		return sendPacket(packet);
	}
	
	public void sendCheckResult(Hashtable result) {
		Packet packet = new Packet(Packet.CHECK_RESULT);
		writeHashtable(packet, result);
		sendPacketNoFuture(packet);
	}
	
	public void sendPong() {
		sendPacketNoFuture(new Packet(Packet.PONG));
	}

	public static void sendException(Object from, PavoException e) {
		System.out.println(from.getClass().getName() + ":" + e.getLocation());
		e.printStackTrace();
		Packet packet = new Packet(Packet.EXCEPTION);
		packet.writeUTF(from.getClass().getName() + ":" + e.getLocation());
		packet.writeUTF(e.toString());
		instance.sendPacketNoFuture(packet);
	}
	
	public static void sendException(Object from, String where, Exception exception) {
		System.out.println(from.getClass().getName() + ":" + where);
		exception.printStackTrace();
		Packet packet = new Packet(Packet.EXCEPTION);
		packet.writeUTF(from.getClass().getName() + ":" + where);
		packet.writeUTF(exception.toString());
		instance.sendPacketNoFuture(packet);
	}
	
	public Future deletePost(String kind, String name, String reference) {
		Packet packet = new Packet(Packet.DELETE2);
		writeHashtable(packet, new Params().set("kind", kind).set("name", name).set("reference", reference));
		return sendPacket(packet);
	}

	public Future getUserProfile(String username) {
		Packet packet = new Packet(Packet.USER_PROFILE);
		Hashtable params = new Hashtable();
		params.put("username", username);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}

	public Future follow(String username) {
		Packet packet = new Packet(Packet.FOLLOW);
		Hashtable params = new Hashtable();
		params.put("username", username);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}
	
	public Future unfollow(String username) {
		Packet packet = new Packet(Packet.UNFOLLOW);
		Hashtable params = new Hashtable();
		params.put("username", username);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}
	
	public Future reTweetPost(int origin, String reference) {
		Packet packet = new Packet(Packet.RE_TWEET);
		Hashtable params = new Hashtable();
		params.put("reference", reference);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}

	public Future favoritePost(String reference) {
		Packet packet = new Packet(Packet.CREATE_FAVORITE);
		Hashtable params = new Hashtable();
		params.put("reference", reference);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}

	public Future unfavoritePost(String reference) {
		Packet packet = new Packet(Packet.DESTROY_FAVORITE);
		Hashtable params = new Hashtable();
		params.put("reference", reference);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}
	
	public void sendSettings(Params settings) {
		Packet packet = new Packet(Packet.SETTINGS);
		writeHashtable(packet, settings);
		sendPacketNoFuture(packet);
	}

	public void sendTweetphoto(Params feedback) {
		Packet packet = new Packet(Packet.TWEETPHOTO);
		writeHashtable(packet, feedback);
		sendPacketNoFuture(packet);
	}
	
	public Future pause() {
		Future future = new Future(new Integer(-1));
		future.result = PAUSE;
		synchronized (lock) {
			out.addElement(future);
			lock.notify();
		}
		return future;
	}

	public Future requestUrl(String url) {
		Packet packet = new Packet(Packet.REQUEST_URL);
		Hashtable params = new Hashtable();
		params.put("url", url);
		writeHashtable(packet, params);
		return sendPacket(packet);
	}
	
	public void unpause(Future future) {
		synchronized (lock) {
			out.addElement(future);
			lock.notify();
		}
	}
	
	public Packet packetDisconnect(boolean temporary) {
		Packet packet = new Packet(Packet.DISCONNECT);
		writeHashtable(packet, new Params().set("temporary", temporary));
		pendingDisconnectOkId = packet.id.intValue();
		return packet;
	}

	public Packet packetResume(Object sessionId) {
		Packet packet = new Packet(Packet.RESUME);
		writeHashtable(packet, new Params().set("session", sessionId));
		return packet;
	}
	
	private Future sendPacket(Packet packet) {
		Future result;
		synchronized (lock) {
			out.addElement(packet);
			result = new Future(packet.getId());
			futures.put(result.id, result);
			lock.notify();
		}
		return result;
	}

	private void sendPacketNoFuture(Packet packet) {
		synchronized (lock) {
			out.addElement(packet);
			lock.notify();
		}
	}
	
	private void writeHashtable(Packet packet, Hashtable hashtable) {
		packet.writeInt(hashtable.size());
		Enumeration keys = hashtable.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			packet.writeUTF(key);
			Object value = hashtable.get(key);
			if(value instanceof String) {
				packet.writeByte(Packet.PARAM_STR);
				packet.writeUTF((String) value);
			} else if (value instanceof Integer) {
				packet.writeByte(Packet.PARAM_INT);
				packet.writeInt(((Integer) value).intValue());
			} else if (value instanceof Long) {
				packet.writeByte(Packet.PARAM_LONG);
				packet.writeLong(((Long) value).longValue());
			} else if (value instanceof byte[]) {
				packet.writeByte(Packet.PARAM_BIN);
				packet.writeInt(((byte[]) value).length);
				packet.write((byte[]) value);
			} else if(value instanceof Boolean) {
				packet.writeByte(Packet.PARAM_BOOL);
				if(((Boolean)value).booleanValue()) {
					packet.writeByte(1);
				} else {
					packet.writeByte(0);
				}
			}
		}
	}
	
	public void receiveMessage(Object message) {
		synchronized (lock) {
			in.addElement(message);
			lock.notify();
		}
	}

	
	private void runConnected() throws IOException, InterruptedException {
		if(getIdleTime() > MAX_IDLE_TIME) {
			out.addElement(packetDisconnect(true));
			state = DISCONNECTING;
		}
		
		// packets to send
		for(int i=0; i<out.size(); i++) {
			outInternal.addElement(out.elementAt(i));
		}
		out.removeAllElements();
		
		if(outInternal.size() > 0) {
			if(paused == null) {
				transmitPacketsFromInternalQueue();
			} else {
				lookForUnpauseInInternalQueue();
			}
		}

		// received packets
		for(int i=0; i<in.size(); i++) {
			inInternal.addElement(in.elementAt(i));
		}
		in.removeAllElements();
		receivePacketsFromInternalQueue();
		
		// sleep
		lock.wait(1000);
	}
	
	private void runDisconnecting() throws InterruptedException {
		while (in.size() > 0) {
			Object firstElement = in.firstElement();
			in.removeElement(firstElement);
			if(firstElement instanceof Message && ((Message)firstElement).id == pendingDisconnectOkId) {
				Params params = (Params) ((Message)firstElement).result;
				sessionId = params.get("session");
			} else if (firstElement == reader) {
				System.out.println("successfully shutdown");
				try {
					is.close();
					os.close();
					socketConnection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				is = null;
				os = null;
				socketConnection = null;
				reader = null;
				
				in.removeAllElements();
				state = DISCONNECTED;
				lastDisconnectTs = System.currentTimeMillis(); 
				break;
			}
		}
		// sleep
		lock.wait(1000);
	}

	private void runDisconnected() throws InterruptedException, IOException {
		System.out.println("stopped, packets in out queue: " + out.size());
		if((System.currentTimeMillis() - lastDisconnectTs) > MAX_DISCONNECTED_TIME) {
			openConnection();
			outInternal.addElement(packetResume(sessionId));
			transmitPacketsFromInternalQueue();
			lastActivityTs = System.currentTimeMillis();
			state = CONNECTED;
		} else {
			// sleep
			lock.wait(1000);
		}
	}
	
	private long getIdleTime() {
		if (in.size() > 0 || out.size() > 0) {
			lastActivityTs = System.currentTimeMillis();
		}
		return System.currentTimeMillis() - lastActivityTs;
	}


	private void openConnection() throws IOException {
		socketConnection = Main.INSTANCE.openConnection(server);
		is = socketConnection.openDataInputStream();
		os = socketConnection.openDataOutputStream();
		reader = new ConnectionReader(this, is);
		new Thread(reader).start();
	}
	
	private void initLoop() throws IOException {
		lastActivityTs = System.currentTimeMillis();
	}
	
	private void runLoop() throws IOException, InterruptedException {
		synchronized (lock) {
			switch (state) {
			case CONNECTED:
				runConnected();
				break;
			case DISCONNECTING:
				runDisconnecting();
				break;
			case DISCONNECTED:
				runDisconnected();
				break;
			}
		}
	}
	
	public void run() {
		try {
			initLoop();
			while (true) {
				runLoop();
			}
		} catch (IOException e) {
			e.printStackTrace();
			UI.error("IO Error " + e.getMessage());
			Main.INSTANCE.stopApplication();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void transmitPacketsFromInternalQueue() throws IOException {
		while(outInternal.size() > 0 && paused == null) {
			Object current = outInternal.elementAt(0);
			if(current instanceof Packet) {
				Packet packet = (Packet) current;
				transmitPacket(packet);
				if(packet.hasAttachment()) {
					if(packet.getAttachment() instanceof byte[]) {
						transmitAttachment((byte[]) packet.getAttachment());
					} else if(packet.getAttachment() instanceof InputStream) {
						transmitAttachment((InputStream) packet.getAttachment());
					} else if(packet.getAttachment() instanceof FileConnection) {
						transmitAttachment((FileConnection) packet.getAttachment(), packet.shouldKeepAttachment());
					} 
				}
			} else if (current instanceof Future) {
				Future future = (Future) current;
				if(future.result == PAUSE) {
					paused = future;
					inInternal.addElement(future);
				}
			}
			outInternal.removeElementAt(0);
		}
	}
	
	private void lookForUnpauseInInternalQueue() {
		for(int i=0; i<outInternal.size() && paused != null; i++) {
			Object current = outInternal.elementAt(i); 
			if(paused == current) {
				paused = null;
				outInternal.removeElementAt(i);
			}
		}
	}
	
	private void receivePacketsFromInternalQueue() {
		for(int i=0; i<inInternal.size(); i++) {
			Object current = inInternal.elementAt(i); 
			final Message message;
			final Future future;
			
			if(current instanceof Message) {
				message = (Message) current;
				Integer messageKey = new Integer(message.id);
				if(futures.containsKey(messageKey)) {
					future = (Future) futures.remove(messageKey);
				} else {
					future = null;
				}
			} else if(current instanceof Future) {
				future = (Future) current;
				message = null;
			} else {
				message = null;
				future = null;
			}
			
			new Thread(new Runnable() {
				public void run() {
					if(future != null) {
						future.arrived(message);
					}
					if(message != null) {
						dispatcher.fireMessageArrivedEvent(message);
					}
				}
			}).start();
		}
		inInternal.removeAllElements();
	}

	private void transmitPacket(Packet packet) throws IOException {
		byte[] bytes = packet.toByteArray();
		os.writeInt(bytes.length);
		os.write(bytes);
		os.flush();
	}
	
	private void transmitAttachment(byte[] attachment) throws IOException {
		os.writeInt(1 + attachment.length); // PACKET TYPE + DATA
		os.writeByte(Packet.STREAM_CHUNK);
		os.write(attachment, 0, attachment.length);
		
		os.writeInt(1);
		os.writeByte(Packet.STREAM_END);
		os.flush();
	}

	private void transmitAttachment(FileConnection attachment, boolean keepAttachment) throws IOException {
		transmitAttachment(attachment.openInputStream());
		if(!keepAttachment) {
			attachment.delete();
		}
	}
	
	private void transmitAttachment(InputStream attachment) throws IOException {
		// start sending chunks
		byte[] buffer = new byte[1400];
		int size = attachment.read(buffer, 0, buffer.length);
		while(size > 0) {
			os.writeInt(1 + size); // PACKET TYPE + DATA
			os.writeByte(Packet.STREAM_CHUNK);
			os.write(buffer, 0, size);
			size = attachment.read(buffer, 0, buffer.length);
		}
		// send final chunk
		os.writeInt(1);
		os.writeByte(Packet.STREAM_END);
		os.flush();
	}
}
