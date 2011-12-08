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
package com.sahana.geosmser.widget;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GoogleReverseGeocodeXmlHandler extends DefaultHandler 
{
	private boolean inLocalityName = false;
	private boolean finished = false;
	private StringBuilder builder;
	private String localityName;
	
	public String getLocalityName()
	{
		return this.localityName;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	    super.characters(ch, start, length);
	    if (this.inLocalityName && !this.finished) {
	    	if ((ch[start] != '\n') && (ch[start] != ' ')) {
	    		builder.append(ch, start, length);
	    	}
	    }
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
	    super.endElement(uri, localName, name);
	    
	    if (!this.finished) {
	    	if (localName.equalsIgnoreCase("address")) { //"LocalityName"))
	    		this.localityName = builder.toString();
	    		this.finished = true;
	    	}
	    	
	    	if (builder != null) {
	    		builder.setLength(0);
	    	}
	    }
    }
	
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        builder = new StringBuilder();
    }
    
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    	super.startElement(uri, localName, name, attributes);
    	
    	if (localName.equalsIgnoreCase("address")) {
    		this.inLocalityName = true;
    	}
    	
    }
}
