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
