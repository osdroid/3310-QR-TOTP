package com.podervisual.trinkets;

import javax.microedition.lcdui.Image;

import dk.onlinecity.qrr.image.MonochromeImage;

public class MonochromeUtils {
	public static int[] scaleImage(MonochromeImage image,
			int iw, int ih, int tw, int th) {
		int scaledPixelsX = iw / tw;
		int scaledPixelsY = ih / th;
		int[] pixels = new int[tw * th];
		for (int x = 0; x < tw; x++) {
			for (int y = 0; y < th; y++) {
				int iniX = x * scaledPixelsX;
				int iniY = y * scaledPixelsY;
				int sum = 0;
				int count = 0;
				for (int originalX = iniX; originalX < iniX + scaledPixelsX 
						&& originalX < iw; originalX++) {
					for (int originalY = iniY; originalY < iniY + scaledPixelsY 
							&& originalY < iw; originalY++) {
						sum += image.getInt(originalX, originalY);
						count++;
					}
				}
				int gray = 0;
				if (count > 0)
					gray = sum / count;
				int color = (gray << 16) + (gray << 8) + gray;
				pixels[x + y * tw] = color;
			}
		}
		return pixels;
	}

	public static Image generateBWImage(MonochromeImage image,
			int width, int height) {
		int[] pixels = new int[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				pixels[x + y * width] = image.get(x, y) ? 0 : 0xffffff;
			}
		}
		return Image.createRGBImage(pixels, width, height, false);
	}
}
