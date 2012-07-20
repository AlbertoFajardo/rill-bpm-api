/* CellStyleCtrlPanel.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 15, 2010 9:19:40 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.zul.ctrl;

import org.zkoss.poi.ss.usermodel.BorderStyle;
import org.zkoss.web.fn.ServletFns;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zss.app.Consts;
import org.zkoss.zss.app.Dropdownbutton;
import org.zkoss.zss.app.zul.Colorbutton;
import org.zkoss.zss.app.zul.DisposedEventListener;
import org.zkoss.zss.app.zul.Zssapp;
import org.zkoss.zss.model.Range;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Toolbarbutton;

/**
 * @author Ian Tsai / Sam
 * 
 */
public class CellStyleCtrlPanel extends Div implements IdSpace {

	private Hlayout container;
	/**
	 * Font
	 */
	private Combobox fontFamily;
	private Combobox fontSize;
	
	private boolean _isBold;
	private Toolbarbutton boldBtn;
	
	private boolean _isItalic;
	private Toolbarbutton italicBtn;
	
	private boolean _isUnderline;
	private Toolbarbutton underlineBtn;
	
	private boolean _isStrikethrough;
	private Toolbarbutton strikethroughBtn;
	
	private Dropdownbutton borderBtn;
	
	private Colorbutton fontColorBtn;
	private Colorbutton cellColorBtn;
	
	private Dropdownbutton valignBtn;
	private Dropdownbutton halignBtn;

	private boolean isWrapText;
	private Toolbarbutton wrapTextBtn;
	
	private final static String ALIGN_TOP_SRC_IMAGE = "~./zssapp/image/edit-vertical-alignment-top.png";
	private final static String ALIGN_MIDDLE_SRC_IMAGE = "~./zssapp/image/edit-vertical-alignment-middle.png";
	private final static String ALIGN_BOTTOM_SRC_IMAGE = "~./zssapp/image/edit-vertical-alignment.png";
	private final static String ALIGN_LEFT_SRC_IMAGE = "~./zssapp/image/edit-alignment.png";
	private final static String ALIGN_CENTER_SRC_IMAGE = "~./zssapp/image/edit-alignment-center.png";
	private final static String ALIGN_RIGHT_SRC_IMAGE = "~./zssapp/image/edit-alignment-right.png";
	
	public CellStyleCtrlPanel() {		
		Executions.createComponents(Consts._CellStylePanel_zul, this, null);
		Components.wireVariables(this, this);
		Components.addForwards(this, this);
	}
	
	public void setSpacing(String spacing) {
		container.setSpacing(spacing);
	}
	
	public void setFontfamilywidth(String width) {
		fontFamily.setWidth(width);
	}
	
	public void setFontsizewidth(String width) {
		fontSize.setWidth(width);
	}
	
	public void setFontfamilyvisible(boolean visible) {
		fontFamily.setVisible(visible);
	}
	
	public void setFontsizevisible(boolean visible) {
		fontSize.setVisible(visible);
	}
	
	public void setBoldvisible(boolean visible) {
		boldBtn.setVisible(visible);
	}
	
	public void setItalicvisible(boolean visible) {
		italicBtn.setVisible(visible);
	}
	
	public void setUnderlinevisible(boolean visible) {
		underlineBtn.setVisible(visible);
	}
	
	public void setStrikethroughvisible(boolean visible) {
		strikethroughBtn.setVisible(visible);
	}
	
	public void setBordervisible(boolean visible) {
		borderBtn.setVisible(visible);
	}
	
	public void setFontcolorvisible(boolean visible) {
		fontColorBtn.setVisible(visible);
	}
	
	public void setCellcolorvisible(boolean visible) {
		cellColorBtn.setVisible(visible);
	}
	
	
	public void setAlignmentvisible(boolean visible) {
		valignBtn.setVisible(visible);
		halignBtn.setVisible(visible);
	}
	
	public void setWraptextvisible(boolean visible) {
		wrapTextBtn.setVisible(visible);
	}
	
	/**
	 * Set control panel attributes when FontTargetChangeEvent fired
	 * @param cellStyle
	 */
	protected void initPanel(CellStyle cellStyle) {
		fontFamily.setValue(cellStyle.getFontFamily());
		fontSize.setValue("" + cellStyle.getFontSize());
		
		_isBold = cellStyle.isBold();
		boldBtn.setSclass(_isBold ? "clicked" : "");
		
		_isItalic = cellStyle.isItalic();
		italicBtn.setSclass(_isItalic ? "clicked" : "");
		
		_isUnderline = cellStyle.getUnderline() == CellStyle.UNDERLINE_SINGLE;
		underlineBtn.setSclass(_isUnderline ? "clicked" : "");
		
		_isStrikethrough = cellStyle.isStrikethrough();
		strikethroughBtn.setSclass(_isStrikethrough ? "clicked" : "");
		fontColorBtn.setColor(cellStyle.getFontColor());
		cellColorBtn.setColor(cellStyle.getCellColor());
		
		String halignImageSrc = getAlignImageSrc(cellStyle.getAlignment());
		if (halignImageSrc != null) {
			halignBtn.setImage(halignImageSrc);
			halignBtn.setSclass("dpbtn-seld");
		}
		String valignImageSrc = getVerticalAlignImageSrc(cellStyle.getVerticalAlignment());
		if (valignImageSrc != null) {
			valignBtn.setImage(valignImageSrc);
			valignBtn.setSclass("dpbtn-seld");	
		}
		
		isWrapText = cellStyle.isWrapText();
		wrapTextBtn.setSclass(isWrapText ? "clicked" : "");
	}

