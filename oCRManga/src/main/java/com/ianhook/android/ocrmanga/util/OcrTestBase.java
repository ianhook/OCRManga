package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;

import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.io.File;
import java.util.List;

/**
 * Created by ian on 3/12/15.
 */
public abstract class OcrTestBase {

    protected int mCurrentPosition;
    protected int mLength;
    protected int mCutOff;

    public boolean mDebug = false;

    public OcrTestBase() {
        mLength = 0;
        mCutOff = 0;
        initTest();
        resetPosition();
    }

    public int next() {
        mCurrentPosition += 1;
        return mCurrentPosition;
    }

    public int resetPosition() {
        return mCurrentPosition = 0;
    }

    public int getPosition() {
        return mCurrentPosition;
    }

    public boolean canContinue(int last) {
        if(last != mCurrentPosition
                && mCurrentPosition < mLength) {
            return true;
        } else {
            return false;
        }
    }

    public int getCutOff() {
        return mCutOff;
    }

    /*
     * This function needs to set mLength
     */
    protected abstract void initTest();

    public abstract Bitmap getImage();

    public abstract String getCurrentName();

    public abstract int evalResults(List<OcrResult> results);
}
