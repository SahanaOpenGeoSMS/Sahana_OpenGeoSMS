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

import com.sahana.geosmser.GeoSMSPack;

import android.content.Context;
import android.text.util.Linkify;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GeoSMSInformationPanel extends TransparentPanel {
	private Context me;
	private boolean mIsInflated = false;
	private LinearLayout mInflatedPanel, shrunkPanel;
	private RelativeLayout.LayoutParams mInflatedParas, mShrunkParas;
	
	public GeoSMSInformationPanel(Context context) {
		super(context);
		me = context;
		init();
		inflate();
	}
	
	private void init() {
		mInflatedParas = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mInflatedParas.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mInflatedParas.setMargins(10, 10, 10, 10);
		
		mShrunkParas = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mShrunkParas.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		mShrunkParas.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mShrunkParas.setMargins(10, 10, 10, 10);
		
		setPadding(5, 5, 5, 5);
		
		mInflatedPanel = new LinearLayout(me);
		shrunkPanel = new LinearLayout(me);
		shrunkPanel.addView(getNewTextView("....."));
		
		this.setOnClickListener(new EvtPanelOnClickListener());
	}

	public GeoSMSInformationPanel render(GeoSMSPack pack) {
		mInflatedPanel.removeAllViews();
		
		if(pack != null) {
			String text = pack.getText();
			if(text != null) {
				switch(pack.getGeoSMSFormat()) {
					case BASIC:
					//case POI:
					case EXTENDED:
					//case AGPS: 
					//Ye New add
					case TASK:
					case INCIDENT:
						mInflatedPanel.addView(getNewTextView(text));
						break;
					case UNKNOWN:
					case QUERY:
					default:
						return null;
				}
				return this;
			}
		}
		return null;
	}
	
	protected TextView getNewTextView(String text) {
		TextView tv = new TextView(me);
		tv.setText(text);
		return tv;
	}
	
	public boolean isInflated() {
		return mIsInflated;
	}
	
	public void inflate() {
		inflate(true);
	}
	
	public void inflate(boolean isInflated) {
		mIsInflated = isInflated;
		this.removeAllViews();
		if(mIsInflated) {
			this.addView(mInflatedPanel);
			setLayoutParams(mInflatedParas);
		}
		else {
			this.addView(shrunkPanel);
			setLayoutParams(mShrunkParas);
		}
	}
	
	
	private class EvtPanelOnClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			GeoSMSInformationPanel panel = (GeoSMSInformationPanel) view;
			panel.inflate(!panel.isInflated());
		}
	}
	
}
