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

public class QuadrupoleLens extends RectDragObject {
	QuadrupoleLens() {
	    materialType = MT_CONDUCTING;
	}

	QuadrupoleLens(StringTokenizer st) {
	    super(st);
	}
	
	static native void drawLens(double cx, double cy, double xr, double yr, double dirx, double diry, int type) /*-{
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
                renderer.drawObject([coords], type);
	}-*/;

	void drawFullLens(int type) {
	    int cx = (topLeft.x+topRight.x)/2;
	    int cy = (topLeft.y+bottomLeft.y)/2;
	    int xr = (topRight.x-topLeft.x)/2;
	    int yr = (bottomLeft.y-topLeft.y)/2;
	    drawLens(cx, cy, xr, yr, 1, 0, type);
	    drawLens(cx, cy, xr, yr, -1, 0, type);
	    flipPotential();
	    drawLens(cx, cy, xr, yr, 0, 1, type);
	    drawLens(cx, cy, xr, yr, 0, -1, type);
	}

	void drawMaterials() { drawFullLens(DO_DRAW); }
	
	static native void flipPotential() /*-{
		var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
		renderer.potential = -renderer.potential;
	}-*/;
	
	boolean hitTestInside(double x, double y) { return false; }

	void draw() {
	    super.draw();
	    if (isConductor())
		drawFullLens(DO_DRAW_CHARGE);
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
	    drawFullLens(DO_CALC_CHARGE);
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
