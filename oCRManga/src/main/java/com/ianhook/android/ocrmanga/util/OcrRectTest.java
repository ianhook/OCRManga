package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ian on 3/12/15.
 *
 * This class uses the area difference between the OCR results and
 * the test input to evaluate results for a genetic algorithm.
 */
public class OcrRectTest extends OcrTestBase {

    private static final String TAG = "com.ianhook.OcrRectTest";
    private static final String TEST_FILE = "/storage/sdcard/Manga/rect_test.ser";

    private ArrayList<OcrTestObject> mTests;

    protected void initTest() {
        readTests();
        if(mTests != null) {
            mLength = mTests.size();
        } else {
            mTests = new ArrayList<OcrTestObject>();
            mLength = 0;
        }
    }

    @Override
    public Bitmap getImage() {
        return mTests.get(mCurrentPosition).getImage();
    }

    @Override
    public String getCurrentName() {
        OcrTestObject test = mTests.get(mCurrentPosition);
        return String.format("%s:%s", test.getFileName(), test.getImageName());
    }

    @Override
    public int evalResults(List<OcrResult> results) {
        int max = mTests.get(mCurrentPosition).getImageSize();
        Rect[] tRects = mTests.get(mCurrentPosition).getRects();
        // assume sort of results and test data for sweep
        // also assume test data does not contain overlaps

        // TODO: sort is done in OcrTaskProcessor::processPix. It is current by X decreasing
        ArrayList<Rect> currentTests = new ArrayList<Rect>();
        ArrayList<Rect> currentResults = new ArrayList<Rect>();
        int currentX = mTests.get(mCurrentPosition).getImageWidth();
        int count = 0;
        int rPos = 0;
        int tPos = 0;
        while(count < results.size() + mTests.size()) {
            Rect rRect = results.get(rPos).getBounds();
            Rect tRect = tRects[tPos];
            Rect currentRect;
            if(rRect.right > tRect.right) {
                rPos += 1;
                currentX = rRect.right;
                currentRect = rRect;

                for(Rect rect : currentTests) {
                    if(rect.left > currentX) {
                        //can't intersect anything anymore
                        currentTests.remove(rect);
                    } else if(Rect.intersects(rect,currentRect)) {
                        Rect intersection = new Rect(currentRect);
                        intersection.intersect(rect);
                        //because both rects are subtracted in full we add back the
                        // intersection twice
                        max += intersection.width() * intersection.height() * 2;
                    }

                }
                currentResults.add(currentRect);
            } else {
                tPos += 1;
                currentX = tRect.right;
                currentRect = tRect;

                for(Rect rect : currentResults) {
                    if(rect.left > currentX) {
                        //can't intersect anything anymore
                        currentResults.remove(rect);
                    } else if(Rect.intersects(rect,currentRect)) {
                        Rect intersection = new Rect(currentRect);
                        intersection.intersect(rect);
                        max += intersection.width() * intersection.height() * 2;
                    }

                }
                currentTests.add(currentRect);
            }

            //subtract rect area from image area
            max -= currentRect.width() * currentRect.height();

            count += 1;
        }


        return max;
    }

    public void writeTests(String zipFile, int position, List<OcrResult> results) {
        OcrTestObject test = new OcrTestObject();
        test.addRects(results);
        mTests.add(test);
        try
        {
            FileOutputStream fileOut =
                    new FileOutputStream(TEST_FILE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(mTests);
            out.close();
            fileOut.close();
            Log.d(TAG, String.format("Serialized data is saved in %s", TEST_FILE));
        }catch(IOException i)
        {
            i.printStackTrace();
        }
    }

    public void readTests() {
        mTests = null;
        try
        {
            FileInputStream fileIn = new FileInputStream(TEST_FILE);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            mTests = (ArrayList<OcrTestObject>) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i)
        {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c)
        {
            Log.d(TAG, "OcrTestObject class not found");
            c.printStackTrace();
            return;
        }
        if(mTests == null) {
            mTests = new ArrayList<OcrTestObject>();
        }
    }

    private class OcrTestObject implements Serializable {
        private String mZipFileName;
        private int mImagePosition;
        private Rect[] mRects;
        private int mImageSize = -1;
        private int mWidth = -1;

        public Bitmap getImage() {
            Bitmap bm = null;
            try {
                MangaReader mr = new MangaReader(mZipFileName);
                bm = mr.getImage(mImagePosition);
                mImageSize = bm.getHeight() * bm.getWidth();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }

        public String getFileName() {
            return mZipFileName;
        }

        public String getImageName() {
            String name = null;
            try {
                MangaReader mr = new MangaReader(mZipFileName);
                name = (String) mr.getImageName(mImagePosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return name;
        }

        public int getImageSize() {
            if (mImageSize < 1) {
                getImage();
            }
            return mImageSize;
        }

        public int getImageWidth() {
            if(mWidth < 1) {
                getImage();
            }
            return mWidth;
        }


        public Rect[] getRects() {
            return mRects;
        }

        public void addRects(List<OcrResult> r) {
            mRects = new Rect[r.size()];
            for(int i = 0; i < r.size(); i++) {
                mRects[i] = r.get(i).getBounds();
            }
        }

        public void setImage(String file_name, int position) {
            mZipFileName = file_name;
            mImagePosition = position;
        }

    }
}
