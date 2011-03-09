package me.pavo.logic;

import me.pavo.NewPost;
import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.PreviewFile;
import me.pavo.ui.PreviewPhoto;
import me.pavo.ui.SelectFile;
import me.pavo.ui.Waiting;

public class AttachFileWF extends Workflow {

	public void run() {
		Params file = show(new SelectFile());
		if(isOk(file)) {
			if(file.getString("attachment").equals("image")) {
				show(new Waiting("Generating preview..."));
				Params server = (Params) Connection.getInstance().sendFileForPreview(file).getResult();
				Params preview = show(new PreviewPhoto(server.getImage("preview")));
				if(isOk(preview)) {
					NewPost post = new NewPost();
					post.set(NewPost.ATTACHMENT, "image");
					post.set(NewPost.PREVIEW_ID, server.getInt("preview_id"));
					post.set(NewPost.BODY, preview.getString("text"));
					post.set(NewPost.ROTATION, preview.getInt("rotation"));
					Connection.getInstance().sendPost(post);
				}
			} else {
				Params preview = show(new PreviewFile(UI.localize("preview_file"), file.getFile("file")));
				if(isOk(preview)) {
					NewPost post = new NewPost();
					post.set(NewPost.ATTACHMENT, file.getString("attachment"));
					post.set(NewPost.CONTENT_TYPE, file.getString("content-type"));
					post.attach(file.getFile("file"));
					post.set(NewPost.BODY, preview.getString("text"));
					post.setKeepAttachment(true);
					show(new Waiting("Uploading..."));
					Connection.getInstance().sendPost(post).getResult();
				}
			}
		}
		showCurrentScreen();
	}
}