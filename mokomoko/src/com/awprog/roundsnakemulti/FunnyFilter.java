package com.awprog.roundsnakemulti;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

public class FunnyFilter {
	static int[] ref1 = null, ref2 = null;
	
	private static int currentFilter = 0, nbFilter = 5;
	public static void clearFilter() {
		currentFilter = 0;
	}
	public static void nextFilter() {
		currentFilter = (currentFilter+1) % nbFilter;
	}
	public static void previousFilter() {
		currentFilter = (currentFilter-1) % nbFilter;
	}
	public static void applyCurrentFilter(Bitmap bmp) {
		switch(currentFilter) {
		case 0:
			break;
		case 1:
			applyBigPixelFilter(bmp, 3, 1);
			break;
		case 2:
			applyBigPixelFilter(bmp, 2, 0);
			break;
		case 3:
			applyGreenFilter(bmp, 5);
			break;
		case 4:
			applyGreenFilter(bmp, 8);
			break;
		}
	}
	
	public static void applyBigPixelFilter(Bitmap bitmap, int colorColumnWidth, int blackWidth) {
		//long t = SystemClock.elapsedRealtime();
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		//Log.i("###", "- "+width+" "+height);
		
		int[] pxlDst = (ref1 == null || ref1.length < width*height) ? new int[width*height] : ref1;
		if(blackWidth > 0) Arrays.fill(pxlDst, 0xff000000);
		int[] pxlSrc = (ref2 == null || ref2.length < width*height) ? new int[width*height] : ref2;
		//Log.i("###", "A "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		bitmap.getPixels(pxlSrc, 0, width, 0, 0, width, height);
		//Log.i("###", "B "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		final int bigPixelSize = colorColumnWidth*3+blackWidth;

		for(int x = 0; x < width; x += bigPixelSize) {
			for(int y = 0; y < height; y += bigPixelSize) {
				int r = 0, g = 0, b = 0, count = 0;
				// moyenne sur pixelColumnWidth*3+blackWidth ^ 2 pixels
				for(int i = x; i < x+bigPixelSize; i++) {
					if(i >= width)
						break;
					
					for(int j = y; j < y+bigPixelSize; j++) {
						if(j >= height)
							break;
						
						int index = i + j * width;
						r += Color.red(pxlSrc[index]);
						g += Color.green(pxlSrc[index]);
						b += Color.blue(pxlSrc[index]);
						
						count++;
					}
				}
				int val[] = {r / count, g / count, b / count};
				// Dessin du gros pixel sur 9*9 pixels  
				for(int color = 0; color < 3; color++)
				for(int i = x + color*colorColumnWidth; i < x + (color+1)*colorColumnWidth; i++) {
					if(i >= width)
						break;
					for(int j = y; j < y + 3*colorColumnWidth; j++) {
						if(j >= height)
							break;
						
						pxlDst[i + j * width] = 0xff000000 | (val[color] << ((2-color)*8));
					}
				}
			}
		}
		//Log.i("###", "C "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		bitmap.setPixels(pxlDst, 0, width, 0, 0, width, height);
		//Log.i("###", "D "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		ref1 = pxlDst;
		ref2 = pxlSrc;
	}


	public static void applyGreenFilter(Bitmap bitmap, int pixelSize) {
		long t = SystemClock.elapsedRealtime();
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		//Log.i("###", "- "+width+" "+height);
		
		int[] pxlDst = (ref1 == null || ref1.length < width*height) ? new int[width*height] : ref1;
		int[] pxlSrc = (ref2 == null || ref2.length < width*height) ? new int[width*height] : ref2;
		//Log.i("###", "A "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		bitmap.getPixels(pxlSrc, 0, width, 0, 0, width, height);
		//Log.i("###", "B "+(SystemClock.elapsedRealtime()-t));
		t=SystemClock.elapsedRealtime();

		for(int x = 0; x < width; x += pixelSize) {
			for(int y = 0; y < height; y += pixelSize) {
				int v = 0, count = 0;
				// moyenne sur pixelColumnWidth*3+blackWidth ^ 2 pixels
				for(int i = x; i < x+pixelSize; i++) {
					if(i >= width)
						break;
					
					for(int j = y; j < y+pixelSize; j++) {
						if(j >= height)
							break;
						
						int index = i + j * width;
						v += Color.red(pxlSrc[index]) + Color.green(pxlSrc[index]) + Color.blue(pxlSrc[index]);
						
						count++;
					}
				}
				// Intesity value
				v = (255 - v / (count*3)) & 0xf0;
				for(int i = x; i < x + pixelSize; i++) {
					if(i >= width)
						break;
					for(int j = y; j < y + pixelSize; j++) {
						if(j >= height)
							break;
						
						pxlDst[i + j * width] = 0xff000000 | (v << 8);
					}
				}
			}
		}
		Log.i("###", "C "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		bitmap.setPixels(pxlDst, 0, width, 0, 0, width, height);
		//Log.i("###", "D "+(SystemClock.elapsedRealtime()-t));t=SystemClock.elapsedRealtime();
		
		ref1 = pxlDst;
		ref2 = pxlSrc;
	}
	
}
