package com.podervisual.trinkets;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

public class ErrorScreen
	extends Alert {
	
	private static Image image;
	private static Display display;
	private static ErrorScreen instance = null;
	
	private ErrorScreen() {
		super("Error");
		setType(AlertType.ERROR);
		setTimeout(5000);
		setImage(image);
	}
	
	public static void init(Image img, Display disp) {
		image = img;
		display = disp;
	}
	
	public static void showError(String message, Displayable next) {
		if (instance == null) {
		    instance = new ErrorScreen();
		}
		instance.setTitle("Error");
		instance.setString(message);
		display.setCurrent(instance, next);
	}
}