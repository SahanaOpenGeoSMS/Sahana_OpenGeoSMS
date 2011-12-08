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
