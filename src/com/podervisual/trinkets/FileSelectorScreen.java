package com.podervisual.trinkets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

public class FileSelectorScreen extends List implements CommandListener {

	private static FileSelectorScreen instance;
	private final static String PHOTOS_PATH = "file:///Phone/Photos/";
	
	private final static Image FOLDER_IMAGE =
	        QrTotpMidlet.makeImage("/folder1.png");
	private final static Image FILE_IMAGE =
			QrTotpMidlet.makeImage("/file1.png");
	private final OperationsQueue queue = new OperationsQueue();
	private final static String FILE_SEPARATOR =
	        (System.getProperty("file.separator") != null) ? System.getProperty("file.separator") : "/";
	private final static String UPPER_DIR = "..";
	
	private QrTotpMidlet midlet;
	private final Command openCommand =
	        new Command("Open", Command.ITEM, 1);
	private final Command exitCommand =
	        new Command("Exit", Command.EXIT, 1);
	private FileConnection currentRoot = null;
	private boolean initialized = false;
	
	public FileSelectorScreen(QrTotpMidlet midlet) {
	    super("Load new QR OTP code", List.IMPLICIT);
	    this.midlet = midlet;
	    addCommand(openCommand);
	    addCommand(exitCommand);
	    setSelectCommand(openCommand);
	    setCommandListener(this);
	}
	public static FileSelectorScreen getInstance(QrTotpMidlet midlet) {
		if (instance == null)
			instance = new FileSelectorScreen(midlet);
		return instance;
	}
	public void initialize() {
		if (initialized)
			return;
		initialized = true;
		queue.enqueueOperation(new OperationsQueue.Operation() {
			public void execute() {
				try {
					currentRoot = (FileConnection) Connector.open(
					        PHOTOS_PATH, Connector.READ);
					displayCurrentRoot();
				} catch (IOException e) {
					initialized = false;
					e.printStackTrace();
				}
			}
		});
	}
	public static void stop() {
		if (instance == null)
			return;
	    instance.queue.abort();
	}
	public void commandAction(Command c, Displayable d) {
	    if (c == openCommand) {
	    	queue.enqueueOperation(new OperationsQueue.Operation() {
				public void execute() {
					openSelected();
				}
	    	});
	    } else if (c == exitCommand) {
	        midlet.fileSelectorExit();
	    }
	}
	private void openSelected() {
	    int selectedIndex = getSelectedIndex();
	    if (selectedIndex >= 0) {
	        String selectedFile = getString(selectedIndex);
	        if (selectedFile.endsWith(FILE_SEPARATOR)) {
	            try {
	                String tmp = selectedFile.replace(FILE_SEPARATOR.charAt(0), '/');
	                if (currentRoot == null) {
	                    currentRoot = (FileConnection) Connector.open(
	                            "file:///" + tmp, Connector.READ);
	                } else {
	                    currentRoot.setFileConnection(tmp);
	                }
	                displayCurrentRoot();
	            } catch (IOException e) {
	                midlet.showError(e.toString());
	            } catch (SecurityException e) {
	                midlet.showError(e.toString());
	            }
	        } else if (selectedFile.equals(UPPER_DIR)) {
	            try {
	                currentRoot.setFileConnection(UPPER_DIR);
	                displayCurrentRoot();
	            } catch (IOException e) {
	                midlet.showError(e.toString());
	            }
	        } else {
	            String url = currentRoot.getURL() + selectedFile;
	            midlet.displayImage(url);
	        }
	    }
	}
	
	private void displayCurrentRoot() {
	    try {
	        deleteAll();
	        append(UPPER_DIR, FOLDER_IMAGE);
	        Enumeration listOfDirs = currentRoot.list("*", false);
	        while (listOfDirs.hasMoreElements()) {
	            String currentDir = (String) listOfDirs.nextElement();
	            if (currentDir.endsWith("/")) {
	                String tmp = currentDir.replace('/', FILE_SEPARATOR.charAt(0));
	                append(tmp, FOLDER_IMAGE);                    // always display the platform specific seperator to the user
	
	            }
	        }
	        Enumeration listOfFiles = currentRoot.list("*.jpg", false);
	        Stack reverseOrder = new Stack();
	        while (listOfFiles.hasMoreElements()) {
	        	reverseOrder.push(listOfFiles.nextElement());
	        }
	        while (!reverseOrder.isEmpty()) {
	            String currentFile = (String)reverseOrder.pop();
	            if (currentFile.endsWith(FILE_SEPARATOR)) {
	                append(currentFile, FOLDER_IMAGE);
	            } else {
	                append(currentFile, FILE_IMAGE);
	            }
	        }
	        setSelectedIndex(0, true);
	    } catch (IOException e) {
	        midlet.showError(e.toString());
	        initialized = false;
	    } catch (SecurityException e) {
	        midlet.showError(e.toString());
	        initialized = false;
	    }
	}


}
