/*******************************************************************************
 * Copyright 2011 Cai Fang, Ye
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sahana.geosmser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.sahana.geosmser.R;
import com.sahana.geosmser.database.WhiteListDBAdapter;
import com.sahana.geosmser.widget.ContactArrayAdapter;

public class ContactListAct extends Activity implements Runnable{
    
    private LinearLayout mLinearLayout;
    private EditText mEditText;
    private ListView mListView;
    private List<Contact> mContacts = null;
    private Contact mContact;
    private ContactArrayAdapter cAdapter;
    private ProgressDialog mProDialog = null;
    private Context mContext;
    private WhiteListDBAdapter mWhiteListDBAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(mLinearLayout);
        initComponent();
        initEvent();
    }

    private void initComponent() {
        mContext = this;
        mWhiteListDBAdapter = new WhiteListDBAdapter(this);
        
        mEditText = new EditText(this);
        mListView = new ListView(this);
        
        LinearLayout.LayoutParams mEditParams
        = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        
        mEditText.setLayoutParams(mEditParams);
        mEditText.setHint("Contact Search");
        mEditText.addTextChangedListener(mTextWatcher);
        mListView.setOnItemClickListener(listener);
        
        mLinearLayout.addView(mEditText);
        mLinearLayout.addView(mListView);
    }
    
    private void initEvent() {
        mProDialog = ProgressDialog.show(this, "White List", "Getting Contacts", true, false);
        Thread thread = new Thread(this);
        thread.start();
    }
    
    /**
     * Fill List by Contacts
     * @return List<Contact>
     */
    private List<Contact> fillContactsList() {
        List<Contact> tmpList = new ArrayList<Contact>();
        // Android 2.0, multi-Contacts by Contacts ID
        /*Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while(c.moveToNext()) {
            String ContactID = c.getString(c.getColumnIndex("_id"));
            String name = c.getString(c.getColumnIndex("display_name"));
            String hasPhone =c.getString(
                    c.getColumnIndex("has_phone_number"));
            if(Integer.parseInt(hasPhone) == 1) {
                Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "contact_id"+"='"+ContactID+"'", null, null);
                while(phoneCursor.moveToNext()) {
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex("data1"));
                    mContact = new Contact();
                    mContact.setName(name);
                    mContact.setNumber(number);
                    tmpList.add(mContact);
                }
                phoneCursor.close();
            }
        }*/
        
        Cursor cursor = getContentResolver().query(Contacts.People.CONTENT_URI, null, null, null, null);
        while(cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex(Contacts.Phones.NUMBER)) != null) {
                mContact = new Contact();
                mContact.setName(cursor.getString(cursor.getColumnIndex(Contacts.Phones.NAME)));
                mContact.setNumber(cursor.getString(cursor.getColumnIndex(Contacts.Phones.NUMBER)));
                tmpList.add(mContact);
            }
        }
        cursor.close();
        Collections.sort(tmpList);
        return tmpList;
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProDialog.dismiss();
            cAdapter = new ContactArrayAdapter(mContext, R.layout.list_item_layout, mContacts);
            mListView.setFastScrollEnabled(true);
          //  cAdapter.getView(0, null,null);
            mListView.setAdapter(cAdapter);
        }
    };
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        
        private int mTextLength = 0;
        private List<Contact> list = new ArrayList<Contact>();
        
        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            mTextLength = mEditText.getText().length();
            list.clear();
            for (int i = 0; i < mContacts.size(); i++) {
                if (mTextLength <= mContacts.get(i).getName().length()) {
                    if (mEditText.getText().toString().equalsIgnoreCase(
                            (String) mContacts.get(i).getName().substring(0, mTextLength))) {
                        list.add(mContacts.get(i));
                    }
                }
            }
            mListView.setAdapter(new ContactArrayAdapter(ContactListAct.this, R.layout.list_item_layout, list));
        }
        
    };
    
    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            // TODO Auto-generated method stub
            TextView label1 = ((TwoLineListItem) view).getText1();
            TextView label2 = ((TwoLineListItem) view).getText2();
            String name = label1.getText().toString();
            String phoneNumber = label2.getText().toString();
            
            if(isThere(name) == -1) {
                 mWhiteListDBAdapter.open();        
                 long id1;
                 
                 id1 = mWhiteListDBAdapter.insertTitle(
                        name,
                        phoneNumber);        
                 mWhiteListDBAdapter.close();
                 Toast.makeText(ContactListAct.this, "Added to white list "+name+" "+ phoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                 mWhiteListDBAdapter.open();
                 if (mWhiteListDBAdapter.deleteTitle(isThere(name)))
                     Toast.makeText(ContactListAct.this, "Delete successful! "+name+" is removed from the list ", 
                         Toast.LENGTH_LONG).show();
                 else
                     Toast.makeText(ContactListAct.this, "Delete failed.", 
                         Toast.LENGTH_LONG).show();            
                 mWhiteListDBAdapter.close();

                 mContacts = null;
            }
        }
        
    };
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (mContacts == null) {
            mContacts = fillContactsList();
        }
        handler.sendEmptyMessage(0);
    }
    
    public int isThere(String name) {
        mWhiteListDBAdapter.open();
        Cursor c = mWhiteListDBAdapter.getAllTitles();
        if (c.moveToFirst()) {
            do {
                if (c.getString(1).equalsIgnoreCase(name)) {
                    int id = Integer.parseInt(c.getString(0));
                    // db.close();
                    return id;
                }
            } while (c.moveToNext());
        }
        mWhiteListDBAdapter.close();

        return -1;
    }

}
