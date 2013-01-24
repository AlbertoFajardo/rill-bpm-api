/* ImportFileWindowCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 2, 2010 12:53:23 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.file;

import nu.com.rill.analysis.report.ReportManager;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zss.app.zul.Dialog;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Window;

/**
 * @author Sam
 *
 */
public class ImportFileWindowCtrl extends GenericForwardComposer  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Dialog _importFileDialog;
	private Label supportedFormat;
	private Radiogroup importOption;
	private Radio createNew;
	
	//TODO: not implement yet
	private Radio insertSheetsToEnd;
	//TODO: not implement yet
	private Radio replaceCurrent;
	
	//TODO: provide search bar for filter file name
	/*All spreadsheet file name list*/
	private Listbox allFilesListbox;
	
	private Button uploadBtn;
	
	private Menupopup fileMenu;
	private Menuitem openFileMenuitem;
	
	private ReportManager reportMgr;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		initSupportFormat();
//		initImportOption();
//		initFileListbox();
		
		reportMgr = (ReportManager) SpringUtil.getBean("reportMgr");
	}
	
	public void onOpen$_importFileDialog() {
		try {
			_importFileDialog.setMode(Window.MODAL);
		} catch (InterruptedException e) {
		}
//		Map<String, SpreadSheetMetaInfo> metaInfos = SpreadSheetMetaInfo.getMetaInfos();
//		if (metaInfos == null || metaInfos.isEmpty())
//			return;
//		allFilesListbox.setModel(new ListModelList(metaInfos.values()));
	}

	private void initSupportFormat() {	
		String val = "";
		String[] supporedFormats = FileHelper.getSupportedFormat();
		for (int i = 0; i < supporedFormats.length; i++) {
			val += i == 0 ? supporedFormats[i] : ", " + supporedFormats[i];
		}
		//TODO: use I18n
		supportedFormat.setValue("Supported formats: " + val);
	}

//	/**
//	 * Initialize all spreadsheet file name as a list 
//	 */
//	private void initFileListbox() {
//
//		//TODO: move this to become a component, re-use in here and fileListOpen.zul
//		Listhead listhead = new Listhead();
//		Listheader filenameHeader = new Listheader("File");
//		filenameHeader.setHflex("2");
//		filenameHeader.setParent(listhead);
//		
//		Listheader dateHeader = new Listheader("Date");
//		dateHeader.setHflex("1");
//		dateHeader.setParent(listhead);
//		allFilesListbox.appendChild(listhead);
//		
//		allFilesListbox.setItemRenderer(new ListitemRenderer() {
//			
//			@Override
//			public void render(Listitem item, Object obj) throws Exception {
//				final SpreadSheetMetaInfo info = (SpreadSheetMetaInfo)obj;
//				item.setValue(info);
//				item.appendChild(new Listcell(info.getFileName()));
//				item.appendChild(new Listcell(info.getFormatedImportDateString()));
//				//TODO: use I18n
//				item.setContext(fileMenu);
//				item.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener() {
//					
//					@Override
//					public void onEvent(Event evt) throws Exception {
//						DesktopWorkbenchContext workbenchCtrl = getDesktopWorkbenchContext();
//						workbenchCtrl.getWorkbookCtrl().openBook(info);
//						workbenchCtrl.fireWorkbookChanged();
//						_importFileDialog.fireOnClose(null);
//					}
//				});
//			}
//		});
//	}

//	/**
//	 * Initialize import option, sets create new file as default
//	 */
//	private void initImportOption() {
//		importOption.setSelectedItem(createNew);
//		//TODO: implement other option
//	}
	
	public void onClick$openFileMenuitem() {
//		getDesktopWorkbenchContext().getWorkbookCtrl().openBook((SpreadSheetMetaInfo)allFilesListbox.getSelectedItem().getValue());
//		getDesktopWorkbenchContext().fireWorkbookChanged();
		_importFileDialog.fireOnClose(null);
	}
	
	public void onFileUpload(ForwardEvent event) {
		
		Media media = ((UploadEvent) event.getOrigin()).getMedia();
		if (reportMgr.getReport(media.getName()) == null) {
			reportMgr.createReport(media.getName(), null, media.getByteData());
		} else {
			reportMgr.updateReport(media.getName(), media.getByteData());
		}
//		FileHelper.store();
		
		_importFileDialog.fireOnClose(null);
		Executions.getCurrent().postEvent(0, new Event("onUser", event.getTarget().getRoot().getFellow("reportGrid")));
	}
	
//	private DesktopWorkbenchContext getDesktopWorkbenchContext() {
//		return Zssapp.getDesktopWorkbenchContext(self);
//	}
}