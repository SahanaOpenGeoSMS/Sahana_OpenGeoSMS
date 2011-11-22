package com.sahana.geosmser;

import com.OpenGeoSMS.format.GeoSMSFormat;
import com.google.android.maps.GeoPoint;


public class GeoSMSPackFactory {

	public static GeoSMSPack createBasicPack(GeoPoint point) {
		GeoSMSPack pack = createDefaultPack(point);
		pack.setGeoSMSFormat(GeoSMSFormat.BASIC);
		return pack;
	}
	
	public static GeoSMSPack createDefaultPack(GeoPoint point) {
		GeoSMSPack pack = new GeoSMSPack();
		pack.setDomainNamePath("http://maps.google.com/");
		pack.setLocation("q", point);
		return pack;
	}
	
	// http://maps.google.com/?&GeoSMS=Q
	public static GeoSMSPack createQueryPack() {
		GeoSMSPack pack = createDefaultPack(null);
		pack.setGeoSMSFormat(GeoSMSFormat.QUERY);
		return pack;
	}
	
	//Ye add
	public static GeoSMSPack createIncidentPack(GeoPoint point){
		GeoSMSPack pack = createDefaultPack(point);
		pack.setGeoSMSFormat(GeoSMSFormat.INCIDENT);
		return pack;
		
	}
	
}
