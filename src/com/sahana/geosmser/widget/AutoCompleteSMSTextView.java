package com.sahana.geosmser.widget;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class AutoCompleteSMSTextView extends AutoCompleteTextView {
	public MultiContactListAdapter contactListAdapter;
	public Context baseContext;
	
	public boolean isClicked = false;
	
	// call when inflating 
	public AutoCompleteSMSTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseContext = context;
		init();
	}
	
	public AutoCompleteSMSTextView(Context context) {
		super(context);
		baseContext = context;
		init();
	}
	
	public AutoCompleteSMSTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		baseContext = context;
		init();
	}
	
	private void init() {
		contactListAdapter = new MultiContactListAdapter(baseContext);
		this.setAdapter(contactListAdapter.getAdapter());
	}

	public String getValue() {
		String text = this.getText().toString();
		
		if(text.matches(".*<\\d{1,}>")) return text.substring(text.indexOf("<")+1, text.length()-1);
		if(text.matches("\\d{1,}")) return text;
		// Modify by Korth 20101203
		if(text.matches("^.?\\d{1,}")) return text;
		
		return "";
	}
	
	@Override
	protected CharSequence convertSelectionToString(Object selectedItem) {
		Cursor cursor = (Cursor) selectedItem;
		
		if(!isClicked) {
			if(this.isPerformingCompletion()) {
				return cursor.getString(5) + " <" + cursor.getString(3) + ">";
			}
			return super.convertSelectionToString(selectedItem);
		}
		isClicked = false;
		
		return cursor.getString(5) + " <" + cursor.getString(3) + ">";
	}

}
