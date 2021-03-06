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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;

public class DoubleCircleMark {
	public int offsetArea = 16, offsetCenter = 3;
	public Paint areaCirclePaint, areaBorderPaint, centerCirclePaint;
	public RectF areaCircleRectF, areaBordeRectF, centerCircleRectF;
	
	public void setCircleOffset(int area, int center) {
		offsetArea = area;
		offsetCenter = center;
	}
	
	public void setPaints(Paint areaCircle, Paint centerCircle, Paint areaBorder) {
		areaCirclePaint = areaCircle;
		centerCirclePaint = centerCircle;
		areaBorderPaint = areaBorder;
	}
	
	public void setDefaultPaints() {
		areaCirclePaint = new Paint();
		areaCirclePaint.setAntiAlias(true);
		areaCirclePaint.setARGB(80, 156, 192, 36); 
		areaCirclePaint.setStrokeWidth(1);
		
		centerCirclePaint = new Paint();
		centerCirclePaint.setAntiAlias(true);
		centerCirclePaint.setStrokeWidth(1); 
		centerCirclePaint.setARGB(255, 80, 150, 30);
		
		areaBorderPaint = new Paint();
		areaBorderPaint.setAntiAlias(true);
		areaBorderPaint.setStrokeWidth(1); 
		areaBorderPaint.setARGB(255, 80, 150, 30); 
		areaBorderPaint.setStyle(Style.STROKE); 
	}
	
	public void drawDoubleCircleMark(Canvas canvas, Point point) {
		areaCircleRectF = new RectF(point.x - offsetArea, point.y - offsetArea, point.x + offsetArea, point.y + offsetArea); 
		canvas.drawOval(areaCircleRectF, areaCirclePaint); 
		
		centerCircleRectF = new RectF(point.x - offsetCenter, point.y - offsetCenter, point.x + offsetCenter, point.y + offsetCenter); 
		canvas.drawOval(centerCircleRectF, centerCirclePaint);
		
		canvas.drawOval(areaCircleRectF, areaBorderPaint); 
		//canvas.drawCircle(point.x, point.y, offsetArea, areaBorderPaint); 
	}
}
