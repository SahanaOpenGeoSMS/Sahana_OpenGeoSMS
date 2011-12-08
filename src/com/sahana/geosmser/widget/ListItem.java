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
package com.sahana.geosmser.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class ListItem implements Parcelable {
	public String key;
	public String value;
	public View view;
	public Object pack;
	
	public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
		public ListItem createFromParcel(Parcel in) {
			return new ListItem(in);
        }

        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
	};
	
	public ListItem() {
		key = "";		
		value = "";
	}
	
	public ListItem(String pKey) {
		key = pKey;
		value = "";
	}
	
	public ListItem(String pKey, String pValue) {
		key = pKey;
		value = pValue;
	}
	
	public ListItem(Parcel in) {
		key = in.readString();
		value = in.readString();
	}

	public View getView() {
		return view;
	}
	
	public void setView(View v) {
		view = v;
	}
	
	public void setValue(String v) {
		value = v;
	}
	
	public String getValue() {
		return value;
	}

	public void setKey(String pKey) {
		key = pKey;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(key);
		out.writeString(value);
	}
}
