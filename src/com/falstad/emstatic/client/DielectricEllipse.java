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

public class DielectricEllipse extends DielectricBox {
	DielectricEllipse() {}
	DielectricEllipse(StringTokenizer st) { super(st); }
	
	void drawMaterials(boolean residual) {
		EMStatic.drawSolidEllipse(
			(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
			(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2, speedIndex, 0);
	}

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
	
	@Override void drawSelection() {
		EMStatic.drawEllipse(
				(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
				(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
	}
	
	int getDumpType() { return 'E'; }

}