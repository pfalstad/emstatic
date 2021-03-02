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

public class Wall extends DragObject {

    double pot;
    
	Wall() {
		handles.add(new DragHandle(this));
		handles.add(new DragHandle(this));
		pot = 1;
		setTransform();
	}
	
	Wall(int x1, int y1, int x2, int y2) {
		handles.add(new DragHandle(this, x1, y1));
		handles.add(new DragHandle(this, x2, y2));
		setTransform();
	}

	Wall(StringTokenizer st) {
		super(st);
		handles.add(new DragHandle(this, st));
		handles.add(new DragHandle(this, st));
		setTransform();
		pot = Double.parseDouble(st.nextToken());
	}

	/*
	    void drawMaterials(boolean residual) {
		EMStatic.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x,
			bottomRight.y, 0, residual ? 0 : pot);
	    }

	    void draw() {
		super.draw();
		EMStatic.displayBoxCharge(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
	    }
	    */
	   
	double wallLen;
	
	void setWallTransform() {
	    wallLen = Math.hypot(handles.get(0).x-handles.get(1).x, handles.get(0).y-handles.get(1).y);
	    double cx = (handles.get(0).x+handles.get(1).x)/2;
	    double cy = (handles.get(0).y+handles.get(1).y)/2;
	    /*
	    double xf[] = new double[6];
	    xf[0] = xf[4] = (handles.get(0).y-cy)*2/wallLen;
	    xf[1] = (handles.get(0).x-cx)*2/wallLen;
            xf[3] = -xf[1];
//            xf[2] = (1-xf[0])*cx - xf[1]*cy;
//            xf[5] = -xf[3]*cx + (1-xf[4])*cy;
            xf[2] = cx;
            xf[5] = cy;
            EMStatic.setTransform(xf[0], xf[1], xf[2], xf[3], xf[4], xf[5]);
            */
	    double xf0 = (handles.get(0).y-cy)*2/wallLen;
	    double xf1 = (handles.get(0).x-cx)*2/wallLen;
	    EMStatic.setTransform(xf0, xf1, cx, -xf1, xf0, cy);
	}

	void drawMaterials(boolean residual) {
	    setWallTransform();
	    /*
	    int len = (int)Math.hypot(handles.get(0).x-handles.get(1).x, handles.get(0).y-handles.get(1).y);
	    int cx = (int) ((handles.get(0).x+handles.get(1).x)/2);
	    int cy = (int) ((handles.get(0).y+handles.get(1).y)/2);
	    cx = cy = 0;
	    EMStatic.drawMedium(cx-2, cy-len/2, cx+2, cy-len/2, cx-2, cy+len/2, cx+2, cy+len/2, 0, residual ? 0 : pot);
	    */
	    int len2 = (int) (wallLen/2);
	    ConductingBox.drawMedium(-2, -len2, 2, -len2, -2, len2, 2, len2, 0, residual ? 0 : pot); 
	}
	
	void draw() {
	    super.draw();
	    setWallTransform();
	    /*
	    int len = (int)Math.hypot(handles.get(0).x-handles.get(1).x, handles.get(0).y-handles.get(1).y);
	    int cx = (int) ((handles.get(0).x+handles.get(1).x)/2);
	    int cy = (int) ((handles.get(0).y+handles.get(1).y)/2);
	    cx = cy = 0;
	    EMStatic.displayBoxCharge(cx-2, cy-len/2, cx+2, cy-len/2, cx-2, cy+len/2, cx+2, cy+len/2);
	    */
	    int len2 = (int) (wallLen/2);
	    ConductingBox.displayBoxCharge(-2, -len2, 2, -len2, -2, len2, 2, len2);
	}
	
	/*
	void drawSelection() {
	    drawMaterials(false);
	}
	*/
	
	/*
	void drawMaterials(boolean residual) {
		EMStatic.drawWall(handles.get(0).x, handles.get(0).y, handles.get(1).x, handles.get(1).y, residual ? 0 : pot);
	}
	*/
	
	int getDumpType() { return 'w'; }

	    public EditInfo getEditInfo(int n) {
		if (n == 0)
		    return new EditInfo("potential", pot, 0, 1);

		return null;
	    }

	    public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
		    pot = ei.value;
	    }

	    String dump() {
		return super.dump() + " " + pot;
	    }

}
