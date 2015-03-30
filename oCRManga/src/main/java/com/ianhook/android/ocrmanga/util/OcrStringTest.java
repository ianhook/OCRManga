package com.ianhook.android.ocrmanga.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.googlecode.eyesfree.ocr.client.OcrResult;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ian on 3/12/15.
 *
 * This class compares the string difference between the OCR results
 * and the expected values from the test data
 */
public class OcrStringTest extends OcrTestBase {
    private static final String TAG = "com.ianhook.OcrStrTest";

    private File[] mImageDir;

    protected void initTest() {
        File temp_f = new File(Environment.getExternalStorageDirectory(), "test_data");
        mImageDir = (new File(temp_f, "test_images")).listFiles();
        Log.d(TAG, temp_f.getAbsolutePath());
        mLength = mImageDir.length;

    }

    public String getCurrentName(){
        return mImageDir[mCurrentPosition].getName();
    }

    public Bitmap getImage() {
        return BitmapFactory.decodeFile(mImageDir[mCurrentPosition].getAbsolutePath());
    }

    public String getExpected() {
        String encoding = "UTF-8";
        String imageName = mImageDir[mCurrentPosition].getName();
        String textName = imageName.substring(0, imageName.length() - 4) + ".txt";
        File stringFile = new File(Environment.getExternalStorageDirectory(), "test_data/test_strings/" + textName);

        String NL = System.getProperty("line.separator");
        StringBuilder text = new StringBuilder();

        Scanner scanner;
        try {
            scanner  = new Scanner(new FileInputStream(stringFile), encoding);

            while (scanner.hasNextLine()){
                text.append(scanner.nextLine() + NL);
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return text.toString();
    }

    public int evalResults(List<OcrResult> results) {

        //  get the ocr results for an image
        String expected = getExpected();
        String message = "";
        if(!results.isEmpty()) {
            for(int i = 0; i < results.size(); i++)
                message += results.get(i).getString() + "\n";
        }

        int distance = StringUtils.getLevenshteinDistance(message, expected);
        int base = expected.length() * 100;
        int temp = 0;

        if(mDebug) {
            Log.i(TAG, String.format("length: %d, found: %s", message.length(), message));
            Log.i(TAG, String.format("length: %d, expected: %s", expected.length(), expected));
        }

        // we give no reward for finding nothing
        if(message.length() > 0 || expected.length() == 0)
            temp = Math.abs(distance) + Math.abs(message.length() - expected.length());

        Log.i(TAG, String.format("base: %d, temp: %d", base, temp));
        if(base < temp) {
            Log.e(TAG, String.format("base too low: %d", temp));
        }

        if(mDebug) {
            Log.d(TAG, String.format("image %d, %s", getPosition(), getCurrentName()));
            //Log.d(TAG, String.format("str %d, %s", mCurrentPosition, stringFile.getAbsolutePath()));
            Log.d(TAG, "Message: " + message.replace("\n","\\n"));
            Log.d(TAG, "Expected: " + expected.replace("\n", "\\n"));
            Log.d(TAG, String.format("distance: %d, %d", temp, base - temp));
        }

        return (base - temp);
    }


}
