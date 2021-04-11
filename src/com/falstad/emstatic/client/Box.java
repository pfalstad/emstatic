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

public class Box extends RectDragObject {

    Box() {
	materialType = MT_CONDUCTING;
    }

    Box(StringTokenizer st) {
	super(st);
    }

    static native JsArray getBox(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) /*-{
        var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
        if (x2-x1 < renderer.getMinFeatureWidth())
            x2 = x4 = x1+renderer.getMinFeatureWidth();
        if (y3-y1 < renderer.getMinFeatureWidth())
            y3 = y4 = y1+renderer.getMinFeatureWidth();
        var medCoords = [x1, y1, x2, y2, x4, y4, x3, y3];
        return [medCoords];
    }-*/;

    JsArray getBoundary() {
	loadTransform();
	return getBox(topLeft.x, topLeft.y, topRight.x, topRight.y, bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
    }
    
    int getDumpType() {
	return 'b';
    }

}
