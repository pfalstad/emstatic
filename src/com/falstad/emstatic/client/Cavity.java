package com.falstad.emstatic.client;

import com.gargoylesoftware.htmlunit.javascript.host.Console;

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

public class Cavity extends RectDragObject {
	int slitWidth;
	
	Cavity() {
		slitWidth = 10000;
	}
	Cavity(StringTokenizer st) {
		super(st);
		slitWidth = 10000;
		try {
			slitWidth = new Integer(st.nextToken()).intValue();
		} catch (Exception e) {}
	}

	Cavity(int x1, int y1, int x2, int y2) {
		super();
		topLeft.x = bottomLeft.x = x1;
		topLeft.y = topRight.y = y1;
		bottomRight.x = topRight.x = x2;
		bottomRight.y = bottomLeft.y = y2;
		slitWidth = 10000;
		setTransform();
	}

	String dump() {
		return super.dump() + " " + slitWidth;
	}

	void prepare() {
		EMStatic.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
		EMStatic.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
		EMStatic.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
		DragHandle h1 = topLeft;
		DragHandle h2 = topRight;
		double dx = h2.x-h1.x;
		double dy = h2.y-h1.y;
		double len = Math.hypot(dx, dy);
		if (slitWidth > len)
			return;
		double n1 = .5-slitWidth/len/2;
		Slit.drawWall(h1.x, h1.y, h1.x+dx*n1, h1.y+dy*n1);
		Slit.drawWall(h2.x, h2.y, h2.x-dx*n1, h2.y-dy*n1);
	}

	@Override void drawSelection() {
		prepare();
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

	int getDumpType() { return 'c'; }
	
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
        	int w = (slitWidth > width()) ? width() : slitWidth;
            return new EditInfo("Opening Width (m)", w*sim.lengthScale, 0, 1);
        }
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	slitWidth = (int)(ei.value/sim.lengthScale);
    }

}
