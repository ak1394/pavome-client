package me.pavo.ui;

import java.util.Date;

import me.pavo.Post;

public class PostRenderingPrototype {

	public int getAttached() {
		return 0;
	}

	public String getAuthor() {
		return "~#renderproto#~";
	}

	public String getBody() {
		return "some body";
	}

	public long getId() {
		return 0;
	}

	public Date getPosted() {
		return new Date(1244217050000L);
	}

	public byte[] getThumbnail() {
		return null;
	}

	public int getOrigin() {
		return Post.ORIGIN_TWITTER;
	}

	public String getReference() {
		return "";
	}

	public boolean isDeleted() {
		return false;
	}

	public void setDeleted(boolean deleted) {
	}

	public String getForwardedBy() {
		return "";
	}

	public boolean isFavorited() {
		return true;
	}

	public void setFavorited(boolean favorited) {
	}

	public String getAuthorId() {
		return "0";
	}

	public String getInReplyTo() {
		return "";
	}

	public boolean isDirty() {
		return false;
	}
}
