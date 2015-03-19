package com.ianhook.android.ocrmanga;

import java.io.File;

import com.ianhook.android.ocrmanga.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.ianhook.android.ocrmanga.MESSAGE";
    public final static String FILE_NAME = "com.ianhook.android.ocrmanga.FILE_NAME";
    public final static String BITMAP = "com.ianhook.android.ocrmanga.BITMAP";
    public final static String HIGHLIGHT = "com.ianhook.android.ocrmanga.HIGHLIGHT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);

	    // Get the message from the intent
	    Intent intent = getIntent();
	    String message = intent.getStringExtra(EXTRA_MESSAGE);
	    String file_name = intent.getStringExtra(FILE_NAME);
        Bitmap resizedBitmap = (Bitmap) intent.getParcelableExtra(BITMAP);
        RectF highlight = (RectF) intent.getParcelableExtra(HIGHLIGHT);

	    // Create the text view
	    TextView textView = (TextView) findViewById(R.id.textView1);
	    //textView.setTextSize(40);
	    textView.setText(message);

		ImageView IV = (ImageView) findViewById(R.id.imageView1);
		if(file_name.length() > 0) {
		    IV.setImageURI(Uri.fromFile(new File(file_name)));
		    Log.d(ImagePagerActivity.TAG, String.format("from file: '%s'", file_name));
		} else {
		    IV.setImageBitmap(resizedBitmap);
            Log.d(ImagePagerActivity.TAG, "from intent");
		}
        if(highlight != null) {
            LinearLayout HV = (LinearLayout) findViewById(R.id.padded_highlighter);
            HV.setVisibility(View.VISIBLE);
            HV.setX(highlight.left);
            HV.setLayoutParams(new ViewGroup.LayoutParams((int) highlight.width(), (int) highlight.height()));
        }

	    // Set the text view as the activity layout
	    //setContentView(textView);
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}
*/
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
}
