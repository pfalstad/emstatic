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


import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
//import java.awt.*;
//import java.awt.event.*;
//import java.text.NumberFormat;
//import java.text.DecimalFormat;

interface Editable {
    EditInfo getEditInfo(int n);
    void setEditValue(int n, EditInfo ei);
}

// class EditDialog extends Dialog implements AdjustmentListener, ActionListener, ItemListener {
class EditDialog extends DialogBox  {
	Editable elm;
	EMStatic cframe;
	Button applyButton, okButton, cancelButton;
	EditInfo einfos[];
	int einfocount;
	final int barmax = 1000;
	VerticalPanel vp;
	HorizontalPanel hp;
	NumberFormat noCommaFormat;
	static EditDialog theEditDialog;

	EditDialog(Editable ce, EMStatic f) {
		setText("Edit Component");
		cframe = f;
		elm = ce;
		theEditDialog = this;
//		setLayout(new EditDialogLayout());
		vp=new VerticalPanel();
		setWidget(vp);
		einfos = new EditInfo[10];
		noCommaFormat=NumberFormat.getFormat("####.###");
//		noCommaFormat = DecimalFormat.getInstance();
//		noCommaFormat.setMaximumFractionDigits(10);
//		noCommaFormat.setGroupingUsed(false);
		hp=new HorizontalPanel();
		hp.setWidth("100%");
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.setStyleName("topSpace");
		vp.add(hp);
		hp.add(applyButton = new Button("Apply"));
		applyButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				apply();
			}
		});
		hp.add(okButton = new Button("OK"));
		okButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				apply();
				closeDialog();
			}
		});
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		hp.add(cancelButton = new Button("Cancel"));
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		buildDialog();
		this.center();
	}
	
	void buildDialog() {
		int i;
		int idx;
		for (i = 0; ; i++) {
			Label l = null;
			einfos[i] = elm.getEditInfo(i);
			if (einfos[i] == null)
				break;
			EditInfo ei = einfos[i];
			idx = vp.getWidgetIndex(hp);
			if (ei.name.startsWith("<"))
			    vp.insert(l = new HTML(ei.name),idx);
			else
			    vp.insert(l = new Label(ei.name),idx);
			if (i!=0 && l != null)
				l.setStyleName("topSpace");
			idx = vp.getWidgetIndex(hp);
			if (ei.choice != null) {
				vp.insert(ei.choice,idx);
				ei.choice.addChangeHandler( new ChangeHandler() {
					public void onChange(ChangeEvent e){
						itemStateChanged(e);
					}
				});
			} else if (ei.checkbox != null) {
				vp.insert(ei.checkbox,idx);
				ei.checkbox.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
					public void onValueChange(ValueChangeEvent<Boolean> e){
						itemStateChanged(e);
					}
				});
			} else if (ei.button != null) {
			    vp.insert(ei.button, idx);
			    ei.button.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent event) {
				    itemStateChanged(event);
				}
			    });
			} else if (ei.textArea != null) {
			    vp.insert(ei.textArea, idx);
			} else {
				vp.insert(ei.textf = new TextBox(), idx);
				if (ei.text != null)
					ei.textf.setText(ei.text);
				if (ei.text == null) {
					ei.textf.setText(unitString(ei));
				}
				ei.textf.addValueChangeHandler( new ValueChangeHandler<String>() {
					public void onValueChange(ValueChangeEvent<String> e){
						itemStateChanged(e);
					}
				});
			}
		}
		einfocount = i;
	}

	String unitString(EditInfo ei) {
		double v = ei.value;
		double va = Math.abs(v);
		if (ei.dimensionless)
			return noCommaFormat.format(v);
		if (v == 0) return "0";
		if (va < 1e-9)
			return noCommaFormat.format(v*1e12) + "p";
		if (va < 1e-6)
			return noCommaFormat.format(v*1e9) + "n";
		if (va < 1e-3)
			return noCommaFormat.format(v*1e6) + "u";
		if (va < 1e-2)
			return noCommaFormat.format(v*1e3) + "m";
		if (va < 1)
			return noCommaFormat.format(v*1e2) + "c";
		if (va < 1e3)
			return noCommaFormat.format(v);
		if (va < 1e6)
			return noCommaFormat.format(v*1e-3) + "k";
		if (va < 1e9)
			return noCommaFormat.format(v*1e-6) + "M";
		if (va < 1e12)
			return noCommaFormat.format(v*1e-9) + "G";
		return noCommaFormat.format(v*1e-12) + "T";
	}

	double parseUnits(EditInfo ei) throws java.text.ParseException {
		String s = ei.textf.getText();
		s = s.trim();
		// rewrite shorthand (eg "2k2") in to normal format (eg 2.2k) using regex
		s=s.replaceAll("([0-9]+)([pPnNuUmMkKgG])([0-9]+)", "$1.$3$2");
		int len = s.length();
		char uc = s.charAt(len-1);
		double mult = 1;
		switch (uc) {
		case 'p': case 'P': mult = 1e-12; break;
		case 'n': case 'N': mult = 1e-9; break;
		case 'u': case 'U': mult = 1e-6; break;
		case 'c': mult = 1e-2; break;

		// for ohm values, we assume mega for lowercase m, otherwise milli
		case 'm': mult = (ei.forceLargeM) ? 1e6 : 1e-3; break;

		case 'k': case 'K': mult = 1e3; break;
		case 'M': mult = 1e6; break;
		case 'G': case 'g': mult = 1e9; break;
		case 't': case 'T': mult = 1e12; break;
		}
		if (mult != 1)
			s = s.substring(0, len-1).trim();
		return noCommaFormat.parse(s) * mult;
	}

	void apply() {
		int i;
		for (i = 0; i != einfocount; i++) {
			EditInfo ei = einfos[i];
			if (ei.textf!=null && ei.text==null) {
				try {
					double d = parseUnits(ei);
					ei.value = d;
				} catch (Exception ex) { /* ignored */ }
			}
			if (ei.button != null)
			    continue;
			elm.setEditValue(i, ei);
		}
		cframe.recalcAndRepaint();
		cframe.repaint();
	}

	public void itemStateChanged(GwtEvent e) {
	    Object src = e.getSource();
	    int i;
	    boolean changed = false;
	    for (i = 0; i != einfocount; i++) {
	    	EditInfo ei = einfos[i];
	    	if (ei.choice == src || ei.checkbox == src || ei.button == src || ei.textf == src) {

	    		// if we're pressing a button, make sure to apply changes first
	    		if (ei.button == src)
	    			apply();
	    		if (ei.textf == src) {
					try {
						double d = parseUnits(ei);
						ei.value = d;
					} catch (Exception ex) { /* ignored */ }
	    		}

	    		elm.setEditValue(i, ei);
	    		if (ei.newDialog)
	    			changed = true;
	    		cframe.recalcAndRepaint();
	    	}
	    }
	    if (changed) {
	    	clearDialog();
	    	buildDialog();
	    }
	}
	
	public void clearDialog() {
		while (vp.getWidget(0)!=hp)
			vp.remove(0);
	}
	
	protected void closeDialog()
	{
		EditDialog.this.hide();
		cframe.editDialog = null;
		cframe.enableDisableUI();
	}
	
	public void updateValue(EditInfo ei) {
		if (ei.text != null)
			ei.textf.setText(ei.text);
		if (ei.text == null) {
			ei.textf.setText(unitString(ei));
		}
	}
}

