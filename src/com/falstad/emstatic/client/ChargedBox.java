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

public class ChargedBox extends RectDragObject {

    double charge;

    ChargedBox() {
	charge = 1;
    }

    ChargedBox(StringTokenizer st) {
	super(st);
	charge = new Double(st.nextToken()).doubleValue();
    }

    void drawCharge() {
	double area = (topRight.x-topLeft.x)*(bottomRight.y-topRight.y);
	EMStatic.drawChargedBox(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x,
		bottomRight.y, charge/area);
    }

    int getDumpType() {
	return 203;
    }

    public EditInfo getEditInfo(int n) {
	if (n == 0)
	    return new EditInfo("Charge", charge, 0, 1);

	return null;
    }

    public void setEditValue(int n, EditInfo ei) {
	if (n == 0)
	    charge = ei.value;
    }

    String dump() {
	return super.dump() + " " + charge;
    }

}
