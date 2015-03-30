package com.ianhook.android.ocrmanga;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.OcrResult;

import java.util.List;

public class Highlighter extends LinearLayout implements OnClickListener {
    public Highlighter(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public Highlighter(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public Highlighter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    private RectF mRect;
    private int mID;
    private RectF mPadding;

    public void setImagePage(ImagePagerActivity.ImageFragment imgf) {
        mImageFragment = imgf;

        updateScreen();
    }

    private void updateScreen() {
        if (mImageFragment != null && mRect != null) {

            Log.d(TAG, mRect.toString());
            RectF temp = new RectF(mRect);
            mImageFragment.getImageView().getImageViewMatrix().mapRect(temp);
            Log.d(TAG, mImageFragment.getImageView().getImageViewMatrix().toString());

            Log.d(TAG, temp.toString());


            ViewGroup.LayoutParams params = getLayoutParams();

            if(params == null) {
                params = new ViewGroup.LayoutParams((int) temp.width(), (int) temp.height());
            }

            setX(temp.left);
            setY(temp.top);
            params.height = (int)temp.height();
            params.width = (int)temp.width();

            setLayoutParams(params);
        }
    }
    
    public void setSelected() {
        setBackground(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR_SELECTED)));
    }
    
    public void setUnselected() {
        setBackground(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));
    }

    public void setHighlightID(int id) {
        mID = id;
    }

    public void setRect(RectF dims) {
        mRect = new RectF(dims);
        updateScreen();
    }

    public void updateRectF(RectF dims) {
        Log.d(TAG, mRect.toString());
        Log.d(TAG, dims.toString());
        Log.d(TAG, mPadding.toString());
        mRect.top += dims.top - mPadding.top;
        mRect.left += dims.left - mPadding.left;
        mRect.bottom += dims.bottom - mPadding.bottom;
        mRect.right += dims.right - mPadding.right;
        Log.d(TAG, mRect.toString());
        redraw();
    }

    public void redraw() {

        updateScreen();
        requestLayout();
    }

    public RectF getRect() {
        return mRect;
    }

    public void onClick(View v) {
        Log.d(TAG, "got click");
        setSelected();

        mPadding = new RectF();
        mResizedBitmap = mImageFragment.getBitmapSection(mRect, mPadding);

        Ocr ocr = mImageFragment.getOcr();

        if(ocr != null) {
            Ocr.CompletionCallback displayText = new DisplayText(mPadding);
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
        private RectF mHighlightPadding;

        public DisplayText(RectF padding) {
            super();
            mHighlightPadding = padding;
        }

        @Override
        public void onCompleted(List<OcrResult> results) {
            setUnselected();

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
            intent.putExtra(DisplayMessageActivity.EXTRA_MESSAGE, message);
            intent.putExtra(DisplayMessageActivity.FILE_NAME, "");
            intent.putExtra(DisplayMessageActivity.BITMAP, mResizedBitmap);
            intent.putExtra(DisplayMessageActivity.HIGHLIGHT, mHighlightPadding);
            intent.putExtra(DisplayMessageActivity.HIGHLIGHT_ID, mID);

            Log.d(TAG, String.format("highlight id: %d", mID));
            mImageFragment.startActivityForResult(intent, 1);

        }

    }

}
