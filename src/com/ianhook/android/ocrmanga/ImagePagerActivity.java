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
import com.googlecode.eyesfree.textdetect.Thresholder;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.ianhook.android.ocrmanga.R;
import com.ianhook.android.ocrmanga.util.OcrGeneticDetection;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
    private ImageFragment mCurrentFragment;
    private static FragmentManager mFragManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //hide the action bar (menu options at top) 
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
        
        setContentView(R.layout.activity_image_pager);
        
        Intent intent = getIntent();
        String file_name = intent.getStringExtra(FILE_NAME);
        actionBar.setTitle(file_name);
        
        mFragManager = getSupportFragmentManager();
        mIPA = new ImagePagerAdapter(mFragManager);
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
            Log.d(TAG, "creating OCR");
            ocr = new Ocr(this, null);
            Parameters params = ocr.getParameters();
            params.setFlag(Parameters.FLAG_DEBUG_MODE, true);
            params.setFlag(Parameters.FLAG_ALIGN_TEXT, false);
            params.setLanguage("jpn");
            params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
            //params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            //Fitness: 20304
            params.setVariable("edge_tile_x", "30");
            params.setVariable("edge_tile_y", "32");
            params.setVariable("edge_thresh", "44");
            params.setVariable("edge_avg_thresh", "4");
            params.setVariable("single_min_aspect", "0.12387389438495032");
            params.setVariable("single_mix_aspect", "7.140991507983401");
            params.setVariable("single_min_area", "25");
            params.setVariable("single_min_density", "0.2814948034673991");
            params.setVariable("pair_h_ratio", "1.2320803151162179");
            params.setVariable("pair_d_ratio", "1.6700535687077367");
            params.setVariable("pair_h_dist_ratio", "1.2066236790559628");
            params.setVariable("pair_v_dist_ratio", "1.0045547320629222");
            params.setVariable("pair_h_shared", "0.14599912098744383");
            params.setVariable("cluster_width_spacing", "3");
            params.setVariable("cluster_shared_edge", "0.8526428970173985");
            params.setVariable("cluster_h_ratio", "0.28984088586226053");
            params.setVariable("cluster_min_blobs", "2");
            params.setVariable("cluster_min_aspect", "1.78901587228034");
            params.setVariable("cluster_min_fdr", "3.0215533597321147");
            params.setVariable("cluster_min_edge", "35");
            params.setVariable("cluster_min_edge_avg", "35");

            ocr.setParameters(params);
        }
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        } else if (id == R.id.action_highlight) {
            mCurrentFragment.findText();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void menuForFragment(ImageFragment v) {

        getActionBar().show();
        mCurrentFragment = v;
        
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
            
            //return getThreshed(bm);
            return bm;
        }
        
        @SuppressWarnings("unused")
        private static Bitmap getThreshed(Bitmap bm) {
            // this is the one that we actually use
            //return WriteFile.writeBitmap(Thresholder.edgeAdaptiveThreshold(ReadFile.readBitmap(bm)));
            //return WriteFile.writeBitmap(Thresholder.edgeAdaptiveThreshold(ReadFile.readBitmap(bm), 100, 100, 32, 1));
            
            //causes blocks of differently thresholded areas
            //return WriteFile.writeBitmap(Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bm)));
            //return WriteFile.writeBitmap(Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bm), 1, 1));
            
            //makes outlines around things, small letters become too bold
            return WriteFile.writeBitmap(Thresholder.sobelEdgeThreshold(ReadFile.readBitmap(bm)));
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
        
        @SuppressWarnings("unused")
        private Highlighter displayHighlight(Rect bounds) {
            return displayHighlight(mImageView, bounds.left, bounds.top, 
                    bounds.width(), bounds.height());
        }
        
        @SuppressLint("NewApi")
        private Highlighter displayHighlight(View v, int x, int y, int width, int height) {
            //FrameLayout current = (FrameLayout) v.getParent();
            
            Highlighter newHighlight = new Highlighter(getActivity());
            newHighlight.setLayoutParams(new LayoutParams(width, height));
            newHighlight.setX(x * mImageView.getScale() * mImageView.getBaseScale() + v.getX());
            newHighlight.setY(y * mImageView.getScale() * mImageView.getBaseScale()  + v.getY());
            
            return newHighlight;          
            
        }
        
        public void findText() {
            CompletionCallback displayText = new DisplayText();
            ocr.setCompletionCallback(displayText);
            ocr.enqueue(mBitmap);
        }
    
        private OnLongClickListener mLongClickListener = new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "Long Click caught");

                ImagePagerActivity IPA = (ImagePagerActivity) getActivity();
                if(IPA.getActionBar().isShowing()) {
                    IPA.getActionBar().hide();
                } else {
                    IPA.menuForFragment(ImageFragment.this);
                }
                    
                return true;
            }   
        };
        /*
        private OnTouchListener mTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_UP) {
                    View current = (View) v.getParent();
                    LinearLayout highlighter = (LinearLayout) current.findViewById(R.id.highlighter);
                
                    if( highlighter.getVisibility() == View.GONE) {
                        highlightX = e.getX();
                        highlightY = e.getY();
                        //highlightX = 456.438f + 150.0f;
                        //highlightY = 166.572f + 150.0f;
                    }
                    Log.v(TAG, String.format("touch %f,%f", highlightX, highlightY));
                    v.performClick();
                }
                return false;
            }
        
        };*/
        private class DisplayText implements CompletionCallback {
        
            @Override
            public void onCompleted(List<OcrResult> results) {                
                
                //String message = "";
                //Intent intent = new Intent(ImageFragment.this.getActivity(), DisplayMessageActivity.class);
                Log.d("DisplayText", "got some results");
                if(results.isEmpty()) {
                    //message = "I was afraid of this.";
                } else {

                    //FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                    for(int i = 0; i < results.size(); i++) {
                        //message += results.get(i).getString() + "\n";
                        if(results.get(i).getBounds().top == 0) {
                            continue;
                        }

                        displayHighlight(results.get(i).getBounds());
                        //ft.add(mRootView.getId(), displayHighlight(results.get(i).getBounds()), String.format("h%d", i));
                        Log.d("DisplayText", results.get(i).getBounds().flattenToString());
                        Log.d("DisplayText", results.get(i).getString());

                    }
                    //ft.commit();           
                }
                //intent.putExtra(ImagePagerActivity.EXTRA_MESSAGE, message);
                //intent.putExtra(ImagePagerActivity.FILE_NAME, "");
                //intent.putExtra(ImagePagerActivity.BITMAP, mResizedBitmap);
        
                //startActivity(intent);
                
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
            //mImageView.setOnTouchListener(mTouchListener);
            
            return mRootView;
        }
        
        private void drawImage() {

            if(mBitmap == null || mBitmap.isRecycled()) {
                if(mScale == -1)
                    mScale = ImagePagerAdapter.getScale(position);
                mBitmap = ImagePagerAdapter.getImage(position, mScale);
                mImageView.setImageBitmap(mBitmap, null, 1f, 8f);
                //TODO fix initial image display
                //works with smaller images
                //mImageView.setDisplayType(DisplayType.NONE);
                //work with larger images
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

            getActivity().getActionBar().hide();
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
