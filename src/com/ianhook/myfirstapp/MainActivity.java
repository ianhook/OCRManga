package com.ianhook.myfirstapp;

import java.util.List;

import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.Ocr.CompletionCallback;
import com.googlecode.eyesfree.ocr.client.Ocr.Parameters;
import com.googlecode.eyesfree.ocr.client.OcrResult;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {
	public final static String EXTRA_MESSAGE = "com.ianhook.myfirstapp.MESSAGE";
	public final static String TRANSLATION = "com.ianhook.myfirstapp.TRANSLATION";
	
	private static Ocr ocr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (ocr == null) {
			ocr = new Ocr(this, null);
			Parameters params = ocr.getParameters();
			params.setFlag(Parameters.FLAG_DEBUG_MODE, true);
			params.setLanguage("jpn+eng");
			//params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
			params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
			ocr.setParameters(params);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
	    
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_search:
	            openSearch();
	            return true;
	        case R.id.action_settings:
	            openSettings();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		
		String root = this.getExternalFilesDir(null).getAbsoluteFile()+"/005_text.jpg";
		Log.d("sendMessage", root);
		//ImageView IV = (ImageView) findViewById(R.id."image view");
		Bitmap bMap = BitmapFactory.decodeFile(root);
		//IV.setImageBitmap(bMap);
		
		CompletionCallback displayText = new DisplayText();
		ocr.setCompletionCallback(displayText);
		ocr.enqueue(bMap);
		Log.d("sendMessage", "made it");
		
		// Do something in response to button
		Intent intent = new Intent(this, DisplayMessageActivity.class);
				
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		//String message = editText.getText().toString();
				
		intent.putExtra(EXTRA_MESSAGE, "hi");
		//intent.putExtra(TRANSLATION, message);
				
		//startActivity(intent);
	}
	
	private void displayText() {
	    // Do something in response to button
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		
		EditText editText = (EditText) findViewById(R.id.edit_message);
		String message = editText.getText().toString();
		
	    intent.putExtra(EXTRA_MESSAGE, message);
		intent.putExtra(TRANSLATION, message);
		
		startActivity(intent);
		
	}
	
	public void openSettings() {
		return;
	}
	
	public void openSearch() {
		return;	
	}
}

class DisplayText implements CompletionCallback {
	public final static String EXTRA_MESSAGE = "com.ianhook.myfirstapp.MESSAGE";

	@Override
	public void onCompleted(List<OcrResult> results) {
		
		String message;
		Intent intent = new Intent("com.ianhook.myfirstapp.DisplayMessageActivity");
		if(results.isEmpty()) {
			message = "I was afraid of this.";
		} else {
			message = results.get(0).getString();
		}
	    intent.putExtra(EXTRA_MESSAGE, message);
		
		
		
	}
	
}
