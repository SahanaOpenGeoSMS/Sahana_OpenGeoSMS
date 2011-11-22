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
