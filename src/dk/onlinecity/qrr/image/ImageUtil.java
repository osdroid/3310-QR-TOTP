/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.image;

import java.util.Enumeration;
import java.util.Hashtable;

public class ImageUtil
{
	
	public static int[] calculateThresholdValues(MonochromeImage image,
			int w, int h, int gw, int gh) throws Exception {
		int[] histogram = new int[256];
		int[][] gridHistograms = new int[gw * gh][256];
		generateHistograms(image, histogram, gridHistograms, w, h, gw, gh);
		return threshold(histogram, gridHistograms);
	}

	private static void generateHistograms(MonochromeImage image, 
			int[] histogram, int[][] gridHistograms, 
			int w, int h, int gw, int gh) throws Exception {
		int gsx = w / gw;
		int gsy = h / gh;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int gray = image.getInt(x, y);
				histogram[gray]++;
				int gridHistogramIndex = (x / gsx) + (y / gsy) * gw;
				if (gridHistogramIndex < gridHistograms.length) 
					gridHistograms[gridHistogramIndex][gray]++;
			}
		}
	}
	
	private static int[] threshold(int[] histogram, int[][] gridHistograms) {
		int l = gridHistograms.length;
		int threshold = Threshold.blackPointEstimate(histogram);
		int tIndex = 0;
		Hashtable uniqueThresholdValues = new Hashtable(l + 1);
		uniqueThresholdValues.put(threshold + "", (tIndex++) + "");

		for (int i = 0; i < l; i++) {
			threshold = Threshold.blackPointEstimate(gridHistograms[i]);
			// ensure we don't have any duplicate threshold values.
			if (!uniqueThresholdValues.containsKey(threshold + ""))
				uniqueThresholdValues.put(threshold + "", (tIndex++) + "");
		}
		int[] result = new int[uniqueThresholdValues.size()];
		String k, v;
		// Enumerate the hash table to get unique threshold values.
		for (Enumeration e = uniqueThresholdValues.keys(); e.hasMoreElements();) {
			k = (String) e.nextElement();
			v = (String) uniqueThresholdValues.get(k);
			result[Integer.parseInt(v)] = Integer.parseInt(k);
		}
		return result;
	}

	/*
	public static Thumbnail getThumbnail(int[] rgb, int w, int h, int tw, int th)
	{
		CoordinateTranslator thumbnailToImage = PerspectiveTransformer.getCoordinateTranslator(0, 0, tw, 0, 0, th, tw, th, 0, 0, w, 0, 0, h, w, h);
		CoordinateTranslator imageToThumbnail = PerspectiveTransformer.getCoordinateTranslator(0, 0, w, 0, 0, h, w, h, 0, 0, tw, 0, 0, th, tw, th);

		int[] thumbnail = new int[tw * th];

		int x, y;

		for (int ty = 0; ty < th; ty++) {
			for (int tx = 0; tx < tw; tx++) {
				x = (int) thumbnailToImage.getU(tx, ty);
				y = (int) thumbnailToImage.getV(tx, ty);
				thumbnail[tx + ty * tw] = rgb[x + y * w];
			}
		}
		return new Thumbnail(thumbnail, imageToThumbnail, tw, th);
	}
*/
}
