/*
    Copyright (C) 2017 by Paul Falstad

    This file is part of RippleGL.

    RippleGL is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    RippleGL is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RippleGL.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.falstad.emstatic.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Ellipse extends RectDragObject {
	Ellipse(boolean square) {
	    materialType = MT_CONDUCTING;
	    flags = (square) ? FLAG_SQUARE : 0;
	}

	Ellipse(StringTokenizer st) {
	    super(st);
	}
	
	static native void drawSolidEllipse(double cx, double cy, double xr, double yr, int type) /*-{
		var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	        var coords = []; // cx, cy];
        	var i;
        	xr = Math.max(xr, renderer.getMinFeatureWidth());
        	yr = Math.max(yr, renderer.getMinFeatureWidth());
        	for (i = -xr; i <= xr; i++)
                    coords.push(cx+i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
        	for (i = -xr; i <= xr; i++)
                    coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
                renderer.drawObject([coords], type);
	}-*/;

	void drawMaterials() {
		drawSolidEllipse(
			(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
			(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2, DO_DRAW);
	}

//	boolean hitTestInside(double x, double y) { return false; }

	@Override double hitTest(int x, int y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.abs(Math.sqrt(x*x/(a*a)+y*y/(b*b))-1)*a;
		return ht;
	}
	
	void draw() {
	    if (isConductor())
		drawSolidEllipse((topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2, (topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2, DO_DRAW_CHARGE);
	    super.draw();
	}

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
	
	void calcCharge() {
	    drawSolidEllipse((topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2, (topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2, DO_CALC_CHARGE);
	}
	
        static native void drawFocus(JavaScriptObject renderer, int x, int y) /*-{
        	renderer.drawFocus(x, y);
	}-*/;


	int getDumpType() { return 'e'; }

	String selectText() {
	    return super.selectText() + " " + sim.getUnitText(conductorCharge, "C");
	}
}
