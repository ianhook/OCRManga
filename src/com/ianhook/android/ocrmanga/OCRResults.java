package com.ianhook.android.ocrmanga;

import com.ianhook.myfirstapp.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class OCRResults extends EditText {
    private final static String TAG = "OCRResults";

    private final Context context;
    private CharSequence mSave; 

    /*
        Just the constructors to create a new EditText...
     */
    public OCRResults(Context context) {
        super(context);
        this.context = context;
    }

    public OCRResults(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public OCRResults(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }
    
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        mSave = text;
    }

    /**
     * <p>This is where the "magic" happens.</p>
     * <p>The menu used to cut/copy/paste is a normal ContextMenu, which allows us to
     *  overwrite the consuming method and react on the different events.</p>
     * @see <a href="http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3_r1/android/widget/TextView.java#TextView.onTextContextMenuItem%28int%29">Original Implementation</a>
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        Log.d(TAG, "Got an event");
        // Do your thing:
        // React:
        boolean consumed = true;
        switch (id){
            case android.R.id.cut:
                onTextCut();
                break;
            case android.R.id.paste:
                onTextPaste();
                break;
            case android.R.id.copy:
                onTextCopy();
                break;
            default:
                consumed = super.onTextContextMenuItem(id);
        }
        return consumed;
    }

    /**
     * Text was cut from this EditText.
     */
    public void onTextCut(){
        Toast.makeText(context, "Cut!", Toast.LENGTH_SHORT).show();
        setText(mSave);
    }

    /**
     * Text was copied from this EditText.
     */
    public void onTextCopy(){
        Log.d(TAG, "copying");
        try {
            int min = 0;
            int max = getText().length();
            final int selStart = getSelectionStart();
            final int selEnd = getSelectionEnd();
            
            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
            Log.d(TAG, String.format("-- %d, %d", selStart, selEnd));
            Log.d(TAG, String.format("-- %d, %d", min, max));
    
            GooDictionary v = (GooDictionary)((View)this.getParent()).findViewById(R.id.gooDict);
            String trans = getText().subSequence(min, max).toString();
            v.setJapanese(trans);
            hideSoftKeyboard();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Text was pasted into the EditText.
     */
    public void onTextPaste(){
        Toast.makeText(context, "Paste!", Toast.LENGTH_SHORT).show();
    }
    
    private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }
}

