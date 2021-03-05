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

public class ConductingBox extends RectDragObject {

    double pot;

    ConductingBox() {
	pot = 1;
    }

    ConductingBox(StringTokenizer st) {
	super(st);
	pot = new Double(st.nextToken()).doubleValue();
    }

    static native void drawMedium(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, double med, double med2) /*-{
        var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
        if (x2-x1 < renderer.minFeatureWidth)
            x2 = x4 = x1+renderer.minFeatureWidth;
        if (y3-y1 < renderer.minFeatureWidth)
            y3 = y4 = y1+renderer.minFeatureWidth;
        var medCoords = [x1, y1, x2, y2, x3, y3, x4, y4];
    	renderer.drawSolid(medCoords, med, med2, false);
    }-*/;

    static native void displayBoxCharge(int x, int y, int x2, int y2, int x3, int y3, int x4, int y4) /*-{
        var thick = 2;
        // 4 triangle strips
        var coords = [x, y, x+thick, y+thick, x2, y2, x2-thick, y2+thick,
                      x2, y2, x2-thick, y2+thick, x4, y4, x4-thick, y4-thick,
                      x4, y4, x4-thick, y4-thick, x3, y3, x3+thick, y3-thick,
                      x3, y3, x3+thick, y3-thick, x, y, x+thick, y+thick];
        var tcoords = [x, y-2, x+thick, y-2, x2, y2-2, x2-thick, y2-2,
                       x2+2, y2, x2+2, y2+thick, x4+2, y4, x4+2, y4-thick,
                       x4, y4+2, x4-thick, y4+2, x3, y3+2, x3+thick, y3+2,                       
                       x3-2, y3, x3-2, y3-thick, x-2, y, x-2, y+thick];
        @com.falstad.emstatic.client.EMStatic::renderer.displayCharge(coords, tcoords);
    }-*/;

    void drawMaterials(boolean residual) {
	drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x,
		bottomRight.y, 0, residual ? 0 : pot);
    }

    void draw() {
	super.draw();
	displayBoxCharge(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
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
