package com.sahana.geosmser;

import com.sahana.geosmser.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

public class Homepage extends Activity {
    
    private boolean active = true;
    private int splashTime = 3000;
    private static final String CLASS_TAG = Homepage.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.homepage_sahana);
        
        // thread for displaying the homepage
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (active && (waited < splashTime)) {
                        sleep(100);
                        if (active) {
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                	Log.e(CLASS_TAG, e.toString());
                } finally {
                    startActivity(new Intent(Homepage.this, Dashboard.class));
                    finish();
                }
            }
        };
        thread.start();
     
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            active = false;
        }
        return true;
    }

}





