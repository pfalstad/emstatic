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

import java.util.Vector;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;

public abstract class DragObject implements Editable {
	Vector<DragHandle> handles;
	boolean selected;
	EMStatic sim;
	double rotation;
	double transform[];
	double invTransform[];
	double centerX, centerY;  // set in setTransform()
	double conductorCharge; // calculated
	double chargeDensity;
	double potential;
	double permittivity;
	double totalChargeFloating;
	int materialType;
	static final int MT_OTHER = 0;
	static final int MT_CHARGED = 1;
	static final int MT_CONDUCTING = 2;
	static final int MT_DIELECTRIC = 3;
	static final int MT_FLOATING = 4;
	static final int DO_DRAW = 0;
	static final int DO_DRAW_CHARGE = 1;
	static final int DO_CALC_CHARGE = 2;
	static DragObject currentFloatingConductor;
	
	int flags;
	
	DragObject() {
	    handles = new Vector<DragHandle>(4);
	    sim = EMStatic.theSim;
	    sim.recalcAndRepaint();
	    setTransform();
	}
	
	DragObject(StringTokenizer st) {
	    handles = new Vector<DragHandle>(4);
	    sim = EMStatic.theSim;
	    sim.recalcAndRepaint();
	    flags = Integer.parseInt(st.nextToken());
	    materialType = Integer.parseInt(st.nextToken());
	    if (materialType == MT_CHARGED)
		chargeDensity = Double.parseDouble(st.nextToken());
	    else if (materialType == MT_CONDUCTING)
		potential = Double.parseDouble(st.nextToken());
	    else if (materialType == MT_DIELECTRIC)
		permittivity = Double.parseDouble(st.nextToken());
	    else if (materialType == MT_FLOATING)
		totalChargeFloating = Double.parseDouble(st.nextToken());
	}

	void prepare() {}
	void setSelected(boolean s) { selected = s; }
	boolean isSelected() { return selected; }
	void delete() {}
	void setTransform() {
	    if (transform == null) {
		transform = new double[6];
		invTransform = new double[6];
	    }
	    int i;
	    double cx = 0, cy = 0;
	    for (i = 0; i != handles.size(); i++) {
		DragHandle dh = handles.get(i);
		cx += dh.x;
		cy += dh.y;
	    }
	    cx /= handles.size();
	    cy /= handles.size();
	    centerX = cx;
	    centerY = cy;

	    // make translation-rotation-translation matrix to rotate object about cx,cy
	    transform[0] = transform[4] = Math.cos(rotation);
	    transform[1] = Math.sin(rotation);
	    transform[3] = -transform[1];
	    transform[2] = (1-transform[0])*cx - transform[1]*cy;
	    transform[5] = -transform[3]*cx + (1-transform[4])*cy;
	    // inverse transform
	    invTransform[0] = invTransform[4] = transform[0];
	    invTransform[1] = transform[3];
	    invTransform[3] = transform[1];
	    invTransform[2] = (1-transform[0])*cx + transform[1]*cy;
	    invTransform[5] = transform[3]*cx + (1-transform[4])*cy;
	}

	boolean drag(int dx, int dy) {
	    int i;
	    for (i = 0; i != handles.size(); i++) {
		DragHandle dh = handles.get(i);
		dh.x += dx;
		dh.y += dy;
	    }
	    setTransform();
	    return true;
	}

	void display() {
	    /*if (isConductor()) {
		JsArray bounds = getBoundary();
		if (bounds != null)
		    displayChargeWithBoundary(bounds);
	    }*/
	    if (selected) {
		int i;
		for (i = 0; i != handles.size(); i++) {
		    DragHandle dh = handles.get(i);
		    int x = (int) (dh.x*transform[0]+dh.y*transform[1]+transform[2]);
		    int y = (int) (dh.x*transform[3]+dh.y*transform[4]+transform[5]);
		    EMStatic.drawHandle(x, y); // -sim.windowOffsetX, y-sim.windowOffsetY);
		}
	    }
	    //		drawSelection();
	}
	
	Point transformPoint(Point p) {
	    int x = (int) (p.x*transform[0]+p.y*transform[1]+transform[2]);
	    int y = (int) (p.x*transform[3]+p.y*transform[4]+transform[5]);
	    return new Point(x, y);
	}

	Point inverseTransformPoint(Point p) {
	    int x = (int) (p.x*invTransform[0]+p.y*invTransform[1]+invTransform[2]);
	    int y = (int) (p.x*invTransform[3]+p.y*invTransform[4]+invTransform[5]);
	    return new Point(x, y);
	}

	void drawSelection() {
	}

	void rotate(double ang) {
	    rotation += ang;
	    setTransform();
	    sim.recalcAndRepaint();
	}

