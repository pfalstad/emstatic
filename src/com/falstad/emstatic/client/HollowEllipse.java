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

public class HollowEllipse extends RectHollowDragObject {

    HollowEllipse(boolean square) {
	materialType = MT_CONDUCTING;
	flags = (square) ? RectDragObject.FLAG_SQUARE : 0;
    }

    HollowEllipse(StringTokenizer st) {
	super(st);
    }

    static native JsArray getEllipse(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) /*-{
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
            
        var coords = [], coords2 = [];
       	var i;
       	var xr = Math.floor(x2-x1)/2;
       	var yr = Math.floor(y2-y1)/2;
       	var cx = Math.floor((x1+x2)*.5);
       	var cy = Math.floor((y1+y2)*.5);
       	for (i = -xr; i <= xr; i++)
            coords.push(cx+i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
       	for (i = -xr; i <= xr; i++)
            coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
       	xr = Math.floor(x4-x3)/2;
       	yr = Math.floor(y4-y3)/2;
       	cx = Math.floor((x3+x4)*.5);
       	cy = Math.floor((y3+y4)*.5);
       	for (i = -xr; i <= xr; i++)
            coords2.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
       	for (i = -xr; i <= xr; i++)
            coords2.push(cx+i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
        return [coords, coords2]; 
    }-*/;

    JsArray getBoundary() {
	DragHandle itl = handles.get(4);
	DragHandle ibr = handles.get(6);
	return getEllipse(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, itl.x, itl.y, ibr.x, ibr.y);
    }
    
    String selectText() { return super.selectText() + " " + sim.getUnitText(conductorCharge, "C"); }
    
    int getDumpType() {
	return 'E';
    }

}
