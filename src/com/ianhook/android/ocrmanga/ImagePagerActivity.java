package com.ianhook.android.ocrmanga;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.ianhook.myfirstapp.R;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImagePagerActivity extends ActionBarActivity {

	public static final String TAG = "ImagePagerActivity";
    private ImagePagerAdapter mIPA;
    private ViewPager mViewPager;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);
        
        Intent intent = getIntent();
        String file_name = intent.getStringExtra(MainActivity.FILE_NAME);
		
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
        mViewPager.setCurrentItem(mIPA.getCount());
        
        /*
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new ImageFragment()).commit();
		}
		*/
	}
    
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_pager, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
                
                Log.d(ImagePagerActivity.TAG, String.format("scale: %d, s_x:%d, s_y:%d, h:%d, w:%d, %d",
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
		private ImageView mImageView;
		private Bitmap mBitmap;
		private int mScale = -1;

		public ImageFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_image_pager,
					container, false);
			
			Bundle args = getArguments();
			position = args.getInt(ARG_OBJECT);
			
			mImageView = (ImageView) rootView.findViewById(R.id.imageView);
			
			return rootView;
		}
		
		private void drawImage() {

		    if(mBitmap == null || mBitmap.isRecycled()) {
		        if(mScale == -1)
		            mScale = ImagePagerAdapter.getScale(position);
		        mBitmap = ImagePagerAdapter.getImage(position, mScale);
                mImageView.setImageBitmap(mBitmap);
                Log.v(TAG, String.format("image set %d", position));
		    }
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
	        //final int resId = ImageDetailActivity.imageResIds[mImageNum];
	        //mImageView.setImageResource(resId); // Load image into ImageView
	        Log.v(TAG, String.format("act create %d", position));
	        //drawImage();
	    }
	    
        @Override
        public void onStart() {
            super.onStart();
            //final int resId = ImageDetailActivity.imageResIds[mImageNum];
            //mImageView.setImageResource(resId); // Load image into ImageView

            Log.v(TAG, String.format("start %d", position));
            //drawImage();
        }
        
        @Override
        public void onResume() {
            super.onResume();
            //final int resId = ImageDetailActivity.imageResIds[mImageNum];
            //mImageView.setImageResource(resId); // Load image into ImageView

            Log.v(TAG, String.format("resume %d", position));
            drawImage();
        }

        @Override
        public void onPause() {
            super.onPause();
            //final int resId = ImageDetailActivity.imageResIds[mImageNum];
            //mImageView.setImageResource(resId); // Load image into ImageView

            Log.v(TAG, String.format("pause %d", position));
            deleteImage();
        }

        @Override
        public void onStop() {
            super.onStop();
            //final int resId = ImageDetailActivity.imageResIds[mImageNum];
            //mImageView.setImageResource(resId); // Load image into ImageView

            Log.v(TAG, String.format("stop %d", position));
            deleteImage();
        }
	}
}
