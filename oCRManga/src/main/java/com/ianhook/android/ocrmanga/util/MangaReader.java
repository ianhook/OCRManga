package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.googlecode.eyesfree.textdetect.Thresholder;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by ian on 3/13/15.
 */
public class MangaReader {

    private static final String TAG = "com.ianhook.MangaR";
    private static ZipFile mZipFile;
    private static int mCount;
    private static Point mScreenSize;

    public MangaReader() {
    }

    public MangaReader(String file_name) throws IOException {
        setFile(file_name);
        mScreenSize = null;
    }

    public MangaReader(File file) throws IOException {
        setFile(file);
        mScreenSize = null;
    }

    public MangaReader(File file, Point screen_size) throws IOException {
        setFile(file);
        mScreenSize = screen_size;
    }

    public MangaReader(String file_name, Point screen_size) throws IOException {
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

    public void setFile(File file) throws ZipException, IOException {
        mZipFile = new ZipFile(file);
        mCount = Collections.list(mZipFile.entries()).size();
        Log.v(TAG, String.format("%d images found", mCount));
    }

    public static String getFileName() {
        File file = new File(mZipFile.getName());
        return file.getName();
    }

    public int getCount() {
        return mZipFile.size();
    }

    public CharSequence getImageName(int position) {
        ArrayList<? extends ZipEntry> imageArray = Collections.list(mZipFile.entries());

        return "file: " + imageArray.get(position).getName();
    }

    public Bitmap getImage(int position) {
        return getImage(position, getScale(position));
    }

    public Bitmap getImage(int position, int scale) {
        InputStream zis;
        Bitmap bm = null;
        ArrayList<? extends ZipEntry> imageArray = Collections.list(mZipFile.entries());

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try {
            if(imageArray.get(position).getSize() == 0)
                return null;
            Log.v(TAG, "file name is "+ imageArray.get(position).getName());
            zis = mZipFile.getInputStream(imageArray.get(position));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            bm = BitmapFactory.decodeStream(zis, null, options);
            zis.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        if(bm == null) {
            Log.e(TAG, String.format("what happened here? size: %d", imageArray.get(position).getSize()) );
        }

        //return getThreshed(bm);
        return bm;
    }

    @SuppressWarnings("unused")
    private Bitmap getThreshed(Bitmap bm) {
        // this is the one that we actually use
        //return WriteFile.writeBitmap(Thresholder.edgeAdaptiveThreshold(ReadFile.readBitmap(bm)));
        //return WriteFile.writeBitmap(Thresholder.edgeAdaptiveThreshold(ReadFile.readBitmap(bm), 100, 100, 32, 1));

        //causes blocks of differently thresholded areas
        //return WriteFile.writeBitmap(Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bm)));
        //return WriteFile.writeBitmap(Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bm), 1, 1));

        //makes outlines around things, small letters become too bold
        return WriteFile.writeBitmap(Thresholder.sobelEdgeThreshold(ReadFile.readBitmap(bm)));
    }

    private int getScale(int position) {
        int scale = 1;
        try {
            ArrayList<? extends ZipEntry> imageArray = Collections.list(mZipFile.entries());
            InputStream zis = mZipFile.getInputStream(imageArray.get(position));
            Rect rect = new Rect();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(zis, rect, options);

            int scaleHeight = options.outHeight / mScreenSize.y;
            int scaleWidth = options.outWidth / mScreenSize.x;

            if(scaleHeight > 1 || scaleWidth > 1) {
                if(scaleHeight > scaleWidth)
                    scale = scaleHeight;
                else
                    scale = scaleWidth;
            }

            Log.v(TAG, String.format("scale: %d, s_x:%d, s_y:%d, h:%d, w:%d, %d",
                    scale, mScreenSize.x, mScreenSize.y, options.outHeight, options.outWidth, scaleHeight));


        } catch (IOException e) {
            scale = 1;
            Log.e(TAG, e.getMessage());
        }
        return scale;
    }
}
