package me.pavo.logic;

import me.pavo.NewPost;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.CapturePhoto;
import me.pavo.ui.PreviewPhoto;
import me.pavo.ui.Waiting;

public class TakePhotoWF extends Workflow {

	public void run() {
		Params result = show(new CapturePhoto());
		if(isOk(result)) {
			show(new Waiting("Generating preview..."));
			Params preview;
			if(result.has("snapshot")) {
				preview = (Params) Connection.getInstance().sendImageForPreview((byte[]) result.get("snapshot")).getResult();
			} else {
				preview = (Params) Connection.getInstance().sendFileForPreview(result).getResult();
			}
			Params previewResult = show(new PreviewPhoto(preview.getImage("preview")));
			if(isOk(previewResult)) {
				NewPost post = new NewPost();
				post.set(NewPost.ATTACHMENT, "image");
				post.set(NewPost.PREVIEW_ID, preview.getInt("preview_id"));
				post.set(NewPost.BODY, previewResult.getString("text"));
				post.set(NewPost.ROTATION, previewResult.getInt("rotation"));
				Connection.getInstance().sendPost(post);
			}
		}
		showCurrentScreen();
	}
}
