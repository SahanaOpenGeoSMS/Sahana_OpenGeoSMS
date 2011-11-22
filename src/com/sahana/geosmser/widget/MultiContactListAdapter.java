package com.sahana.geosmser.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Contacts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import com.sahana.geosmser.view.ListItemView;

public class MultiContactListAdapter {
	public Uri contactsUri;
	public Cursor contactsCursor;
	public String[] cursorColumn;
	
	private Context me;
	private ContactListAdapterImpl contactListAdapter;
	
	public float titleTextSize = 19, contentTextSize = 14;
	public int titleTextColor = Color.BLACK, contentTextColor = Color.BLACK;
	public int rowPadLeft = 10, rowPadTop = 8, rowPadRight = 10, rowPadBottom = 8;
	public int rowOrientation = LinearLayout.VERTICAL;
	public int rowValuePadding = 0;
	
	public MultiContactListAdapter(Context act) {
		init(act);
	}
	
	private void init(Context act) {
		me = act;
		/*contactsUri = People.CONTENT_URI;
		cursorColumn = new String[] {People._ID, People.NAME, People.NUMBER};
		contactsCursor = adapterContext.getContentResolver().query(contactsUri, cursorColumn, null, null, People.NAME + " ASC");
		me.startManagingCursor(contactsCursor);*/
		
		ContentResolver content = me.getContentResolver();
        Cursor cursor = content.query(Contacts.People.CONTENT_URI, ContactListAdapter.PEOPLE_PROJECTION, null, null, Contacts.People.DEFAULT_SORT_ORDER);
        contactListAdapter = new ContactListAdapterImpl(me, cursor);       
	}

	/*public void bindListData() {
		getColumnData(contactsCursor);
	}
	
	private void getColumnData(Cursor cur){ 
	    if (cur.moveToFirst()) {
	        String name; 
	        String phoneNumber; 
	        int NameColIdx = cur.getColumnIndex(People.NAME); 
	        int phoneColIdx = cur.getColumnIndex(People.NUMBER);
	    
	        do {
	            name = cur.getString(NameColIdx);
	            phoneNumber = cur.getString(phoneColIdx);
	            if(phoneNumber != null && name != null) {
	            	this.addItem(new ListItem(name, phoneNumber));
	            }
	        } while (cur.moveToNext());

	    }
	}*/
	
	public ContactListAdapterImpl getAdapter() {
		return contactListAdapter;
	}
	
    public class ContactListAdapterImpl extends CursorAdapter implements Filterable {
        public ContactListAdapterImpl(Context context, Cursor c) {
            super(context, c);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            /*final LayoutInflater inflater = LayoutInflater.from(context);
            final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            view.setText(cursor.getString(5));
            view.setTextSize(19);
            return view;*/
        	ListItemView itemView = new ListItemView(me);
            itemView.keyView.setTextSize(titleTextSize);
            itemView.keyView.setTextColor(titleTextColor);
            itemView.valueView.setTextSize(contentTextSize);
            itemView.valueView.setTextColor(contentTextColor);
            itemView.setPadding(rowPadLeft, rowPadTop, rowPadRight, rowPadBottom);
            itemView.setOrientation(rowOrientation);
            itemView.valueView.setPadding(rowValuePadding, 0, 0, 0);
            return itemView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //((TextView) view).setText(cursor.getString(5));
        	ListItemView itemView = (ListItemView) view;
            itemView.setKeyText(cursor.getString(5));
            itemView.setValueText(cursor.getString(3));
        }
        
        @Override
        public String convertToString(Cursor cursor) {
        	//String name = cursor.getString(5);
        	//String phoneNumber = cursor.getString(3);
        	String result = cursor.getString(5) + " <" + cursor.getString(3) + ">";
            return result;
        }
        
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        	if(constraint == null) return null;
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }

            String[] keyWorks = constraint.toString().split(",");
            String keyWork = keyWorks[keyWorks.length-1].trim();
            if(keyWork.equals("")) return null;
            
            if(keyWork.matches(".*<.*")) {
            	keyWorks = constraint.toString().split("<");
            	keyWork = keyWorks[keyWorks.length-1];
            }
            else {
            	keyWork = keyWork.trim();
            }
            
            //String keyWork = constraint.toString();
            String where = null;
            String[] args = null;
            if (constraint != null) {
            	where = "(UPPER(" + Contacts.ContactMethods.NAME + ") GLOB ? AND number IS NOT NULL) OR number LIKE ?";
                args = new String[] {keyWork.toUpperCase() + "*", keyWork + "%"};
            }
            
            Cursor c =  mContent.query(Contacts.People.CONTENT_URI, ContactListAdapter.PEOPLE_PROJECTION, where, args, Contacts.People.DEFAULT_SORT_ORDER);
            return c;
        }
        private ContentResolver mContent;
    }
}
