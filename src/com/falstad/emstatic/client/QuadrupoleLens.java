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
	
	static native void drawLens(double cx, double cy, double xr, double yr, double dirx, double diry) /*-{
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
                    coords.push(cx+i*dirx+yd*diry, cy+yd*dirx+i*diry); // , cx+i*dirx+yr*diry, cy+yr*dirx+i*diry);
        	}
        	coords.push(cx+xr*dirx+yr*diry, cy+yr*dirx+xr*diry);
                renderer.drawSolid(coords, false);
	}-*/;

	void drawMaterials() {
	    int cx = (topLeft.x+topRight.x)/2;
	    int cy = (topLeft.y+bottomLeft.y)/2;
	    int xr = (topRight.x-topLeft.x)/2;
	    int yr = (bottomLeft.y-topLeft.y)/2;
	    drawLens(cx, cy, xr, yr, 1, 0);
	    drawLens(cx, cy, xr, yr, -1, 0);
	    flipPotential();
	    drawLens(cx, cy, xr, yr, 0, 1);
	    drawLens(cx, cy, xr, yr, 0, -1);
	}

	static native void flipPotential() /*-{
		var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
		renderer.potential = -renderer.potential;
	}-*/;
	
	boolean hitTestInside(double x, double y) { return false; }

	/*
	@Override double hitTest(int x, int y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.abs(Math.sqrt(x*x/(a*a)+y*y/(b*b))-1)*a;
		return ht;
	}
	*/
	
	void draw() {
	    super.draw();
	    /*
	    if (isConductor())
		doEllipseCharge(false, (topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
		    (topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
		    */
	}

	    static native void doEllipseCharge(boolean calc, double cx, double cy, double xr, double yr) /*-{
		var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
		var margin = (calc) ? 5 : 2;
        	var insetMultX = (xr-margin)/xr;
        	var insetMultY = (yr-margin)/yr;
        	var outMultX = 2/xr;
        	var outMultY = 2/yr;
        	var tcx = cx;
        	var tcy = cy;
        	if (!calc) {
        	    outMultX += 1;
        	    outMultY += 1;
        	} else
        	    tcx = tcy = 0;
        	var i;
        	var coords = [];
        	var tcoords = [];
        	for (i = -xr; i <= xr; i++) {
                	coords.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
                	coords.push(cx-i*insetMultX, cy-yr*insetMultY*Math.sqrt(1-i*i/(xr*xr)));
                	tcoords.push(tcx-i*outMultX, tcy-yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
                	tcoords.push(tcx-i*outMultX, tcy-yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
        	}
        	for (i = xr-1; i >= -xr; i--) {
                	coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
                	coords.push(cx-i*insetMultX, cy+yr*insetMultY*Math.sqrt(1-i*i/(xr*xr)));
                	tcoords.push(tcx-i*outMultX, tcy+yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
                	tcoords.push(tcx-i*outMultX, tcy+yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
        	}
        	if (calc) {
        	    for (i = 0; i != coords.length; i++)
        	    	tcoords[i] = Math.round(tcoords[i]);
        	    renderer.calcCharge(coords, tcoords);
        	} else
                    renderer.displayCharge(coords, tcoords);	    
	}-*/;
	    
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
	    doEllipseCharge(true, (topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2, (topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
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
