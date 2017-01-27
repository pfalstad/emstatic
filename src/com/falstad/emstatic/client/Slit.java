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

public class Slit extends Wall {
	int slitCount, slitWidth, slitSeparation;
	
	Slit() {
		slitCount = 1;
		slitWidth = 10;
		slitSeparation = 10;
	}
	
	Slit(StringTokenizer st) {
		super(st);
		slitCount = new Integer(st.nextToken()).intValue();
		slitWidth = new Integer(st.nextToken()).intValue();
		slitSeparation = new Integer(st.nextToken()).intValue();
	}
	
	String dump() {
		return super.dump() + " " + slitCount + " " + slitWidth + " " + slitSeparation;
	}
	
	void prepare() {
		DragHandle h1 = handles.get(0);
		DragHandle h2 = handles.get(1);
		double dx = h2.x-h1.x;
		double dy = h2.y-h1.y;
		double len = Math.hypot(dx, dy);
		double pos0 = (len-slitCount*slitWidth-(slitCount-1)*slitSeparation)/2;
		if (pos0 < 0)
			return;
		int i;
		double n1 = 0;
		for (i = 0; i != slitCount; i++) {
			double n2 = (pos0+(slitWidth+slitSeparation)*i)/len;
			drawWall(h1.x+dx*n1, h1.y+dy*n1, h1.x+dx*n2, h1.y+dy*n2);
			n1 = n2+slitWidth/len;
		}
		EMStatic.drawWall(h2.x, h2.y, (int)(h1.x+dx*n1), (int)(h1.y+dy*n1)); 
	}

	static void drawWall(double x1, double y1, double x2, double y2) {
		EMStatic.drawWall((int)x1, (int)y1, (int)x2, (int)y2);
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Slit Count", slitCount, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("Slit Width (m)", slitWidth*sim.lengthScale, 0, 1);
        if (n == 2)
            return new EditInfo("Slit Separation (m)", slitSeparation*sim.lengthScale, 0, 1);
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	slitCount = (int)ei.value;
        if (n == 1)
        	slitWidth = (int)(ei.value/sim.lengthScale);
        if (n == 2)
        	slitSeparation = (int)(ei.value/sim.lengthScale);
    }

	int getDumpType() { return 203; }

}
