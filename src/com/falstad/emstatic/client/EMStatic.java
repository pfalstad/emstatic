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
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EMStatic implements MouseDownHandler, MouseMoveHandler,
		MouseUpHandler, ClickHandler, DoubleClickHandler, ContextMenuHandler,
		NativePreviewHandler, MouseOutHandler, MouseWheelHandler, ChangeHandler {

	Logger logger = Logger.getLogger(EMStatic.class.getName());
	
	Image dbimage;
	ImageData data;
	Dimension winSize;
	Random random;
	int gridSizeX;
	int gridSizeY;
	int gridSizeXY;
	int windowWidth = 50;
	int windowHeight = 50;
	int windowOffsetX = 0;
	int windowOffsetY = 0;
	int windowBottom = 0;
	int windowRight = 0;
	public static final int sourceRadius = 17;
	public static final double freqMult = .0233333 * 5;

	// Container main;
	Button blankButton;
	Button blankWallsButton;
	Button borderButton;
	Button boxButton;
	Button exportButton;
	Checkbox stoppedCheck;
//	Checkbox debugCheck1, debugCheck2;
	Checkbox equipCheck;
	Choice setupChooser;
	Choice colorChooser;
	Choice displayChooser;
	Vector<Setup> setupList;
	Vector<DragObject> dragObjects;
	DragObject selectedObject, mouseObject, menuObject;
	DragHandle draggingHandle;
	Setup setup;
	Scrollbar dampingBar;
	Scrollbar speedBar;
	Scrollbar freqBar;
	Scrollbar resBar;
	Scrollbar brightnessBar;
	Scrollbar equipotentialBar;
	double dampcoef;
	double freqTimeZero;
	double movingSourcePos = 0;
	double zoom3d = 1.2;
	Point mouseLocation;
	static final double pi = 3.14159265358979323846;
	static final int MODE_SETFUNC = 0;
	static final int MODE_WALLS = 1;
	static final int MODE_MEDIUM = 2;
	static final int MODE_FUNCHOLD = 3;
	static final int DISP_FIELD = 0;
	static final int DISP_LINES = 1;
	static final int DISP_POT = 3;
	static final int DISP_3D = 4;
	static final int DISP_CHARGE = 5;
	static final int DISP_D = 6;
	static final int DISP_P = 7;
	static final int DISP_POLARIZATION_CHARGE = 8;
	int dragX, dragY, dragStartX = -1, dragStartY;
	int selectedSource = -1;
	int sourceIndex;
	int freqBarValue;
	boolean dragging;
	boolean dragClear;
	boolean dragSet;
	public boolean useFrame;
	boolean showControls;
	
	// level of calculation we've done.  0 = need to recalculate, 1 = less accurate, 2 = full accuracy
	int calcLevel;
	
	boolean ignoreFreqBarSetting;
	double t;
	double lengthScale, waveSpeed;
	int iters;
	// MemoryImageSource imageSource;
	CanvasPixelArray pixels;
	int sourceCount = -1;
	boolean sourcePlane = false;
	boolean sourceMoving = false;
	boolean increaseResolution = false;
	boolean adjustResolution = true;
	boolean rotationMode = false;
	boolean preserveSelection = false;
	int sourceFreqCount = -1;
	int sourceWaveform = SWF_SIN;
	int auxFunction;
	int mouseWheelAccum;
	long startTime;
	MenuBar menuBar;
	MenuBar mainMenuBar;
	MenuBar fileMenuBar;
	MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmCutMenuItem;
    MenuItem elmCopyMenuItem;
    MenuItem elmDeleteMenuItem;
    MenuItem elmRotateMenuItem;
    
    MenuItem aboutItem;
    MenuItem importFromLocalFileItem, importFromTextItem,
    	exportAsUrlItem, exportAsLocalFileItem, exportAsTextItem;
    MenuItem undoItem, redoItem,
	cutItem, copyItem, pasteItem, selectAllItem, optionsItem;

	Color wallColor, posColor, negColor, zeroColor, medColor, posMedColor,
			negMedColor, sourceColor;
	Color schemeColors[][];
	Point dragPoint;
	// Method timerMethod;
	int timerDiv;
	static final int mediumMax = 191;
	static final double mediumMaxIndex = .5;
	static final int SWF_SIN = 0;
	static final int SWF_SQUARE = 1;
	static final int SWF_PULSE = 2;

//	Frame iFrame;
    LoadFile loadFileInput;
	DockLayoutPanel layoutPanel;
	VerticalPanel verticalPanel;
	AbsolutePanel absolutePanel;
	Rectangle ripArea;
    String clipboard;
    Vector<String> undoStack, redoStack;
	Canvas cv;
	Context2d cvcontext;
	Canvas backcv;
	Label coordsLabel;
	Context2d backcontext;
	HandlerRegistration handler;
	DialogBox dialogBox;
	int verticalPanelWidth;
	int chargeSource;
	String startLayoutText = null;
	String versionString = "1.0";
    public static NumberFormat showFormat, shortFormat, noCommaFormat;
	static EMStatic theSim;
    static EditDialog editDialog;
    static ExportAsUrlDialog exportAsUrlDialog;
    static ExportAsTextDialog exportAsTextDialog;
    static ExportAsLocalFileDialog exportAsLocalFileDialog;
    static ImportFromTextDialog importFromTextDialog;
    static AboutBox aboutBox;
    static JavaScriptObject renderer;
    static final double e0 = 8.854e-12;
    long calcStart;

	static final int MENUBARHEIGHT = 30;
	static final int MAXVERTICALPANELWIDTH = 166;
	static final int POSTGRABSQ = 16;

	final Timer timer = new Timer() {
		public void run() {
			
			update();
		}
	};
	final int FASTTIMER = 33; // 16;

	int getrand(int x) {
		int q = random.nextInt();
		if (q < 0)
			q = -q;
		return q % x;
	}

	public void setCanvasSize() {
		int width, height;
		int fullwidth = width = (int) RootLayoutPanel.get().getOffsetWidth();
		height = (int) RootLayoutPanel.get().getOffsetHeight();
		height = height - MENUBARHEIGHT;   // put this back in if we add a menu bar
		width = width - MAXVERTICALPANELWIDTH;
		width = height = (width < height) ? width : height;
		winSize = new Dimension(width, height);
		verticalPanelWidth = fullwidth-width;
		if (layoutPanel != null)
			layoutPanel.setWidgetSize(verticalPanel, verticalPanelWidth);
		if (resBar != null) {
			resBar.setWidth(verticalPanelWidth);
			dampingBar.setWidth(verticalPanelWidth);
			speedBar.setWidth(verticalPanelWidth);
			freqBar.setWidth(verticalPanelWidth);
			brightnessBar.setWidth(verticalPanelWidth);
			equipotentialBar.setWidth(verticalPanelWidth);
		}
		if (cv != null) {
			cv.setWidth(width + "PX");
			cv.setHeight(height + "PX");
			cv.setCoordinateSpaceWidth(width);
			cv.setCoordinateSpaceHeight(height);
		}
		if (coordsLabel != null)
			absolutePanel.setWidgetPosition(coordsLabel, 0, height-coordsLabel.getOffsetHeight());
		/*
		if (backcv != null) {
			backcv.setWidth(width + "PX");
			backcv.setHeight(height + "PX");
			backcv.setCoordinateSpaceWidth(width);
			backcv.setCoordinateSpaceHeight(height);
		}
		*/
		int h = height / 5;
		/*
		 * if (h < 128 && winSize.height > 300) h = 128;
		 */
		ripArea = new Rectangle(0, 0, width, height - h);

	}

    public static native void console(String text)
    /*-{
	    console.log(text);
	}-*/;

    	// pass the canvas element to emstatic.js and get renderer object with all the callbacks we need 
	static native JavaScriptObject passCanvas(CanvasElement cv) /*-{
		return $doc.passCanvas(cv);
	}-*/;

	static native void displayPotential(int src, int rs, double bright) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawScenePotential(src, rs, bright);
	}-*/;

	static native void displayEquip(int src, int rs, double equipMult) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawSceneEquip(src, rs, equipMult);
	}-*/;

	static native void display3D(int src, int rs, double bright, double equipMult) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawScene3D(src, rs, bright, equipMult);
	}-*/;

	static native void displayField(int src, int rs, double bright, double emult, double pmult) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.displayField(src, rs, bright, emult, pmult);
	}-*/;

	static native void fetchPotentialPixels(int src) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.fetchPotentialPixels(src);
	}-*/;

	static native void freePotentialPixels() /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.freePotentialPixels();
	}-*/;

	static native void drawFieldLine(double x, double y, int dir) /*-{
	    @com.falstad.emstatic.client.EMStatic::renderer.drawFieldLine(x, y, dir);	    
	}-*/;

	static native void setDestinationRenderTexture(int d) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.setDestination(d);
	}-*/;

	static native void clearDestination() /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.clearDestination();
	}-*/;

	static native void runRelax(int src, int rsnum, boolean residual) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.runRelax(src, rsnum, residual);
	}-*/;

	static native void copyTextureRG(int src) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.copyRG(src);
	}-*/;

	static native void copyTextureRGB(int src) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.copyRGB(src);
	}-*/;

	static native double calcDifference(int src1, int src2) /*-{
		return @com.falstad.emstatic.client.EMStatic::renderer.calcDifference(src1, src2);
	}-*/;

	static native void sumTexture(int src) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.sum(src);
	}-*/;
	
	static native void addTextures(int src, int src2) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.add(src, src2);
	}-*/;
	
	static native double getCharge() /*-{
		return @com.falstad.emstatic.client.EMStatic::renderer.getCharge();
	}-*/;
	
	static native int getRenderTextureCount() /*-{
		return @com.falstad.emstatic.client.EMStatic::renderer.getRenderTextureCount();
	}-*/;

	static native void set3dViewAngle(double angle1, double angle2) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.set3dViewAngle(angle1, angle2);
	}-*/;

	static native void set3dViewZoom(double zoom) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.set3dViewZoom(zoom);
	}-*/;

	static native void setResolutionGL(int x, int y, int wx, int wy) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.setResolution(x, y, wx, wy);
	}-*/;
	
	static native void drawHandle(int x, int y) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawHandle(x, y);
	}-*/;

	static native void drawWall(int x1, int y1, int x2, int y2, double pot) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawWall(x1, y1, x2, y2, pot);
	}-*/;

	static native void setDrawingSelection(double ds) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawingSelection = ds;
	}-*/;

	static native void setTransform(double a, double b, double c, double d, double e, double f) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.setTransform(a, b, c, d, e, f);
	}-*/;

	static native void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, double med) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.drawTriangle(x1, y1, x2, y2, x3, y3, med);
	}-*/;

	static native void setResidualFlag(boolean res) /*-{
	   var renderer = @com.falstad.emstatic.client.EMStatic::renderer;
	   renderer.residual = res;
	}-*/;
	
	static native JsArrayNumber getProbeValue(int x, int y) /*-{
		return @com.falstad.emstatic.client.EMStatic::renderer.getProbeValue(x, y);
	}-*/;
	
	static native void setColors(int wallColor, int posColor, int negColor,
			int zeroColor, int posMedColor, int negMedColor,
			int medColor, int sourceColor, int zeroColor3d) /*-{
		@com.falstad.emstatic.client.EMStatic::renderer.setColors(wallColor, posColor, negColor, zeroColor, posMedColor, negMedColor,
			medColor, sourceColor, zeroColor3d);
	}-*/;

	static native void setChargeSource(int cs) /*-{
	    	@com.falstad.emstatic.client.EMStatic::renderer.chargeSource = cs;
	}-*/;
	
	Frame iFrame;
	
	public void init() {
		theSim = this;
//		logger.log(Level.SEVERE, "RAwr");
		
        QueryParameters qp = new QueryParameters();
        
        try {
                // look for layout embedded in URL
                String cct=qp.getValue("rol");
                if (cct!=null)
                	startLayoutText = cct.replace("%24", "$");
        } catch (Exception e) { }

		cv = Canvas.createIfSupported();
		renderer = passCanvas(cv.getCanvasElement());
		if (cv == null) {
			RootPanel
					.get()
					.add(new Label(
							"Not working. You need a browser that supports the CANVAS element."));
			return;
		}

		dragObjects = new Vector<DragObject>();
		cvcontext = cv.getContext2d();
//		backcv = Canvas.createIfSupported();
//		backcontext = backcv.getContext2d();
		setCanvasSize();
		layoutPanel = new DockLayoutPanel(Unit.PX);
		verticalPanel = new VerticalPanel();
		
		setupList = new Vector<Setup>();
        undoStack = new Vector<String>();
        redoStack = new Vector<String>();

		setupChooser = new Choice();
		setupChooser.addChangeHandler(this);
//		setupChooser.addItemListener(this);
		getSetupList();
		
		colorChooser = new Choice();
		colorChooser.addChangeHandler(this);
        colorChooser.addStyleName("topSpace");

		displayChooser = new Choice();
		displayChooser.add("Show Electric Field (E)");
		displayChooser.add("Show Field Lines");
		displayChooser.add("Show E + lines");
		displayChooser.add("Show Potential");
		displayChooser.add("Show Potential in 3-D");
		displayChooser.add("Show Charge (rho)");
		displayChooser.add("Show Displacement (D)");
		displayChooser.add("Show Polarization (P)");
		displayChooser.add("Show Polarization Charge");
		displayChooser.add("Show E/rho");
		displayChooser.add("Show E lines/rho");
		displayChooser.add("Show E/Potential");
		displayChooser.add("Show E lines/Potential");
		displayChooser.add("Show Ex");
		displayChooser.add("Show Ey");
		displayChooser.add("Show Dx");
		displayChooser.add("Show Dy");
		displayChooser.addChangeHandler(this);
		displayChooser.addStyleName("topSpace");

		
		verticalPanel.add(setupChooser);
//		verticalPanel.add(sourceChooser);
//		verticalPanel.add(modeChooser);
		verticalPanel.add(displayChooser);
		verticalPanel.add(colorChooser);
		verticalPanel.add(blankButton = new Button("Clear Waves"));
		blankButton.addClickHandler(this);

		verticalPanel.add(stoppedCheck = new Checkbox("Stopped"));
		verticalPanel.add(equipCheck = new Checkbox("Show Equipotentials", true));
		equipCheck.addClickHandler(this);

//		verticalPanel.add(debugCheck1 = new Checkbox("Limit V-Cycles"));
//		verticalPanel.add(debugCheck2 = new Checkbox("Limit Steps"));

        if (LoadFile.isSupported())
            verticalPanel.add(loadFileInput = new LoadFile(this));

		int res = 512;
		Label l;
		verticalPanel.add(l = new Label("Simulation Speed"));
        l.addStyleName("topSpace");
		verticalPanel.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 4, 1, 1, 220, new Command() { public void execute() { recalcAndRepaint(); }}));
		verticalPanel.add(l = new Label("Resolution"));
        l.addStyleName("topSpace");
		verticalPanel.add(resBar = new Scrollbar(Scrollbar.HORIZONTAL, res, 5, 64, 1024));
		resBar.addClickHandler(this);
		setResolution();
		
