package me.pavo;

import java.util.Hashtable;

public class NewPost extends Hashtable {
	public static final String BODY = "body";
	public static final String AUTHOR = "author";
	public static final String PREVIEW_ID = "preview_id";
	public static final String ROTATION = "rotation";
	public static final String CONTENT_TYPE = "content_type";
	public static final String ATTACHMENT = "attachment";
	public static final String IRT = "irt";
	public static final String DM = "dm";
	public Object attachment;

	public NewPost() {
		super();
	}

	public NewPost(String body) {
		super();
		set(BODY, body);
	}

	public NewPost(String author, String body) {
		super();
		set(AUTHOR, body);
		set(BODY, body);
	}
	
	public void set(String key, Object value) {
		put(key, value);
	}

	public void set(String key, int value) {
		put(key, new Integer(value));
	}
	
	public void attach(Object attachment) {
		this.attachment = attachment;
	}
	
	public void setKeepAttachment(boolean keep) {
		if(keep) {
			set("keep_attachment", "true");
		} else {
			remove("keep_attachment");
		}
	}
	
	public boolean shouldKeepAttachment() {
		return containsKey("keep_attachment");
	}
}
