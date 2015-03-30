package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.googlecode.eyesfree.textdetect.Thresholder;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by ian on 3/30/15.
 */
public abstract class ImageFileReader {

    private static final String TAG = "com.ianhook.IFileR";

    protected Point mScreenSize;

    public ImageFileReader() {
    }

    public ImageFileReader(String file_name) throws IOException {
        setFile(file_name);
        mScreenSize = null;
    }

    public ImageFileReader(File file) throws IOException {
        setFile(file);
        mScreenSize = null;
    }

    public ImageFileReader(File file, Point screen_size) throws IOException {
        setFile(file);
        mScreenSize = screen_size;
    }

    public ImageFileReader(String file_name, Point screen_size) throws IOException {
        setFile(file_name);
        mScreenSize = screen_size;
    }

    public void setScreen(Point point) {
        mScreenSize = point;
    }

    public void setFile(String file_name) throws IOException {
        Log.v(TAG, "loading file " + file_name);
        setFile(new File(file_name));

    }

    public abstract void setFile(File file) throws IOException;

    public abstract String getFileName();

    public abstract int getCount();

    public abstract CharSequence getImageName(int position);

    public Bitmap getImage(int position) {
        return getImage(position, getScale(position));
    }

    public abstract Bitmap getImage(int position, int scale);

    @SuppressWarnings("unused")
    protected Bitmap getThreshed(Bitmap bm) {
        // this is the one that we actually use
        //return WriteFile.writeBitmap(Thresholder.edgeAdaptiveThreshold(ReadFile.readBitmap(bm)));
        //return WriteFile.writeBitmap(Thresholder.edgeAdaptiveThreshold(ReadFile.readBitmap(bm), 100, 100, 32, 1));

        //causes blocks of differently thresholded areas
        //return WriteFile.writeBitmap(Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bm)));
        //return WriteFile.writeBitmap(Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bm), 1, 1));

        //makes outlines around things, small letters become too bold
        return WriteFile.writeBitmap(Thresholder.sobelEdgeThreshold(ReadFile.readBitmap(bm)));
    }

    protected abstract int getScale(int position);
}
