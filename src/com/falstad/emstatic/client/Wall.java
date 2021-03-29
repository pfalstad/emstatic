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

import com.google.gwt.core.client.JsArray;

public class Wall extends DragObject {

	Wall() {
		handles.add(new DragHandle(this));
		handles.add(new DragHandle(this));
		setTransform();
		materialType = MT_CONDUCTING;
	}
	
	Wall(int x1, int y1, int x2, int y2) {
		handles.add(new DragHandle(this, x1, y1));
		handles.add(new DragHandle(this, x2, y2));
		setTransform();
		materialType = MT_CONDUCTING;
	}

	Wall(StringTokenizer st) {
		super(st);
		handles.add(new DragHandle(this, st));
		handles.add(new DragHandle(this, st));
		setTransform();
	}

	double wallLen;
	
	void setWallTransform() {
	    wallLen = Math.hypot(handles.get(0).x-handles.get(1).x, handles.get(0).y-handles.get(1).y);
	    double cx = (handles.get(0).x+handles.get(1).x)/2;
	    double cy = (handles.get(0).y+handles.get(1).y)/2;
	    double xf0 = (handles.get(0).y-cy)*2/wallLen;
	    double xf1 = (handles.get(0).x-cx)*2/wallLen;
	    EMStatic.setTransform(xf0, xf1, cx, -xf1, xf0, cy);
	}

	JsArray getBoundary() {
	    setWallTransform();
	    int len2 = (int) (wallLen/2);
	    return Box.getBox(-2, -len2, 2, -len2, -2, len2, 2, len2); 
	}
	    
	void draw() {
	    super.draw();
	    drawChargeWithBoundary(getBoundary());
	}
	
	String selectText() { return super.selectText() + " " + sim.getUnitText(conductorCharge, "C"); }
	
	public EditInfo getEditInfo(int n) {
	    return super.getEditInfo(n+1);
	}

	public void setEditValue(int n, EditInfo ei) {
	    super.setEditValue(n+1, ei);
	}

	int getDumpType() { return 'w'; }
}
