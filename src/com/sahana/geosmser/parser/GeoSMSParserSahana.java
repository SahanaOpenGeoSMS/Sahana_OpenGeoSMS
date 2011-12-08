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
package com.sahana.geosmser.parser;

import com.OpenGeoSMS.exception.GeoSMSException;
import com.OpenGeoSMS.renderer.Renderer;
import com.sahana.geosmser.GeoSMSPack;
import com.sahana.geosmser.format.GeoSMSSahanaFormat;

public class GeoSMSParserSahana {
    
	public GeoSMSPack parse(GeoSMSPack pack, String sms) throws GeoSMSException {
	
		String items[] = sms.split(Renderer.PAT_NEWLINE);
        //items[0] ex: http://maps.google.com/?q=24.4365,121.39648&GeoSMS=B
		//items[1] ex: My location is here
	
		pack = parseHeader(pack, items[0]);
        
        return pack;
    }
	
	public GeoSMSPack parseHeader(GeoSMSPack pack, String header) throws GeoSMSException {
		//?
		String[] items = header.split("\\" + Renderer.PAT_PARAMETER_PREFIX); 
		//items[0] ex: http://maps.google.com/
		//items[1] ex: q=24.4365,121.39648&GeoSMS=B
		
		//&
        String[] param = items[1].split(Renderer.PAT_PARAMETER_SPLIT);
		//param[0] ex: q=24.4365,121.39648
		//param[length-1] ex: GeoSMS=B
        
        //=
        int plen = param.length;      
        String[] p = param[plen-1].split(Renderer.PAT_PARAMETER_INFIX);
        //p[0]=> GeoSMS
        //p[1]=> special format for Sahana: SI, ST 
        
        String value = p[1];
        pack.setGeoSMSSahanaFormat(parseGeoSMSSahanaTypeFormat(value));
    
        return pack;
	 }
	
    public GeoSMSSahanaFormat parseGeoSMSSahanaTypeFormat(String fs) {
    	GeoSMSSahanaFormat format = GeoSMSSahanaFormat.getByTypeString(fs);
        if(format == null) format = GeoSMSSahanaFormat.UNKNOWN;
        return format;
    }
}
