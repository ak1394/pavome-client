package me.pavo.logic;

import com.sun.lwuit.Form;

import me.pavo.NewPost;
import me.pavo.UI;
import me.pavo.server.Connection;
import me.pavo.server.Params;
import me.pavo.ui.CaptureAudio;
import me.pavo.ui.PreviewFile;

public class CaptureAudioWF extends Workflow {

	public void run() {
		Params audio = show((Form)new CaptureAudio());
		if(isOk(audio)) {
			Params preview = show(new PreviewFile(UI.localize("preview_audio"), audio.getFile("file"))); 
			if(isOk(preview)) {
				NewPost post = new NewPost();
				post.set(NewPost.ATTACHMENT, "audio");
				post.set(NewPost.CONTENT_TYPE, audio.getString("content-type"));
				post.attach(audio.getFile("file"));
				post.set(NewPost.BODY, preview.getString("text"));
				Connection.getInstance().sendPost(post);
			} else {
				try {audio.getFile("file").delete(); } catch (Exception e) { }
			}
		}
		showCurrentScreen();
	}
}