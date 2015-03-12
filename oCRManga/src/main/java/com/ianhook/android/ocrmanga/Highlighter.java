package com.ianhook.android.ocrmanga;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class Highlighter extends LinearLayout implements OnClickListener {
    public Highlighter(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        setOnClickListener(this);
    }

    public Highlighter(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        setOnClickListener(this);
    }

    public Highlighter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        setOnClickListener(this);
    }

    public static final String TAG = "Highlighter";
    
    public static final String TOP = "top";
    public static final String LEFT = "left";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";
    public static final String BACKGROUND_COLOR = "#50134BE8";
    public static final String BACKGROUND_COLOR_SELECTED = "#50E8B013";

    private Bitmap mResizedBitmap;
    private ImagePagerActivity.ImageFragment mImageFragment;

    private View mRootView;
    private int left;
    private int top;

    public void updatePosition() {
        Log.d(TAG, "updating");
        Log.d(TAG, mRootView.getParent().toString());
        //mRootView.setLeft(left);
        //mRootView.setX(left);
        //mRootView.setTop(top);
        //mRootView.setY(top);
        mRootView.setTranslationX(left);
        mRootView.setTranslationY(top);
    }

    public void setImagePage(ImagePagerActivity.ImageFragment imgf) {
        mImageFragment = imgf;
    }
    
    public void setSelected() {
        mRootView.setBackground(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR_SELECTED)));
    }
    
    public void setUnselected() {
        mRootView.setBackground(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));
    }

    public void onClick(View v) {
        Log.d(TAG, "got click");

        Rect highlightRect = new Rect();
        v.getHitRect(highlightRect);
        mResizedBitmap = mImageFragment.getBitmapSection(highlightRect);

        Ocr ocr = mImageFragment.getOcr();

        if(ocr != null) {
            Ocr.CompletionCallback displayText = new DisplayText();
            ocr.setCompletionCallback(displayText);
            ocr.enqueue(mResizedBitmap);
        }

        /*
        View current = (View) v.getParent();
        LinearLayout highlighter = (LinearLayout) current.findViewById(R.id.highlighter);
        highlighter.setVisibility(View.GONE);
        */
    }

    private class DisplayText implements Ocr.CompletionCallback {

        @Override
        public void onCompleted(List<OcrResult> results) {

            String message = "";
            Intent intent = new Intent(mImageFragment.getActivity(), DisplayMessageActivity.class);
            Log.d("DisplayText", "got some results");
            if(results.isEmpty()) {
                message = "I was afraid of this.";
            } else {

                for(int i = 0; i < results.size(); i++) {
                    message += results.get(i).getString() + "\n";
                    Log.d("DisplayText", results.get(i).getString());

                }
            }
            intent.putExtra(ImagePagerActivity.EXTRA_MESSAGE, message);
            intent.putExtra(ImagePagerActivity.FILE_NAME, "");
            intent.putExtra(ImagePagerActivity.BITMAP, mResizedBitmap);

            mImageFragment.startActivity(intent);

        }

    }

}
