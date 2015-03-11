package com.ianhook.android.ocrmanga;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class Highlighter extends LinearLayout {
    public Highlighter(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public static final String TAG = "Highlighter";
    
    public static final String TOP = "top";
    public static final String LEFT = "left";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";
    public static final String BACKGROUND_COLOR = "#50134BE8";
    public static final String BACKGROUND_COLOR_SELECTED = "#50E8B013";
    
    private Bitmap mBitmap;

    private View mRootView;
    private int left;
    private int top;

    /*
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Log.d(TAG, String.format("highlight "));
        mRootView = inflater.inflate(R.layout.fragment_highlighter,
                container, false);
        
        Bundle args = getArguments();
        left = args.getInt(LEFT);
        top = args.getInt(TOP);
        
        LayoutParams lp;
        lp = new LayoutParams(args.getInt(WIDTH), args.getInt(HEIGHT));
        //lp.setMargins(left, top, 0, 0);
        /*
        if (container != null) {
            lp = (LayoutParams) container.getLayoutParams();
            lp.width = args.getInt(WIDTH);
            lp.height = args.getInt(HEIGHT);
        } else {
            lp = new LayoutParams(args.getInt(WIDTH), args.getInt(HEIGHT));
        }* /
        
        updatePosition();
        mRootView.setLayoutParams(lp);
        //mRootView.layout(left, top, left + args.getInt(WIDTH), top + args.getInt(HEIGHT));
        
        setUnselected();
        
        Log.d(TAG, String.format("highlight x:%d, y:%d, w:%d, h:%d", 
                args.getInt(TOP), 
                args.getInt(LEFT), args.getInt(WIDTH), args.getInt(HEIGHT)));
        /*
        current.addView(newHighlight);
        
        
        position = args.getInt(ARG_OBJECT);
        * /
        
        LinearLayout highlighter = (LinearLayout) mRootView.findViewById(R.id.highlighter);
        highlighter.setOnClickListener(mHighlightClickListener);
        
        return mRootView;
    }
    */
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
    
    public void setSelected() {
        mRootView.setBackground(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR_SELECTED)));
    }
    
    public void setUnselected() {
        mRootView.setBackground(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));
    }

    private OnClickListener mHighlightClickListener = new OnClickListener() {
        
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            Log.d(TAG, "got click");
            /*
            RectF outRect = new RectF();
            Rect highlightRect = new Rect();
            v.getHitRect(highlightRect);
            */
            /*
            RectF bitmapRect = mImageView.getBitmapRect();
            Log.v(TAG, bitmapRect.toString());
            
            RectF highlightRectF = new RectF(highlightX,
                    highlightY,
                    highlightRect.width() + highlightX,
                    highlightRect.height() + highlightY
                    );                
       
            Matrix dmat = mImageView.getImageViewMatrix();
            
            Log.v(TAG, String.format("dmat %s", dmat.toString()));

            dmat.invert(dmat);
            dmat.mapRect(outRect, highlightRectF);
            outRect.left = (float) Math.max(0.0, outRect.left);
            outRect.top = (float) Math.max(0.0, outRect.top);
            
            outRect.bottom = (float) Math.min(mBitmap.getHeight(), outRect.bottom);
            outRect.right = (float) Math.min(mBitmap.getWidth(), outRect.right);

            Log.v(TAG, String.format("highlight %d,%d, %d,%d", (int)highlightRect.left,
                    (int)highlightRect.top,
                    (int)highlightRect.width(),
                    (int)highlightRect.height()));
            Log.v(TAG, String.format("outRect %f,%f, %f,%f", outRect.left, 
                    outRect.top,
                    outRect.width(), outRect.height()));

            Log.v(TAG, String.format("imageView %d,%d,%d,%d", mImageView.getLeft(), 
                    mImageView.getTop(), mImageView.getWidth(), mImageView.getHeight()));
            Log.v(TAG, String.format("bitmap %d,%d", mBitmap.getWidth(), mBitmap.getHeight()));

            mResizedBitmap = Bitmap.createBitmap(mBitmap, 
                    (int)outRect.left,
                    (int)outRect.top, 
                    (int)outRect.width(), 
                    (int)outRect.height());

            mImageView.setImageBitmap(mResizedBitmap, null, 1f, 8f);
            
            if(ocr != null) {
                CompletionCallback displayText = new DisplayText();
                ocr.setCompletionCallback(displayText);
                ocr.enqueue(mResizedBitmap);
            }
            
            View current = (View) v.getParent();
            LinearLayout highlighter = (LinearLayout) current.findViewById(R.id.highlighter);
            highlighter.setVisibility(View.GONE);
            */
        }
    };
    
}
