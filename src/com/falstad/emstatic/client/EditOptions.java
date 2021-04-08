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

public class EditOptions implements Editable {
	EMStatic sim;
    public EditOptions(EMStatic s) { sim = s; }
    EditInfo offsetEditInfo;
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Grid size", sim.gridSizeX, 0, 0).setDimensionless();
        if (n == 1)
            return offsetEditInfo = new EditInfo("Offscreen boundary width", sim.windowOffsetX, 0, 0).setDimensionless();
        if (n == 2)
        	return new EditInfo("Screen width scale (m)", sim.lengthScale*sim.windowWidth, 0, 0);
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.value > 0) {
        	sim.setResolution((int)ei.value);
        	offsetEditInfo.value = sim.windowOffsetX;
        	EditDialog.theEditDialog.updateValue(offsetEditInfo);
        }
        if (n == 1 && ei.value > 0)
        	sim.setResolution(sim.gridSizeX, (int)ei.value);
        if (n == 2)
        	sim.lengthScale = ei.value/sim.windowWidth;
    }

}
