package me.pavo.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import me.pavo.Main;
import me.pavo.logic.Showable;
import me.pavo.logic.WorkflowElement;
import me.pavo.server.Params;

public class CapturePhoto implements WorkflowElement, Showable, ActionListener, FileSystemJournalListener {
	
	private Handler handler;
	private long bottomUsn;
	private String photoPath;

	public CapturePhoto() {
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void show() {
		bottomUsn = FileSystemJournal.getNextUSN();
		Main.addBackgroundEventListener(this);
        Main.INSTANCE.addFileSystemJournalListener(this);
		Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
	}

	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == Main.backgroundEvent) {
			Main.removeBackgroundEventListener(this);
			Main.addForegroundEventListener(this);			
		} else if(ev.getSource() == Main.foregoundEvent) {
			Main.removeForegroundEventListener(this);
	        Main.INSTANCE.removeFileSystemJournalListener(this);
	        
	        if(photoPath != null) {
	        	try {
					FileConnection photo = getFile(photoPath);
					handle(new Params().set("result", Handler.OK).set("file", photo));
				} catch (IOException e) {
			        handle(new Params().set("result", Handler.CANCEL));
				}
	        } else {
		        handle(new Params().set("result", Handler.CANCEL));
	        }
		}
	}

	public void fileJournalChanged() {
		long nextUSN = FileSystemJournal.getNextUSN();
		for (long currentUSN = nextUSN-1; currentUSN >= bottomUsn; currentUSN--) {
			FileSystemJournalEntry entry = FileSystemJournal.getEntry(currentUSN);
			if (entry == null) {
				// we didn't find an entry.
				break;
			}
			
			if (entry.getEvent() == FileSystemJournalEntry.FILE_ADDED) {
				String path = entry.getPath();
				if (path != null && path.toLowerCase().indexOf(".jpg") != -1) {
					photoPath = path;
					break;
				}
			}
			
		}
		bottomUsn = nextUSN;
	}
	
	private void handle(Params result) {
		if(handler != null) {
			handler.handle(result);
		}
	}
	
	private FileConnection getFile(String path) throws IOException {
	    FileConnection fileConnection = (FileConnection) Connector.open("file://" + path);
	    return fileConnection;
	}
	
//	private byte[] getSnapshot(String path) throws IOException {
//        FileConnection fileConnection = (FileConnection) Connector.open("file://" + path);
//
//        //Get the file data
//        InputStream input = fileConnection.openInputStream();
//        ByteArrayOutputStream dataOuput = new ByteArrayOutputStream();
//        int i = 0;
//        while ((i = input.read()) != -1) {
//            dataOuput.write(i);
//        }
//        
//        byte[] fileData = dataOuput.toByteArray();
//        fileConnection.close();
//
//        return fileData;
//	}
	
//	private byte[] getScaledJPEG(byte[] jpegbytes)
//    {
////        EncodedImage image = EncodedImage.createEncodedImage(jpegbytes, 0, jpegbytes.length, "image/jpeg");
//////        EncodedImage scaled = image.scaleImage32(Fixed32.toFP(640), Fixed32.toFP(480));
////        Bitmap bitmap = image.getBitmap();
////        return JPEGEncodedImage.encode(bitmap, 100).getData();
//////        return scaled.getData();
//////        JPEGEncodedImage encoded = JPEGEncodedImage.encode(image.scaleImage32(Fixed32.toFP(640), Fixed32.toFP(480)).getBitmap(), 60);
//////        return encoded.getData();
//    }
}
