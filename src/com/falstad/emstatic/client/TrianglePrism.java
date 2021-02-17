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

public class TrianglePrism extends DielectricBox {
	
	TrianglePrism() {}
	TrianglePrism(StringTokenizer st) { super(st); }
	
	void prepare() {
		EMStatic.drawTriangle(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				speedIndex);
	}

	@Override void drawSelection() {
		EMStatic.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y, 0);
		EMStatic.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y, 0); 
		EMStatic.drawWall(bottomLeft.x, bottomLeft.y, topRight.x, topRight.y, 0);
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

	int getDumpType() { return 't'; }

}
