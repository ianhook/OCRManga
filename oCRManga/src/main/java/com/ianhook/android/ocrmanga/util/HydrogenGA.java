package com.ianhook.android.ocrmanga.util;

import android.os.Environment;

import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by ian on 3/12/15.
 */
public class HydrogenGA {
    public static final String TEST_DIR = "td_tests";

    public static void saveTest(List<OcrResult> results, String file_name, int page) throws IOException {
        //find the text in the image, in case it hasn't been done already
        File temp_f = getTestDir();

        File[] files = temp_f.listFiles();
        File outFile = new File(temp_f, String.format("test%d.ser", files.length));
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
        try {
            out.writeObject(results);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    public static Object readData(int position) {
        File temp_f = getTestDir();

        File[] files = temp_f.listFiles();
        if(position >= files.length) {
            return null;
        }
        Object x = null;
        try {
            FileInputStream door = new FileInputStream(files[position]);
            ObjectInputStream reader = new ObjectInputStream(door);

            x = (Object) reader.readObject();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return x;

    }

    protected static File getTestDir() {
        return new File(Environment.getExternalStorageDirectory(), TEST_DIR);
    }
}
