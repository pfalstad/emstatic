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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;

//import java.awt.*;

class EditInfo {
	EditInfo(String n, double val, double mn, double mx) {
		name = n;
		value = val;
		if (mn == 0 && mx == 0 && val > 0) {
			minval = 1e10;
			while (minval > val/100)
				minval /= 10.;
			maxval = minval * 1000;
		} else {
			minval = mn;
			maxval = mx;
		}
		forceLargeM = name.indexOf("(ohms)") > 0 ||
				name.indexOf("(Hz)") > 0;
				dimensionless = false;
	}
	
	EditInfo setDimensionless() { dimensionless = true; return this; }
	
	String name, text;
	double value, minval, maxval;
	TextBox textf;
	//    Scrollbar bar;
	Choice choice;
	Checkbox checkbox;
	Button button;
	TextArea textArea;
	
	boolean newDialog;
	boolean forceLargeM;
	boolean dimensionless;
}
    
