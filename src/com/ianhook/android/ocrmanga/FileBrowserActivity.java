package com.ianhook.android.ocrmanga;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.ianhook.myfirstapp.R;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowserActivity extends ActionBarActivity {
	private final static String TAG = "file browser";
	private final static String PREFS = "com.ianhook.ocrmanga.FileBrowserActvity";
	private final static String LAST_FILE_PREF = "last_file";
	
	private ListAdapter mAdapter;
	
	private class FileAdapter extends ArrayAdapter<File> {

	    private File[] objects;

	    public FileAdapter(Context context, int textViewResourceId, File[] files) {
	        super(context, textViewResourceId, files);
	        Arrays.sort(files);
	        this.objects = files;
	    }
	    
	    public int getCount(){
	        return objects.length + 1;
	    }
	    
	    public File getItem(int position) {
	        if(position == 0)
	            return objects[0].getParentFile().getParentFile();
            return objects[position - 1];
	    }

	    public View getView(int position, View convertView, ViewGroup parent){

	        // assign the view we are converting to a local variable
	        View v = convertView;

	        // first check to see if the view is null. if so, we have to inflate it.
	        // to inflate it basically means to render, or show, the view.
	        if (v == null) {
	            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = inflater.inflate(R.layout.fragment_simple_list, null);
	        }

	        /*
	         * Recall that the variable position is sent in as an argument to this method.
	         * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
	         * iterates through the list we sent it)
	         * 
	         * Therefore, i refers to the current Item object.
	         */
	        File i = getItem(position);

	        if (i != null) {

	            // This is how you obtain a reference to the TextViews.
	            // These TextViews are created in the XML files we defined.

	            TextView tt = (TextView) v.findViewById(R.id.text1);
	            Log.d(TAG, i.getName());

	            // check to see if each individual textview is null.
	            // if not, assign some text!
	            if (tt != null){
	                if(position != 0)
	                    tt.setText(i.getName());
	                else 
                        tt.setText("..");
	            }
	        }

	        // the view must be returned to our activity
	        return v;
	    }
	} 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browser);

        Intent intent = getIntent();
		String file_name = intent.getStringExtra(ImagePagerActivity.FILE_NAME);
	    
	    //TODO add filter to listFiles calls
	    if(file_name == null) {
	    	Log.d(TAG, "initialize file browser");
	        SharedPreferences settings = getSharedPreferences(PREFS, 0);
	        file_name = settings.getString(LAST_FILE_PREF, Environment.getExternalStorageDirectory().getAbsolutePath());
	    }
	    
    	Log.d(TAG, "file name: "+ file_name);
    	File temp_file = new File(file_name);
        File directory;
    	if(temp_file.isDirectory()) {
    	    directory = temp_file;
    	} else {
    	    directory = temp_file.getParentFile();
    	}
        File[] files = directory.listFiles();
        
        ListView listView = (ListView) findViewById(R.id.listView1);
        /* TODO extend the ArrayAdapter class and override getView() to 
         * return the type of view you want for each item.
         */
        mAdapter = new FileAdapter(this,
                R.layout.fragment_simple_list, files);
        listView.setAdapter(mAdapter);
        
        listView.setOnItemClickListener(mMessageClickedHandler);
        if(files.length > 0) {
            setTitle(new File(file_name).getPath());
        }
        
        if(temp_file.isFile()) {
            openManga(temp_file);
        }
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
	        File file = (File) mAdapter.getItem(position);
	        if(file.isDirectory()) {
	            intent = new Intent(FileBrowserActivity.this, FileBrowserActivity.class);
	            intent.putExtra(ImagePagerActivity.FILE_NAME, file.getAbsolutePath());
	            startActivity(intent);
	        } else {
	            openManga(file);
	        }

	        SharedPreferences settings = getSharedPreferences(PREFS, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString(LAST_FILE_PREF, file.getAbsolutePath());

	        // Commit the edits!
	        editor.commit();
	    }
	};
	
	private void openManga(File file) {
        Intent intent = new Intent(FileBrowserActivity.this, ImagePagerActivity.class);
        intent.putExtra(ImagePagerActivity.FILE_NAME, file.getAbsolutePath());
        
        startActivity(intent);
	    
	}

}
