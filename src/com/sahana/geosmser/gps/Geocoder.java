package com.sahana.geosmser.gps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.OpenGeoSMS.GeoLocation;
import com.sahana.geosmser.widget.GoogleReverseGeocodeXmlHandler;

import android.location.Location;

public class Geocoder {
	public static String reverseGeocode(GeoLocation loc) {
	    //http://maps.google.com/maps/geo?q=40.714224,-73.961452&output=json&oe=utf8&sensor=true_or_false&key=your_api_key
		String localityName = "";
	    HttpURLConnection connection = null;
	    URL serverAddress = null;

	    try {
	        // build the URL using the latitude & longitude you want to lookup
	        // NOTE: I chose XML return format here but you can choose something else
	        // http://maps.google.com/maps/geo?q=_,_&output=xml&oe=utf8&sensor=true
	        serverAddress = new URL("http://maps.google.com/maps/geo?q="
	                + Double.toString(loc.getLatitude())
	                + "," + Double.toString(loc.getLongitude())
	                + "&output=xml&oe=utf8&sensor=true");
	        
	        //set up out communications stuff
	        connection = null;
		      
	        //Set up the initial connection
			connection = (HttpURLConnection)serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
		                  
			connection.connect();
		    
			try {
				InputStreamReader isr = new InputStreamReader(connection.getInputStream());
				InputSource source = new InputSource(isr);
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				XMLReader xr = parser.getXMLReader();
				GoogleReverseGeocodeXmlHandler handler = new GoogleReverseGeocodeXmlHandler();
				xr.setContentHandler(handler);
				xr.parse(source);
				
				localityName = handler.getLocalityName();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    
	    return localityName;
	}
	
	public class CountryNameRetriveHandler extends DefaultHandler {
	    private StringBuilder mCountryName;
	    private boolean isFinished = false;
	    private boolean inCountryName = false;
	    private String mTargetName;
	    
	    public CountryNameRetriveHandler(String name) {
	        this.mTargetName = name;
	    }
	    
	    public String getResult() {
	        return mCountryName.toString();
	    }
	    
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
            if (inCountryName && !isFinished) {
                if ((ch[start] != '\n') && (ch[start] != ' ')) {
                    mCountryName.append(ch, start, length);
                }
            }
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            if (!isFinished) {
                if (localName.equalsIgnoreCase(mTargetName)) {
                    
                    isFinished = true;
                }
                
            }
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            mCountryName = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(mTargetName)) inCountryName = true;
        }
	    
	}
}
