package com.sahana.geosmser.parser;

import com.OpenGeoSMS.Direction;
import com.OpenGeoSMS.GeoLocation;
import com.OpenGeoSMS.GeoLocationPart;
import com.OpenGeoSMS.GeoSMS;
import com.OpenGeoSMS.NMEALocationPart;
import com.OpenGeoSMS.exception.GeoSMSException;
import com.OpenGeoSMS.format.GeoSMSFormat;
import com.OpenGeoSMS.parser.HeaderGeoSMSNotFoundException;
import com.OpenGeoSMS.parser.HeaderLocationFormatException;
import com.OpenGeoSMS.parser.HeaderParameterNotFoundException;
import com.OpenGeoSMS.parser.HeaderUnknownGeoSMSTypeFormatException;

public class GeoSMSParserV2 {
	private final static String PAT_V2_ITEM_SPLIT = ";";
	private final static String PAT_V2_LOCTION_SPLIT = ",";
	private final static String PAT_FAST_URL = "GeoSMS/2;.+;.+;[ABEPQ];.*";
	
	public boolean checkStructure(String sms) {
		return sms.matches(PAT_FAST_URL);
	}
	
	public GeoSMS parse(String sms) throws GeoSMSException {
		String[] raw = sms.split(PAT_V2_ITEM_SPLIT);
		int rLen = raw.length;
		GeoSMS gsms = null;
		
		if(rLen > 3 && rLen < 6) {
			gsms = new GeoSMS();
			
			if(!raw[0].equals("GeoSMS/2")) throw new HeaderGeoSMSNotFoundException(null);
			gsms.setDomainNamePath("http://maps.google.com/");
			
			gsms.setGeoSMSFormat(parseGeoSMSTypeFormat(raw[3]));
			if(gsms.getGeoSMSFormat().equals(GeoSMSFormat.UNKNOWN)) throw new HeaderUnknownGeoSMSTypeFormatException(gsms, raw[3]);
			
			if(!gsms.getGeoSMSFormat().equals(GeoSMSFormat.QUERY)) {
				GeoLocationPart latPart = parseLocationPart(raw[1]);
				GeoLocationPart lngPart = parseLocationPart(raw[2]);
				if(latPart == null || lngPart == null) throw new HeaderLocationFormatException(gsms);
				
				gsms.setLocation("q", new GeoLocation(latPart, lngPart));
				if(rLen == 5) {
					gsms.setText(raw[4].replaceAll("/", "\n"));
				}
			}
			return gsms;
		}
		throw new HeaderParameterNotFoundException();
		
	}
	
	public GeoSMSFormat parseGeoSMSTypeFormat(String fs) {
		GeoSMSFormat format = GeoSMSFormat.getByTypeString(fs);
		if(format == null) format = GeoSMSFormat.UNKNOWN;
		return format;
	}
	
	public GeoLocationPart parseLocationPart(String loc) {
		GeoLocationPart part = null;
		
		try {
			String[] spart = loc.split(PAT_V2_LOCTION_SPLIT);
			if(spart.length == 2) {
				Direction dict = Direction.getByTypeString(spart[1]);
				if(dict != null) {
					NMEALocationPart nmeaPart = new NMEALocationPart(Double.parseDouble(spart[0]), dict);
					return GeoLocationPart.valueOf(nmeaPart);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return part;
	}
}
