package nu.com.rill.analysis.report.explorer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.schedule.DynamicScheduleService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.zhtml.Input;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zss.app.file.SpreadSheetMetaInfo;
import org.zkoss.zss.app.zul.Dialog;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

public class ReportListComposer extends GenericForwardComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static Log LOG = LogFactory.getLog(ReportListComposer.class);
	
	private Grid reportGrid;
	private Dialog reportParamDialog;
	private Grid configGrid;
	private static volatile DynamicScheduleService dynamicScheduleService = null;
	
	public void onUser$reportGrid() {
		
		reportGrid.setModel(new ListModelArray(new ArrayList<SpreadSheetMetaInfo>(SpreadSheetMetaInfo.getMetaInfos().values())));
		reportGrid.invalidate();
	}
	
	private static class ReportJob implements Runnable {
		
		private String reportFileName;
		
		public ReportJob(String reportFileName, Map<String, String> reportParams) {
			super();
			this.reportFileName = reportFileName;
			this.reportParams = reportParams;
		}
		
		private Map<String, String> reportParams = new HashMap<String, String>();

		public void run() {
			
			ReportEngine re = ReportEngine.INSTANCE;
			
			SpreadSheetMetaInfo ssmi = SpreadSheetMetaInfo.getMetaInfos().get(reportFileName);
			
			try {
				// Set schedule flag
				reportParams.put(ReportEngine.REPORT_SCHEDULE_MODE, "true");
				Workbook wb = re.generateReport(new FileInputStream(new File(ssmi.getHashFileSrc())), reportFileName, reportParams);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				wb.write(baos);
				
				File temFile = File.createTempFile(reportFileName + System.currentTimeMillis(), ".xlsx");
				FileUtils.writeByteArrayToFile(temFile, baos.toByteArray());
				LOG.info("Generate report file: " + temFile);
			} catch (Exception e) {
				LOG.error("Exception occurred when try to generate report. " + reportFileName, e);
			}
			
		}
		
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		if (dynamicScheduleService == null) {
			dynamicScheduleService = (DynamicScheduleService) SpringUtil.getBean("dynamicScheduleService");
		}
		
		reportParamDialog.fireOnClose(null);
		
		reportGrid.setModel(new ListModelArray(new ArrayList<SpreadSheetMetaInfo>(SpreadSheetMetaInfo.getMetaInfos().values())));
		reportGrid.setRowRenderer(new RowRenderer() {
			
			int index = 0;
			@Override
			public void render(Row row, Object data) throws Exception {
				final SpreadSheetMetaInfo ssmi = (SpreadSheetMetaInfo) data;
				row.appendChild(new Label(new Integer(++index).toString()));
				row.appendChild(new Label(ssmi.getFileName()));
				// Use cron expression in meta info.
				Textbox cronExpression = new Textbox(ObjectUtils.getDisplayString(ssmi.getCronExpression()));
				cronExpression.addEventListener(Events.ON_BLUR, new EventListener() {
					
					@Override
					public void onEvent(Event arg0) throws Exception {
						String cronExpressionText = ((Textbox) arg0.getTarget()).getValue();
						ssmi.setCronExpression(cronExpressionText);
						// Save meta info
						SpreadSheetMetaInfo.add(ssmi);
						// Process schedule
						dynamicScheduleService.submitReportJob(ssmi.getCronExpression(), new ReportJob(ssmi.getFileName(), ssmi.getReportParams()));
					}
				});
				
				row.appendChild(cronExpression);
				row.appendChild(new Label(ssmi.getFormatedImportDateString()));
				Div div = new Div();
				Button open = new Button("打开");
				open.setWidgetAttribute("fileName", ssmi.getFileName());
				open.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						if (!openReportDialog(event.getTarget().getWidgetAttribute("fileName"))) {
							Executions.getCurrent().sendRedirect("view.zul?fileName=" + event.getTarget().getWidgetAttribute("fileName"));
						}
					}
				});
				div.appendChild(open);
				
				Button edit = new Button("编辑");
				edit.setWidgetAttribute("fileName", ssmi.getFileName());
				edit.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						Executions.getCurrent().sendRedirect("view.zul?edit=1&fileName=" + event.getTarget().getWidgetAttribute("fileName"));
					}
				});
				div.appendChild(edit);
				
				row.appendChild(div);
			}
		});
	}
	
	private boolean openReportDialog(String fileName) {
		
		SpreadSheetMetaInfo ssmi = SpreadSheetMetaInfo.getMetaInfos().get(fileName);
		Map<String, String> params = ssmi.getReportParams();
		if (CollectionUtils.isEmpty(params)) {
			return false;
		}
		
		configGrid = (Grid) reportParamDialog.getFellow("configGrid");
		configGrid.setModel(new ListModelArray(new ArrayList<Entry<String, String>>(params.entrySet())));
		configGrid.setRowRenderer(new RowRenderer() {
			
			@Override
			public void render(Row row, Object data) throws Exception {
				@SuppressWarnings("unchecked")
				Entry<String, String> param = (Entry<String, String>) data;
				row.appendChild(new Label(param.getKey()));
				Textbox paramTextBox = new Textbox();
				paramTextBox.setName(param.getKey());
				paramTextBox.setValue(param.getValue());
				row.appendChild(paramTextBox);
			}
		});
		Input fileNameInput = (Input) reportParamDialog.getFellow("reportParamForm").getFellow("fileName");
		fileNameInput.setValue(fileName);
		
		reportParamDialog.fireOnOpen(null);
		return true;
	}

}
