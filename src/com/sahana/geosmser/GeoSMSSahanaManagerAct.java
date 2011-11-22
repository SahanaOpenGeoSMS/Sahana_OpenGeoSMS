package com.sahana.geosmser;

import android.net.Uri;
import android.os.Bundle;

import com.OpenGeoSMS.GeoSMS;
import com.sahana.geosmser.format.GeoSMSSahanaFormat;
import com.sahana.geosmser.widget.SMSListAdapter.SMSColumn;

public class GeoSMSSahanaManagerAct extends GeoSMSManagerAct {
	
	@Override
	public String getDefaultFilterString() {
		// TODO Auto-generated method stub
		String task = GeoSMSSahanaFormat.TASK.getTypeString();
		return "http://*[?]*&" + GeoSMS.CONST_GEOSMS_PARA_KEY + "=" + GeoSMSSahanaFormat.TASK.getTypeString() + "*";
	}

	@Override
	public String getVersion2FilterString() {
		// TODO Auto-generated method stub
		return "GeoSMS/2;*;*;ST;*";
	}
	
}
