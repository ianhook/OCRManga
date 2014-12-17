package com.ianhook.android.ocrmanga;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.OcrResult;
import com.googlecode.eyesfree.ocr.client.Ocr.CompletionCallback;
import com.googlecode.eyesfree.ocr.client.Ocr.Parameters;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.ianhook.android.ocrmanga.R;
import com.ianhook.android.ocrmanga.util.OcrGeneticDetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class ImagePagerActivity extends FragmentActivity {
    public final static String EXTRA_MESSAGE = "com.ianhook.android.ocrmanga.MESSAGE";
    public final static String FILE_NAME = "com.ianhook.android.ocrmanga.FILE_NAME";
    public final static String BITMAP = "com.ianhook.android.ocrmanga.BITMAP";
    public final static String TRANSLATION = "com.ianhook.android.ocrmanga.TRANSLATION";
    private final static String PAGE_NUM = "page";

	public static final String TAG = "ImagePagerActivity";
    private ImagePagerAdapter mIPA;
    private ViewPager mViewPager;
    private static Ocr ocr;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);
        
        Intent intent = getIntent();
        String file_name = intent.getStringExtra(FILE_NAME);
		
		mIPA = new ImagePagerAdapter(getSupportFragmentManager());
		try {
		    mIPA.setFile(file_name);
            mIPA.setScreen(recordDisplaySize());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mIPA);
        mViewPager.setBackgroundColor(Color.BLACK);

        SharedPreferences settings = getSharedPreferences(mIPA.getFileName(), 0);
        mViewPager.setCurrentItem(settings.getInt(PAGE_NUM, mIPA.getCount()));

        if (savedInstanceState == null && !OcrGeneticDetection.mDoGA) {
            ocr = new Ocr(this, null);
            Parameters params = ocr.getParameters();
            params.setFlag(Parameters.FLAG_DEBUG_MODE, true);
            params.setFlag(Parameters.FLAG_ALIGN_TEXT, false);
            params.setLanguage("jpn");
            params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
            //params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            ocr.setParameters(params);
        }
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
    
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(mIPA.getFileName(), 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putInt(PAGE_NUM, mViewPager.getCurrentItem());

      // Commit the edits!
      editor.commit();
    }
    
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point recordDisplaySize() {

        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point()  ;
        
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenSize);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(screenSize);
        } else {
            screenSize = new Point(display.getWidth(), display.getHeight());
        }
        return screenSize;
    }
	
	public static class ImagePagerAdapter extends FragmentStatePagerAdapter {
	    
	    private static ZipFile mZipFile;
	    private static int mCount;
	    private static Point mScreenSize;

		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}
		
		public void setScreen(Point point) {
		    mScreenSize = point;
		}

	    public void setFile(String file_name) throws ZipException, IOException {
	        Log.v(TAG, "loading file " + file_name);
	        setFile(new File(file_name));
	        
	    }

	    public void setFile(File file) throws ZipException, IOException {
	        mZipFile = new ZipFile(file);
	        mCount = Collections.list(mZipFile.entries()).size();
	        Log.v(TAG, String.format("%d images found", mCount));
	    }
	    
	    public String getFileName() {
	        File file = new File(mZipFile.getName());
	        return file.getName();
	    }
	    
		@Override
		public Fragment getItem(int arg0) {
            Log.v(TAG, String.format("setting image %d", arg0));
		    
            Fragment fragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putInt(ImageFragment.ARG_OBJECT, getCount() - arg0);
            
            fragment.setArguments(args);
			
			// TODO Auto-generated method stub
	        // Our object is just an integer :-P
	        return fragment;
		}

		@Override
		public int getCount() {
			return mZipFile.size();
		}

	    @Override
	    public CharSequence getPageTitle(int position) {
            ArrayList<? extends ZipEntry> imageArray = Collections.list(mZipFile.entries());
            
	        return "file: " + imageArray.get(position).getName();
	    }
	    
        static private Bitmap getImage(int position, int scale) {
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
            return bm;
	    }
        
        private static int getScale(int position) {
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
                
                Log.v(ImagePagerActivity.TAG, String.format("scale: %d, s_x:%d, s_y:%d, h:%d, w:%d, %d",
                        scale, mScreenSize.x, mScreenSize.y, options.outHeight, options.outWidth, scaleHeight));
                
            
            } catch (IOException e) {
                scale = 1;
                Log.e(TAG, e.getMessage());
            }
            return scale;
        }
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ImageFragment extends Fragment { 
		public static final String ARG_OBJECT = "int";
		
		private int position;
		private ImageViewTouch mImageView;
		private Bitmap mBitmap;
		private Bitmap mResizedBitmap;
		private int mScale = -1;
		private View mRootView;
		private float highlightX;
        private float highlightY;
    
        private OnLongClickListener mLongClickListener = new OnLongClickListener() {

            @SuppressLint("NewApi")
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "Long Click caught");
                View current = (View) v.getParent();
                LinearLayout highlighter = (LinearLayout) current.findViewById(R.id.highlighter);
                if( highlighter.getVisibility() == View.GONE) {
                    highlighter.setVisibility(View.VISIBLE);
                } else {
                    highlighter.setVisibility(View.GONE);
                }
                
                int width = highlighter.getWidth() == 0 ? 300 : highlighter.getWidth();
                int height = highlighter.getHeight() == 0 ? 300 : highlighter.getHeight();
                
                highlightX = (float) Math.max(0.0, highlightX - (float)width / 2.0); 
                highlightY = (float) Math.max(0.0, highlightY - (float)height / 2.0);
                
                highlighter.setX(highlightX + v.getX());
                highlighter.setY(highlightY + v.getY());
                Log.d(TAG, String.format("x:%f, y:%f", highlightX, highlightY));
                
                return true;
            }   
        };
        
        private OnTouchListener mTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                View current = (View) v.getParent();
                LinearLayout highlighter = (LinearLayout) current.findViewById(R.id.highlighter);
            
                if( highlighter.getVisibility() == View.GONE) {
                    highlightX = e.getX();
                    highlightY = e.getY();
                    //highlightX = 456.438f + 150.0f;
                    //highlightY = 166.572f + 150.0f;
                }
                Log.v(TAG, String.format("touch %f,%f", highlightX, highlightY));
                
                return false;
            }
        };
        
        private OnClickListener mHighlightClickListener = new OnClickListener() {
            
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                RectF outRect = new RectF();
                Rect highlightRect = new Rect();
                v.getHitRect(highlightRect);
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

                mImageView.setImageBitmap(mResizedBitmap, null, -1, 8f);
                
                if(ocr != null) {
                    CompletionCallback displayText = new DisplayText();
                    ocr.setCompletionCallback(displayText);
                    ocr.enqueue(mResizedBitmap);
                }
                
                View current = (View) v.getParent();
                LinearLayout highlighter = (LinearLayout) current.findViewById(R.id.highlighter);
                highlighter.setVisibility(View.GONE);
                
            }
        };
        
        private class DisplayText implements CompletionCallback {
        
            @Override
            public void onCompleted(List<OcrResult> results) {
                
                String message = "";
                Intent intent = new Intent(ImageFragment.this.getActivity(), DisplayMessageActivity.class);
                Log.d("sendMessage", "got some results");
                if(results.isEmpty()) {
                    message = "I was afraid of this.";
                } else {
                    for(int i = 0; i < results.size(); i++)
                        message += results.get(i).getString() + "\n";
                }
                intent.putExtra(ImagePagerActivity.EXTRA_MESSAGE, message);
                intent.putExtra(ImagePagerActivity.FILE_NAME, "");
                intent.putExtra(ImagePagerActivity.BITMAP, mResizedBitmap);
        
                startActivity(intent);
                
            }
            
        }

        public ImageFragment() {
        }
        
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mRootView = inflater.inflate(R.layout.fragment_image_pager,
					container, false);
			
			Bundle args = getArguments();
			position = args.getInt(ARG_OBJECT);
			
			mImageView = (ImageViewTouch) mRootView.findViewById(R.id.imageView);
            mImageView.setOnLongClickListener(mLongClickListener);
            mImageView.setOnTouchListener(mTouchListener);
            
            LinearLayout highlighter = (LinearLayout) mRootView.findViewById(R.id.highlighter);
            highlighter.setOnClickListener(mHighlightClickListener);
			
			return mRootView;
		}
		
		private void drawImage() {

		    if(mBitmap == null || mBitmap.isRecycled()) {
		        if(mScale == -1)
		            mScale = ImagePagerAdapter.getScale(position);
		        mBitmap = ImagePagerAdapter.getImage(position, mScale);
                mImageView.setImageBitmap(mBitmap, null, -1, 8f);
                mImageView.setDisplayType(DisplayType.FIT_TO_SCREEN);
                
                Log.v(TAG, String.format("image set %d", position));
                Log.v(TAG, String.format("image dims %d,%d", mBitmap.getWidth(), mBitmap.getHeight()));
                
		    }
		    ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.progress);
            progress.setVisibility(View.GONE);
		}
		
        private void deleteImage() {

            if(mBitmap != null && !mBitmap.isRecycled()) {
                mImageView.setImageBitmap(null);
                mBitmap.recycle();
                Log.v(TAG, String.format("image delete %d", position));
            }
        }

	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        
	        Log.v(TAG, String.format("act create %d", position));
	    }
	    
        @Override
        public void onStart() {
            super.onStart();
            
            Log.v(TAG, String.format("start %d", position));
        }
        
        @Override
        public void onResume() {
            super.onResume();
            
            Log.v(TAG, String.format("resume %d", position));
            drawImage();
        }

        @Override
        public void onPause() {
            super.onPause();
            
            Log.v(TAG, String.format("pause %d", position));
            deleteImage();
        }

        @Override
        public void onStop() {
            super.onStop();
            
            Log.v(TAG, String.format("stop %d", position));
            deleteImage();
        }
	}
}
