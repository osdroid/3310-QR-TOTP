package com.podervisual.trinkets;

import java.io.InputStream;

import slim.texture.io.JPEGDecoder;
import dk.onlinecity.qrr.core.exceptions.QrrException;
import dk.onlinecity.qrr.image.MonochromeImage;

public class MonochromeJpeg extends MonochromeImage {

	public interface ProgressListener {
		public void showProgress(int current, int total);
	}
	private static final int ROWS_ROUND = 2;
	private byte[][] data;
	private JPEGDecoder decoder;
	private int rowsPerArray;

	public MonochromeJpeg(InputStream input, ProgressListener listener) throws Exception {
		decoder = new JPEGDecoder(input);
		decoder.startDecode();
		w = decoder.getImageWidth();
		h = decoder.getImageHeight();
		int rows = decoder.getNumMCURows();
		data = new byte[(rows + ROWS_ROUND - 1) / ROWS_ROUND][];
		int round = 0;
		for (int i = 0; i < rows; i = i + ROWS_ROUND) {
			if (listener != null)
				listener.showProgress(i, rows);
			data[round++] = decoder.decodeGray(ROWS_ROUND);
		}
		rowsPerArray = data[0].length / w;
	}
	

	public boolean get(int x, int y) {
		if (0 <= x && x < w && 0 <= y && y < h)
			return getInt(x, y) <= threshold;
		throw new QrrException();
	}

	public boolean get(int[] p) {
		return get(p[0], p[1]);
	}

	public int getInt(int i) {
		int x = i % w;
		int y = i / w;
		return getInt(x, y);
	}
	public int getInt(int x, int y) {
		if (0 <= x && x < w && 0 <= y && y < h) {
			int arr = y / rowsPerArray;
			int pos = y % rowsPerArray;
			return data[arr][x + pos * w] & 0xFF;
		}
		return 0;
	}
}
