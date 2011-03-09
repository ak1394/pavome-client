package me.pavo.ui;

import me.pavo.Post;

import com.sun.lwuit.Display;

public class TopicManager implements Runnable {

	private String name;
	private String kind;
	int index_first;
	int index_last;
	
	private TweetContainer tc;
	private Screen screen;
	private Tweet first;
	private Tweet last;
	
	private TopicModel model;
	private String error;
	public float scrollOffset;
	public float scrollBlock;
	
	public TopicManager(String kind, String name) {
		this.kind = kind;
		this.name = name;
	}

	public boolean open() {
		model = new TopicModel(kind, name);
		try {
			model.open();
			return true;
		} catch (Exception e) {
			error = e.getMessage();
			return false;
		}
	}
	
	public void close() {
		model.close();
	}
	
	public void init(Screen screen, TweetContainer tc) {
		this.tc = tc;
		this.screen = screen;
		int height = screen.getContentPane().getHeight();
		int heightSum = 0;
		
//		tc.setSmoothScrolling(Settings.getBool(Settings.TOUCHSCREEN));
//		tc.setSmoothScrolling(true);
		
		Post post = model.first();
		Tweet prev = null;
		
		while(post != null && heightSum < height) {
			Tweet tweet = new Tweet(post);
			tweet.setPrev(prev);
			tweet.setPaintListener(this);
			tweet.setPreferredW(Display.getInstance().getDisplayWidth());
			heightSum = heightSum + tweet.getPreferredH();
			tc.addComponent(tweet);
			prev = tweet;
			post = model.getNext(post);
		}
		
		if(tc.getComponentCount() > 0) {
			screen.setFocused(tc.getComponentAt(0));
		}
		
	}
	
	public void painted(Tweet tweet, int x, int y) {
		int index = tc.getComponentIndex(tweet);
		index_first = Math.min(index_first, index);
		index_last = Math.max(index_last, index);
	}

	public void startedPainting() {
		index_first = Integer.MAX_VALUE;
		index_last = Integer.MIN_VALUE;
	}

	public void finishedPainting() {
		
		try {
//			System.out.println("first " + index_first + " last " + index_last);
			
			first = (Tweet) tc.getComponentAt(index_first);
			last = (Tweet) tc.getComponentAt(index_last);
			
			boolean run = true;
			
			if(first != null && first.prev == null) {
				run = true;
			} else if(first != null && first.prev.prev != null) {
				run = true;
			}
			
			if(last != null && last.next == null) {
				run = true;
			} else if(last != null && last.next.next != null) {
				run = true;
			}

			if(first != null && last != null) {
				int model_size = model.size();
				int index_first = model.index(first.getPost());
				int index_last = model.index(last.getPost());
				scrollOffset = ((float) index_first) / ((float)model_size);
				scrollBlock = ((float)(index_last - index_first + 1)) / ((float)model_size);
			}
			
			if(run) {
				Display.getInstance().callSerially(this);
			}
			
		} catch (ArrayIndexOutOfBoundsException e) {
		}
	}
	
	public void run() {
		
		boolean revalidate = false;
		
		if(first != null && first.prev == null && model.hasPrevious(first.getPost())) {
//			System.out.println("adding ");
			Tweet previous = new Tweet(model.getPrevious(first.getPost()));
			first.setPrev(previous);
			previous.setPaintListener(this);
			tc.addComponent(0, previous);
			tc.setScrollY(tc.getScrollY() + previous.getPreferredH());
			revalidate = true;
		} else if(first != null && first.prev != null &&  first.prev.prev != null) {
			Tweet toRemove = first.prev.prev;
			while(toRemove != null) {
				toRemove.setNext(null);
				tc.setScrollY(tc.getScrollY() - toRemove.getHeight());
				tc.removeComponent(toRemove);
				toRemove = toRemove.prev;
			}
			revalidate = true;
		}

		if(last != null && last.next == null && model.hasNext(last.getPost())) {
			Tweet tweet = new Tweet(model.getNext(last.getPost()));
			last.setNext(tweet);
			tweet.setPaintListener(this);
			tc.addComponent(tweet);
			revalidate = true;
		} else if(last != null && last.next != null &&  last.next.next != null) {
			Tweet toRemove = last.next.next;
			while(toRemove != null)  {
				toRemove.setPrev(null);
				tc.removeComponent(toRemove);
				toRemove = toRemove.next;
			}
			revalidate = true;
		}
		
		if(revalidate) {
			screen.relayout();
		}
	}

	public String getError() {
		return error;
	}
	
	public Tweet getTweetByPost(Post post) {
		for(int i=0; i<tc.getComponentCount(); i++) {
			Tweet tweet = (Tweet) tc.getComponentAt(i);
			if(tweet.getPost() == post) {
				return tweet;
			}
		}
		return null;
	}

	public void delete(Post post) {
		model.delete(post);
		Tweet tweet = getTweetByPost(post);
		if(tweet != null) {
			if(tweet.next != null && tweet.prev != null) {
				tweet.prev.setNext(tweet.next);
			} else if(tweet.next != null && tweet.prev == null) {
				tweet.next.setPrev(null);
			} else if(tweet.next == null && tweet.prev != null) {
				tweet.prev.setNext(null);
			}
			tc.removeComponent(tweet);
			if(tc.getComponentCount() > 0) {
				screen.setFocused(tc.getComponentAt(tc.getComponentCount() > 1 ? 1 : 0));
			}
			screen.revalidate();
		}
	}
}
