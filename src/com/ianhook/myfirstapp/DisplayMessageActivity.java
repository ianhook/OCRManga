package com.ianhook.myfirstapp;

import java.io.File;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayMessageActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    // Get the message from the intent
	    Intent intent = getIntent();
	    String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
	    //message = this.getExternalFilesDir(null).getAbsolutePath();
	    // Create the text view
	    TextView textView = new TextView(this);
	    textView.setTextSize(40);
	    textView.setText(message);

		File root = this.getExternalFilesDir(null);
		//ImageView IV = (ImageView) findViewById(R.id.image);
		ImageView IV = new ImageView(this);
		Bitmap bMap = BitmapFactory.decodeFile(root.getAbsolutePath()+"/tessdata/005.jpg");
		IV.setImageBitmap(bMap);

	    // Set the text view as the activity layout
	    setContentView(textView);
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