	void rotateTo(int x, int y) {
	    rotation = Math.atan2(-y+centerY, x-centerX)-Math.PI/2;
	    double step = Math.PI/12;
	    rotation = Math.round(rotation/step)*step;
	    setTransform();
	    sim.recalcAndRepaint();
	}

	boolean canRotate() { return false; }
	
	static double hypotf(double x, double y) {
	    return Math.sqrt(x*x+y*y);
	}
	
	static double distanceToLineSegment(double x, double y, double lx1, double ly1, double lx2, double ly2)
	{
	    x   -= lx1;
	    y   -= ly1;
	    lx2 -= lx1;
	    ly2 -= ly1;
	    double lr = hypotf(lx2, ly2);
	    
	    // project along line
	    double proj1 = (x*lx2+y*ly2)/(lr*lr);
	    
	    // if we are off edge of line, return distance to nearest endpoint
	    if (proj1 < 0)
	        return hypotf(x, y);
	    if (proj1 > 1) {
	        x -= lx2;
	        y -= ly2;
	        return hypotf(x, y);
	    }
	    
	    // return distance from line
	    double proj2 = x*ly2-y*lx2;
	    double dist = Math.abs(proj2)/lr;
	    return dist;
	}

	boolean hitTestInside(double x, double y) { return false; }
	
	double hitTest(int x, int y) {
		if (handles.size() == 1) {
			DragHandle dh = handles.get(0);
			return hypotf(dh.x-x, dh.y-y);
		}
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
		double d = distanceToLineSegment(x, y, dh1.x, dh1.y, dh2.x, dh2.y);
		return d;
	}
	
	boolean dragHandle(DragHandle dh, int x, int y) {
		sim.recalcAndRepaint();
		return true;
	}
	
	void setInitialPosition() {
	    if (handles.size() == 1) {
		Rectangle start = sim.findSpace(this, 0, 0);
		DragHandle dh = handles.get(0);
		dh.x = start.x;
		dh.y = start.y;
	    }
	    if (handles.size() == 2) {
		Rectangle start = sim.findSpace(this, 40, 0);
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
		dh1.x = start.x;
		dh1.y = start.y;
		dh2.x = start.x + start.width;
		dh2.y = start.y;
	    }
	}
	
	Rectangle boundingBox() {
	    int minx = 10000, miny = 10000, maxx = -10000, maxy = -10000;
	    int i;
	    for (i = 0; i != handles.size(); i++) {
		DragHandle dh = handles.get(i);
		Point p = transformPoint(new Point(dh.x, dh.y));
		if (p.x < minx)
		    minx = p.x;
		if (p.y < miny)
		    miny = p.y;
		if (p.x > maxx)
		    maxx = p.x;
		if (p.y > maxy)
		    maxy = p.y;
	    }
	    return new Rectangle(minx, miny, maxx-minx, maxy-miny);
	}

	abstract int getDumpType();
	
	String dump() {
	    int t = getDumpType();
	    String out;
	    if (t >= 200)
		out = t + " " + flags;
	    else
		out = (char)t + " " + flags;
	    out += " " + materialType;
	    if (materialType == MT_CONDUCTING)
		out += " " + potential;
	    else if (materialType == MT_DIELECTRIC)
		out += " " + permittivity;
	    else if (materialType == MT_CHARGED)
		out += " " + chargeDensity;
	    else if (materialType == MT_FLOATING)
		out += " " + totalChargeFloating;
	    out += dumpHandles();
	    return out;
	}

	String dumpHandles() {
	    String out = "";
	    int i;
	    for (i = 0; i != handles.size(); i++) {
		DragHandle dh = handles.get(i);
		out += " " + dh.x + " " + dh.y;
	    }
	    return out;
	}

	@Override
	public EditInfo getEditInfo(int n) {
	    if (n == 0 && materialType != MT_OTHER) {
		EditInfo ei = new EditInfo("Type", 0);
		ei.choice = new Choice();
		ei.choice.add("Charged");
		ei.choice.add("Conducting");
		ei.choice.add("Dielectric");
		ei.choice.add("Floating");
		ei.choice.setSelectedIndex(materialType-MT_CHARGED);
		return ei;
	    }
	    if (n == 1 && materialType == MT_CHARGED)
		return new EditInfo("Charge Density (C/m^2)", chargeDensity, 0, 1);
	    if (n == 1 && materialType == MT_DIELECTRIC)
		return new EditInfo("Relative Permittivity", permittivity, 0, 1);
	    if (n == 1 && materialType == MT_CONDUCTING)
		return new EditInfo("Potential (V)", potential, 0, 1);
	    return null;
	}

