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
package com.sahana.geosmser.view;

import com.OpenGeoSMS.format.GeoSMSFormat;
import com.sahana.geosmser.R;
import com.sahana.geosmser.GeoSMSManagerAct;
import com.sahana.geosmser.WhereToMeet;
import com.sahana.geosmser.database.QueryRepliedIDDBAdapter;
import com.sahana.geosmser.widget.GeoSMSListItem;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GeoSMSListItemView extends RelativeLayout {
	private Context baseContext;
	
	private GeoSMSListItem listItem;
	private TextView vAddress, vDate, vMsgBody, vReply;
	private ImageView vFormatType;
	private LayoutParams lpAddress;
	
	private final Object mListItemLock = new Object();
	private Handler mHandler = new Handler();
	private QueryRepliedIDDBAdapter mQueryRepliedIDDBAdapter;
	
	private boolean reply = false;

	public GeoSMSListItemView(Context context) {
		super(context);
		this.baseContext = context;
	}
	
	public GeoSMSListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setReply(boolean bol) {
	    this.reply = bol;
	}
	
    @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		vAddress = (TextView) findViewById(R.id.TextViewSMSListItemAddress);
		vDate = (TextView) findViewById(R.id.TextViewSMSListItemDate);
		vMsgBody = (TextView) findViewById(R.id.TextViewSMSListItemBody);
		vReply = (TextView) findViewById(R.id.TextViewSMSReplier);
		vFormatType = (ImageView) findViewById(R.id.ImageViewFormatType);
	}
	
	private void init(Context context) {
		vAddress = new TextView(baseContext);
		lpAddress = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpAddress.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		this.addView(vAddress, lpAddress);
	}
	
	public GeoSMSListItem getListItem() {
		synchronized (mListItemLock) {
            return listItem;
        }
	}
	
	public void setListItem(GeoSMSListItem item) {
		synchronized (mListItemLock) {
            listItem = item;
        }
	}
	
	public boolean hasListItem() {
		if(getListItem() != null) return true;
		return false;
	}
	
	private void bindLoadingView() {
		//setListItem(null);
		vAddress.setText(R.string.loading_inbox_list_item);
		vDate.setText("");
		vMsgBody.setText("");
		vReply.setVisibility(View.INVISIBLE);
		vFormatType.setVisibility(View.GONE);
	}
	
	public void bindListItem() {
	    vAddress.setText(listItem.mAddress);
		vDate.setText(listItem.mDateString);
		vMsgBody.setText(listItem.mSMSBody);
		  /** TODO Here is too diverse and complicated, optimization! */
        if (GeoSMSManagerAct.SMSIDTable.contains(listItem.mID)) {
            if (listItem.mGeoSMSPack.getGeoSMSFormat().equals(GeoSMSFormat.QUERY)) {
                vReply.setTextColor(Color.GREEN);
                vReply.setVisibility(TextView.VISIBLE);
                vReply.setText("Replied");
            }
        } else {
            vReply.setVisibility(TextView.INVISIBLE);
        }
		vFormatType.setVisibility(View.VISIBLE);
		vFormatType.setImageResource(listItem.mImageSourceID);
	}

	public void bind(final GeoSMSListItem item) {
//		GeoSMSListItem oldItem = getListItem();
		setListItem(item);
		
		if(item.isEmpty()) {
			bindLoadingView();
			item.setLoadingView(this);
		}
		else {
		    bindListItem();
		}
	}
	
	public void onListItemLoaded(final GeoSMSListItem item) {
		synchronized (mListItemLock) {
			if(listItem != item) return;
			mHandler.post(new showListItemRunner(item));
		}
	}
	
	private class showListItemRunner implements Runnable {
		public GeoSMSListItem mItem;
		
		public showListItemRunner(final GeoSMSListItem item) {
			mItem = item;
		}
		
		@Override
		public void run() {
			synchronized (mListItemLock) {
				if(listItem == mItem) {
					bindListItem();
				}
			}
		}
	}
}
