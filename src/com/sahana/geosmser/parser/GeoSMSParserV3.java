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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.OpenGeoSMS.GeoLocation;
import com.OpenGeoSMS.GeoSMS;
import com.OpenGeoSMS.NMEALocation;
import com.OpenGeoSMS.codec.Base64NMEALocationCodec;
import com.OpenGeoSMS.exception.GeoSMSException;
import com.OpenGeoSMS.format.GeoSMSFormat;
import com.OpenGeoSMS.parser.HeaderGeoSMSNotFoundException;
import com.OpenGeoSMS.parser.HeaderLackParameterException;
import com.OpenGeoSMS.parser.HeaderLocationFormatException;
import com.OpenGeoSMS.parser.HeaderParameterFormatExcpetion;
import com.OpenGeoSMS.parser.HeaderParameterNotFoundException;
import com.OpenGeoSMS.parser.HeaderUnknownGeoSMSTypeFormatException;
import com.OpenGeoSMS.reader.QueryResult;
import com.OpenGeoSMS.renderer.Renderer;
import com.sahana.geosmser.WhereToMeet;


public class GeoSMSParserV3 {
    
    private IGeoSMSDataParserAdapter mDataParser;
    private Object mExceptionReturn;
    
    /** http://.+\\?.*&GeoSMS(=.?)? */
    private final static String PAT_FAST_URL = "http://.+" + "\\"
            + Renderer.PAT_PARAMETER_PREFIX + ".*"
            + Renderer.PAT_PARAMETER_SPLIT + GeoSMS.CONST_GEOSMS_PARA_KEY 
            + "(" + Renderer.PAT_PARAMETER_INFIX + ".?)?";
    
    /** .+=(.+,?.+|.+,[NSns],.+,[EWew]) */
    private final static String PAT_FAST_LOC = ".+=(.+,?.+|.+,[NSns],.+,[EWew])";

    /** .+=(.+,?.+|.+,[NSns],.+,[EWew]) */
    private final static String PAT_FAST_LOC_OTHER = "=(.+,?.+|.+,[NSns],.+,[EWew])";
    
    /** GeoSMS=Q */
    private final static String PAT_FAST_QTYPE = GeoSMS.CONST_GEOSMS_PARA_KEY
            + Renderer.PAT_PARAMETER_INFIX + "Q";

    
    public GeoSMSParserV3() {
        mDataParser = new GeoSMSDataParserV3();
    }

    public void setExceptionReturn(Object obj) {
        mExceptionReturn = obj;
    }
    
    public void setGeoSMSDataParserAdapter(IGeoSMSDataParserAdapter adapter) {
        mDataParser = adapter;
    }
    
    public IGeoSMSDataParserAdapter getGeoSMSDataParserAdapter() {
        return mDataParser;
    }
    
    public boolean checkStructure(String sms) {
        String items[] = sms.split(Renderer.PAT_NEWLINE);
        if(checkHeaderStructure(items[0])) {
            // TODO checkDataStructure
            return true;
        }
        return false;
    }
    
    public GeoSMS parse(String sms) throws GeoSMSException {
        GeoSMS geosms;
        
        String items[] = sms.split(Renderer.PAT_NEWLINE);
        geosms = parseHeader(items[0]);
        
        if(items.length > 1) {
            geosms = mDataParser.parse(geosms, items);
        }
        return geosms;
    }
    
    public static boolean checkHeaderStructure(String header) {
        //if(header.matches(PAT_FAST_URL)) {
        if (header.contains("http://") && header.contains("?") && header.contains("&GeoSMS")) {
            String[] items = header.split("\\" + Renderer.PAT_PARAMETER_PREFIX);
            if(items.length == 2) {
                String[] paras = items[1].split(Renderer.PAT_PARAMETER_SPLIT);
                if(paras.length > 1) {
                    if(paras[paras.length - 1].matches(PAT_FAST_QTYPE)) return true;
                    if(paras[0].matches(PAT_FAST_LOC_OTHER) || paras[0].matches(PAT_FAST_LOC)) {
                        return true;
                    }
                }
            }
        }
        //} 
        return false;
    }

