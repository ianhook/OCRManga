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
	public final static String FILE_NAME = "com.ianhook.myfirstapp.FILE_NAME";
	public final static String TRANSLATION = "com.ianhook.myfirstapp.TRANSLATION";
	
	private static Ocr ocr;
	
	private String file_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		file_name = this.getExternalFilesDir(null).getAbsoluteFile()+"/005_text.jpg";
		EditText editText = (EditText) findViewById(R.id.edit_message);
		editText.setText(file_name);

		if (savedInstanceState == null) {
			ocr = new Ocr(this, null);
			Parameters params = ocr.getParameters();
			params.setFlag(Parameters.FLAG_DEBUG_MODE, false);
			params.setFlag(Parameters.FLAG_ALIGN_TEXT, false);
			params.setLanguage("jpn");
			params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
			//params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
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
	

	private class DisplayText implements CompletionCallback {
		public final static String EXTRA_MESSAGE = "com.ianhook.myfirstapp.MESSAGE";
	
		@Override
		public void onCompleted(List<OcrResult> results) {
			
			String message = "";
			Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
			Log.d("sendMessage", "got some results");
			if(results.isEmpty()) {
				message = "I was afraid of this.";
			} else {
				for(int i = 0; i < results.size(); i++)
					message += results.get(i).getString();
			}
		    intent.putExtra(EXTRA_MESSAGE, message);
			intent.putExtra(FILE_NAME, file_name);
	
			startActivity(intent);
			
		}
		
	}
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		
		file_name = this.getExternalFilesDir(null).getAbsoluteFile()+"/005_text.jpg";
		Bitmap bMap = BitmapFactory.decodeFile(file_name);
		
		CompletionCallback displayText = new DisplayText();
		ocr.setCompletionCallback(displayText);
		ocr.enqueue(bMap);
	}
	
	public void openSettings() {
		return;
	}
	
	public void openSearch() {

		Intent intent = new Intent(this, FileBrowserActivity.class);
		Log.d("sendMessage", "search clicked");
		startActivity(intent);
	}
}

