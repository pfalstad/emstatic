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

public class ChargedEllipse extends ChargedBox {
	ChargedEllipse() {}
	ChargedEllipse(StringTokenizer st) { super(st); }
	
	void drawCharge() {
	    double a = (topRight.x-topLeft.x)/2;
	    double b = (bottomLeft.y-topLeft.y)/2;
	    double area = a*b*Math.PI;
		drawChargedEllipse(
			(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
			(int)a, (int)b, charge);
	}

        static native void drawChargedEllipse(int x1, int y1, int rx, int ry, double chg) /*-{
        	@com.falstad.emstatic.client.EMStatic::renderer.drawSolidEllipse(x1, y1, rx, ry, undefined, chg);
	}-*/;


	@Override double hitTest(int x, int y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.abs(Math.sqrt(x*x/(a*a)+y*y/(b*b))-1)*a;
		return ht;
	}
	
	/*
	@Override boolean hitTestInside(double x, double y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.sqrt(x*x/(a*a)+y*y/(b*b));
		return ht <= 1;
	}
	*/

	static native void drawEllipse(int x1, int y1, int rx, int ry) /*-{
	     	@com.falstad.emstatic.client.EMStatic::renderer.drawEllipse(x1, y1, rx, ry);
	}-*/;

	@Override void drawSelection() {
		drawEllipse(
				(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
				(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
	}
	
	int getDumpType() { return 204; }

}
