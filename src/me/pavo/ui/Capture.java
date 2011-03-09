package me.pavo.ui;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.RecordControl;

import me.pavo.PavoException;
import me.pavo.server.Settings;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

public class Capture implements PlayerListener {
	public Player player;
	public RecordControl recordControl;
	
	protected ActionListener listener;
	protected long fileSystemFree;
	
	protected FileConnection captureResult;
	protected String contentType;

	public static final int INIT = 0;
	public static final int STARTING = 1;
	public static final int CAPTURING = 2;
	public static final int COMMITED = 3;
	public static final int FINISHED = 4;
	public int state = INIT;
	
	public Capture(ActionListener listener) {
		this.listener = listener;
	}
	
	public FileConnection getResult() {
		return captureResult;
	}

	public void initPlayer(String device) throws PavoException {
		try {
			player = Manager.createPlayer(device);
			player.addPlayerListener(this);
			//player.prefetch();
			player.realize();
		} catch (Exception e) {
			throw new PavoException("createPlayer", e);
		}
	}
	
	public void initRecordControl(String path) throws PavoException {
		try {
			captureResult = createFile(path);
			recordControl = (RecordControl) player.getControl("RecordControl");
			recordControl.setRecordStream(captureResult.openOutputStream());
		} catch (Exception e) {
			throw new PavoException("makeRecordControlPath", e);
		}
	}
	
	public void setRecordSizeLimit(int limit) {
		try {
			int free = Integer.MAX_VALUE-1;
			recordControl.setRecordSizeLimit(Math.min(limit, free - Settings.getInt(Settings.FS_ALWAYS_FREE)));
		} catch (MediaException e) {
			// TODO: report?
		}		
	}
	
	private FileConnection createFile(String path) throws PavoException {
		try {
			FileConnection fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
			long free = fc.availableSize();
			if (free < Settings.getInt(Settings.FS_MIN_FREE)) {
				throw new Exception("Not enough space on filesystem, required "
						+ Settings.getInt(Settings.FS_MIN_FREE) + " available "
						+ free);
			}
			if (!fc.exists()) {
				fc.create();
			}
			return fc;
		} catch (Exception e) {
			throw new PavoException("createFile", e);
		}
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void commitRecording() throws PavoException {
		try {
			recordControl.commit();
			contentType = recordControl.getContentType();
		} catch (Exception e) {
			throw new PavoException("commitRecord", e);
		}
	}
	
    public void stopPlayer() {
		try { player.stop(); } catch (Exception e) { }
		try { player.deallocate(); } catch (Exception e) {}
		player = null;
		recordControl = null;
    }
    
    public void abortRecording() {
    	stopPlayer();
		try { captureResult.delete(); } catch (Exception e) {}
		captureResult = null;
	}
    
	public synchronized void playerUpdate(Player player, java.lang.String event, java.lang.Object eventData) {
		if(event.equals(PlayerListener.RECORD_STOPPED)) {
			state = COMMITED;
			listener.actionPerformed(new ActionEvent(this));
		}
	}
}
