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
