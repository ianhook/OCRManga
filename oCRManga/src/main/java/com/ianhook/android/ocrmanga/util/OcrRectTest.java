package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
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
            mCutOff = 1723200 * mTests.size();
        } else {
            mTests = new ArrayList<OcrTestObject>();
            mLength = 0;
            mCutOff = 0;
        }
    }

    @Override
    public Bitmap getImage() {
        return mTests.get(mCurrentPosition).getImage();
    }

    public Bitmap getImage(int position) {
        return mTests.get(position).getImage(OcrTestObject.AUTO_SCALE);
    }

    @Override
    public String getCurrentName() {
        OcrTestObject test = mTests.get(mCurrentPosition);
        return String.format("%s:%s", test.getFileName(), test.getImageName());
    }

    @Override
    public int evalResults(List<OcrResult> results) {
        int max = mTests.get(mCurrentPosition).getImageSize();
        ArrayList<SerialRect> tRects = mTests.get(mCurrentPosition).getRects();
        // assume sort of results and test data for sweep
        // also assume test data does not contain overlaps

        // TODO: sort is done in OcrTaskProcessor::processPix. It is current by X decreasing
        ArrayList<Rect> currentTests = new ArrayList<Rect>();
        ArrayList<Rect> currentResults = new ArrayList<Rect>();
        int currentX = mTests.get(mCurrentPosition).getImageWidth();
        int rPos = 0;
        int tPos = 0;
        Rect rRect = null;
        Rect tRect = null;
        Log.d(TAG, String.format("Found Rects %d; %d", results.size(), tRects.size()));
        while((rPos + tPos) < (results.size() + tRects.size())) {
            ArrayList<Rect> tempRects = new ArrayList<Rect>();
            if(rPos < results.size()) {
                rRect = results.get(rPos).getBounds();
            } else {
                rRect = null;
            }
            if (tPos < tRects.size()) {
                tRect = tRects.get(tPos).getRect();
            } else {
                tRect = null;
            }
            Rect currentRect;
            if(rRect != null && (tRect == null || rRect.right > tRect.right)) {
                rPos += 1;
                currentX = rRect.right;
                currentRect = rRect;
                Log.d(TAG, String.format("%d: %d Result Rect: %s", rPos, currentX, currentRect.toString()));

                for(Rect rect : currentTests) {
                    if(rect.left > currentX) {
                        //can't intersect anything anymore
                        //currentTests.remove(rect);
                    } else if(Rect.intersects(rect,currentRect)) {
                        Rect intersection = new Rect(currentRect);
                        intersection.intersect(rect);
                        //because both rects are subtracted in full we add back the
                        // intersection twice
                        max += intersection.width() * intersection.height() * 2;
                        tempRects.add(rect);
                    } else {
                        tempRects.add(rect);
                    }
                }
                currentResults.add(currentRect);
                currentTests = tempRects;
            } else {
                tPos += 1;
                currentX = tRect.right;
                currentRect = tRect;
                Log.d(TAG, String.format("%d: %d, Expected Rect: %s", tPos, currentX, currentRect.toString()));

                for(Rect rect : currentResults) {
                    if(rect.left > currentX) {
                        //can't intersect anything anymore
                        //currentResults.remove(rect);
                    } else if(Rect.intersects(rect,currentRect)) {
                        Rect intersection = new Rect(currentRect);
                        intersection.intersect(rect);
                        max += intersection.width() * intersection.height() * 2;
                        tempRects.add(rect);
                    } else {
                        tempRects.add(rect);
                    }

                }
                currentTests.add(currentRect);
                currentResults = tempRects;
            }
            Log.d(TAG, String.format("Current Tests: %d; Current Results: %d", currentTests.size(), currentResults.size()));

            //subtract rect area from image area
            max -= currentRect.width() * currentRect.height();
        }


        return max;
    }

    public void writeTests(String zipFile, int position, List<OcrResult> results, Point size) {
        OcrTestObject test = new OcrTestObject(zipFile, position, results, size);
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
            Log.d(TAG, mTests.get(0).toString());
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

    private class SerialRect implements Serializable {
        private transient Rect mRect;

        SerialRect(){};

        SerialRect(Rect rect) {
            mRect = rect;
        }

        public Rect getRect() {
            return mRect;
        }

        private void writeObject(ObjectOutputStream outputStream) throws IOException {
            outputStream.writeInt(mRect.left);
            outputStream.writeInt(mRect.right);
            outputStream.writeInt(mRect.top);
            outputStream.writeInt(mRect.bottom);
        }

        private void readObject(ObjectInputStream inputStream) throws IOException {
            int left = inputStream.readInt();
            int right = inputStream.readInt();
            int top = inputStream.readInt();
            int bottom = inputStream.readInt();

            mRect = new Rect(left, top, right, bottom);
        }
    }

    public class OcrTestObject implements Serializable {
        private static final long serialVersionUID = 1L;
        public static final int AUTO_SCALE = -1;

        private String mZipFileName;
        private int mImagePosition;
        private ArrayList<SerialRect> mRects;
        private int mImageSize = -1;
        private int mWidth = -1;
        private Point mSize;

        OcrTestObject(String zipFile, int position, List<OcrResult> results, Point size) {
            setImage(zipFile, position);
            addRects(results);
            mSize = size;
        }

        public Bitmap getImage() {
            //get the full size image
            return getImage(1);
        }

        public Bitmap getImage(int scale) {
            Bitmap bm = null;
            try {
                MangaReader mr = new MangaReader(mZipFileName);
                if(scale == AUTO_SCALE) {
                    bm = mr.getImage(mImagePosition);
                } else {
                    mr.setScreen(mSize);
                    bm = mr.getImage(mImagePosition, scale);
                }
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

        public ArrayList<SerialRect> getRects() {
            return mRects;
        }

        public void addRects(List<OcrResult> r) {
            mRects = new ArrayList<SerialRect>();
            for(int i = 0; i < r.size(); i++) {
                mRects.add(new SerialRect(r.get(i).getBounds()));
            }
        }

        public void setImage(String file_name, int position) {
            mZipFileName = file_name;
            mImagePosition = position;
        }

        public String toString() {
            return String.format("Test: %s %d; Rect Count %d", mZipFileName, mImagePosition, mRects.size());
        }

        private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
            Log.d(TAG, "reading");
            mZipFileName = "/storage/sdcard/Manga/" + (String) in.readObject();
            mImagePosition = in.readInt();
            mImageSize = in.readInt();
            mWidth = in.readInt();
            mSize = new Point(in.readInt(), in.readInt());
            mRects = (ArrayList<SerialRect>) in.readObject();

            Log.d(TAG, mZipFileName);
            Log.d(TAG, String.format("Rects Found %d", mRects.size()));
            for(SerialRect r : mRects) {
                Log.d(TAG, r.toString());
            }
        }

        private void writeObject(ObjectOutputStream out) throws IOException {

            out.writeObject(mZipFileName);
            out.writeInt(mImagePosition);
            out.writeInt(mImageSize);
            out.writeInt(mWidth);
            out.writeInt(mSize.x);
            out.writeInt(mSize.y);
            out.writeObject(mRects);
        }

    }
}
