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