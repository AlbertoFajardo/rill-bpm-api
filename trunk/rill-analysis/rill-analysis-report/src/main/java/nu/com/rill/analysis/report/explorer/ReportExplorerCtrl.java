package nu.com.rill.analysis.report.explorer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import nu.com.rill.analysis.report.ReportManager;
import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.zkoss.poi.ss.usermodel.Workbook;
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
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

public class ReportExplorerCtrl extends GenericForwardComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static Log LOG = LogFactory.getLog(ReportExplorerCtrl.class);
	
	private Grid reportGrid;
	private Dialog reportScheduleDialog;
	
	public void onUser$reportGrid() {
		
		ReportManager reportMgr = (ReportManager) SpringUtil.getBean("reportMgr");
		reportGrid.setModel(new ListModelArray(reportMgr.listReport()));
		reportGrid.invalidate();
	}
	
	private static class ReportJob implements Runnable {
		
		private String reportFileName;
		
		static final ConcurrentMap<String, ConcurrentLinkedQueue<String>> REPORT_SCHEDULE_INFO = new ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>();
		
		public ReportJob(String reportFileName) {
			super();
			this.reportFileName = reportFileName;
			Assert.hasText(reportFileName);
			
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
		
		reportScheduleDialog.fireOnClose(null);
		
		final ReportManager reportMgr = (ReportManager) SpringUtil.getBean("reportMgr");
		
		reportGrid.setModel(new ListModelArray(reportMgr.listReport()));
		reportGrid.setRowRenderer(new RowRenderer() {
			
			@Override
			public void render(Row row, Object data) throws Exception {
				final Report report = (Report) data;
				row.appendChild(new Label(report.getId().toString()));
				row.appendChild(new Label(report.getName()));
				// Use cron expression in meta info.
				Textbox cronExpression = new Textbox(ObjectUtils.getDisplayString(report.getCronExpression()));
				cronExpression.addEventListener(Events.ON_BLUR, new EventListener() {
					
					@Override
					public void onEvent(Event arg0) throws Exception {
						String cronExpressionText = ((Textbox) arg0.getTarget()).getValue();
						if (ObjectUtils.getDisplayString(report.getCronExpression()).equals(cronExpressionText)) {
							LOG.info("Do nothing because not change cron expression.");
							return;
						}
						
						reportMgr.unscheduleReport(report.getName());
						if (StringUtils.hasText(cronExpressionText)) {
							reportMgr.scheduleReport(report.getName(), cronExpressionText, new ReportJob(report.getName()));
						}
						
					}
				});
				
				row.appendChild(cronExpression);
				
				Button scheduleResult = new Button("查看");
				scheduleResult.setWidgetAttribute("fileName", report.getName());
				scheduleResult.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						
						String fileName = event.getTarget().getWidgetAttribute("fileName");
						openReportScheduleDialog(fileName);
					}
				});
				row.appendChild(scheduleResult);
				
				row.appendChild(new Label(report.getAddDateFormatString()));
				Div div = new Div();
				Button open2 = new Button("打开");
				open2.setWidgetAttribute("fileName", report.getName());
				open2.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						Executions.getCurrent().sendRedirect("view2.zul?fileName=" + event.getTarget().getWidgetAttribute("fileName"));
					}
				});
				div.appendChild(open2);
				
				Button download = new Button("下载");
				download.setWidgetAttribute("fileName", report.getName());
				download.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						Report report = reportMgr.getReport(event.getTarget().getWidgetAttribute("fileName"));
						try {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							IOUtils.write(report.getReportContent(), baos);
							Filedownload.save(baos.toByteArray(), 
									"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
									report.getName());
						} catch (Exception e) {
							// Ignore~~
						}
						
					}
				});
				div.appendChild(download);
				
				row.appendChild(div);
			}
		});
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