//		verticalPanel.add(new Label("Damping"));
//		verticalPanel.add(
				dampingBar = new Scrollbar(Scrollbar.HORIZONTAL, 10, 1, 2, 100);
//						);
		dampingBar.addClickHandler(this);
		verticalPanel.add(l = new Label("Source Frequency"));
        l.addStyleName("topSpace");
		verticalPanel.add(freqBar = new Scrollbar(Scrollbar.HORIZONTAL, freqBarValue = 15, 1, 1, 30,
				new Command() {
			public void execute() { if (!ignoreFreqBarSetting) setFreq(); }
		}));
//		freqBar.addClickHandler(this);
		verticalPanel.add(l = new Label("Brightness"));
        l.addStyleName("topSpace");
		verticalPanel.add(brightnessBar = new Scrollbar(Scrollbar.HORIZONTAL, 27, 1, 1, 2200,
			new Command() { public void execute() { repaint(); }}));
		verticalPanel.add(l = new Label("Equipotential Count"));
	        l.addStyleName("topSpace");
			verticalPanel.add(equipotentialBar = new Scrollbar(Scrollbar.HORIZONTAL, 27, 1, 1, 2200,
				new Command() { public void execute() { repaint(); }}));
		
        verticalPanel.add(iFrame = new Frame("iframe.html"));
        iFrame.setWidth(verticalPanelWidth+"px");
        iFrame.setHeight("100 px");
        iFrame.getElement().setAttribute("scrolling", "no");

		
        l.addStyleName("topSpace");
		resBar.setWidth(verticalPanelWidth);
		dampingBar.setWidth(verticalPanelWidth);
		speedBar.setWidth(verticalPanelWidth);
		freqBar.setWidth(verticalPanelWidth);
		brightnessBar.setWidth(verticalPanelWidth);
		equipotentialBar.setWidth(verticalPanelWidth);

		absolutePanel = new AbsolutePanel();
		coordsLabel = new Label("(0,0)");
		coordsLabel.setStyleName("coordsLabel");
		absolutePanel.add(cv);
		absolutePanel.add(coordsLabel, 0, 0);
		
		createMenus();
        layoutPanel.addNorth(menuBar, MENUBARHEIGHT);
		layoutPanel.addEast(verticalPanel, verticalPanelWidth);
		layoutPanel.add(absolutePanel);
		RootLayoutPanel.get().add(layoutPanel);

		mainMenuBar = new MenuBar(true);
		mainMenuBar.setAutoOpen(true);
		composeMainMenu(mainMenuBar);
		
        elmMenuBar = new MenuBar(true);
        elmMenuBar.addItem(elmEditMenuItem = new MenuItem("Edit",new MyCommand("elm","edit")));
        elmMenuBar.addItem(elmCutMenuItem = new MenuItem("Cut",new MyCommand("elm","cut")));
        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem("Copy",new MyCommand("elm","copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem("Delete",new MyCommand("elm","delete")));
        elmMenuBar.addItem(elmRotateMenuItem = new MenuItem("Rotate",new MyCommand("elm","rotate")));
        elmMenuBar.addItem(                    new MenuItem("Duplicate",new MyCommand("elm","duplicate")));

//		winSize = new Dimension(256, 256);
//		if (pixels == null) {
//		    pixels = new int[winSize.width*winSize.height];
//		    int j;
//		    for (j = 0; j != winSize.width*winSize.height; j++)
//		    	pixels[j] = 0xFF000000;
//		    
//		    
//		}
		
        showFormat=NumberFormat.getFormat("####.##");
        shortFormat=NumberFormat.getFormat("####.#");
        
		schemeColors = new Color[20][8];
		if (colorChooser.getItemCount() == 0)
		    addDefaultColorScheme();
		doColor();
		setDamping();
//		setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
		
		cv.addMouseMoveHandler(this);
		cv.addMouseDownHandler(this);
		cv.addMouseOutHandler(this);
		cv.addMouseUpHandler(this);
        cv.addMouseWheelHandler(this);
        cv.addClickHandler(this);
        doTouchHandlers(cv.getCanvasElement());
		cv.addDomHandler(this,  ContextMenuEvent.getType());
		
		reinit();
		set3dViewZoom(zoom3d);
		setCanvasSize();
		repaint();
		
		// String os = Navigator.getPlatform();
		// isMac = (os.toLowerCase().contains("mac"));
		// ctrlMetaKey = (isMac) ? "Cmd" : "Ctrl";
//		timer.scheduleRepeating(FASTTIMER);

	}
	
	void createMenus() {
		  fileMenuBar = new MenuBar(true);
		  importFromLocalFileItem = new MenuItem("Import From Local File", new MyCommand("file","importfromlocalfile"));
		  importFromLocalFileItem.setEnabled(LoadFile.isSupported());
		  fileMenuBar.addItem(importFromLocalFileItem);
		  importFromTextItem = new MenuItem("Import From Text", new MyCommand("file","importfromtext"));
		  fileMenuBar.addItem(importFromTextItem);
//		  importFromDropboxItem = new MenuItem("Import From Dropbox", new MyCommand("file", "importfromdropbox"));
//		  fileMenuBar.addItem(importFromDropboxItem); 
		  exportAsUrlItem = new MenuItem("Export As Link", new MyCommand("file","exportasurl"));
		  fileMenuBar.addItem(exportAsUrlItem);
		  exportAsLocalFileItem = new MenuItem("Export As Local File", new MyCommand("file","exportaslocalfile"));
		  exportAsLocalFileItem.setEnabled(ExportAsLocalFileDialog.downloadIsSupported());
		  fileMenuBar.addItem(exportAsLocalFileItem);
		  exportAsTextItem = new MenuItem("Export As Text", new MyCommand("file","exportastext"));
		  fileMenuBar.addItem(exportAsTextItem);
		  fileMenuBar.addItem(getClassCheckItem("Options...", "Options"));
		  fileMenuBar.addSeparator();
		  aboutItem=new MenuItem("About",(Command)null);
		  fileMenuBar.addItem(aboutItem);
		  aboutItem.setScheduledCommand(new MyCommand("file","about"));
		  
          menuBar = new MenuBar();
          menuBar.addItem("File", fileMenuBar);

		MenuBar m = new MenuBar(true);
		final String edithtml="<div style=\"display:inline-block;width:80px;\">";
		String sn=edithtml+"Undo</div>Ctrl-Z";
		m.addItem(undoItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","undo")));
		// undoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z));
		sn=edithtml+"Redo</div>Ctrl-Y";
		m.addItem(redoItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","redo")));
		//redoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z, true));
		m.addSeparator();
//		m.addItem(cutItem = new MenuItem("Cut", new MyCommand("edit","cut")));
		sn=edithtml+"Cut</div>Ctrl-X";
		m.addItem(cutItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","cut")));
		//cutItem.setShortcut(new MenuShortcut(KeyEvent.VK_X));
		sn=edithtml+"Copy</div>Ctrl-C";
		m.addItem(copyItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","copy")));
		sn=edithtml+"Paste</div>Ctrl-V";
		m.addItem(pasteItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","paste")));
		//pasteItem.setShortcut(new MenuShortcut(KeyEvent.VK_V));
		pasteItem.setEnabled(false);
		
		sn=edithtml+"Duplicate</div>Ctrl-D";
		m.addItem(new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","duplicate")));
		
		m.addSeparator();
		sn=edithtml+"Select All</div>Ctrl-A";
		m.addItem(selectAllItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","selectAll")));
		//selectAllItem.setShortcut(new MenuShortcut(KeyEvent.VK_A));
		menuBar.addItem("Edit",m);

		MenuBar drawMenuBar = new MenuBar(true);
		drawMenuBar.setAutoOpen(true);

		menuBar.addItem("Add", drawMenuBar);
		
		mainMenuBar = new MenuBar(true);
		mainMenuBar.setAutoOpen(true);
		composeMainMenu(mainMenuBar);
		composeMainMenu(drawMenuBar);
	}
	
    // install touch handlers to handle touch events properly on mobile devices.
    // don't feel like rewriting this in java.  Anyway, java doesn't let us create mouse
    // events and dispatch them.
    native void doTouchHandlers(CanvasElement cv) /*-{
	// Set up touch events for mobile, etc
	var lastTap;
	var tmout;
	var sim = this;
	cv.addEventListener("touchstart", function (e) {
        	mousePos = getTouchPos(cv, e);
  		var touch = e.touches[0];
  		var etype = "mousedown";
  		clearTimeout(tmout);
  		if (e.timeStamp-lastTap < 300) {
     		    etype = "dblclick";
  		} else {
  		    tmout = setTimeout(function() {
  		        sim.@com.falstad.emstatic.client.EMStatic::longPress()();
  		    }, 1000);
  		}
  		lastTap = e.timeStamp;
  		
  		var mouseEvent = new MouseEvent(etype, {
    			clientX: touch.clientX,
    			clientY: touch.clientY
  		});
  		e.preventDefault();
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchend", function (e) {
  		var mouseEvent = new MouseEvent("mouseup", {});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchmove", function (e) {
  		var touch = e.touches[0];
  		var mouseEvent = new MouseEvent("mousemove", {
    			clientX: touch.clientX,
    			clientY: touch.clientY
  		});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);

	// Get the position of a touch relative to the canvas
	function getTouchPos(canvasDom, touchEvent) {
  		var rect = canvasDom.getBoundingClientRect();
  		return {
    			x: touchEvent.touches[0].clientX - rect.left,
    			y: touchEvent.touches[0].clientY - rect.top
  		};
	}
	
    }-*/;
    

    public void composeMainMenu(MenuBar mainMenuBar) {
    	mainMenuBar.addItem(getClassCheckItem("Add Wall", "Wall"));
    	mainMenuBar.addItem(getClassCheckItem("Add Point Charge", "Charge"));
    	mainMenuBar.addItem(getClassCheckItem("Add Box", "Box"));
    	mainMenuBar.addItem(getClassCheckItem("Add Circle", "Circle"));
    	mainMenuBar.addItem(getClassCheckItem("Add Ellipse", "Ellipse"));
    	mainMenuBar.addItem(getClassCheckItem("Add Hollow Box", "HollowBox"));
    	mainMenuBar.addItem(getClassCheckItem("Add Hollow Circle", "HollowCircle"));
    	mainMenuBar.addItem(getClassCheckItem("Add Hollow Ellipse", "HollowEllipse"));
    	mainMenuBar.addItem(getClassCheckItem("Add Cavity", "Cavity"));
    	mainMenuBar.addItem(getClassCheckItem("Add Quadrupole Lens", "QuadrupoleLens"));
    }

    MenuItem getClassCheckItem(String s, String t) {
        return new MenuItem(s, new MyCommand("main", t));
    }

    public void wallsChanged() {
	calcLevel = 0;
    }
    
    public void menuPerformed(String menu, String item) {
    	if (item=="about")
    		aboutBox = new AboutBox(versionString);
    	if (item=="importfromlocalfile") {
    		pushUndo();
    		loadFileInput.click();
    	}
    	if (item=="importfromtext") {
    		importFromTextDialog = new ImportFromTextDialog(this);
    	}
    	if (item=="exportasurl") {
    		doExportAsUrl();
    	}
    	if (item=="exportaslocalfile")
    		doExportAsLocalFile();
    	if (item=="exportastext")
    		doExportAsText();

    	if (item=="undo")
    		doUndo();
    	if (item=="redo")
    		doRedo();
    	if (item == "cut") {
    		if (menu!="elm")
    			menuObject = null;
    		doCut();
    	}
    	if (item == "copy") {
    		if (menu!="elm")
    			menuObject = null;
    		doCopy();
    	}
    	if (item == "delete") {
    		if (menu!="elm")
    			menuObject = null;
    		doDelete();
    	}
    	if (item=="paste")
    		doPaste(null);
    	if (item=="duplicate") {
    		if (menu!="elm")
    			menuObject = null;
    		doDuplicate();
    	}
    	if (item=="selectAll")
    		doSelectAll();

    	if (menu=="elm" && contextPanel!=null)
    		contextPanel.hide();
    	if (contextPanel != null)
    		contextPanel.hide();
    	if (item == "edit")
    		doEdit(selectedObject);
    	if (item == "rotate" && selectedObject != null && selectedObject.canRotate())
    		rotationMode = true;
    	DragObject newObject = null;
    	if (item == "Wall")
    		newObject = new Wall();
    	if (item == "Ellipse")
    		newObject = new Ellipse(false);
    	if (item == "Circle")
		newObject = new Ellipse(true);
    	if (item == "QuadrupoleLens")
		newObject = new QuadrupoleLens();
    	if (item == "Box")
    		newObject = new Box();
    	if (item == "HollowBox")
		newObject = new HollowBox();
    	if (item == "HollowEllipse")
		newObject = new HollowEllipse(false);
    	if (item == "HollowCircle")
		newObject = new HollowEllipse(true);
    	if (item == "Charge")
    		newObject = new Charge();
    	if (newObject != null) {
    		pushUndo();
    		newObject.setInitialPosition();
    		dragObjects.add(newObject);
    		setSelectedObject(newObject);
    		preserveSelection = true;
    	}
    	if (item == "Options") {
    		doEdit(new EditOptions(this));
    	}
    	repaint();
    }

    DragObject createObj(int tint, StringTokenizer st) {
    	if (tint == 'e') return new Ellipse(st);
    	if (tint == 'E') return new HollowEllipse(st);
    	if (tint == 'b') return new Box(st);
    	if (tint == 'B') return new HollowBox(st);
    	if (tint == 'c') return new Charge(st, 1);
    	if (tint == 'w') return new Wall(st);
    	if (tint == 'q') return new QuadrupoleLens(st);
    	return null;
    }
    
    void doEdit(Editable eable) {
        clearSelection();
        pushUndo();
        if (editDialog != null) {
    //          requestFocus();
                editDialog.setVisible(false);
                editDialog = null;
        }
        editDialog = new EditDialog(eable, this);
        editDialog.show();
    }
    
    void doExportAsUrl()
    {
    	String dump = dumpLayout();
    	exportAsUrlDialog = new ExportAsUrlDialog(dump);
    	exportAsUrlDialog.show();
    }

    void doExportAsText()
    {
    	String dump = dumpLayout();
    	exportAsTextDialog = new ExportAsTextDialog(this, dump);
    	exportAsTextDialog.show();
    }

    void doExportAsLocalFile() {
    	String dump = dumpLayout();
    	exportAsLocalFileDialog = new ExportAsLocalFileDialog(dump);
    	exportAsLocalFileDialog.show();
    }

	boolean moveRight = true;
	boolean moveDown = true;

	long lastTime = 0, lastFrameTime, secTime = 0;
	int frames = 0;
	int steps = 0;
	int framerate = 0, steprate = 0;

	void reinit() {
		reinit(true);
	}

	void reinit(boolean setup) {
		sourceCount = -1;
		System.out.print("reinit " + gridSizeX + " " + gridSizeY + "\n");
		gridSizeXY = gridSizeX * gridSizeY;
		if (setup)
			doSetup();
	}

	String lastLog = "";

	// solve poisson equation exactly by relaxation, start with src, use dest as scratch, rs = right side
	int solveExactly(int src, int dest, int rs) {
	    int i;
	    for (i = 0; i != 50; i++) {
		setDestinationRenderTexture(dest);
		runRelax(src, rs, false);
		// swap dest and src
		int q = dest;
		dest = src;
		src = q;
	    }

	    // return last destination (which is src because of swap)
	    return src;
	}
	
	// calculate improved solution given an initial guess, and the right side.
	// src = initial guess, dest = render texture to use as scratch, rsGrid = right side.  returns result texture
	int multigridVCycle(int src, int dest, int rsGrid) {
	    if (src < 3)
		return solveExactly(src, dest, rsGrid);

	    // iterate a few times on fine grid
	    // iterate more times for higher accuracy on second try
	    int iterCount = (calcLevel == 0) ? 9 : 20;
	    int i;
	    for (i = 0; i != iterCount; i++) {
		setDestinationRenderTexture(dest);
		runRelax(src, rsGrid, false);
		int q = dest; dest = src; src = q;
	    }

	    // calculate residual
	    setDestinationRenderTexture(dest);
	    runRelax(src, rsGrid, true);

	    // restrict residual to coarser grid
	    int coarseResidual = rsGrid-3;
	    setDestinationRenderTexture(coarseResidual);
	    copyTextureRG(dest);

	    // draw materials on coarse grid (should draw all conductors as 0 potentials)
	    setResidualFlag(true);
	    writeMaterials();

	    // start with zeroes as initial guess
	    setDestinationRenderTexture(src-3);
	    clearDestination();

	    // solve coarser problem recursively
	    int correction = multigridVCycle(src-3, dest-3, coarseResidual);

	    // set destination to a fine grid and add result of last step
	    // to the fine grid solution we got earlier.
	    setDestinationRenderTexture(dest);
	    addTextures(correction, src);
	    { int q = dest; dest = src; src = q; }

	    // iterate some more on fine grid
	    for (i = 0; i != iterCount; i++) {
		setDestinationRenderTexture(dest);
		runRelax(src, rsGrid, false);
		int q = dest; dest = src; src = q;
	    }

	    return src;
	}
	
	void createEmptyRightSide(int dest) {
	    setDestinationRenderTexture(dest);
	    clearDestination();
	}

	void createRightSide(int dest, int scratch1, int scratch2) {
	    int j;
	    setDestinationRenderTexture(dest);
	    clearDestination();

	    // alpha isn't well supported for floating point textures so we need to do extra work to handle overlapping charges
	    for (j = 0; j != dragObjects.size(); j++) {
		// draw charged object into scratch texture
		setDestinationRenderTexture(scratch1);
		clearDestination();
		dragObjects.get(j).writeCharge();

		// add scratch texture to destination
		setDestinationRenderTexture(scratch2);
		addTextures(scratch1, dest);

		// copy to destination
		setDestinationRenderTexture(dest);
		copyTextureRG(scratch2);
	    }
	}
	
	// write materials (conductors, dielectrics) into render texture
	void writeMaterials() {
	    int i;
	    for (i = 0; i != dragObjects.size(); i++) {
		DragObject obj = dragObjects.get(i);
		if (obj.isCharged())
		    continue;
		obj.useMaterial();
		double xform[] = obj.transform;
		setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
		obj.writeMaterials();
	    }
	    setTransform(1, 0, 0, 0, 1, 0);
	}
	
	    boolean needsRepaint;
	    
	    void recalcAndRepaint() {
		calcLevel = 0;
		repaint();
	    }
	    
	    void repaint() {
	        if (!needsRepaint) {
	            needsRepaint = true;
	            forceRepaint();
	        }
	    }

	    void forceRepaint() {
		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
		    public boolean execute() {
			update();
			needsRepaint = false;
			return false;
		    }
		}, FASTTIMER);
	    }
	    
	    static int finalSrc = 0;
	    
	    // check if floater is touching fixed conductor
	    boolean touchingFixedObject(DragObject obj) {
		int j;
		for (j = 0; j != dragObjects.size(); j++) {
		    DragObject obj2 = dragObjects.get(j);
		    if (obj2 == obj)
			continue;
		    if (obj2.isFixedConductor() && obj.intersects(obj2)) {
			obj.setPotential(obj2.potential);
			return true;
		    }
		}
		return false;
	    }
	    
	    // recalculate potential
	    void recalculate() {
		if (calcLevel > 0) {
		    // if there are floating conductors, we don't bother to recalculate the charges/potentials on them.
		    int i;
		    for (i = 0; i != 2; i++) {
			recalculateStep(false, true);
			calcLevel++;
		    }
		    return;
		}
		
		int i;
		DragObject.currentFloatingConductor = null;
		Vector<DragObject> floatingVec = new Vector<DragObject>();
		Vector<DragObject> touchingVec = new Vector<DragObject>();
		for (i = 0; i != dragObjects.size(); i++) {
		    // check for floating conductors, see if they are touching fixed conductors or really floating
		    DragObject obj = dragObjects.get(i);
		    if (!obj.isFloating())
			continue;
		    if (touchingFixedObject(obj))
			touchingVec.add(obj);
		    else {
			floatingVec.add(obj);
			obj.setPotential(0);
		    }
		}
		
		recalculateStep(false, floatingVec.size() == 0);
		
		// update charge for floaters touching conductors
		for (i = 0; i != touchingVec.size(); i++) {
		    DragObject obj = touchingVec.get(i);
		    obj.updateFloatingCharge();
		}
		
		// no floating conductors?  we're done
		if (floatingVec.size() == 0) {
		    calcLevel++;
		    return;
		}
		
		int fct = floatingVec.size();
		double chargeMatrix[][] = new double[fct][fct];
		double baseCharge[] = new double[fct];
		for (i = 0; i != floatingVec.size(); i++)
		    baseCharge[i] = floatingVec.get(i).conductorCharge;
		
		// for each floating conductor, set the potential to 1 and then calculate the charge on all floating conductors
		// (with all charges removed and all other conductors grounded)
		for (i = 0; i != floatingVec.size(); i++) {
		    DragObject fo = floatingVec.get(0);
		    DragObject.currentFloatingConductor = fo;
		    recalculateStep(true, false);
		    int j;
		    for (j = 0; j != floatingVec.size(); j++)
			chargeMatrix[i][j] = floatingVec.get(j).conductorCharge;
		}
		DragObject.currentFloatingConductor = null;
		
		// we only handle case of single floating conductor.
		if (fct == 1) {
		    DragObject f0 = floatingVec.get(0);
		    // calculate potential needed to get charge on floating conductor equal to goal charge
		    double pot = (f0.totalChargeFloating-baseCharge[0])/chargeMatrix[0][0];
		    f0.setPotential(pot);
		    recalculateStep(false, true);
		}
		calcLevel++;
	    }
	    
	    // recalculate potential, assuming all floating conductors have fixed potentials.
	    // suppressCharges = true when leaving out charges (for floating calculation)
	    // finalResult = false if we're doing intermediate calculation for handling floaters.
	    // finalResult = true if no floaters or if we have the floating potentials.  it doesn't mean we can't refine the result.
	    void recalculateStep(boolean suppressCharges, boolean finalResult) {
		// get render texture count.  there are 3 of each size, each group of 3 having twice the width and height of the last group.
		// there is also an extra one not included in the count.
		int rtnum = getRenderTextureCount();
		
		console("Recalc " + calcLevel);
		if (stoppedCheck.getState())
			return;
		int i;

		setResidualFlag(false);
		
		// create right side using largest render texture
		if (suppressCharges)
		    createEmptyRightSide(rtnum-1);
		else
		    createRightSide(rtnum-1, rtnum-2, rtnum-3);
		writeMaterials();
		
		// create right side for smaller render textures
		for (i = rtnum-1-3; i > 0; i -= 3) {
		    setDestinationRenderTexture(i);
		    // scale down charges from larger texture
		    copyTextureRG(i+3);
		    // create materials (this seems to give better results than scaling down)
		    writeMaterials();
		}
		
		if (calcLevel > 0 && finalResult) {
		    // refining result?  just do a v-cycle
		    setDestinationRenderTexture(rtnum-3);
		    copyTextureRG(finalSrc);
		    int src = multigridVCycle(rtnum-3, rtnum-2, rtnum-1);
		    
		    setDestinationRenderTexture(rtnum-3);
		    // calc difference from last result, and if it's small enough, declare it done
		    if (calcDifference(finalSrc, src) < 25)
			calcLevel = 5000;
		    
		    setDestinationRenderTexture(finalSrc);
		    copyTextureRGB(src);
		    calculateCharge(src);
		    return;
		}
		
		// start with zeroes on smallest grid
		setDestinationRenderTexture(0);
		clearDestination();
		
		// solve exactly
		solveExactly(0, 1, 2);

		int src = 1;
		for (i = 3; i < rtnum; i += 3) {
		    // interpolate to finer grid
		    setDestinationRenderTexture(i);
		    copyTextureRG(src);

		    // do a v-cycle
		    src = multigridVCycle(i, i+1, i+2);
		}
		
		if (finalResult) {
		    // copy to result render texture (not included in rtnum)
		    setDestinationRenderTexture(rtnum);
		    copyTextureRGB(src);
		    finalSrc = rtnum;
		}
		
		calculateCharge(src);
	    }
	    
	    void calculateCharge(int csrc) {
		// calculate charge on conductors given potential calculation in csrc
		setChargeSource(csrc);
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
		    int src = csrc-1;
		    DragObject obj = dragObjects.get(i);
		    if (!obj.isConductor())
			continue;
		    
		    // calculate charge on each conductor one at a time
		    setDestinationRenderTexture(src);
		    clearDestination();
		    double xform[] = obj.transform;
		    setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
		    obj.calcCharge();
//		    if (i >= 0) { finalSrc = src; break; }   // uncomment to test charge calculation
		
		    // sum charge into smaller and smaller bitmaps so we can count it more efficiently.
		    // it turns out it's so fast to count the large bitmap that we don't really need this,
		    // but I wrote it before I discovered that
		    while (src >= 3) {
			setDestinationRenderTexture(src-3);
			clearDestination();
			sumTexture(src);
			src = src-3;
		    }
		    // don't know where sqrt(2) comes from
		    // e0 is from Gauss's law, .5 is from fact that we use 2-pixel thick layer to compute charge
		    obj.setConductorCharge(getCharge()*e0*.5/Math.sqrt(2));
		}
		
		// now do it again to get one map with all charges
		int src = csrc-1;
		setDestinationRenderTexture(src);
		clearDestination();
		for (i = 0; i != dragObjects.size(); i++) {
		    DragObject obj = dragObjects.get(i);
		    if (!obj.isConductor())
			continue;		    
		    double xform[] = obj.transform;
		    setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
		    obj.calcCharge();
		}
		setChargeSource(src);
		chargeSource = src;
	    }
	    
	    public void update() {
		if (calcLevel == 0)
		    calcStart = System.currentTimeMillis(); 
		int rtnum = getRenderTextureCount();
		if (calcLevel < 2000) {
		    recalculate();
		    if (calcLevel < 2000)
			forceRepaint();
		    else
			console("calc time: " + (System.currentTimeMillis() - calcStart));
		}

		int src = finalSrc;

		// render textures 0-2 are size 16
		// render textures 3-5 are size 32
		// etc.
		double brightMult = Math.exp(brightnessBar.getValue() / 100. - 5.);
		double equipMult = Math.exp(equipotentialBar.getValue() / 100. - 5.);
		equipMult *= brightMult/.9;
		if (!equipCheck.getState())
		    equipMult = 0;
		int i;
		int rsrc = rtnum-1;
		// tweak brightness for potential display or 3D
		switch (displayChooser.getSelectedIndex()) {
		case DISP_POT:
		    displayPotential(src, rsrc, brightMult*.02666);
		    displayEquip(src, rsrc, equipMult);
		    break;
		case DISP_CHARGE:
		    // this only includes calculated charge, need to show charge objects too!
		    displayPotential(chargeSource, rsrc, brightMult*.02666);
		    break;
		case DISP_3D:
		    display3D(src, rsrc, brightMult*.05333, equipMult);
		    break;
		case DISP_FIELD:
		case DISP_LINES:
		    displayField(src, rsrc, brightMult, 1, 0);
		    displayEquip(src, rsrc, equipMult);
		    break;
		case DISP_D:
		    displayField(src, rsrc, brightMult, 1, 1);
		    displayEquip(src, rsrc, equipMult);
		    break;
		case DISP_P:
		    displayField(src, rsrc, brightMult, 0, 1);
		    break;
		}
		if (displayChooser.getSelectedIndex() == DISP_LINES) {
		    fetchPotentialPixels(src);
		    for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			obj.drawFieldLines();
		    }
		    freePotentialPixels();
		}
		if (displayChooser.getSelectedIndex() != DISP_3D)
		    for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (obj.selected)
			    setDrawingSelection(.6+.4*Math.sin(t*.2));
			else
			    setDrawingSelection(1);
			double xform[] = obj.transform;
			setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
			obj.display();
		    }
		setTransform(1, 0, 0, 0, 1, 0);
		setDrawingSelection(-1);
		doCoordsLabel();
	    }

	void doCoordsLabel() {
	    if (calcLevel < 2000) {
		coordsLabel.setText("Calculating...");
		coordsLabel.setVisible(true);
		return;
	    }
		if (mouseLocation == null) {
			coordsLabel.setVisible(false);
			return;
		}
		setDestinationRenderTexture(finalSrc); // for getProbeValue()
		Point pt = mouseLocation;
		JsArrayNumber probe = getProbeValue(pt.x, pt.y);
		String txt = "V = " + getUnitText(probe.get(0), "V") + ", E = (" +
			getUnitText((probe.get(3)-probe.get(4))/(2*lengthScale), "V/m") + ", " +
			getUnitText((probe.get(2)-probe.get(1))/(2*lengthScale), "V/m") + ")";
		if (selectedObject != null) {
		    if (selectedObject.isConductor())
			txt += ", Q = " + getUnitText(selectedObject.getDisplayedCharge(), "C");
		    String more = selectedObject.selectText();
		    if (more != null)
			txt += ", " + more;
		}
		coordsLabel.setText("(" + getLengthText(pt.x) + ", " + getLengthText(windowHeight-1-pt.y) + ") " + txt);
		absolutePanel.setWidgetPosition(coordsLabel,
				0,
				(pt.x < windowWidth/4 && pt.y > windowHeight*3/4) ? 0 :
					cv.getOffsetHeight()-coordsLabel.getOffsetHeight());
		coordsLabel.setVisible(true);
	}
	
	int abs(int x) {
		return x < 0 ? -x : x;
	}

	int sign(int x) {
		return (x < 0) ? -1 : (x == 0) ? 0 : 1;
	}

	void setDamping() {
		/*
		 * int i; double damper = dampingBar.getValue() * .00002;// was 5
		 * dampcoef = Math.exp(-damper);
		 */
		dampcoef = 1;
	}

	void setFreqBar(int x) {
		freqBar.setValue(x);
		freqBarValue = x;
		freqTimeZero = 0;
	}

	void setFreq() {
	}

	void setResolution() {
		int newWidth = resBar.getValue();
		setResolution(newWidth, 0);
	}

	void setResolution(int ns, int border) {
		int newSize = 1;
		while (newSize < ns)
		    newSize *= 2;
		int oldWidth = windowWidth;
		if (newSize == gridSizeX && border == 0)
			return;
		if (border == 0) {
			border = newSize / 4;
			if (border < 20)
				border = 20;
		}
//		border = 0;
		gridSizeX = gridSizeY = newSize;
		windowWidth = windowHeight = newSize-border*2;
		windowOffsetX = windowOffsetY = border;
		windowBottom = windowOffsetY + windowHeight - 1;
		windowRight = windowOffsetX + windowWidth - 1;
		setResolutionGL(gridSizeX, gridSizeY, windowOffsetX, windowOffsetY);
		console("res gs=" + gridSizeX + " ww=" + windowWidth + " wo=" + windowOffsetX + " "+ ns);
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			obj.rescale(windowWidth/(double)oldWidth);
		}
		calcLevel = 0;
	}

	void setResolution(int x) {
		setResolution(x, 0);
	}

	void view3dDrag(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		set3dViewAngle(x-dragX, y-dragY);
		dragX = x;
		dragY = y;
	}

	void deleteAllObjects() {
		dragObjects.removeAllElements();
		selectedObject = null;
	}

	void resetTime() {
		t = 0;
		iters = 0;
	}
	
	void doSetup() {
		if (setupList.size() == 0)
			return;
		resetTime();
		if (resBar.getValue() < 32)
			setResolution(32);
		deleteAllObjects();
		dampingBar.setValue(10);
		setFreqBar(5);
		setBrightness(10);
		setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
		setup.select();
		setDamping();
		enableDisableUI();
	}

	void setBrightness(int x) {
		double m = x / 5.;
		m = (Math.log(m) + 5.) * 100;
		brightnessBar.setValue((int) m);
	}

	void doColor() {
		int cn = colorChooser.getSelectedIndex();
		wallColor = schemeColors[cn][0];
		posColor = schemeColors[cn][1];
		negColor = schemeColors[cn][2];
		zeroColor = schemeColors[cn][3];
		posMedColor = schemeColors[cn][4];
		negMedColor = schemeColors[cn][5];
		medColor = schemeColors[cn][6];
		sourceColor = schemeColors[cn][7];
		int zerocol3d = zeroColor.toInteger();
		if (zerocol3d == 0)
			zerocol3d = 0x808080;
		setColors(wallColor.toInteger(), posColor.toInteger(), negColor.toInteger(),
				zeroColor.toInteger(), posMedColor.toInteger(), negMedColor.toInteger(),
				  medColor.toInteger(), sourceColor.toInteger(), zerocol3d);
	}

	void addDefaultColorScheme() {
		String schemes[] = {
				"#808080 #00ffff #000000 #008080 #0000ff #000000 #000080 #ffffff",
				"#808080 #00ff00 #ff0000 #000000 #00ffff #ff00ff #0000ff #0000ff",
				"#800000 #00ffff #0000ff #000000 #80c8c8 #8080c8 #808080 #ffffff",
				"#800000 #ffffff #000000 #808080 #0000ff #000000 #000080 #00ff00",
				"#800000 #ffff00 #0000ff #000000 #ffff80 #8080ff #808080 #ffffff",
				"#808080 #00ff00 #ff0000 #FFFFFF #00ffff #ff00ff #0000ff #0000ff",
				"#FF0000 #00FF00 #0000FF #FFFF00 #00FFFF #FF00FF #FFFFFF #000000" };
		int i;

		for (i = 0; i != 7; i++)
			decodeColorScheme(i, schemes[i]);
		// colorChooser.hide();
        colorChooser.select(1);
	}

	void decodeColorScheme(int cn, String s) {
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			int i;
			for (i = 0; i != 8; i++)
				schemeColors[cn][i] = Color.hex2Rgb(st.nextToken());
		}
		colorChooser.add("Color Scheme " + (cn + 1));
	}

    void getSetupList() {

    	String url;
    	url = GWT.getModuleBaseURL()+"setuplist.txt"+"?v="+random.nextInt(); 
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					GWT.log("File Error Response", exception);
				}

				public void onResponseReceived(Request request, Response response) {
					// processing goes here
					if (response.getStatusCode()==Response.SC_OK) {
					String text = response.getText();
					processSetupList(text.getBytes(), text.length());
					if (startLayoutText == null)
						doSetup();
					else
						readImport(startLayoutText);
					// end of processing
					}
					else 
						GWT.log("Bad file server response:"+response.getStatusText() );
				}
			});
		} catch (RequestException e) {
			GWT.log("failed file reading", e);
		}
    }
		
    void processSetupList(byte b[], int len) {
    	int p;
    	for (p = 0; p < len; ) {
    		int l;
    		for (l = 0; l != len-p; l++)
    			if (b[l+p] == '\n') {
    				l++;
    				break;
    			}
    		String line = new String(b, p, l-1);
    		if (line.charAt(0) == '#')
    			;
/*    		else if (line.charAt(0) == '+') {
    		//	MenuBar n = new Menu(line.substring(1));
    			MenuBar n = new MenuBar(true);
    			n.setAutoOpen(true);
    			currentMenuBar.addItem(line.substring(1),n);
    			currentMenuBar = stack[stackptr++] = n;
    		} else if (line.charAt(0) == '-') {
    			currentMenuBar = stack[--stackptr-1];
    		} */
    		else {
    			int i = line.indexOf(' ');
    			if (i > 0) {
    				String title = line.substring(i+1);
    				boolean first = false;
    				if (line.charAt(0) == '>')
    					first = true;
    				String file = line.substring(first ? 1 : 0, i);
    				Setup s = new FileSetup(title, file);
    				setupList.add(s);
    				setupChooser.add("Example: " + title);
    			}
    		}
    		p += l;
    	}
}


	void readSetupFile(String str, String title) {
		resetTime();
		console("reading example " + str);
		String url=GWT.getModuleBaseURL()+"examples/"+str+"?v="+random.nextInt(); 
		loadFileFromURL(url);
		enableDisableUI();
	}

	void loadFileFromURL(String url) {
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					GWT.log("File Error Response", exception);
				}

				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode()==Response.SC_OK) {
					String text = response.getText();
					readImport(text);
					}
					else 
						GWT.log("Bad file server response:"+response.getStatusText() );
				}
			});
		} catch (RequestException e) {
			GWT.log("failed file reading", e);
		}
		
	}

	String dumpLayout() {
		String dump = "";

		int i;
		dump = "$ 1 " + windowWidth + " " + windowOffsetX + " " + dampingBar.getValue() + " " +
				displayChooser.getSelectedIndex() + " " + brightnessBar.getValue() + " " + lengthScale + " " + equipotentialBar.getValue() + "\n";
/*		for (i = 0; i != sourceCount; i++) {
			OscSource src = sources[i];
			dump += "s " + src.x + " " + src.y + "\n";
		}*/
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			dump += obj.dump() + "\n";
		}
		return dump;
	}
	
	void readImport(String s) { readImport (s, false); }
	
	void readImport(String s, boolean retain) {
		if (!retain) {
			resetTime();
			deleteAllObjects();
		}
		char b[] = new char[s.length()];
		s.getChars(0, s.length(), b, 0);
		int len = s.length();
		int p;
		int x = 0;
		int srci = 0;
		int storedWidth = windowWidth;
//		setupChooser.select(0);
//		setup = (Setup) setupList.elementAt(0);
		for (p = 0; p < len;) {
		    int l;
		    int linelen = len-p; // IES - changed to allow the last line to not end with a delim.
		    for (l = 0; l != len - p; l++)
			if (b[l + p] == '\n' || b[l + p] == '\r') {
			    linelen = l++;
			    if (l + p < b.length && b[l + p] == '\n')
				l++;
			    break;
			}
		    String line = new String(b, p, linelen);
		    StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
		    while (st.hasMoreTokens()) {
			String type = st.nextToken();
			int tint = type.charAt(0);
			try {
			    if (tint == '$') {
				int flags = new Integer(st.nextToken()).intValue();
				if ((flags & 1) == 0)
				    return;

				//						dump = "$ 1 " + windowWidth + " " + windowOffsetX + " " +
				//								fixedEndsCheck.getState() + " " + brightnessBar.getValue() + "\n";

				int ww = Integer.parseInt(st.nextToken());
				int wo = Integer.parseInt(st.nextToken());
				storedWidth = ww;
				setResolution(ww+wo*2, wo);
				reinit(false);

				dampingBar.setValue(Integer.parseInt(st.nextToken()));
				st.nextToken();
				//						displayChooser.setSelectedIndex(Integer.parseInt(st.nextToken()));
				brightnessBar.setValue(new Integer(st.nextToken())
					.intValue());
				lengthScale = Double.parseDouble(st.nextToken());
				try {
				    equipotentialBar.setValue(Integer.parseInt(st.nextToken()));
				} catch (Exception e) {}
				break;
			    }
			    if (tint >= '0' && tint <= '9')
				tint = new Integer(type).intValue();
			    DragObject newobj = createObj(tint, st);
			    if (newobj==null) {
				console("unrecognized dump type: " + type);
				break;
			    }
			    if (newobj.getDumpType() != tint)
				console("dump type mismatch for " + tint);
			    dragObjects.add(newobj);
			} catch (Exception ee) {
			    console("got exception when reading setup");
			    ee.printStackTrace();
			    break;
			}
			break;
		    }
		    p += l;

		}
		
                int i;
                for (i = 0; i != dragObjects.size(); i++) {
                        DragObject obj = dragObjects.get(i);
                        obj.rescale(windowWidth/(double)storedWidth);
                }

		setDamping();
		wallsChanged();
		enableDisableUI();
	}

	abstract class Setup {
		abstract String getName();

		void select() {}

		void deselect() {
		}

		double sourceStrength() {
			return 1;
		}

		Setup createNext() { return null; }

		void eachFrame() {
		}

		float calcSourcePhase(double ph, float v, double w) {
			return v;
		}
	};

	class FileSetup extends Setup {
		String title, file;
		
		FileSetup(String t, String f) {
			title = t;
			file = f;
		}
		
		void select() {
			readSetupFile(file, title);
		}
		
		String getName() { return title; }
	}
	
	@Override
	public void onMouseUp(MouseUpEvent event) {
		event.preventDefault();
//		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
//		    return;
		dragging = false;
		dragSet = dragClear = false;
		if (mouseObject == null)
			preserveSelection = false;
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		event.preventDefault();
		doMouseMove(event);
	}
	
	Point getPointFromEvent(MouseEvent<?> event) {
		int xp = event.getX()*windowWidth/winSize.width;
		int yp = event.getY()*windowHeight/winSize.height;
		return new Point(xp, yp);
	}
	
	double getRealTime() {
	    return t;
	}
	
	void doMouseMove(MouseEvent<?> event) {
		Point pt = getPointFromEvent(event);
		mouseLocation = pt;
		if (rotationMode) {
			selectedObject.rotateTo(pt.x, pt.y);
			return;
		}
		if (dragging) {
			dragMouse(event);
			repaint();
			return;
		}
		int x = event.getX();
		int y = event.getY();
		
		dragPoint = getPointFromEvent(event);
		dragStartX = dragX = x;
		dragStartY = dragY = y;
		
		double minf = 5 * windowWidth/winSize.height + 1;
		double bestf = minf;
		Point mp = getPointFromEvent(event);
		draggingHandle = null;
		if (selectedObject != null) {
			int i;
			// select handle?
			Point p = selectedObject.inverseTransformPoint(mp);
			for (i = 0; i != selectedObject.handles.size(); i++) {
				DragHandle dh = selectedObject.handles.get(i);
				double r = DragObject.hypotf(p.x-dh.x, p.y-dh.y);
				if (r < bestf) {
					draggingHandle = dh;
					bestf = r;
				}
			}
			if (draggingHandle != null)
				return;
		}

		// select object?
		DragObject sel = null;
		bestf = 1e8;
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			Point p = obj.inverseTransformPoint(mp);
			double ht = obj.hitTest(p.x, p.y);
			
	        // if there are no better options, select a RectDragObject if we're tapping
	        // inside it.
			if (ht > minf && !obj.hitTestInside(p.x, p.y))
				continue;
			
			// find best match
			if (ht < bestf) {
				sel = obj;
				bestf = ht;
			}
		}
		if (!preserveSelection)
			setSelectedObject(sel);
		mouseObject = sel;
		repaint();
	}

    static String getUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-14)
            return "0 " + u;
        if (va < 1e-9)
            return showFormat.format(v*1e12) + " p" + u;
        if (va < 1e-6)
            return showFormat.format(v*1e9) + " n" + u;
        if (va < 1e-3)
            return showFormat.format(v*1e6) + " \u03bc" + u;
        if (va < 1e-2 || (va < 1 && !u.equals("m")))
            return showFormat.format(v*1e3) + " m" + u;
        if (va < 1)
            return showFormat.format(v*1e2) + " c" + u;
        if (va < 1e3)
            return showFormat.format(v) + " " + u;
        if (va < 1e6)
            return showFormat.format(v*1e-3) + " k" + u;
        if (va < 1e9)
            return showFormat.format(v*1e-6) + " M" + u;
        if (va < 1e12)
            return shortFormat.format(v*1e-9) + " G" + u;
        if (va < 1e15)
            return shortFormat.format(v*1e-12) + " T" + u;
        if (va < 1e18)
            return shortFormat.format(v*1e-15) + " P" + u;
        return showFormat.format(v*1e-18) + " E" + u;
    }
    
    static String getShortUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-13)
            return null;
        if (va < 1e-9)
            return shortFormat.format(v*1e12) + "p" + u;
        if (va < 1e-6)
            return shortFormat.format(v*1e9) + "n" + u;
        if (va < 1e-3)
            return shortFormat.format(v*1e6) + "\u03bc" + u;
        if (va < 1e-2)
            return shortFormat.format(v*1e3) + "m" + u;
        if (va < 1)
            return shortFormat.format(v*1e2) + "c" + u;
        if (va < 1e3)
            return shortFormat.format(v) + u;
        if (va < 1e6)
            return shortFormat.format(v*1e-3) + "k" + u;
        if (va < 1e9)
            return shortFormat.format(v*1e-6) + "M" + u;
        if (va < 1e12)
            return shortFormat.format(v*1e-9) + "G" + u;
        if (va < 1e15)
            return shortFormat.format(v*1e-12) + "T" + u;
        if (va < 1e18)
            return shortFormat.format(v*1e-15) + "P" + u;
        return shortFormat.format(v*1e-18) + "E" + u;
    }
    
    // convert pixels to meters and return as string
    String getLengthText(double px) {
    	return getUnitText(px * lengthScale, "m");
    }

    // convert pixels/iter to m/s and return as string
    String getSpeedText(double px) {
	return "speed";
    }
    
	void dragMouse(MouseEvent<?> event) {
		if (displayChooser.getSelectedIndex() == DISP_3D) {
			view3dDrag(event);
			return;
		}
		dragging = true;
		adjustResolution = false;

		Point pt = getPointFromEvent(event);
		if (draggingHandle != null) {
			Point mp = selectedObject.inverseTransformPoint(pt);
			draggingHandle.dragTo(mp.x, mp.y);
			calcLevel = 0;
		} else if (isSelection()) {
			if (dragPoint.x != pt.x || dragPoint.y != pt.y) {
				int i;
				for (i = 0; i != dragObjects.size(); i++) {
					DragObject obj = dragObjects.get(i);
					if (obj.isSelected())
						obj.drag(pt.x-dragPoint.x, pt.y-dragPoint.y);
				}
				dragPoint = pt;
				calcLevel = 0;
			}
		}
	}

	boolean isSelection() {
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (obj.isSelected())
				return true;
		}
		return false;
	}
	
	void enableDisableUI() {
		int i;
		
		// check if all sources are same frequency
		Charge src1 = null;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (!(obj instanceof Charge))
				continue;
			Charge src = (Charge)obj;
			
		}
		/*
		if (src1 == null)
			freqBar.disable();
		else {
			freqBar.enable();
			ignoreFreqBarSetting = true;
			freqBar.setValue((int)(src1.frequency/freqMult));
			ignoreFreqBarSetting = false;
		}
		*/
	}
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		event.preventDefault();
		adjustResolution = false;
		pushUndo();
		doMouseMove(event);
		if (rotationMode) {
			rotationMode = false;
			return;
		}
		dragging = true;
		
		if (displayChooser.getSelectedIndex() == DISP_3D)
			return;

		Point mp = getPointFromEvent(event);

		if (draggingHandle == null) {
			if (mouseObject == null)
				setSelectedObject(null);
			else {
				if (!mouseObject.isSelected())
					setSelectedObject(mouseObject);
				preserveSelection = true;
			}
		}
