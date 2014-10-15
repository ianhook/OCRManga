package com.ianhook.android.ocrmanga;

import java.io.File;

import com.ianhook.myfirstapp.R;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FileBrowserActivity extends ActionBarActivity {
	private final static String TAG = "file browser";
	
	private File[] files;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browser);

	    Intent intent = getIntent();
	    String file_name = intent.getStringExtra(MainActivity.FILE_NAME);
	    
	    //TODO add filter to listFiles calls
	    if(file_name == null) {
	    	Log.d(TAG, "default dir");
            files = Environment.getExternalStorageDirectory().listFiles();
	    } else {
	    	Log.d(TAG, "dir: "+ file_name);
	    	files = new File(file_name).listFiles();
	    }
		ListView listView = (ListView) findViewById(R.id.listView1);
		/* TODO extend the ArrayAdapter class and override getView() to 
		 * return the type of view you want for each item.
		 */
		ListAdapter adapter = new ArrayAdapter<File>(this,
		        android.R.layout.simple_list_item_1, files);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(mMessageClickedHandler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_browser, menu);
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
	
	// Create a message handling object as an anonymous class.
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView parent, View v, int position, long id) {
	        // Do something in response to the click

	        Intent intent;
	        if(files[position].isDirectory()) {
	            intent = new Intent(FileBrowserActivity.this, FileBrowserActivity.class);
	        } else {
	            intent = new Intent(FileBrowserActivity.this, ImagePagerActivity.class);
	            
	        }
            intent.putExtra(MainActivity.FILE_NAME, files[position].getAbsolutePath());
            startActivity(intent);

	    }
	};

}
