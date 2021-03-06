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

import com.google.gwt.core.client.JavaScriptObject;

public class Charge extends DragObject {
	double charge;
    
	Charge() {
		handles.add(new DragHandle(this));
		int i;
		charge = 1e-9;
		setTransform();
	}
	
	Charge(StringTokenizer st, int ct) {
		super(st);
		while (ct-- > 0)
			handles.add(new DragHandle(this, st));
		charge = new Double(st.nextToken()).doubleValue();
		setTransform();
	}
	
	Charge(int x, int y) {
		handles.add(new DragHandle(this, x, y));
		charge = 1;
		setTransform();
	}
	
	String dump() {
		return super.dump() + " " + charge;
	}
	
	void writeCharge() {
		DragHandle dh = handles.get(0);
		writeChargeAt(sim.renderer, dh.x, dh.y, charge*sim.gridSizeX*sim.gridSizeX/sim.e0);
	}

	static native void writeChargeAt(JavaScriptObject renderer, int x, int y, double value) /*-{
		renderer.writeCharge(x, y, value);
	}-*/;


	int chargeSize = 8;
	
	void display() {
	    /*
		int i;
		for (i = 0; i != handles.size(); i++) {
			DragHandle dh = handles.get(i);
			EMStatic.drawHandle(dh.x,  dh.y);
		}
		super.draw();
		*/
	    chargeSize = 8*sim.windowWidth/256;
	    DragHandle dh = handles.get(0);
	    drawChargeObject(dh.x, dh.y, chargeSize, charge);
	}
	
	native void drawChargeObject(int x, int y, int r, double charge) /*-{
	    @com.falstad.emstatic.client.EMStatic::renderer.drawChargeObject(x, y, r, charge);	    
	}-*/;
	
	double hitTest(int x, int y) {
	    DragHandle dh = handles.get(0);
	    return Math.hypot(x-dh.x, y-dh.y)-chargeSize;
	}
	
    public EditInfo getEditInfo(int n) {
	if (n == 0)
	    return new EditInfo("Charge (C)", charge, 0, 10);
    	return null;
    }
    
    public void setEditValue(int n, EditInfo ei) {
    	if (n == 0)
    	    charge = ei.value;
    }
    
    @Override
    String selectText() {
	return "q = " + sim.getUnitText(charge, "C");
    }
    
	int getDumpType() { return 'c'; }
	void drawFieldLines() {
	    int i;
	    DragHandle dh = handles.get(0);
	    for (i = 0; i != 16; i++) {
		double ang = Math.PI*i/8;
		sim.drawFieldLine(dh.x+chargeSize*Math.cos(ang), dh.y+chargeSize*Math.sin(ang), charge > 0 ? -1 : 1);
	    }
	}
		
	}
