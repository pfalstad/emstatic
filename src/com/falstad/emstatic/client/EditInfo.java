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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;

//import java.awt.*;

class EditInfo {
	EditInfo(String n, double val) {
	    name = n;
	    value = val;
	}
	
	EditInfo(String n, double val, double mn, double mx) {
	    name = n;
	    value = val;
	}
	
	
	EditInfo setDimensionless() { dimensionless = true; return this; }
	
	String name, text;
	double value;
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
    
