package me.pavo;

import java.util.Date;

public class Post {
	
	public static final int ATTACHMENT_NONE = 0;
	public static final int ATTACHMENT_IMAGE = 1;
	public static final int ATTACHMENT_VIDEO = 2;
	public static final int ATTACHMENT_AUDIO = 3;
	public static final int ORIGIN_TWITTER = 5;
	public static final int ORIGIN_SYSTEM = 6;
	
	private long id;
	private int origin;
	private String reference;
	private String body;
	private String author;
	private String authorId;
	private Date posted;
	private int attached;
	private String forwardedBy;
	private String inReplyTo;
	private boolean isFavorited;

	public Post(long id, int origin, String reference, String author, String authorId, String forwarder_by, String body,
					 Date posted, int attached, boolean favorited, String in_reply_to) {
		this.id = id;
		this.origin = origin;
		this.reference = reference;
		this.author = author;
		this.authorId = authorId;
		this.forwardedBy = forwarder_by;
		this.body = body;
		this.posted = posted;
		this.attached = attached;
		this.isFavorited = favorited;
		this.inReplyTo = in_reply_to;
	}

	public  long getId() {
		return id;
	}

	public String getReference() {
		return reference;
	}

	public int getOrigin() {
		return origin;
	}
	
	public String getBody() {
		return body;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getAuthorId() {
		return authorId;
	}

	public Date getPosted() {
		return posted;
	}

	public int getAttached() {
		return attached;
	}
	
	public String getForwardedBy() {
		return forwardedBy;
	}

	public void setForwardedBy(String forwardedBy) {
		this.forwardedBy = forwardedBy;
	}
	
	public String getInReplyTo() {
		return inReplyTo;
	}

	public boolean isFavorited() {
		return isFavorited;
	}
	
	public void setFavorited(boolean favorited) {
		isFavorited = favorited;
	}
}