/* Zssapp.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 16, 2010 6:22:52 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.zul;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zss.app.Consts;
import org.zkoss.zss.app.file.SpreadSheetMetaInfo;
import org.zkoss.zss.app.zul.ctrl.DesktopCellStyleContext;
import org.zkoss.zss.app.zul.ctrl.DesktopWorkbenchContext;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menubar;

/**
 * 
 * @author sam
 *
 */
public class Zssapp extends Div implements IdSpace  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static String KEY_ZSSAPP = "org.zkoss.zss.app.zul.zssApp";
	/*Default spreadsheet*/
	//TODO: in mainWin id space, move to here
	private Spreadsheet spreadsheet;
	
	//private Window mainWin;
	private Div mainWin;
	
	Appmenubar _appmenubar;
	
	/* Context */
	DesktopWorkbenchContext workbenchContext = new DesktopWorkbenchContext();
	DesktopCellStyleContext cellStyleContext = new DesktopCellStyleContext();
	
	public Zssapp() {
		
		if (Executions.getCurrent().getParameter("edit") != null) {
			Executions.createComponents("~./zssapp/html/zssappEdit.zul", this, null);
		} else {
			Executions.createComponents(Consts._Zssapp_zul, this, null);
		}
//		Executions.createComponents(Consts._Zssapp_zul, this, null);
		Components.wireVariables(this, this, '$', true, true);
		Components.addForwards(this, this, '$');
		
		spreadsheet = (Spreadsheet)mainWin.getFellow("spreadsheet");
//		Menubar bar = (Menubar) mainWin.getFellow("menubar");
//		_appmenubar = new Appmenubar(bar);
		
		// set src from request parameters
		String fileName = Executions.getCurrent().getParameter("fileName");
		SpreadSheetMetaInfo ssmi = SpreadSheetMetaInfo.getMetaInfos().get(fileName);
		for (Object entry : Executions.getCurrent().getParameterMap().entrySet()) {
			@SuppressWarnings("unchecked")
			Entry<String, String[]> e = (Entry<String, String[]>) entry;
			// Update report parameters only. Don't support new parameters. 
			if (ssmi.getReportParams().containsKey(e.getKey())) {
				ssmi.getReportParams().put(e.getKey(), e.getValue()[0]);
			}
		}
		
		this.setSrc(ssmi.getSrc());
	}
	
	public void setSrc(String src) {
		getDesktopWorkbenchContext().getWorkbookCtrl().setBookSrc(src);
	}
	
	/**
	 * Sets {@link #Book}
	 * @param book
	 */
	public void setBook(Book book) {
		getDesktopWorkbenchContext().getWorkbookCtrl().setBook(book);
	}
	
	public void setMaxrows(int maxrows) {
		spreadsheet.setMaxrows(maxrows);
	}
	
	public void setMaxcolumns(int maxcols) {
		spreadsheet.setMaxcolumns(maxcols);
	}
	
	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	} 
	
	public static Zssapp getInstance(Component comp) {
		Zssapp self = null;
		Component n = null;
		ArrayList<Component> setPropList = null;
		for (n = comp; n != null; n = n.getParent()) {
			self = (Zssapp) n.getAttribute(KEY_ZSSAPP);
			if (self == null) {
				if (setPropList == null)
					setPropList = new ArrayList<Component>();
				setPropList.add(n);
			} else {
				break;
			}
			
			if (n instanceof Zssapp) {
				self = (Zssapp) n;
				break;
			}
		}
		
		if (self != null && setPropList != null && setPropList.size() > 0) {
			for (Component c : setPropList) {
				c.setAttribute(KEY_ZSSAPP, self);
			}
		}
		return self;
	}
	public DesktopWorkbenchContext getDesktopWorkbenchContext() {
		return workbenchContext;
	}
	
	public static DesktopWorkbenchContext getDesktopWorkbenchContext(Component comp) {
		return getInstance(comp).getDesktopWorkbenchContext();
	}
	
	public DesktopCellStyleContext getDesktopCellStyleContext() {
		return cellStyleContext;
	}

	public static DesktopCellStyleContext getDesktopCellStyleContext(Component comp) {
		return getInstance(comp).getDesktopCellStyleContext();
	}
	public Appmenubar getAppmenubar() {
		return _appmenubar;
	}
	
	public class Appmenubar {
		Menubar _menubar;
		
		FileMenu fileMenu;
		ViewMenu viewMenu;
		
		public Appmenubar(Menubar menubar) {
			_menubar = menubar;
			Components.wireVariables(menubar, this);
		}

		public FileMenu getFileMenu() {
			return fileMenu;
		}

		public ViewMenu getViewMenu() {
			return viewMenu;
		}
	}
}