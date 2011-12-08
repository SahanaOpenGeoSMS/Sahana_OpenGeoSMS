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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.sahana.geosmser.GeoSMSPack;

import android.view.MenuItem.OnMenuItemClickListener;

public interface IDel {
	public interface IOverlayLocation<T> {
		public T getOverlay();
		public void setOverlay(T overlay);
		public GeoPoint getLocation();
		public void setLocation(GeoPoint location);
		public void moveToLocation();
		public void bindMapView(MapView mapView);
		public GeoSMSPack getSource();
		public void setSource(GeoSMSPack pack);
	}
	
	public interface IMenuEvtOverlayLocation<T> extends IOverlayLocation<T>, OnMenuItemClickListener { }  
	
	public interface ISourceBindable<T> {
		public void setSourceBinder(T binder);
	}
	
	public interface IGeoSMSPackBinder {
		public void onBind(GeoSMSPack pack);
	}
}
