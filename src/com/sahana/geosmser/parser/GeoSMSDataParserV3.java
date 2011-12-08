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

import com.OpenGeoSMS.GeoSMS;
import com.sahana.geosmser.parser.GeoSMSParserV3.IGeoSMSDataParserAdapter;

public class GeoSMSDataParserV3 implements IGeoSMSDataParserAdapter {
    @Override
    public GeoSMS parse(GeoSMS sms, String[] items) {
        if(items.length < 2) return sms;
        
        StringBuilder sb = new StringBuilder();
        int i = 1;
        sb.append(items[i++]);
        for(; i<=items.length-1; i++ ) {
            sb.append("\n").append(items[i]);
        }
        sms.setText(sb.toString());
        return sms;
    }
}
