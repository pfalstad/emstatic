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

public class SolidBox extends RectDragObject {

    double pot;

    SolidBox() {
	pot = 1;
    }

    SolidBox(StringTokenizer st) {
	super(st);
	pot = new Double(st.nextToken()).doubleValue();
    }

    void drawMaterials(boolean residual) {
	EMStatic.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x,
		bottomRight.y, 0, residual ? 0 : pot);
    }

    void draw() {
	super.draw();
	EMStatic.displayBoxCharge(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
    }
    
    int getDumpType() {
	return 202;
    }

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
