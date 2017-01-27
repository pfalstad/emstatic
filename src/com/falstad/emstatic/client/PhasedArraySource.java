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

public class PhasedArraySource extends LineSource {
	
	PhasedArraySource() {}
	
	PhasedArraySource(StringTokenizer st) {
		super(st);
		phaseShift2 = new Double(st.nextToken()).doubleValue();
	}
	
	PhasedArraySource(int x1, int y1, int x2, int y2, double ps1, double ps2) {
		super(x1, y1, x2, y2);
		phaseShift  = ps1;
		phaseShift2 = ps2;
	}
	
	String dump() { return super.dump() + " " + phaseShift2; }
	
	double phaseShift2;
	
	void run() {
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
		double freq = frequency; // sim.freqBar.getValue() * EMStatic.freqMult;
		double w = freq * (sim.t - freqTimeZero) + phaseShift;
		w %= Math.PI*2;
       	EMStatic.drawPhasedArray(dh1.x, dh1.y, dh2.x, dh2.y, w, w+phaseShift2-phaseShift); 
	}
	
    public EditInfo getEditInfo(int n) {
    	if (n == 0)
    		return new EditInfo("Frequency (Hz)", frequency, 4, 500);
    	if (n == 1)
    		return new EditInfo("Phase Offset 1 (degrees)", phaseShift*180/Math.PI,
    					-180, 180).setDimensionless();
    	if (n == 2)
    		return new EditInfo("Phase Offset 2 (degrees)", phaseShift2*180/Math.PI,
    					-180, 180).setDimensionless();
    	return null;
    }
    public void setEditValue(int n, EditInfo ei) {
    	if (n == 0) {
    		// adjust time zero to maintain continuity in the waveform
    		// even though the frequency has changed.
    		double oldfreq = frequency;
    		frequency = ei.value;
    		freqTimeZero = sim.t-oldfreq*(sim.t-freqTimeZero)/frequency;
    	}
    	if (n == 1)
    		phaseShift = ei.value*Math.PI/180;
    	if (n == 2)
    		phaseShift2 = ei.value*Math.PI/180;
    }

	int getDumpType() { return 201; }

}
