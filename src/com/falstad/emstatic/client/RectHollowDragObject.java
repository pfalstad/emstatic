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

public abstract class RectHollowDragObject extends DragObject {
	DragHandle topLeft, topRight, bottomLeft, bottomRight;
	
	RectHollowDragObject() {
		int i;
		for (i = 0; i != 8; i++) {
			DragHandle dh = new DragHandle(this);
			handles.add(dh);
		}
		topLeft =  handles.get(0);
		topRight = handles.get(1);
		bottomRight = handles.get(2);
		bottomLeft  = handles.get(3);
		setTransform();
	}
	
	RectHollowDragObject(StringTokenizer st) {
		super(st);
		int i;
		// get 4 handles
		for (i = 0; i != 4; i++) {
		    DragHandle dh = new DragHandle(this, st);
		    handles.add(dh);
		}
		topLeft =  handles.get(0);
		bottomRight = handles.get(1);
		DragHandle h2 = handles.get(2);
		DragHandle h3 = handles.get(3);
		
		// recreate the others from those 4 and insert them into place
		topRight = new DragHandle(this, bottomRight.x, topLeft.y);
		bottomLeft = new DragHandle(this, bottomRight.x, topLeft.y);
		handles.insertElementAt(topRight, 1);
		handles.insertElementAt(bottomLeft, 3);
		handles.insertElementAt(new DragHandle(this, h3.x, h2.y), 5);
		handles.insertElementAt(new DragHandle(this, h2.x, h3.y), 7);
		rotation = new Double(st.nextToken()).doubleValue();
		setTransform();
	}
	
	String dump() {
		return super.dump() + " " + rotation;
	}
	
	boolean hitTestInside(double x, double y) {
		Point origin = rotatedOrigin();
		x -= origin.x;
		y -= origin.y;
		return x >= 0 && x <= width() && y >= 0 && y <= height(); 
	}
	
	static boolean minXHandles[] = { true, false, false, true };
	static boolean minYHandles[] = { true, true, false, false };

	// drag handle to x, y in our rotated coordinate frame
	@Override boolean dragHandle(DragHandle handle, int x, int y) {
	    int i;
	    int handleIndex = -1;
	    for (i = 0; i != 8; i++) {
	        DragHandle dh = handles.get(i);
	        if (dh == handle) {
	            handleIndex = i;
	            break;
	        }
	    }
	    if (handleIndex < 0)
	        return false;
	    
	    Point pt = new Point(x, y);
	    
	    int base = handleIndex >= 4 ? 4 : 0;
	    int baseIndex = handleIndex-base;
	    int dx = x-handles.get(handleIndex).x;
	    int dy = y-handles.get(handleIndex).y;
	    	    
	    // check that rectangle is not backwards
	    for (i = 0; i != 4; i++) {
	        DragHandle dh = handles.get(i+base);
	        Point hp = new Point(dh.x, dh.y);
	        if (minXHandles[i] && !minXHandles[baseIndex] && pt.x <= hp.x)
	            return false;
	        if (!minXHandles[i] && minXHandles[baseIndex] && pt.x >= hp.x)
	            return false;
	        if (minYHandles[i] && !minYHandles[baseIndex] && pt.y <= hp.y)
	            return false;
	        if (!minYHandles[i] && minYHandles[baseIndex] && pt.y >= hp.y)
	            return false;
	    }

	    if (handleIndex >= 4) {
		if (pt.x < topLeft.x || pt.y < topLeft.y || pt.x > bottomRight.x || pt.y > bottomRight.y)
		    return false;
	    }
	    
	    // move affected points
	    for (i = base; i != 8; i++) {
	        DragHandle dh = handles.get(i);
	        Point hp = new Point(dh.x, dh.y);
	        if (minXHandles[i % 4] == minXHandles[baseIndex])
	            hp.x += dx;
	        if (minYHandles[i % 4] == minYHandles[baseIndex])
	            hp.y += dy;
	        
	        dh.x = hp.x;
	        dh.y = hp.y;        
	    }
	    
	    return false;
	}
	
	boolean mustBeSquare() { return (flags & RectDragObject.FLAG_SQUARE) != 0; }
	
	@Override double hitTest(int x, int y) {
		double result = 1e8;
		
	    // find minimum distance to any edge
	    int i;
	    for (i = 0; i != 8; i++) {
	        DragHandle dh1 = handles.get(i); 
	        DragHandle dh2 = handles.get((i+1) % 4);
	        double d = distanceToLineSegment(x, y, dh1.x, dh1.y, dh2.x, dh2.y);
	        if (d < result)
	            result = d;
	    }

		return result;
	}
	
	@Override boolean canRotate() { return true; }

	int width() { return topRight.x-topLeft.x; }
	int height() { return bottomLeft.y-topLeft.y; }
	Point rotatedOrigin() { return new Point(topLeft.x, topLeft.y); }
	
	@Override void setInitialPosition() {
		Rectangle start = sim.findSpace(this, 50, 50);
		topLeft.x = bottomLeft.x = start.x;
		topLeft.y = topRight.y = start.y;
		bottomRight.x = topRight.x = start.x + start.width;
		bottomRight.y = bottomLeft.y = start.y + start.height;
		DragHandle dh = handles.get(4);
		int mg = 6;
		dh.x = start.x + mg;
		dh.y = start.y + mg;
		dh = handles.get(5);
		dh.x = start.x + start.width - mg;
		dh.y = start.y + mg;
		dh = handles.get(6);
		dh.x = start.x + start.width - mg;
		dh.y = start.y + start.height - mg;
		dh = handles.get(7);
		dh.x = start.x + mg;
		dh.y = start.y + start.height - mg;
	}
	
	@Override void drawSelection() {
		EMStatic.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y, 0);
		EMStatic.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y, 0); 
		EMStatic.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y, 0);
		EMStatic.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y, 0);
	}
	
	String dumpHandles() {
            String out = "";
            int i;
            // just need to dump every other one
            for (i = 0; i != handles.size(); i += 2) {
                DragHandle dh = handles.get(i);
                out += " " + dh.x + " " + dh.y;
            }
            return out;
	}

	String selectText() {
		return "" + sim.getLengthText(width()) + " x " + sim.getLengthText(height());
	}
}
