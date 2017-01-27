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

public class MovingWall extends RectDragObject {

	int moveDuration, pauseDuration;
	int startIter;
	int phase;
	int lastWallX1, lastWallX2, lastWallY1, lastWallY2;
	
	MovingWall() {
		moveDuration = 900;
		pauseDuration = 500;
		startIter = 0;
		phase = 0;
	}
	
	MovingWall(StringTokenizer st) {
    	super(st);
    	moveDuration = new Integer(st.nextToken()).intValue();
    	pauseDuration = new Integer(st.nextToken()).intValue();
    }

	String dump() { return super.dump() + " " + moveDuration + " " + pauseDuration; }
	
	void run() {
        if (sim.iters < startIter)
        	startIter = sim.iters;
		
        EMStatic.setTransform(transform[0], transform[1], transform[2], transform[3], transform[4], transform[5]);
		EMStatic.clearWall(lastWallX1, lastWallY1, lastWallX2, lastWallY2);
		int x1 = topLeft.x;
		int x2 = topRight.x;
		int y1 = topLeft.y;
		int y2 = bottomLeft.y;
		double dur = (phase == 0 || phase == 2) ? moveDuration : pauseDuration;
		double step = (sim.iters-startIter)/dur;
		if (step > 1) {
			step = 1;
			startIter = sim.iters;
			phase++;
			if (phase >= 4)
				phase = 0;
			step = 0;
		}
		if (phase == 1)
			step = 1;
		if (phase == 3)
			step = 0;
		if (phase == 2)
			step = 1-step;
		int y = (int) (y1+(y2-y1)*step); 
		lastWallX1 = x1;
		lastWallY1 = y;
		lastWallX2 = x2;
		lastWallY2 = y;
		EMStatic.drawWall(x1, y, x2, y);
		EMStatic.setTransform(1,0,0,0,1,0);
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Move Duration (iters)", moveDuration, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("Pause Duration (iters)", pauseDuration, 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	moveDuration = (int)ei.value;
        if (n == 1)
        	pauseDuration = (int)ei.value;
    }

	String selectText() {
		String s = super.selectText();
		return s + ", v = " + sim.getSpeedText(height()/(double)moveDuration);
	}

	int getDumpType() { return 'W'; }

}
