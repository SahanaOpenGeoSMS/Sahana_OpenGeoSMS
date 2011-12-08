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

import android.net.Uri;
import android.os.Bundle;

import com.OpenGeoSMS.GeoSMS;
import com.sahana.geosmser.format.GeoSMSSahanaFormat;
import com.sahana.geosmser.widget.SMSListAdapter.SMSColumn;

public class TaskDispatch extends GeoSMSManagerAct {
	
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
