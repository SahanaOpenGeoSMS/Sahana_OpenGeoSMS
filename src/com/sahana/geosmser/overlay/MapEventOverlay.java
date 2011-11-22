package com.sahana.geosmser.overlay;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapEventOverlay extends Overlay {
	private GestureDetector touchAction; 
	private MapEvtOnTouchListener mapTourchListener;
	private ITapEventListener singleTapListener;
	
	public MapEventOverlay(MapView pMapView) {
		mapTourchListener = new MapEvtOnTouchListener();
		touchAction = new GestureDetector(mapTourchListener);
		bindMap(pMapView);	
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		touchAction.onTouchEvent(e);
		return false;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if(singleTapListener != null) {
			return singleTapListener.onTap(p, mapView);
		}
		return false;
	}
	
	public void bindMap(MapView pMapView) {
		if(pMapView != null) {
			pMapView.getOverlays().add(this);
		}
	}
	
	public void setOnTapListener(ITapEventListener iEvt) {
		singleTapListener = iEvt;
	}
	
	public void setOnTapDownListener(ITouchTapEventListener iEvt) {
		mapTourchListener.tapDownListener = iEvt;
	}
	
	public void setOnDoubleTapListener(ITouchTapEventListener iEvt) {
		mapTourchListener.doubleTapListener = iEvt;
	}
	
	public interface ITapEventListener {
		boolean onTap(GeoPoint p, MapView mapView);
	}
	
	public interface ITouchTapEventListener {
		boolean onEvent(MotionEvent e);
	}
	
	private class MapEvtOnTouchListener extends GestureDetector.SimpleOnGestureListener {
		public ITouchTapEventListener tapDownListener, doubleTapListener;
		
		@Override
		public boolean onDown(MotionEvent e) {
			if(tapDownListener != null) {
				return tapDownListener.onEvent(e);
			}
			return false;
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(doubleTapListener != null) {
				return doubleTapListener.onEvent(e);
			}
			return false;	
		}
	}
}
