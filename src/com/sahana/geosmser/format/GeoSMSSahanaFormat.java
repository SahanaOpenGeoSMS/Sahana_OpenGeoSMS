package com.sahana.geosmser.format;

import java.util.HashMap;
import java.util.Map;

public enum GeoSMSSahanaFormat {
	//S stands for Sahana	
	INCIDENTREPORT("SI"),
	TASK("ST"),
	UNKNOWN("U");
	

	private final static Map<String, GeoSMSSahanaFormat> mFormatnMap;
	private String mTypeStr;
	
	static {
		mFormatnMap = getTypeMap();
	}
	
	GeoSMSSahanaFormat(String ts) {
		mTypeStr = ts;
	}
	
	public String getTypeString() {
		return mTypeStr;
	}
	
	public static Map<String, GeoSMSSahanaFormat> getTypeMap() {
		Map<String, GeoSMSSahanaFormat> map = new HashMap<String, GeoSMSSahanaFormat>();
				
		map.put(INCIDENTREPORT.mTypeStr, INCIDENTREPORT);	
		map.put(TASK.mTypeStr, TASK);
		map.put(UNKNOWN.mTypeStr, UNKNOWN);
		
		return map;
	}
	
	public static GeoSMSSahanaFormat getByTypeString(String ts) {
		return mFormatnMap.get(ts);
	}
}
