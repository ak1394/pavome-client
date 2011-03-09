package me.pavo.logic;

import me.pavo.NewPost;
import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.CaptureVideo;
import me.pavo.ui.PreviewFile;

public class CaptureVideoWF extends Workflow {

	private Connection connection;
	
	public CaptureVideoWF(Connection connection) {
		this.connection = connection;
	}
	
	public void run() {
		Params video = show(new CaptureVideo());
		if(isOk(video)) {
			Params preview = show(new PreviewFile(UI.localize("preview_audio"), video.getFile("file"))); 
			if(isOk(preview)) {
				NewPost post = new NewPost();
				post.set(NewPost.ATTACHMENT, "video");
				post.set(NewPost.CONTENT_TYPE, video.getString("content-type"));
				post.attach(video.getFile("file"));
				post.set(NewPost.BODY, preview.getString("text"));
				connection.sendPost(post);
			} else {
				try {video.getFile("file").delete(); } catch (Exception e) { }
			}
		}
		showCurrentScreen();
	}
}