//		console("onmd " + mouseObject + " " + preserveSelection + " " + draggingHandle);
	}
	
	void setSelectedObject(DragObject obj) {
		if (obj != null && obj.isSelected())
			return;
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject dd = dragObjects.get(i);
			dd.setSelected(false);
		}
		selectedObject = obj;
		if (obj != null)
			selectedObject.setSelected(true);
		preserveSelection = false;
	}
	
	@Override
	public void onMouseWheel(MouseWheelEvent event) {
        event.preventDefault();
        if (selectedObject != null && selectedObject.canRotate()) {
        	// rotate in 15 degree steps, but save mouse wheel motions that aren't large enough for a rotation
        	int dy = event.getDeltaY() + mouseWheelAccum;
        	int dy10 = dy/10;
        	mouseWheelAccum = dy-dy10*10;
        	console("wheel " + mouseWheelAccum + " " + dy + " " + dy10 + " " + event.getDeltaY());
        	selectedObject.rotate(dy10*Math.PI/12);
        	preserveSelection = true;
        }
	if (displayChooser.getSelectedIndex() == DISP_3D) {
        	zoom3d *= Math.exp(-event.getDeltaY() * .01);
        	set3dViewZoom(zoom3d);
        }
	repaint();
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		dragging = false;
		dragSet = dragClear = false;
		mouseLocation = null;
	}

	@Override
	public void onPreviewNativeEvent(NativePreviewEvent event) {
		// TODO Auto-generated method stub

	}

	int menuX, menuY;
    PopupPanel contextPanel = null;

	@Override
	public void onContextMenu(ContextMenuEvent e) {
        e.preventDefault();
        menuX = e.getNativeEvent().getClientX();
        menuY = e.getNativeEvent().getClientY();
        doPopupMenu();
	}

    void longPress() {
    	menuX = dragStartX;
    	menuY = dragStartY;
    	doPopupMenu();
    }

    void doPopupMenu() {
    	if (selectedObject != null) {
                elmEditMenuItem .setEnabled(selectedObject.getEditInfo(0) != null);
                elmRotateMenuItem.setEnabled(selectedObject.canRotate());
                contextPanel=new PopupPanel(true);
                contextPanel.add(elmMenuBar);
                contextPanel.setPopupPosition(menuX, menuY);
                contextPanel.show();
        } else {
    	int x, y;
    	
                contextPanel=new PopupPanel(true);
                contextPanel.add(mainMenuBar);
                x=Math.max(0, Math.min(menuX, cv.getCoordinateSpaceWidth()-400));
                y=Math.max(0, Math.min(menuY,cv.getCoordinateSpaceHeight()-450));
                contextPanel.setPopupPosition(x,y);
                contextPanel.show();
        }
    }

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		event.preventDefault();
	}

	void doCreateWall() {
		Wall w = new Wall();
		w.setInitialPosition();
		dragObjects.add(w);
	}
	
	Rectangle findSpace(DragObject obj, int sx, int sy) {
		int spsize = 20;
		boolean spacegrid[][] = new boolean[spsize][spsize];
		int i;
		int jx, jy;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject d = dragObjects.get(i);
			Rectangle r = d.boundingBox();
			for (jx = r.x*spsize/windowWidth; jx <= (r.x+r.width)*spsize/windowWidth; jx++)
				for (jy = r.y*spsize/windowHeight; jy <= (r.y+r.height)*spsize/windowHeight; jy++) {
					if (jx >= 0 && jy >= 0 && jx < spsize && jy < spsize) {
						spacegrid[jx][jy] = true;
					}
				}
		}
        int spiralIndex = 1, spiralCounter = 1;
        int tx = spsize/2;
        int ty = spsize/2;
        int dx = 1;
        int dy = 0;
        while (true) {
        	if (!spacegrid[tx][ty]) {
        		return new Rectangle(tx*windowWidth/spsize+2, ty*windowHeight/spsize+2,
        				windowWidth/spsize-4,
        				windowHeight/spsize-4);
        	}
        	tx += dx;
        	ty += dy;
            if (--spiralIndex == 0) {
                int d0 = dx;
                dx = dy;
                dy = -d0;
                if (dy == 0) spiralCounter++;
                spiralIndex = spiralCounter;
            }
            if (tx < 0 || ty < 0 || tx >= spsize || ty >= spsize)
            	break;
        }
		return new Rectangle(gridSizeX/2, gridSizeY/2, spsize, spsize);
	}
	
	@Override
	public void onClick(ClickEvent event) {
//		event.preventDefault();
		repaint();
		if (event.getSource() == resBar) {
		    setResolution();
//		    reinit();
		}
		if (event.getSource() == dampingBar)
		    setDamping();
		if (event.getSource() == freqBar) {
		    setFreq();
		}
	}

	@Override
	public void onChange(ChangeEvent event) {

			if (event.getSource() == stoppedCheck) {
//			    cv.repaint();
			    return;
			}
			if (event.getSource() == setupChooser)
			    doSetup();
			if (event.getSource() == colorChooser){
			    doColor();
			}
			repaint();
	}

    void pushUndo() {
        String s = dumpLayout();
        if (undoStack.size() > 0 &&
                        s.compareTo(undoStack.lastElement()) == 0)
                return;
        redoStack.removeAllElements();
        undoStack.add(s);
        enableUndoRedo();
    }

    void doUndo() {
        if (undoStack.size() == 0)
                return;
        redoStack.add(dumpLayout());
        String s = undoStack.remove(undoStack.size()-1);
        readImport(s);
        enableUndoRedo();
    }

    void doRedo() {
        if (redoStack.size() == 0)
                return;
        undoStack.add(dumpLayout());
        String s = redoStack.remove(redoStack.size()-1);
        readImport(s);
        enableUndoRedo();
    }

    void enableUndoRedo() {
        redoItem.setEnabled(redoStack.size() > 0);
        undoItem.setEnabled(undoStack.size() > 0);
    }

    void setMenuSelection() {
        if (menuObject != null) {
                if (menuObject.selected)
                        return;
                clearSelection();
                menuObject.setSelected(true);
        }
    }

    void doCut() {
        int i;
        pushUndo();
        setMenuSelection();
        clipboard = "";
        for (i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
                if (ce.isSelected()) {
                        clipboard = ce.dump() + "\n" + clipboard;
                        ce.delete();
                        dragObjects.removeElementAt(i);
                }
        }
        writeClipboardToStorage();
        enablePaste();
        wallsChanged();
    }

    void writeClipboardToStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
                return;
        stor.setItem("emstaticClipboard", clipboard);
    }
    
    void readClipboardFromStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
                return;
        clipboard = stor.getItem("emstaticClipboard");
    }

    void doDelete() {
        int i;
        pushUndo();
        setMenuSelection();
        boolean hasDeleted = false;

        for (i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
                if (ce.isSelected()) {
                        ce.delete();
                        dragObjects.removeElementAt(i);
                        hasDeleted = true;
                }
        }

        if ( !hasDeleted )
        {
        	/*
            for (i = dragObjects.size()-1; i >= 0; i--) {
            	DragObject ce = dragObjects.get(i);
                        if (ce == mouseObject) {
                                ce.delete();
                                dragObjects.removeElementAt(i);
                                hasDeleted = true;
                                setMouseElm(null);
                                break;
                        }
                }
                */
        }

        if ( hasDeleted )
        	wallsChanged();
    }

    void doCopy() {
        int i;
        clipboard = "";
        setMenuSelection();
        for (i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
        	if (ce.isSelected())
        		clipboard += ce.dump() + "\n";
        }
        writeClipboardToStorage();
        enablePaste();
    }

    void enablePaste() {
        if (clipboard == null || clipboard.length() == 0)
                readClipboardFromStorage();
        pasteItem.setEnabled(clipboard != null && clipboard.length() > 0);
    }

    void doDuplicate() {
        int i;
        String s = "";
        setMenuSelection();
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	if (ce.isSelected())
        		s += ce.dump() + "\n";
        }
        doPaste(s);
    }

    void doPaste(String dump) {
        pushUndo();
        clearSelection();
        int i;
        int oldsz = dragObjects.size();
        if (dump != null)
            readImport(dump, true);
        else {
            readClipboardFromStorage();
            readImport(clipboard, true);
        }

        // select new items
        for (i = oldsz; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	int j;
        	// make sure new items are not on top of old items
        	for (j = 0; j != oldsz; j++) {
        		if (ce.boundingBox().equals(dragObjects.get(j).boundingBox())) {
        			// move new one slightly
        			ce.drag(windowWidth/32, 0);
        			j = -1;
        		}
        	}
        	ce.setSelected(true);
        }
        if (dragObjects.size() == oldsz+1)
        	selectedObject = dragObjects.get(oldsz);
        preserveSelection = true;
        wallsChanged();
    }

    void clearSelection() {
        int i;
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
            ce.setSelected(false);
        }
        selectedObject = null;
        preserveSelection = false;
    }
    
    void doSelectAll() {
        int i;
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	ce.setSelected(true);
        }
        selectedObject = null;
        preserveSelection = true;
    }

    void createNewLoadFile() {
        // This is a hack to fix what IMHO is a bug in the <INPUT FILE element
        // reloading the same file doesn't create a change event so importing the same file twice
        // doesn't work unless you destroy the original input element and replace it with a new one
        int idx=verticalPanel.getWidgetIndex(loadFileInput);
        LoadFile newlf=new LoadFile(this);
        verticalPanel.insert(newlf, idx);
        verticalPanel.remove(idx+1);
        loadFileInput=newlf;
    }

    boolean canMakeFloating(DragObject obj) {
	int ct = 0;
	int i;
	for (i = 0; i != dragObjects.size(); i++) {
	    DragObject ce = dragObjects.get(i);
	    if (ce.isFloating() && obj != ce)
		ct++;
	}
	return ct == 0;
    }
}
