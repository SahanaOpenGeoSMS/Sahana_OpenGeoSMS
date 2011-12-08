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
