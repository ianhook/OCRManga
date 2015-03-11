package com.ianhook.android.ocrmanga;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.ianhook.android.ocrmanga.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GooDictionary extends ListView {
    private final static String TAG = "GOO";
    private View mHeader;
    
    public GooDictionary(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public GooDictionary(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    public GooDictionary(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
    
    static final String searchURI_all = "http://dictionary.goo.ne.jp/smp/ajsearch/all/%s/m0p1u/";
    static final String searchURI_jpn = "http://dictionary.goo.ne.jp/smp/ajsearch/jn2/%s/m0p1u/";
    // http://dictionary.goo.ne.jp/smp/leaf/jn2/268472/m0u/%E5%AD%97%E6%9B%B8/
    // 116487c
    
    public void setJapanese(CharSequence text) {

        Log.d(TAG, String.format("setting text: %s", text));
        if(mHeader == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHeader = inflater.inflate(R.layout.fragment_simple_list, null);
            addHeaderView(mHeader);
        } 
        TextView tt = (TextView) mHeader.findViewById(R.id.text1);
        tt.setText(text);
        new RetrieveDefinitionTask().execute(text);
    }
    
    private class RetrieveDefinitionTask extends AsyncTask<CharSequence, Void, String> {

        private DefinitionsAdapter mDefinitionsAdapter;
        
        protected String doInBackground(CharSequence... params) {
            HttpClient httpclient = new DefaultHttpClient();

            // Prepare a request object
            HttpGet httpget = new HttpGet();
            // Execute the request
            HttpResponse response;
            String result = "Unset";
            try {
                Log.d(TAG, "starting");
                String uri_string = String.format(searchURI_jpn, params[0]);
                Log.d(TAG, uri_string);
                
                URI uri = new URI(uri_string);
                httpget.setURI(uri);

                response = httpclient.execute(httpget);
                // Examine the response status
                Log.d(TAG,response.getStatusLine().toString());

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release

                if (entity != null) {
                    Log.d(TAG, "found something");

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    result = convertStreamToString(instream);
                    // now you have the string representation of the HTML request
                    instream.close();
                }


            } catch (Exception e) {
                Log.d(TAG, "error");
                result = e.getMessage();
                e.printStackTrace();
            }

            if(result != null ) {
                Log.d(TAG, result);
            } else {
                Log.d(TAG, "is null");
            }
            return result;
        }

        protected void onPostExecute(String definition) {
            JSONObject jObject;
            try {
                jObject = new JSONObject(definition);
                jObject = jObject.getJSONObject("jn2");
                if(mDefinitionsAdapter == null) {
                    mDefinitionsAdapter = new DefinitionsAdapter(jObject);
                    setAdapter(mDefinitionsAdapter);
                } else {
                    mDefinitionsAdapter.setDefinitions(jObject);
                    mDefinitionsAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }

    }
    
    private class DefinitionsAdapter extends BaseAdapter {
        
        private JSONObject mDefinitions;
        
        public DefinitionsAdapter(JSONObject defs) {
            super();
            setDefinitions(defs);
        }

        @Override
        public int getCount() {
            try {
                return mDefinitions.getJSONArray("LIST").length();
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public JSONObject getItem(int position) {
            try {
                return (JSONObject) mDefinitions.getJSONArray("LIST").get(position);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            try {
                return ((JSONObject)mDefinitions.getJSONArray("LIST").get(position)).getLong("url_id");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.fragment_simple_list, null);
            }
            
            JSONObject i = getItem(position);

            if (i != null) {

                TextView tt = (TextView) v.findViewById(R.id.text1);
                
                if (tt != null){
                    try {
                        tt.setText(String.format("%s - %s",i.getString("title_word"),i.getString("description")));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        tt.setText("JSON error");
                    }
                }
            }

            // the view must be returned to our activity
            return v;
        }

        public JSONObject getDefinitions() {
            return mDefinitions;
        }

        public void setDefinitions(JSONObject mDefinitions) {
            this.mDefinitions = mDefinitions;
        }
    }
    
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}