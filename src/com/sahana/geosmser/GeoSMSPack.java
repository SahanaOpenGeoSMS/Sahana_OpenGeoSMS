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

import java.util.LinkedHashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.OpenGeoSMS.GeoLocation;
import com.OpenGeoSMS.GeoSMS;
import com.OpenGeoSMS.format.GeoSMSFormat;
import com.OpenGeoSMS.renderer.Renderer;
import com.google.android.maps.GeoPoint;
import com.sahana.geosmser.format.GeoSMSSahanaFormat;

public class GeoSMSPack extends GeoSMS implements Parcelable {
	
	private GeoSMSSahanaFormat mGeoSMSSahanaFormat;
	
	public static final Parcelable.Creator<GeoSMSPack> CREATOR = new Parcelable.Creator<GeoSMSPack>() {
		public GeoSMSPack createFromParcel(Parcel in) {
			return new GeoSMSPack(in);
        }

        public GeoSMSPack[] newArray(int size) {
            return new GeoSMSPack[size];
        }
	};

	
	
	public GeoSMSPack() {
		super();
	}
	
	public GeoSMSPack(GeoSMS gsms) {
		assign(gsms);
	}
	
	public GeoSMSPack(Parcel in) {
		readFromParcel(in);
	}
	
	public void assign(GeoSMS gsms) {
		this.setDomainNamePath(gsms.getDomainNamePath());
		this.setLocationKey(gsms.getLocationKey());
		this.setGeoLoc(gsms.getGeoLocation());
		this.setParameterMap(gsms.getParameterMap());
		this.setGeoSMSFormat(gsms.getGeoSMSFormat());
		this.setText(gsms.getText());
		

	}
	
	public GeoPoint toGeoPoint() {
		GeoLocation gloc = getGeoLocation();
		
		if(gloc != null) {
			int gpLat = (int) (gloc.getLatitude() * 1E6);
			int gpLng = (int) (gloc.getLongitude() * 1E6);
			return new GeoPoint(gpLat, gpLng);
		}
		return null;
	}
	
	public void setPositionFromGeoPoint(GeoPoint point) {
		if(point == null) return;
		double lat = point.getLatitudeE6() / 1E6;
		double lng = point.getLongitudeE6() / 1E6;
		GeoLocation gloc = getGeoLocation();
		
		if(gloc != null) {
			gloc.setLatitude(lat);
			gloc.setLongitude(lng);
		}
		else {
			setGeoLoc(new GeoLocation(lat, lng));
		}
	}
	
	public void setLocation(String key, GeoPoint point) {
		setLocationKey(key);
		setPositionFromGeoPoint(point);
	}
	
	public String getHeaderString() {
		String gs = getDomainNamePath() + Renderer.PAT_PARAMETER_PREFIX + getLocationKey()
		+ Renderer.PAT_PARAMETER_INFIX + getGeoLocation().toString() + Renderer.PAT_PARAMETER_SPLIT;
		
		Map<String, String> pMap = getParameterMap();
		for(String key : pMap.keySet()) {
			gs += key + Renderer.PAT_PARAMETER_INFIX + pMap.get(key) + Renderer.PAT_PARAMETER_SPLIT;
		}
		
		//Ye modify
//		if (getGeoSMSFormat().equals(GeoSMSFormat.BASIC)) {
//		    gs += CONST_GEOSMS_PARA_KEY;
//		} else {
		    gs += CONST_GEOSMS_PARA_KEY + Renderer.PAT_PARAMETER_INFIX + getGeoSMSFormat().getTypeString();
//		}
		return gs;
	}
	
	//Ye New add
	public void setGeoSMSSahanaFormat(GeoSMSSahanaFormat geoSMSSahanaFormat) {
		mGeoSMSSahanaFormat = geoSMSSahanaFormat;
	}

	//Ye New add
	public GeoSMSSahanaFormat getGeoSMSSahanaFormat() {
		return mGeoSMSSahanaFormat;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getDomainNamePath());
		out.writeString(getLocationKey());
		GeoLocation gloc = getGeoLocation();
		if (gloc != null) {
		    out.writeDouble(gloc.getLatitude());
		    out.writeDouble(gloc.getLongitude());
		}
		out.writeMap(getParameterMap());
		out.writeString(getGeoSMSFormat().getTypeString());

		//Ye Revised
//		if(getGeoSMSFormat().equals(GeoSMSFormat.UNKNOWN))
//			out.writeString(getGeoSMSSahanaFormat().getTypeString());

		out.writeString(getText());
		
	}
	
	public void readFromParcel(Parcel in) {
		setDomainNamePath(in.readString());
		setLocationKey(in.readString());
		setGeoLoc(new GeoLocation(in.readDouble(), in.readDouble()));
		Map<String, String> map = new LinkedHashMap<String, String>();
		in.readMap(map, LinkedHashMap.class.getClassLoader());
		setParameterMap(map);
	
		//Ye Revised
//		String format = in.readString();
//		setGeoSMSFormat(GeoSMSFormat.getByTypeString(format));
		setGeoSMSFormat(GeoSMSFormat.getByTypeString(in.readString()));
//		if(format =="UK"){
//		if(GeoSMSFormat.getByTypeString(format)== GeoSMSFormat.UNKNOWN)
//			setGeoSMSSahanaFormat(GeoSMSSahanaFormat.getByTypeString(format));
//			setGeoSMSSahanaFormat(GeoSMSSahanaFormat.getByTypeString(in.readString()));
//		}
		
		setText(in.readString());
	}
	
}
