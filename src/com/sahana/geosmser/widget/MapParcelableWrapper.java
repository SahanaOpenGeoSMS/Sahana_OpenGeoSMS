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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class MapParcelableWrapper implements Parcelable {
	public Map<Integer, GeoSMSListItem> mMap;
	
	public static final Parcelable.Creator<MapParcelableWrapper> CREATOR = new Parcelable.Creator<MapParcelableWrapper>() {
		public MapParcelableWrapper createFromParcel(Parcel in) {
			return new MapParcelableWrapper(in);
        }

        public MapParcelableWrapper[] newArray(int size) {
            return new MapParcelableWrapper[size];
        }
	};
	
	public MapParcelableWrapper(Map<Integer, GeoSMSListItem> cMap) {
		mMap = cMap;
	}
	
	public MapParcelableWrapper(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flag) {
		/*Bundle bundle = new Bundle();
		for(Integer key : mMap.keySet()) {
			bundle.putParcelable(key.toString(), mMap.get(key));
		}
		out.writeBundle(bundle);*/
		out.writeMap(mMap);
	}

	public void readFromParcel(Parcel in) {
		mMap = new ConcurrentHashMap<Integer, GeoSMSListItem>();
		in.readMap(mMap, ConcurrentHashMap.class.getClassLoader());
	}

	public Map<Integer, GeoSMSListItem> getMap() {
		return mMap;
	}
	
}
