package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by ian on 3/12/15.
 *
 * This class uses the area difference between the OCR results and
 * the test input to evaluate results for a genetic algorithm.
 */
public class OcrRectTest extends OcrTestBase {

    private static final String TAG = "com.ianhook.OcrRectTest";
    private static final String TEST_FILE = "rect_test.ser";

    private OcrTestObject[] mTests;

    protected void initTest() {
        mLength = mTests.length;
    }

    @Override
    public Bitmap getImage() {
        return mTests[mCurrentPosition].getImage();
    }

    @Override
    public String getCurrentName() {
        return null;
    }

    @Override
    public int evalResults(List<OcrResult> results) {
        return 0;
    }

    private class OcrTestObject implements Serializable {
        private String mZipFileName;
        private int mImagePosition;
        private Rect[] mRects;
        //serialization
        public Bitmap getImage() {
            Bitmap bm = null;
            try {
                MangaReader mr = new MangaReader(mZipFileName);
                bm = mr.getImage(mImagePosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }

    }
}
