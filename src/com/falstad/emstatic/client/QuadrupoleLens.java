/*
    Copyright (C) 2017 by Paul Falstad

    This file is part of EMStatic.

    EMStatic is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    EMStatic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EMStatic.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.falstad.emstatic.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class QuadrupoleLens extends RectDragObject {
	QuadrupoleLens() {
	    materialType = MT_CONDUCTING;
	}

	QuadrupoleLens(StringTokenizer st) {
	    super(st);
	}
	
	@SuppressWarnings("rawtypes")
	static native JsArray getLensPiece(double cx, double cy, double xr, double yr, double dirx, double diry) /*-{
		var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	        var coords = [];
        	var i;
        	xr = Math.max(xr, renderer.getMinFeatureWidth());
        	yr = Math.max(yr, renderer.getMinFeatureWidth());
        	var h = yr*.4;
        	for (i = -xr; i <= xr; i++) {
        	    var yd = Math.sqrt(i*i+h*h);
        	    if (yd > yr)
        	    	continue;
                    coords.push(cx+i*dirx+yd*diry, cy+yd*dirx+i*diry); //, cx+i*dirx+yr*diry, cy+yr*dirx+i*diry);
        	}
        	return coords;
	}-*/;

	@SuppressWarnings("rawtypes")
	static native void writeLensPieces(JsArray arr1, JsArray arr2, boolean flip) /*-{
	    var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	    if (flip)
	    	renderer.potential = -renderer.potential;
	    renderer.writeShape([arr1, arr2]);
	}-*/;
	
	@SuppressWarnings("rawtypes")
	void writeMaterials() {
	    int cx = (topLeft.x+topRight.x)/2;
	    int cy = (topLeft.y+bottomLeft.y)/2;
	    int xr = (topRight.x-topLeft.x)/2;
	    int yr = (bottomLeft.y-topLeft.y)/2;
	    JsArray arr1 = getLensPiece(cx, cy, xr, yr, 1, 0);
	    JsArray arr2 = getLensPiece(cx, cy, xr, yr, -1, 0);
	    JsArray arr3 = getLensPiece(cx, cy, xr, yr, 0, 1);
	    JsArray arr4 = getLensPiece(cx, cy, xr, yr, 0, -1);
	    writeLensPieces(arr1, arr2, false);
	    writeLensPieces(arr3, arr4, true);
	}

	@SuppressWarnings("rawtypes")
	static native JsArray getFullLens(JsArray arr1, JsArray arr2, JsArray arr3, JsArray arr4) /*-{
	    return [arr1, arr2, arr3, arr4];
	}-*/;

        native void drawFieldLinesShape(JsArray bound) /*-{
	    var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	    renderer.drawFieldLinesShape([bound[0]]);
	    renderer.drawFieldLinesShape([bound[1]]);
	    renderer.drawFieldLinesShape([bound[2]]);
	    renderer.drawFieldLinesShape([bound[3]]);
	}-*/;

	
	@SuppressWarnings("rawtypes")
	JsArray getBoundary() {
	    loadTransform();
	    int cx = (topLeft.x+topRight.x)/2;
	    int cy = (topLeft.y+bottomLeft.y)/2;
	    int xr = (topRight.x-topLeft.x)/2;
	    int yr = (bottomLeft.y-topLeft.y)/2;
	    JsArray arr1 = getLensPiece(cx, cy, xr, yr, 1, 0);
	    JsArray arr2 = getLensPiece(cx, cy, xr, yr, -1, 0);
	    JsArray arr3 = getLensPiece(cx, cy, xr, yr, 0, 1);
	    JsArray arr4 = getLensPiece(cx, cy, xr, yr, 0, -1);
	    return getFullLens(arr1, arr2, arr3, arr4);
	}
	
	boolean hitTestInside(double x, double y) { return false; }

	@Override void drawSelection() {
	    //		drawMaterials(false);
	    double a = (topRight.x-topLeft.x)/2;
	    double b = (bottomRight.y-topRight.y)/2;
	    int fc = (int)Math.sqrt(Math.abs(a*a-b*b));
	    int fd = fc;
	    if (a > b)
		fd = 0;
	    else
		fc = 0;
	    drawFocus(sim.renderer, (topLeft.x+topRight.x)/2-fc, (topLeft.y+bottomLeft.y)/2-fd);
	    drawFocus(sim.renderer, (topLeft.x+topRight.x)/2+fc, (topLeft.y+bottomLeft.y)/2+fd);
	}
	
	boolean mustBeSquare() { return true; }

        static native void drawFocus(JavaScriptObject renderer, int x, int y) /*-{
        	renderer.drawFocus(x, y);
	}-*/;


	int getDumpType() { return 'q'; }

	String selectText() {
	    return super.selectText() + " " + sim.getUnitText(conductorCharge, "C");
	}
	
	public EditInfo getEditInfo(int n) {
	    return super.getEditInfo(n+1);
	}

	public void setEditValue(int n, EditInfo ei) {
	    super.setEditValue(n+1, ei);
	}

}
