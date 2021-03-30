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

public class HollowBox extends RectHollowDragObject {

    HollowBox() {
	materialType = MT_CONDUCTING;
    }

    HollowBox(StringTokenizer st) {
	super(st);
    }

    static native JsArray getBox(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) /*-{
        var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
        if (x2-x1 < renderer.getMinFeatureWidth())
            x2 = x1+renderer.getMinFeatureWidth();
        if (y2-y1 < renderer.getMinFeatureWidth())
            y2 = y1+renderer.getMinFeatureWidth();
        if (x3-x1 < renderer.getMinFeatureWidth())
            x3 = x1+renderer.getMinFeatureWidth();
        if (x2-x4 < renderer.getMinFeatureWidth())
            x4 = x2-renderer.getMinFeatureWidth();
        if (y3-y1 < renderer.getMinFeatureWidth())
            y3 = x1+renderer.getMinFeatureWidth();
        if (y2-y4 < renderer.getMinFeatureWidth())
            y4 = y2-renderer.getMinFeatureWidth();
        var medCoords = [[x1, y1, x2, y1, x2, y2, x1, y2]];
        if (!(x4 < x3 || y4 < y3))
            medCoords.push([x3, y3, x3, y4, x4, y4, x4, y3]);
        return medCoords;
    }-*/;

    
    JsArray getBoundary() {
	loadTransform();
	DragHandle itl = handles.get(4);
	DragHandle ibr = handles.get(6);
	return getBox(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, itl.x, itl.y, ibr.x, ibr.y);
    }
    
    String selectText() { return super.selectText() + " " + sim.getUnitText(conductorCharge, "C"); }
    
    int getDumpType() {
	return 'B';
    }

}
