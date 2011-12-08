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
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.sahana.geosmser.overlay.IDel.IGeoSMSPackBinder;
import com.sahana.geosmser.overlay.IDel.ISourceBindable;

public class SingleLocationOverlay extends DoubleCircleOverlay implements ISourceBindable<IGeoSMSPackBinder> {
	protected DoubleCircleMark mark;
	protected IGeoSMSPackBinder sourceBinder;
	
	public SingleLocationOverlay() {
		init();
	}
	
	private void init() {		
		mark = new DoubleCircleMark();
		initMark();
		this.setMark(mark);
	}
	
	public void initMark() {
		mark.setDefaultPaints();
	}
	
	public GeoPoint getLocation() {
		return this.Position;		
	}
	
	public boolean moveToLocation(MapView mapView) {
		if(mapView != null) {
			MapController mc = mapView.getController();
			GeoPoint gloc = this.getLocation();
			if(gloc != null) {
				mc.animateTo(gloc);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setSourceBinder(IGeoSMSPackBinder binder) {
		sourceBinder = binder;
	}
	
}
