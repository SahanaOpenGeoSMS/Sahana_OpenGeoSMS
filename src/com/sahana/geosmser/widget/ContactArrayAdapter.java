package com.sahana.geosmser.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sahana.geosmser.Contact;
import com.sahana.geosmser.database.WhiteListDBAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TwoLineListItem;

public class ContactArrayAdapter extends ArrayAdapter<Contact> implements SectionIndexer {

    private final int resourceId;
    ArrayList<Contact> myElements;
    HashMap<String, Integer> alphaIndexer;
    String[] sections;
    WhiteListDBAdapter db;

    @SuppressWarnings({ "unchecked" })
    public ContactArrayAdapter(Context context, int textViewResourceId, List objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        myElements = (ArrayList<Contact>) objects;
        alphaIndexer = new HashMap<String, Integer>();
        int size = objects.size();
        db = new WhiteListDBAdapter(context);
        
        for (int i = size - 1; i >= 0; i--) {
            Contact element = myElements.get(i);
            alphaIndexer.put(element.getName().substring(0, 1), i);
        }
        Set<String> keys = alphaIndexer.keySet(); 
        Iterator<String> it = keys.iterator();
        ArrayList<String> keyList = new ArrayList<String>();
        while (it.hasNext()) {
            String key = it.next();
            keyList.add(key);
        }
        Collections.sort(keyList);
        sections = new String[keyList.size()];
        keyList.toArray(sections);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Contact c = (Contact) getItem(position);
        db.open();
        Cursor cur = db.getAllTitles();
        // if the array item is null, nothing to display, just return null
        if (c == null) {
            return null;
        }

        // We need the layoutinflater to pick up the view from xml
        LayoutInflater inflater = (LayoutInflater)
        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Pick up the TwoLineListItem defined in the xml file
        TwoLineListItem view;
        if (convertView == null) {
            view = (TwoLineListItem) inflater.inflate(resourceId, parent, false);
        } else {
            view = (TwoLineListItem) convertView;
        }

        /* Set value for the first text field
        //if (view.getText1() != null && isThere(c.getName())!=-1) {
            view.getText1().setText(c.getName() + "   CHECKED");
            
        }else */
        if(view.getText1() != null){
            view.getText1().setText(c.getName());
        }

        // set value for the second text field
        if (view.getText2() != null) {
            view.getText2().setText(c.getNumber());
        }
        //view.getText1().setText("Yepeee");
        db.close();
        return view;
    }

    public int getPositionForSection(int section) {
        String letter = sections[section];
        return alphaIndexer.get(letter);
    }

    public int getSectionForPosition(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object[] getSections() {
        return sections;
    }

    public int isThere(String name) {
        db.open();
        Cursor c = db.getAllTitles();
        if (c.moveToFirst()) {
            do {
                if (c.getString(1).equalsIgnoreCase(name)) {
                    int id = Integer.parseInt(c.getString(0));
                    // db.close();
                    return id;
                }
            } while (c.moveToNext());
        }
        db.close();
        return -1;
    }
}
