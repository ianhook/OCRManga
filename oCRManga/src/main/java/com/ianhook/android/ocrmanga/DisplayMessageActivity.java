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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
    public final static String TAG = "com.ianhook.DMA";
    public final static String EXTRA_MESSAGE = "com.ianhook.android.ocrmanga.MESSAGE";
    public final static String FILE_NAME = "com.ianhook.android.ocrmanga.FILE_NAME";
    public final static String BITMAP = "com.ianhook.android.ocrmanga.BITMAP";
    public final static String HIGHLIGHT = "com.ianhook.android.ocrmanga.HIGHLIGHT";
    public final static String HIGHLIGHT_ID = "com.ianhook.android.ocrmanga.HIGHLIGHT_ID";

    public final static int UPDATE_HIGHLIGHT = 0;
    public final static int NEXT = 1;
    public final static int PREVIOUS = 2;
    public final static int DELETE = 3;

    private final static long TOP = 0;
    private final static long BOTTOM = 1;
    private final static long LEFT = 2;
    private final static long RIGHT = 3;
    private final static long MOVE_UP = 0;
    private final static long MOVE_DOWN = 1;
    private final static long MOVE_LEFT = 2;
    private final static long MOVE_RIGHT = 3;


    private boolean mHighlighted = false;
    private int mHighlight_ID;

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
        mHighlight_ID = intent.getIntExtra(HIGHLIGHT_ID, 0);

        // Create the text view
        TextView textView = (TextView) findViewById(R.id.textView1);
        //textView.setTextSize(40);
        textView.setText(message);

        ImageView IV = (ImageView) findViewById(R.id.imageView1);
        if(file_name.length() > 0) {
            IV.setImageURI(Uri.fromFile(new File(file_name)));
            Log.d(TAG, String.format("from file: '%s'", file_name));
        } else {
            IV.setImageBitmap(resizedBitmap);
            Log.d(TAG, "from intent");
        }

        if(highlight != null) {
            mHighlighted = true;
            Log.d(TAG, "highlight");
            Log.d(TAG, highlight.toString());
            LinearLayout HV = (LinearLayout) findViewById(R.id.padded_highlighter);
            HV.setVisibility(View.VISIBLE);
            HV.setX(highlight.left);
            HV.setY(highlight.top);
            ViewGroup.LayoutParams params = HV.getLayoutParams();
            params.height = (int) (highlight.height() * IV.getScaleY());
            params.width = (int) (highlight.width() * IV.getScaleX());

            Log.d(TAG, String.format("params %f, %f, %d, %d", highlight.left * IV.getScaleX(),
                    highlight.top * IV.getScaleY(),
                    params.width, params.height));
            setUpButtons();
        }

        // Set the text view as the activity layout
        //setContentView(textView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(mHighlighted) {
            getMenuInflater().inflate(R.menu.display_message, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_edit) {

            findViewById(R.id.gooDict).setVisibility(View.GONE);
            findViewById(R.id.edit_highlighter).setVisibility(View.VISIBLE);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void editHighlighter(long direction) {
        Spinner mySpinner=(Spinner) findViewById(R.id.spinner);
        long corner = mySpinner.getSelectedItemId();

        LinearLayout HV = (LinearLayout) findViewById(R.id.padded_highlighter);
        ViewGroup.LayoutParams params = HV.getLayoutParams();
        if(corner == TOP || corner == LEFT) {
            if(direction == MOVE_UP) {
                HV.setY(HV.getY() - 1);
                params.height += 1;
            } else if(direction == MOVE_DOWN) {
                HV.setY(HV.getY() + 1);
                params.height -= 1;
            } else if(direction == MOVE_LEFT) {
                HV.setX(HV.getX() - 1);
                params.width += 1;
            } else if(direction == MOVE_RIGHT) {
                HV.setX(HV.getX() + 1);
                params.width -= 1;
            }
        } else if(corner == BOTTOM || corner == RIGHT) {
            if(direction == MOVE_UP) {
                params.height -= 1;
            } else if(direction == MOVE_DOWN) {
                params.height += 1;
            } else if(direction == MOVE_LEFT) {
                params.width -= 1;
            } else if(direction == MOVE_RIGHT) {
                params.width += 1;
            }
        }
        HV.requestLayout();
    }

    private void setUpButtons() {
        View.OnClickListener ocl = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.getId() == R.id.button_right) {
                    editHighlighter(MOVE_RIGHT);
                } else if (v.getId() == R.id.button_left) {
                    editHighlighter(MOVE_LEFT);
                } else if (v.getId() == R.id.button_up) {
                    editHighlighter(MOVE_UP);
                } else if (v.getId() == R.id.button_down) {
                    editHighlighter(MOVE_DOWN);
                }
            }
        };
        ImageButton button = (ImageButton) findViewById(R.id.button_right);
        button.setOnClickListener(ocl);
        button = (ImageButton) findViewById(R.id.button_left);
        button.setOnClickListener(ocl);
        button = (ImageButton) findViewById(R.id.button_up);
        button.setOnClickListener(ocl);
        button = (ImageButton) findViewById(R.id.button_down);
        button.setOnClickListener(ocl);

        View.OnClickListener controls= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra(HIGHLIGHT_ID, mHighlight_ID);
                int result = -1;

                if(v.getId() == R.id.button_save) {
                    LinearLayout HV = (LinearLayout) findViewById(R.id.padded_highlighter);
                    RectF temp = new RectF(HV.getX(), HV.getY(), HV.getWidth() + HV.getX(), HV.getHeight() + HV.getY());
                    Log.d(TAG, temp.toString());
                    newIntent.putExtra(HIGHLIGHT, temp);
                    result = UPDATE_HIGHLIGHT;
                } else if(v.getId() == R.id.button_next) {
                    result = NEXT;
                } else if(v.getId() == R.id.button_previous) {
                    result = PREVIOUS;
                } else if(v.getId() == R.id.button_delete) {
                    result = DELETE;
                }
                setResult(result, newIntent);
                finish();
            }
        };

        Button ob = (Button) findViewById(R.id.button_save);
        ob.setOnClickListener(controls);
        ob = (Button) findViewById(R.id.button_next);
        ob.setOnClickListener(controls);
        ob = (Button) findViewById(R.id.button_previous);
        ob.setOnClickListener(controls);
        ob = (Button) findViewById(R.id.button_delete);
        ob.setOnClickListener(controls);
    }

}
