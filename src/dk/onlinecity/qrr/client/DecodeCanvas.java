/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import com.podervisual.trinkets.MonochromeJpeg;
import com.podervisual.trinkets.MonochromeUtils;
import com.podervisual.trinkets.QrTotpMidlet;

import dk.onlinecity.qrr.QrReader;
import dk.onlinecity.qrr.image.MonochromeImage;

public class DecodeCanvas extends GameCanvas implements Runnable
{
	private QrTotpMidlet midlet;
	private Thread thread;
	private int ih;
	private int iw;
	private int th;
	private int tw;
	private int[] ul;
	private int[] ur;
	private int[] ll;
	private int[] lr;
	
	private InputStream imageInput;
	private boolean loadingFile = false;
	private Image photo;
	private MonochromeImage monoImage;

	private int attempt = 1;
	private int attempts;

	private String message = "No QR Code found.";
	private boolean showMessage = false;
	private String result;

	public DecodeCanvas(QrTotpMidlet midlet, Image image)
	{
		super(true);
		this.midlet = midlet;
		photo = image;
		initialize();
	}
	public DecodeCanvas(QrTotpMidlet midlet, byte[] rawImage)
	{
		super(true);
		this.midlet = midlet;
		photo = Image.createImage(rawImage, 0, rawImage.length);
		initialize();
	}
	
	public DecodeCanvas(QrTotpMidlet midlet, InputStream input) {
		super(true);
		this.midlet = midlet;
		setFullScreenMode(true);
		this.loadingFile = true;
		this.imageInput = input;
		
		render(getGraphics());
		flushGraphics();
	}
	private void initialize() {
		setFullScreenMode(true);
		iw = photo.getWidth();
		ih = photo.getHeight();

		int[] pixels = new int[iw * ih];
		photo.getRGB(pixels, 0, iw, 0, 0, iw, ih);
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = (((pixels[i] >> 16) & 0xff) +
					((pixels[i] >> 8) & 0xff) +
					(pixels[i] & 0xff)) / 3;
		}
		monoImage = new MonochromeImage(pixels, iw, ih);
		render(getGraphics());
		flushGraphics();
	}

	protected void showNotify()
	{
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}

	}
	
	private void showMessage(String message) {
		showMessage = true;
		this.message = message;
	}

	public void run()
	{
		try {
			loadImage();
			decode();
		} catch(Exception e) {
			showMessage = true;
			message = e.toString();
		} catch(Error e) {
			showMessage = true;
			message = e.toString();
		}
		render(getGraphics());
		flushGraphics();
	}
	
	private void loadImage() throws Exception {
		if (!loadingFile)
			return;
		try {
			MonochromeJpeg monoImage = new MonochromeJpeg(imageInput, new MonochromeJpeg.ProgressListener() {
				
				public void showProgress(int current, int total) {
					attempt = current;
					attempts = total;
					render(getGraphics());
					flushGraphics();
				}
			});
	        imageInput.close();
	        
	        iw = monoImage.getWidth();
			ih = monoImage.getHeight();
			tw = getWidth();
			th = tw * ih / iw;
			if (th > getHeight()) {
				th = getHeight();
				tw = th * iw / ih;
			}
			
			int[] pixels = MonochromeUtils.scaleImage(monoImage, iw, ih, tw, th);
			photo = Image.createRGBImage(pixels, tw, th, false);
			for (int i = 0; i < pixels.length; i++)
				pixels[i] &= 0xff;
			this.monoImage = new MonochromeImage(pixels, tw, th);
			iw = tw;
			ih = th;
			loadingFile = false;
	    } catch (IOException e) {
	    	showMessage(e.toString());
	    } catch (Exception e) {
	    	showMessage(e.toString());
	    } catch (OutOfMemoryError e) {
	    	showMessage("File is too large to dsplay");
	    } catch (Error e) {
	    	showMessage("Failed to load file. " + e.toString());
	    }
	}

	private void decode() throws Exception
	{
		QrReader qrReader = new QrReader(monoImage, iw, ih, 8, 6);
		attempt = 0;
		attempts = qrReader.getAttempts();
		render(getGraphics());
		flushGraphics();
		try {
			result = null;
			while (result == null) {
				result = qrReader.scan();
				ul = qrReader.getUl();
				ur = qrReader.getUr();
				ll = qrReader.getLl();
				lr = qrReader.getLr();
				photo = MonochromeUtils.generateBWImage(monoImage, iw, ih);
				render(getGraphics());
				flushGraphics();
				attempt++;
			}
			
			render(getGraphics());
			flushGraphics();
		} catch (Exception e) {
			e.printStackTrace();
		}

		message = (result == null) ?
				"No QR Code found." : "Bingo!! QR Found!";
		long time = System.currentTimeMillis() + 2500;
		while (System.currentTimeMillis() < time) {
			showMessage = true;
			render(getGraphics());
			flushGraphics();
			Thread.yield();
		}
		midlet.displayFileBrowser();
		if (result != null)
			midlet.showMsg(result);
	}

	private void render(Graphics g)
	{
		g.setColor(0x0);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (photo != null)
			g.drawImage(photo, (getWidth() - tw) >> 1, (getHeight() - th) >> 1, Graphics.TOP | Graphics.LEFT);
		g.setColor(0x00ff00);
		if (ul != null && ur != null) g.drawLine(ul[0], ul[1], ur[0], ur[1]);
		if (ur != null && lr != null) g.drawLine(ur[0], ur[1], lr[0], lr[1]);
		if (lr != null && ll != null) g.drawLine(lr[0], lr[1], ll[0], ll[1]);
		if (ll != null && ul != null) g.drawLine(ll[0], ll[1], ul[0], ul[1]);
		
		g.setColor(0xffffff);
		if (loadingFile) {
			g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE));
			g.drawString("Loading...", getWidth() >> 1, getHeight() >> 1, Graphics.TOP | Graphics.HCENTER);
		}
		if (showMessage) {
			g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
			g.drawString(message, getWidth() >> 1, getHeight() - 5, Graphics.BOTTOM | Graphics.HCENTER);
		} else {
			renderProgress(g, 20, getWidth() - 20, 10);
		}
		
	}

	private int getProcess(int x0, int x1)
	{
		int w = x1 - x0;
		double progress = attempts > 0 ? 
				((double) attempt / (double) attempts) : 0;
		return (int) (progress * ((double) w));
	}

	private void renderProgress(Graphics g, int x0, int x1, int height)
	{
		g.setColor(0xffffff);
		g.fillRect(x0, getHeight() - height - 10, x1 - x0, height);
		g.setColor(0);
		g.fillRect(x0 + 1, getHeight() - height - 10 + 1, x1 - x0 - 2, height - 2);
		g.setColor(0xffffff);
		g.fillRect(x0 + 2, getHeight() - height - 10 + 2, getProcess(x0, x1 - 2 - 2), height - 4);
	}
}
