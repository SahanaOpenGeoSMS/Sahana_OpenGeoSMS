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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class GeoSMSServiceControlAct extends Activity {
	public Button btnStartGSService, btnEndGSService;
	public final String TAG = "GeoSMSServiceControlAct";
	public Activity ThisAct = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		render();
		EventInit();
	}

	private void EventInit() {
		btnStartGSService.setOnClickListener(new Evt_StartGSServiceOnClickListener());
		btnEndGSService.setOnClickListener(new Evt_EndGSServiceOnClickListener());
	}

	private void render() {
		LinearLayout mainPanel = new LinearLayout(this);
		mainPanel.setOrientation(LinearLayout.VERTICAL);
    	ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	btnStartGSService = new Button(this);
    	btnStartGSService.setText("Start Service");
        
    	btnEndGSService = new Button(this);
    	btnEndGSService.setText("End Service");
        
        mainPanel.addView(btnStartGSService, lparams);
        mainPanel.addView(btnEndGSService, lparams);
    	setContentView(mainPanel);
	}
	
	
	public class Evt_StartGSServiceOnClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			Log.w(TAG, "Start Service");
			Intent mainIntent = new Intent(ThisAct, GeoSMSService.class);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			ThisAct.startService(mainIntent); 
		}
	}
	
	public class Evt_EndGSServiceOnClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			Log.w(TAG, "Stop Service");
			stopService(new Intent(ThisAct, GeoSMSService.class));			
		}
	}	
	
}
