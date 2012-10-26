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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import nu.com.rill.analysis.report.ReportManager;
import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zss.app.Consts;
import org.zkoss.zss.app.file.FileHelper;
import org.zkoss.zss.app.zul.ctrl.DesktopCellStyleContext;
import org.zkoss.zss.app.zul.ctrl.DesktopWorkbenchContext;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.ExcelImporter;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.impl.Utils;
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
	
	private static final Log LOGGER = LogFactory.getLog(Zssapp.class);
	
	private final static String KEY_ZSSAPP = "org.zkoss.zss.app.zul.zssApp";
	/*Default spreadsheet*/
	private Spreadsheet spreadsheet;
	
	//private Window mainWin;
	private Div mainWin;
	
	private String parentIframeId;
	
	public String getParentIframeId() {
		return parentIframeId;
	}

	public void setParentIframeId(String parentIframeId) {
		this.parentIframeId = parentIframeId;
		this.smartUpdate("parentIframeId", parentIframeId);
	}

	Appmenubar _appmenubar;
	
	/* Context */
	transient DesktopWorkbenchContext workbenchContext = new DesktopWorkbenchContext();
	transient DesktopCellStyleContext cellStyleContext = new DesktopCellStyleContext();
	
	final ReportManager reportMgr = (ReportManager) SpringUtil.getBean("reportMgr");
	
	private boolean editMode = false;
	
	private static byte[] initBookBytes = null;
	static {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ClassPathResource initCpr = new ClassPathResource("Init.xlsx");
		try {
			IOUtils.copy(initCpr.getInputStream(), baos);
			initBookBytes = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				initCpr.getInputStream().close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		Assert.notNull(initBookBytes);
	}
	
	
	
	@Override
	protected void renderProperties(ContentRenderer renderer)
			throws IOException {
		super.renderProperties(renderer);
		
		renderer.render("parentIframeId", parentIframeId);
	}

	public Zssapp() {
		
		Executions.createComponents(Consts._Zssapp_zul, this, null);
		Components.wireVariables(this, this, '$', true, true);
		Components.addForwards(this, this, '$');
		spreadsheet = (Spreadsheet)mainWin.getFellow("spreadsheet");
		final String parentIframeId = Executions.getCurrent().getParameter("parentIframeId");
		this.setParentIframeId(parentIframeId);
		
		// FIXME: MENGRAN. View/edit mode
		if (Executions.getCurrent().getParameter("edit") != null) {
			editMode = true;
			// set src from request parameters
			String fileName = Executions.getCurrent().getParameter("fileName");
			Report report = reportMgr.getReport(fileName);
			this.setReport(report);
			
			return;
		} else {
			this.addEventListener(Events.ON_USER, new EventListener() {
				
				@Override
				public void onEvent(Event event) throws Exception {
					Zssapp.this.setReport((Report) event.getData());
				}
			});
		}
		
//		spreadsheet.setHiderowhead(true);
//		spreadsheet.setHidecolumnhead(true);
		spreadsheet.setShowContextMenu(false);
		spreadsheet.setShowFormulabar(false);
		spreadsheet.setShowToolbar(false);
		spreadsheet.setShowSheetbar(false);
		spreadsheet.disableClientUpdate(true);
		
		Book initBook = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(initBookBytes);
		try {
			initBook = new ExcelImporter().imports(bais, "Init.xlsx");
		} catch (Exception e) {
			// Ignore
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
				// Ignore
			}
		}
		if (initBook != null) {
			this.setBook(initBook);
			// Calculate height
			calculateHeight(null, spreadsheet.getSelectedSheet());
			spreadsheet.setMaxrows(spreadsheet.getSelectedSheet().getLastRowNum());
			spreadsheet.setMaxcolumns(spreadsheet.getSelectedSheet().getRow(spreadsheet.getSelectedSheet().getLastRowNum()).getLastCellNum());
		}
	}
	
	public void setReport(Report report) {
		
		String cookie = Executions.getCurrent().getHeader("Cookie");
		cookie = StringUtils.replace(cookie, "JSESSIONID=", "IGNORE_JSESSIONID=");
		try {
			ReportEngine.registCookie(cookie);
			FileHelper.openSpreadsheet(spreadsheet, report);
			
			if (!editMode) {
				spreadsheet.setMaxrows(spreadsheet.getSelectedSheet().getLastRowNum());
				spreadsheet.setMaxcolumns(spreadsheet.getSelectedSheet().getRow(spreadsheet.getSelectedSheet().getLastRowNum()).getLastCellNum() + 1);
			}
			
		} finally {
			ReportEngine.registCookie(null);
			
			// Calculate height
			calculateHeight(report, spreadsheet.getSelectedSheet());
		}
		
	}
	
	private void calculateHeight(Report report, Worksheet sheet) {
		
		int height = (report == null || report.getParams() == null) ? 15 : 75;
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			height +=Utils.getRowHeightInPx(sheet, sheet.getRow(i));
			LOGGER.debug("height : " + height + " after calculate row num" + i);
		}
		this.setHeight(height + "px");
	}
	
	/**
	 * Sets {@link #Book}
	 * @param book
	 */
	public void setBook(Book book) {
		spreadsheet.setBook(book);
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