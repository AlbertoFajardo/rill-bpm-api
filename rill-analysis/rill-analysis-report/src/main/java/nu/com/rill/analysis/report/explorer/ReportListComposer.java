package nu.com.rill.analysis.report.explorer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.schedule.DynamicScheduleService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
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
	private Dialog reportScheduleDialog;
	
	private Grid configGrid;
	private static volatile DynamicScheduleService dynamicScheduleService = null;
	
	static {
		
		dynamicScheduleService = (DynamicScheduleService) SpringUtil.getBean("dynamicScheduleService");
		
		// Read jobs and register
		for (Entry<String, SpreadSheetMetaInfo> entry : SpreadSheetMetaInfo.getMetaInfos().entrySet()) {
			if (StringUtils.hasText(entry.getValue().getCronExpression())) {
				LOG.info("Submit job and add to quartz schedule." + entry.getKey() + " " + entry.getValue().getCronExpression());
				dynamicScheduleService.submitJob(entry.getValue().getCronExpression(), new ReportJob(entry.getValue().getFileName(), entry.getValue().getReportParams()));
			}
		}
		
	}
	
	public void onUser$reportGrid() {
		
		reportGrid.setModel(new ListModelArray(new ArrayList<SpreadSheetMetaInfo>(SpreadSheetMetaInfo.getMetaInfos().values())));
		reportGrid.invalidate();
	}
	
	private static class ReportJob implements Runnable {
		
		private String reportFileName;
		
		static final ConcurrentMap<String, ConcurrentLinkedQueue<String>> REPORT_SCHEDULE_INFO = new ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>();
		
		public ReportJob(String reportFileName, Map<String, String> reportParams) {
			super();
			this.reportFileName = reportFileName;
			Assert.hasText(reportFileName);
			
			this.reportParams = reportParams;
		}
		
		private Map<String, String> reportParams = new HashMap<String, String>();

		@Override
		public String toString() {
			
			return reportFileName;
		}

		public void run() {
			
			ReportEngine re = ReportEngine.INSTANCE;
			
			SpreadSheetMetaInfo ssmi = SpreadSheetMetaInfo.getMetaInfos().get(reportFileName);
			
			try {
				// Set schedule flag
				reportParams.put(ReportEngine.REPORT_SCHEDULE_MODE, "true");
				Workbook wb = re.generateReport(new FileInputStream(new File(ssmi.getHashFileSrc())), reportFileName, reportParams);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				wb.write(baos);
				
				File temFile = File.createTempFile(reportFileName + "_" + new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date()), ".xlsx");
				FileUtils.writeByteArrayToFile(temFile, baos.toByteArray());
				LOG.info("Generate report file: " + temFile);
				
				ConcurrentLinkedQueue<String> queue = null;
				if (!REPORT_SCHEDULE_INFO.containsKey(reportFileName)) {
					queue = new ConcurrentLinkedQueue<String>();
					REPORT_SCHEDULE_INFO.putIfAbsent(reportFileName, queue);
					Assert.notNull(queue);
				}
				REPORT_SCHEDULE_INFO.get(reportFileName).add(temFile.getAbsolutePath());
				
			} catch (Exception e) {
				LOG.error("Exception occurred when try to generate report. " + reportFileName, e);
			}
			
		}
		
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		reportParamDialog.fireOnClose(null);
		reportScheduleDialog.fireOnClose(null);
		
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
						if (ssmi.getCronExpression().equals(cronExpressionText)) {
							LOG.info("Do nothing because not change cron expression.");
							return;
						}
						ssmi.setCronExpression(cronExpressionText);
						// Save meta info
						SpreadSheetMetaInfo.add(ssmi);
						// Process schedule
						if (StringUtils.hasText(cronExpressionText)) {
							dynamicScheduleService.deleteJob(new ReportJob(ssmi.getFileName(), ssmi.getReportParams()));
							dynamicScheduleService.submitJob(ssmi.getCronExpression(), new ReportJob(ssmi.getFileName(), ssmi.getReportParams()));
						} else {
							dynamicScheduleService.deleteJob(new ReportJob(ssmi.getFileName(), ssmi.getReportParams()));
						}
					}
				});
				
				row.appendChild(cronExpression);
				
				Button scheduleResult = new Button("查看");
				scheduleResult.setWidgetAttribute("fileName", ssmi.getFileName());
				scheduleResult.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						
						String fileName = event.getTarget().getWidgetAttribute("fileName");
						openReportScheduleDialog(fileName);
					}
				});
				row.appendChild(scheduleResult);
				
				row.appendChild(new Label(ssmi.getFormatedImportDateString()));
				Div div = new Div();
				Button open = new Button("打开");
				open.setWidgetAttribute("fileName", ssmi.getFileName());
				open.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						if (!openReportParamDialog(event.getTarget().getWidgetAttribute("fileName"))) {
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
	
	private boolean openReportParamDialog(String fileName) {
		
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
	
	private boolean openReportScheduleDialog(String fileName) {
		
		SpreadSheetMetaInfo ssmi = SpreadSheetMetaInfo.getMetaInfos().get(fileName);
		String params = ssmi.getCronExpression();
		if (!StringUtils.hasText(params) && !ReportJob.REPORT_SCHEDULE_INFO.containsKey(fileName)) {
			return false;
		}
		
		Grid reportScheduleGrid = (Grid) reportScheduleDialog.getFellow("reportScheduleGrid");
		reportScheduleGrid.setModel(new ListModelArray(new ArrayList<String>(ReportJob.REPORT_SCHEDULE_INFO.get(fileName))));
		reportScheduleGrid.setRowRenderer(new RowRenderer() {
			int i = 0;
			@Override
			public void render(Row row, Object data) throws Exception {
				String schedule = (String) data;
				row.appendChild(new Label(new Integer(++i).toString()));
				row.appendChild(new Label(schedule));
			}
		});
		
		reportScheduleDialog.fireOnOpen(null);
		return true;
	}

}