    public GeoSMS parseHeader(String header) throws GeoSMSException {
        GeoSMS gsms = new GeoSMS();
        String[] items = header.split("\\" + Renderer.PAT_PARAMETER_PREFIX);
        if(items.length == 2) {
            gsms.setDomainNamePath(items[0]);
            
            String[] paras = items[1].split(Renderer.PAT_PARAMETER_SPLIT);
            HeaderParameter hp;
            int plen = paras.length;
            int pIndex = 0;
            if(plen > 1) {
                hp =  parseParameter(gsms, paras[plen-1]);
                // TODO Problem in Here
                if(!hp.mKey.trim().equals(GeoSMS.CONST_GEOSMS_PARA_KEY)) throw new HeaderGeoSMSNotFoundException(getReturnedObj(gsms));
                
                //original
                //gsms.setGeoSMSFormat((hp.mValue.equals("")) ? GeoSMSFormat.BASIC : parseGeoSMSTypeFormat(hp.mValue.trim()));
                //Ye Revised
                gsms.setGeoSMSFormat(parseGeoSMSTypeFormat(hp.mValue.trim()));
            	
                //Ye Mark, for Unknow format show the content of SMS  
                //if(gsms.getGeoSMSFormat().equals(GeoSMSFormat.UNKNOWN)) throw new HeaderUnknownGeoSMSTypeFormatException(getReturnedObj(gsms), hp.mValue);
                
                //if NOT Query format, must has lat, lng
                // http://maps.google.com/?&GeoSMS=Q
                if(!gsms.getGeoSMSFormat().equals(GeoSMSFormat.QUERY)) {
                    hp = parseParameter(gsms, paras[0]);
                    GeoLocation gloc = parseLocation(hp.mValue);
                    if(gloc == null) throw new HeaderLocationFormatException(getReturnedObj(gsms));
                    gsms.setLocation(hp.mKey, gloc);
                    pIndex = 1;
                }
                
                //setting param between ? and GeoSMS
                for(; pIndex<plen-1; pIndex++) {
                    hp = parseParameter(paras[pIndex]);
                    if(hp != null) gsms.set(hp.mKey, hp.mValue);
                }
                
                return gsms;
            }
            throw new HeaderLackParameterException(getReturnedObj(gsms));
        }
        throw new HeaderParameterNotFoundException();
    }
    
    public Object getReturnedObj(GeoSMS gsms) {
        if(mExceptionReturn instanceof QueryResult) {
            ((QueryResult) mExceptionReturn).setGeoSMS(gsms);
        }
        else {
            mExceptionReturn = gsms;
        }
        return mExceptionReturn;
    }
    
    
    public GeoLocation parseLocation(String loc) {
        String[] items = loc.split(Renderer.PAT_LOCATION_SPLIT);
        try {
            switch(items.length) {
                case 2: // COORDINATE
                    return GeoLocation.valueOf(items[0], items[1]);
                case 4: // NMEA
                    if(items[1].matches("([NSns])") && items[3].matches("([EWew])")) {
                        return GeoLocation.valueOf(NMEALocation.valueOf(items[0], items[1], items[2], items[3]));
                    }
                    break;
                case 1: // EncodedNMEA
                    NMEALocation decoded = Base64NMEALocationCodec.decodeBase64NMEALocation(items[0]); 
                    return GeoLocation.valueOf(decoded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public GeoSMSFormat parseGeoSMSTypeFormat(String fs) {
        GeoSMSFormat format = GeoSMSFormat.getByTypeString(fs);
        if(format == null) format = GeoSMSFormat.UNKNOWN;
        return format;
    }
    
    protected HeaderParameter parseParameter(GeoSMS gsms, String para) throws HeaderParameterFormatExcpetion {
        HeaderParameter hp = parseParameter(para);
        
        if(hp != null) return hp;
        throw new HeaderParameterFormatExcpetion(getReturnedObj(gsms), para);
    }
    
    public HeaderParameter parseParameter(String para) {
        String[] p = para.split(Renderer.PAT_PARAMETER_INFIX);
        HeaderParameter hp = null;
        
        switch(p.length) {
            case 2:
                hp = new HeaderParameter(p[0], p[1]);
                break;
            case 1: 
                hp = new HeaderParameter(p[0], "");
                break;
        }
        return hp; 
    }
    
    
    public class HeaderParameter {
        String mKey, mValue;
        
        public HeaderParameter(String key, String value) {
            mKey = key;
            mValue = value;
        }
    } 
    
    public interface IGeoSMSDataParserAdapter {
        public GeoSMS parse(GeoSMS gsms, String[] items);
    }
    
}
