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
