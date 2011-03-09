package me.pavo.ui;

import java.util.Hashtable;
import java.util.Vector;

import com.sun.lwuit.Display;

import me.pavo.Post;
import me.pavo.server.Connection;
import me.pavo.server.Future;
import me.pavo.server.FutureCallback;
import me.pavo.server.Message;
import me.pavo.server.MessageListener;
import me.pavo.server.Packet;
import me.pavo.server.Params;
import me.pavo.server.Settings;

public class TopicModel implements FutureCallback, MessageListener {
	
	private String kind;
	private String name;
	private Connection connection;
	private Vector items;
	
	private Post topPost;
	private Post bottomPost;
	
	private int extraTop = 0;
	private int extraBottom = 0;
	
	private boolean pendingNext = false;
	private boolean pendingPrevious = false;

	public TopicModel(String kind, String name) {
		this.kind = kind;
		this.name = name;
		this.connection = Connection.getInstance();
	}
	
	public synchronized int open() throws Exception {
		Future result = connection.topicOpen(kind, name);
		if(result.isSuccess()) {
			if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
				items = (Vector) result.getResult();
			} else {
				items = reverse((Vector) result.getResult());
			}
			connection.addMessageListener(this);
			return items.size();
		} else {
			throw new Exception((String) result.getResult());
		}
	}
	
	private Vector reverse(Vector vector) {
		if(vector.size() > 1) {
			int a = 0; int b = vector.size()-1;
			Object tmp;
			while(a < b) {
				tmp = vector.elementAt(a);
				vector.setElementAt(vector.elementAt(b), a);
				vector.setElementAt(tmp, b);
				a++; 	b--;
			}
		}
		return vector;
	}

	public synchronized Post first() {
		return (Post) items.firstElement();
	}
	
	public synchronized int size() {
		return extraTop + items.size() + extraBottom;
	}
	
	public synchronized int index(Post post) {
		return extraTop + items.indexOf(post);
	}
	
	public synchronized boolean hasNext(Post post) {
		int i = items.indexOf(post);
		boolean result = i != -1 && i < items.size() - 1;
		
		if(items.size() - i < 10 && items.lastElement() != bottomPost && ! pendingNext) {
			Post last = (Post) items.lastElement();
			if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
				connection.topicPageAfter(kind, name, last.getId()).addCallback(this, new Object[] {"bottom", last});
			} else {
				connection.topicPageBefore(kind, name, last.getId()).addCallback(this, new Object[] {"bottom", last});
			}
			pendingNext  = true;
		}
		
		if(i > 20) {
			for(int j = 0; j < i - 20; j++) {
				extraTop++;
				items.removeElementAt(0);
			}
		}
		
		return result;
	}

	public synchronized Post getNext(Post post) {
		try {
			int i = items.indexOf(post);
			return (Post) items.elementAt(i+1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public synchronized boolean hasPrevious(Post post) {
		int i = items.indexOf(post);
		boolean result = i != -1 && i > 0;
		
		if(i < 10 && items.firstElement() != topPost && !pendingPrevious) {
			Post first = (Post) items.firstElement();
			if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
				connection.topicPageBefore(kind, name, first.getId()).addCallback(this, new Object[] {"top", first});
			} else {
				connection.topicPageAfter(kind, name, first.getId()).addCallback(this, new Object[] {"top", first});
			}
			pendingPrevious = true;
		}

		if(i + 20 < items.size()) {
			for(int j = items.size() -( i + 20); j > 0; j--) {
				items.removeElementAt(items.size()-1);
				extraBottom++;
			}
		}
		
		return result;
	}

	public synchronized Post getPrevious(Post post) {
		try {
			int i = items.indexOf(post);
			System.out.println("previous " + i);
			return  (Post) items.elementAt(i-1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public synchronized void callbackFired(Future future) {
		Object[] params = (Object[]) future.callbackParams;
		Vector result = (Vector) future.getResult();
		
		if(params[0].equals("bottom")) {
			pendingNext = false;
		} else if(params[0].equals("top")) {
			pendingPrevious = false;
		}
		
		if(params[0].equals("bottom") && items.lastElement().equals(params[1])) {
			if(result != null) {
				if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
					for(int i=0; i<result.size(); i++) {
						items.addElement(result.elementAt(i));
					}
				} else {
					for(int i=result.size()-1; i>=0; i--) {
						items.addElement(result.elementAt(i));
					}
				}
				if(extraBottom > 0) {
					if(extraBottom >= result.size()) {
						extraBottom = extraBottom - result.size();
					} else {
						extraBottom = 0;
					}
				}
			} else {
				System.out.println("null bottom");
				bottomPost = (Post) params[1];
			}
		} else if(params[0].equals("top") && items.firstElement().equals(params[1])) {
			if(result != null) {
				if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
					for(int i=result.size()-1; i>=0; i--) {
						items.insertElementAt(result.elementAt(i), 0);
					}
				} else {
					for(int i=0; i<result.size(); i++) {
						items.insertElementAt(result.elementAt(i), 0);
					}
				}
				if(extraTop > 0) {
					if(extraTop >= result.size()) {
						extraTop = extraTop - result.size();
					} else {
						extraTop = 0;
					}
				}
			} else {
				System.out.println("null top");
				topPost = (Post) params[1];
			}
		}
		
		Display.getInstance().callSerially(new Runnable() {
			public void run() {
				System.out.println("repainting");
				Display.getInstance().getCurrent().repaint();
			}
		});
	}

	public synchronized void messageArrived(final Message message) {
		if(message.packet == Packet.TOPIC_MESSAGE && ((Params)message.result).getString("kind").equals(kind) && ((Params)message.result).getString("name").equals(name)) {
			System.out.println("message arrived");
			Integer count = (Integer) ((Hashtable)message.result).get("count");
			extraTop = extraTop + count.intValue();
			
			if(Settings.getBool(Settings.TWEET_LIST_REVERSED)) {
				bottomPost = null;
			} else {
				topPost = null;
			}
			
			Display.getInstance().callSerially(new Runnable() {
				public void run() {
					Display.getInstance().getCurrent().repaint();
				}
			});
		}
	}

	public void close() {
		connection.topicClose(kind, name);
	}

	public synchronized void delete(Post post) {
		if(post == topPost) {
			topPost = null;
		} else if(post == bottomPost) {
			bottomPost = null;
		}
		items.removeElement(post);
	}
}
