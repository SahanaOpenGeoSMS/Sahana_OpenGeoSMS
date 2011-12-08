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

import com.sahana.geosmser.widget.ListItem;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListItemView extends LinearLayout {
	public TextView keyView, valueView;
	public LayoutParams keyParams, valueParams;
	public Context baseContext;
	
	public ListItemView(Context pContext) {
		super(pContext);
		baseContext = pContext;
		render();
	}
	
	public ListItemView(Context pContext, ListItem pItem) {
		super(pContext);
		baseContext = pContext;
		render();
		setItem(pItem);
	}
	
	public void render() {
		keyView = new TextView(baseContext);
		keyParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		this.addView(keyView, keyParams);
				
		valueView = new TextView(baseContext);
		valueParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		this.addView(valueView, valueParams);
	}
	
	public void setItem(ListItem pItem) {
		keyView.setText(pItem.getKey());
		valueView.setText(pItem.getValue());
	}
	
	public void setKeyText(String t) {
		keyView.setText(t);
	}
	
	public void setValueText(String t) {
		valueView.setText(t);
	}

}
