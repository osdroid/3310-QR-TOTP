package com.podervisual.trinkets;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import dk.onlinecity.qrr.client.CameraCanvas;
import dk.onlinecity.qrr.client.CameraControl;
import dk.onlinecity.qrr.client.DecodeCanvas;

public class QrTotpMidlet extends MIDlet {
	public final static Command CMD_NEW = new Command("New", Command.OK, 1);
	public final static Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
	public final static Command CMD_OK = new Command("OK", Command.OK, 1);
	public final static Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 1);

	private Image logo;
    
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		FileSelectorScreen.stop();
		notifyDestroyed();
	}
	protected void pauseApp() {}
	protected void startApp() throws MIDletStateChangeException {
		logo = makeImage("/logo1.png");
        ErrorScreen.init(logo, Display.getDisplay(this));
        displayFileBrowser();
		//Display.getDisplay(this).setCurrent(CameraCanvas.getInstance(this));
	}
	public void showCamera() {
		CameraControl cameraControl = CameraControl.getInstance();
		CameraCanvas cameraCanvas = CameraCanvas.getInstance(this);
		Player player = cameraControl.getPlayer();
		VideoControl videoControl = cameraControl.getVideoControl(cameraCanvas);
		try {
			videoControl.setDisplayFullScreen(true);
			videoControl.setDisplayLocation(0, 0);
			player.start();
			videoControl.setVisible(true);
			Display.getDisplay(this).setCurrent(cameraCanvas);
		} catch (MediaException e) {
			e.printStackTrace();
		}
	}

	public void takeSnapshot() {
		CameraControl cameraControl = CameraControl.getInstance();
		showDecoding(cameraControl.getSnapshot());
	}
	void showDecoding(byte[] rawImage) {
		CameraControl.stop();
		DecodeCanvas decodeCanvas = new DecodeCanvas(this, rawImage);
		Display.getDisplay(this).setCurrent(decodeCanvas);
	}
	void showDecoding(Image image) {
		CameraControl.stop();
		DecodeCanvas decodeCanvas = new DecodeCanvas(this, image);
		Display.getDisplay(this).setCurrent(decodeCanvas);
	}
	void showDecoding(InputStream imageStream) {
		CameraControl.stop();
		DecodeCanvas decodeCanvas = new DecodeCanvas(this, imageStream);
		Display.getDisplay(this).setCurrent(decodeCanvas);
	}

	void showResult(String result) {
		showText(result);
	}

	private void showText(final String text) {
		Form form = new Form("Text");
		form.append(text);
		form.addCommand(CMD_NEW);
		form.addCommand(CMD_EXIT);
		
		form.setCommandListener(new CommandListener()
		{

			public void commandAction(Command cmd, Displayable d)
			{
				if (cmd == CMD_NEW) {
					showCamera();
				} else if (cmd == CMD_EXIT) {
					notifyDestroyed();
				}
			}
		});
		Display.getDisplay(this).setCurrent(form);
	}
	
	

	    public void fileSelectorExit() {
	        
	    	
	    }

	    /*
	    void cancelInput() {
	        Display.getDisplay(this).setCurrent(fileSelector);
	    }

	    void input(String input) {
	       // fileSelector.inputReceived(input, operationCode);
	        Display.getDisplay(this).setCurrent(fileSelector);
	    }*/

	    
	    public void displayImage(String imageName) {
	    	try {
	            FileConnection fileConn = (FileConnection) 
	            		Connector.open(imageName, Connector.READ);
	            InputStream imageStream = fileConn.openInputStream();
	            showDecoding(imageStream);
	            // TODO: Check if it's enough to only close the InputStream
	            // fileConn.close(); 
	        } catch (Exception e) {
	        	showError("--2" + e.toString());
	        }
	    }
	    public void displayFileBrowser() {
	    	FileSelectorScreen fileSelector = FileSelectorScreen.getInstance(this);
	        Display.getDisplay(this).setCurrent(fileSelector);
	        fileSelector.initialize();
	    }
	    public void showError(String errMsg) {
	        ErrorScreen.showError(errMsg, Display.getDisplay(this).getCurrent());
	    }
	    public void showMsg(String text) {
	        Alert infoScreen = new Alert(null, text, logo, AlertType.INFO);
	        infoScreen.setTimeout(3000);
	        Display.getDisplay(this).setCurrent(infoScreen, Display.getDisplay(this).getCurrent());
	    }
	    public static Image makeImage(String filename) {
	        try {
	            return Image.createImage(filename);
	        } catch (Exception e) {
	        	return null;
	        }
	    }

}