	public void setEditValue(int n, EditInfo ei) {
	    if (n == 0 && materialType != MT_OTHER) {
		int type = ei.choice.getSelectedIndex() + MT_CHARGED;
		if (type == MT_FLOATING && !sim.canMakeFloating(this)) {
		    // we'd need to solve a matrix equation to support this.  Not hard, but not worth the effort.
		    // It would also be very slow.
		    Window.alert("Multiple floating conductors isn't supported.");
		    ei.choice.select(1);
		    return;
		}
		materialType = type;
		ei.newDialog = true;
		return;
	    }
	    if (n == 1) {
		if (materialType == MT_CHARGED)
		    chargeDensity = ei.value;
		else if (materialType == MT_DIELECTRIC)
		    permittivity = ei.value;
		else if (materialType == MT_CONDUCTING)
		    potential = ei.value;
		return;
	    }
	}

	void writeCharge() {
	    if (materialType == MT_CHARGED) {
		useMaterialType(MT_CHARGED, 0, chargeDensity*sim.gridSizeX*sim.gridSizeY*sim.lengthScale*sim.lengthScale/sim.e0, false);
		writeMaterials();
	    }
	}
	
	void useMaterial() {
	    if (currentFloatingConductor != null && isConductor()) {
		// if this member is set then all conductors must have 0 potential except this one, which has 1
		useMaterialType(materialType, 0, currentFloatingConductor == this ? 1 : 0, false);
	    } else
		useMaterialType(materialType, permittivity, potential, materialType == MT_DIELECTRIC);
	}

	native static void useMaterialType(int type, double pm, double pot, boolean dielec) /*-{
	   var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	   renderer.materialType = type;
	   renderer.permittivity = dielec ? pm : 0;
	   renderer.potential = pot;
	}-*/;
	
	void rescale(double scale) {
	    int i;
	    for (i = 0; i != handles.size(); i++) {
		DragHandle dh = handles.get(i);
		dh.rescale(scale);
	    }
	    setTransform();
	}
	
	String selectText() {
	    if (handles.size() != 2)
		return null;
	    return "length = " + sim.getLengthText(length());
	}

	JsArray getBoundary() { return null; }
	
	native boolean intersects(DragObject obj) /*-{
	   var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	    var bounds1 = this.@com.falstad.emstatic.client.DragObject::getBoundary()();
	    if (!bounds1)
	    	return false;
	    renderer.transformBoundary(bounds1);
	    var bounds2 = obj .@com.falstad.emstatic.client.DragObject::getBoundary()();
	    if (!bounds2)
	    	return false;
	    renderer.transformBoundary(bounds2);
	   return renderer.checkIntersection(bounds1[0], bounds2[0]);
	}-*/;
	
	void loadTransform() {
	    double xform[] = transform;
	    sim.setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
	}
	
	native void writeMaterials() /*-{
	    var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	    var bounds = this.@com.falstad.emstatic.client.DragObject::getBoundary()();
	    if (bounds)
	    	renderer.writeShape(bounds);
	}-*/;

	native void displayChargeWithBoundary(JsArray bounds) /*-{
	    var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	    renderer.displayCharge(bounds);
	}-*/;


	native void calcCharge() /*-{
	    	var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	        var bounds = this.@com.falstad.emstatic.client.DragObject::getBoundary()();
	        if (bounds)
	    	    renderer.calcCharge(bounds);
	    }-*/;

        native void drawFieldLinesShape(JsArray bound) /*-{
        	var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
        	renderer.drawFieldLinesShape(bound);
    	}-*/;
    
        void drawFieldLines() {
            if (isConductor() || isCharged()) {
        	JsArray bounds = getBoundary();
        	if (bounds != null)
        	    drawFieldLinesShape(bounds);
            }
        }

        // conductorCharge should be the same as totalChargeFloating but it's not, because of calculation error
        double getDisplayedCharge() {
            if (isFloating())
        	return totalChargeFloating;
            return conductorCharge;
        }
	
	double length() {
	    DragHandle dh1 = handles.get(0);
	    DragHandle dh2 = handles.get(1);
	    double len = Math.round(Math.hypot(dh1.x-dh2.x, dh1.y-dh2.y));
	    return len;
	}
	int getMaterialType() { return materialType; }
	boolean isConductor() { return materialType == MT_CONDUCTING || materialType == MT_FLOATING; }
	boolean isFixedConductor() { return materialType == MT_CONDUCTING; }
	boolean isFloating() { return materialType == MT_FLOATING; }
	boolean isCharged() { return materialType == MT_CHARGED; }
	void setPotential(double x) { potential = x; }
	void setConductorCharge(double c) { conductorCharge = c; }
	void updateFloatingCharge() { totalChargeFloating = conductorCharge; }
}
