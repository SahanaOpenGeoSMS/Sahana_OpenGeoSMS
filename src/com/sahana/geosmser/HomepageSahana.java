package com.sahana.geosmser;

import com.sahana.geosmser.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class HomepageSahana extends Activity {
    
    private ImageView homepageImage;
    private ImageView homepageLogo;
    private Context baseContext;
	private int mScreenOriention;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //original
        //setContentView(R.layout.homepage);
        //init();
        
        //Ye revised
        setContentView(R.layout.homepage_sahana);
     
        //init();
    }

    public void init() {
        baseContext = this;
        
        //homepageImage = (ImageView) findViewById(R.id.imgHomepageImage);        
        //homepageLogo = (ImageView) findViewById(R.id.imgHomepageLogo);
        //homepageLogo = (ImageView) findViewById(R.id.imgHomepageLogo_sms);
        AlphaAnimation anim = new AlphaAnimation(0f, 100f);
        anim.setRepeatCount(Animation.ABSOLUTE);
        anim.setDuration(15000);
        homepageLogo.startAnimation(anim);

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //startActivity(new Intent(Homepage.this, MainAct.class));
        	startActivity(new Intent(HomepageSahana.this, MenusSahana.class));
            finish();
            
        }
        return super.onTouchEvent(event);
    }
}