	private static String getAlignImageSrc(int align) {
		switch (align) {
		case CellStyle.ALIGN_LEFT:
			return ALIGN_LEFT_SRC_IMAGE;
		case CellStyle.ALIGN_CENTER:
			return ALIGN_CENTER_SRC_IMAGE;
		case CellStyle.ALIGN_RIGHT:
			return ALIGN_RIGHT_SRC_IMAGE;
		}
		return null;
	}
	
	private static String getVerticalAlignImageSrc(int align) {
		switch (align) {
		case CellStyle.ALIGN_TOP:
			return ALIGN_TOP_SRC_IMAGE;
		case CellStyle.ALIGN_MIDDLE:
			return ALIGN_MIDDLE_SRC_IMAGE;
		case CellStyle.ALIGN_BOTTOM:
			return ALIGN_BOTTOM_SRC_IMAGE;
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param event
	 */
	public void onSelect$fontFamily() {
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setFontFamily(fontFamily.getValue());
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	//TODO: move this to spreadsheet onblur event (not implement yet)
	public void onOpen$fontFamily() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	//TODO: move this to spreadsheet onblur event (not implement yet)
	public void onOpen$fontSize() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	
	public void onClick$boldBtn() {
		//TODO: move this to spreadsheet onblur event (not implement yet)
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		
		_isBold = !_isBold;
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setBold(_isBold);
				boldBtn.setSclass(_isBold ? "clicked" : "");
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	public void onClick$italicBtn() {
		//TODO: move this to spreadsheet onblur event (not implement yet)
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		
		_isItalic = !_isItalic;
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setItalic(_isItalic);
				italicBtn.setSclass(_isItalic ? "clicked" : "");
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	public void onClick$underlineBtn() {
		//TODO: move this to spreadsheet onblur event (not implement yet)
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		
		_isUnderline = !_isUnderline;
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setUnderline(_isUnderline ? CellStyle.UNDERLINE_SINGLE : CellStyle.UNDERLINE_NONE);
				underlineBtn.setSclass(_isUnderline ? "clicked" : "");
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	public void onClick$strikethroughBtn() {
		//TODO: move this to spreadsheet onblur event (not implement yet)
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		
		_isStrikethrough = !_isStrikethrough;
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setStrikethrough(_isStrikethrough);
				strikethroughBtn.setSclass(_isStrikethrough ? "clicked" : "");
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	public void onSelect$fontSize() {
		//getCellStyleContext().getFontStyle().setFontSize(Integer.parseInt(fontSize.getText()));
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setFontSize(Integer.parseInt(fontSize.getText()));
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	//TODO: move this to spreadsheet onblur event (not implement yet)
	public void onClick$fontColorBtn() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	public void onChange$fontColorBtn() {
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setFontColor(fontColorBtn.getColor());
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	//TODO: move this to spreadsheet onblur event (not implement yet)
	public void onClick$cellColorBtn() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	public void onChange$cellColorBtn() {
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setCellColor(cellColorBtn.getColor());
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}

	public void onDropdown$borderBtn() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	
	public void onBorderSelector(ForwardEvent evt) {
		
		final String color = "#000000";
		final String param = (String)evt.getData();
	
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				BorderStyle borderStyle = BorderStyle.MEDIUM;
				if ("no".equals(param))
					borderStyle = BorderStyle.NONE;
				style.setBorder(getBorderType(param), borderStyle, color);
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	private static int getBorderType(String borderType) {
		if (borderType == null) {
			return Range.BORDER_EDGE_BOTTOM;
		}

		if ("bottom".equals(borderType))
			return Range.BORDER_EDGE_BOTTOM;
		else if ("right".equals(borderType))
			return Range.BORDER_EDGE_RIGHT;
		else if ("top".equals(borderType))
			return Range.BORDER_EDGE_TOP;
		else if ("left".equals(borderType))
			return Range.BORDER_EDGE_LEFT;
		else if ("outside".equals(borderType))
			return Range.BORDER_OUTLINE;
		else if ("inside".equals(borderType))
			return Range.BORDER_INSIDE;
		else if ("insideHorizontal".equals(borderType))
			return Range.BORDER_INSIDE_HORIZONTAL;
		else if ("insideVertical".equals(borderType))
			return Range.BORDER_INSIDE_VERTICAL;
		else if ("no".equals(borderType))
			return Range.BORDER_FULL;
		else if ("full".equals(borderType))
			return Range.BORDER_FULL;
		else if ("diagonalDown".equals(borderType))
			return Range.BORDER_DIAGONAL_DOWN;
		else if ("diagonalUp".equals(borderType))
			return Range.BORDER_DIAGONAL_UP;
		else if ("diagonal".equals(borderType))
			return Range.BORDER_DIAGONAL;

		return Range.BORDER_EDGE_BOTTOM;
	}
	
	public void onDropdown$valignBtn() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	
	public void onDropdown$halignBtn() {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	
	public void onAlignSelector(ForwardEvent evt) {
		//TODO: move this to spreadsheet onblur event (not implement yet)
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		
		final String align = (String)evt.getData();
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				int alignment = CellStyle.ALIGN_LEFT;
				
				if ("left".equals(align)) {
					alignment = CellStyle.ALIGN_LEFT;
				} else if ("center".equals(align)) {
					alignment = CellStyle.ALIGN_CENTER;
				} else if ("right".equals(align)) {
					alignment = CellStyle.ALIGN_RIGHT;
				}
				halignBtn.setImage(getAlignImageSrc(alignment));
				style.setAlignment(alignment);
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	public void onVerticalAlignSelector(ForwardEvent evt) {
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		final String align = (String)evt.getData();
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				int alignment = CellStyle.ALIGN_TOP;
				if ("top".equals(align)) {
					alignment = CellStyle.ALIGN_TOP;
				} else if ("middle".equals(align)) {
					alignment = CellStyle.ALIGN_MIDDLE;
				} else if ("bottom".equals(align)) {
					alignment = CellStyle.ALIGN_BOTTOM;
				}	
				valignBtn.setImage(getVerticalAlignImageSrc(alignment));
				style.setVerticalAlignment(alignment);
			}
		});
		Events.postEvent(Events.ON_CLICK, this, null);
	}
	
	public void onClick$wrapTextBtn() {
		//TODO: move this to spreadsheet onblur event (not implement yet)
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
		
		isWrapText = !isWrapText;
		getCellStyleContext().modifyStyle(new StyleModification(){
			public void modify(CellStyle style, CellStyleContextEvent candidteEvt) {
				candidteEvt.setExecutor(CellStyleCtrlPanel.this);
				style.setWrapText(isWrapText);
				wrapTextBtn.setSclass(isWrapText ? "clicked" : "");
			}
		});
	}
	
	protected CellStyleContext getCellStyleContext(){
		return Zssapp.getDesktopCellStyleContext(this);
	}
	
	protected DesktopWorkbenchContext getDesktopWorkbenchContext() {
		return Zssapp.getDesktopWorkbenchContext(this);
	}

	public void onCreate() {
		//bug on IE6, cause div expand
		if (ServletFns.isBrowser("ie6")) {
			container.setHflex("min");
		}
		
		CellStyleContext context = getCellStyleContext();
		DisposedEventListener listener = new DisposedEventListener() {
			public boolean isDisposed() {
				return CellStyleCtrlPanel.this.getDesktop() == null;
			}
			public void onEvent(Event arg0) throws Exception {
				CellStyleContextEvent event = (CellStyleContextEvent) arg0;
				if(event.getExecutor() != CellStyleCtrlPanel.this)
					initPanel(event.getCellStyle());
			}
		};
		context.addEventListener(Consts.ON_STYLING_TARGET_CHANGED, listener);
		context.addEventListener(Consts.ON_CELL_STYLE_CHANGED, listener);

		getDesktopWorkbenchContext().addEventListener(Consts.ON_WORKBOOK_CHANGED, new EventListener() {
			public void onEvent(Event event) throws Exception {
				//clear all UI attribute when sheet open or close
				fontFamily.setValue(null);
				fontSize.setValue(null);
				
				_isBold = false;
				boldBtn.setSclass("");
				
				_isItalic = false;
				italicBtn.setSclass("");
				
				_isUnderline = false;
				underlineBtn.setSclass("");
				
				_isStrikethrough = false;
				strikethroughBtn.setSclass("");
				fontColorBtn.setColor("#000000");
				cellColorBtn.setColor("#FFFFFF");

				valignBtn.setSclass("");
				valignBtn.setImage(ALIGN_TOP_SRC_IMAGE);
				halignBtn.setSclass("");
				halignBtn.setImage(ALIGN_LEFT_SRC_IMAGE);
				
				isWrapText = false;
				wrapTextBtn.setSclass("");
			}
		});
	}
}