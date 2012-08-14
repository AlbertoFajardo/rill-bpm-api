/* SpreadSheetMetaInfo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 3, 2010 2:54:56 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.rill.bpm.api.WorkflowOperations;

import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

/**
 * @author Sam
 *
 */
public class SpreadSheetMetaInfo {
	private String fileName;
	private long timeInMillis;
	private String hashFileName;
	private String importDate;
	
	private Map<String, Map<PARAM_CONFIG, String>> reportParams = new LinkedHashMap<String, Map<PARAM_CONFIG,String>>(2);
	private String cronExpression = "";

	public final String getCronExpression() {
		return cronExpression;
	}

	public final void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	

	private SpreadSheetMetaInfo(){}
	
	/**
	 * @param fileName
	 * @param timeInMillis
	 * @param hashFileName
	 */
	private SpreadSheetMetaInfo(String fileName, long timeInMillis,
			String hashFileName) {
		this.fileName = fileName;
		this.timeInMillis = timeInMillis;
		this.hashFileName = hashFileName;
		
		//TODO: move these object to a helper class
		Calendar calendar = Calendar.getInstance();
		DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
		calendar.setTimeInMillis(timeInMillis);
		importDate = formatter.format(calendar.getTime());
	}

	public final Map<String, Map<PARAM_CONFIG, String>> getReportParams() {
		return reportParams;
	}

	public final void setReportParams(
			Map<String, Map<PARAM_CONFIG, String>> reportParams) {
		this.reportParams = reportParams;
	}

	public String getSrc() {
		return FileHelper.getSpreadsheetStorageFolderPath() + fileName;
	}
	
	public String getHashFileSrc() {
		return FileHelper.getSpreadsheetStorageFolderPath() + hashFileName;
	}

	public String getFileName() {
		return fileName;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}

	/*package*/ String getHashFileName() {
		return hashFileName;
	}

	public String getFormatedImportDateString() {
		return importDate;
	}
	
	/**
	 * 
	 * @param src
	 * @return
	 */
	public static SpreadSheetMetaInfo newInstance(String src){
		SpreadSheetMetaInfo info = new SpreadSheetMetaInfo();
		info.fileName = removeFolderPath(src);
		String extName = FileHelper.getMediaExtention(src);

		info.timeInMillis = System.currentTimeMillis();	
		info.hashFileName = info.fileName.substring(0, info.fileName.indexOf("."))
				+ "-" + info.timeInMillis + "." + extName;
		return info;
	}
	
	private static String removeFolderPath(String src) {
		int idx = -1;
		String fileName = src;
		if ((idx = fileName.lastIndexOf("\\")) >= 0 || (idx = fileName.lastIndexOf("/")) >= 0) {
			return fileName.substring(idx + 1);
		}
		return fileName;
	}
	
	/**
	 * 
	 * @param info
	 */
	public static void add(SpreadSheetMetaInfo info) {
		FileHelper.createMetaInfoFileIfNeeded();
		long timeInMillis = info.getTimeInMillis();
		String fileName = info.getFileName();
		String hashFilename = info.getHashFileName();
		
		String reportParams = "";
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(info.getHashFileSrc()));
			Map<String, Map<PARAM_CONFIG, String>> reportParamNames = ReportEngine.INSTANCE.retrieveReportParams(fis, info.getFileName());
			for (Entry<String, Map<PARAM_CONFIG, String>> entry : reportParamNames.entrySet()) {
				reportParams = reportParams + "|" + entry.getKey() + ":" + WorkflowOperations.XStreamSerializeHelper.serializeXml("PARAM_CONFIG", entry.getValue());
			}
			if (reportParams.length() > 0) {
				reportParams = reportParams.substring(1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		//TODO: replace Properties with json format, to provide version control 
		Properties prop = new Properties();
		try {
			reader = new BufferedReader(new FileReader(FileHelper.getSpreadsheetStorageFolderPath() + "metaFile"));
			prop.load(reader);
			
			prop.put(fileName,  timeInMillis + "," + fileName + ","	+ hashFilename + "," + reportParams + "," + info.getCronExpression());
			writer = new BufferedWriter(new FileWriter(FileHelper.getSpreadsheetStorageFolderPath() + "metaFile", false));
			prop.store(writer, null);
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
				}
		}
	}
	
	public static boolean delete(SpreadSheetMetaInfo info) throws IOException {
		//TODO: replace Properties with json format, to provide version control 
		Properties prop = getMetaInfoProperties();
		
		if (prop.containsKey(info.getFileName())) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(FileHelper.getSpreadsheetStorageFolderPath() + "metaFile", false));
			prop.remove(info.getFileName());
			prop.store(writer, null);
			writer.close();
		} else
			throw new IOException("Delete " + info.getFileName() + " fail.");
		return false;
	}
	
	/**
	 * Returns {@link #SpreadSheetMetaInfo} of all spreadsheet file
	 * @return
	 */
	public static Map<String, SpreadSheetMetaInfo> getMetaInfos() {
		if (!checkMetaInfoFileModified())
			return cachedMetaInfos;
		
		Map<String, SpreadSheetMetaInfo> infos;
		try {
			infos = readMetaInfos();
			cachedMetaInfos.clear();
			cachedMetaInfos.putAll(infos);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return cachedMetaInfos;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, SpreadSheetMetaInfo> readMetaInfos() 
		throws FileNotFoundException, IOException {
		HashMap<String, SpreadSheetMetaInfo> map = 
			new HashMap<String, SpreadSheetMetaInfo>();

		Properties prop = getMetaInfoProperties();

		String fileName, hashFileName;
		long time;

		for (Object obj : prop.values()) {

			String info = obj.toString();
			String[] setting = info.split(",");

			time = Long.parseLong(setting[0].trim());
			fileName = setting[1].trim();
			hashFileName = setting[2].trim();

			SpreadSheetMetaInfo metaInfo = new SpreadSheetMetaInfo(fileName,
					time, hashFileName);
			if (setting.length > 3) {
				String reportParams = setting[3].trim();
				if (!StringUtils.isEmpty(reportParams)) {
					for (String param : reportParams.split("\\|")) {
						metaInfo.getReportParams().put(param.split(":")[0], WorkflowOperations.XStreamSerializeHelper.deserializeObject(param.split(":")[1].replaceAll("\n", ""), "PARAM_CONFIG", LinkedHashMap.class));
					}
				}
			}
			if (setting.length > 4) {
				String expression = setting[4].trim();
				metaInfo.setCronExpression(expression);
			}

			SpreadSheetMetaInfo exist = map.get(fileName);
			if (exist == null
					|| exist.getTimeInMillis() < metaInfo.getTimeInMillis()) {

				map.put(fileName, metaInfo);
			}

		}
		return map;
	}
	
	//TODO: replace Properties with json format, to provide version control 
	private static Properties getMetaInfoProperties() throws FileNotFoundException, IOException {
		BufferedReader reader = null;
		Properties prop = new Properties();
		reader = new BufferedReader(
				new FileReader(FileHelper.getSpreadsheetStorageFolderPath()	+ "metaFile"));
		prop.load(reader);
		return prop;
	}
	
	/* last metainfo modified time */
	private static long metainfoFileLasttModified;
	/* cache mata info object */
	private static ConcurrentHashMap<String, SpreadSheetMetaInfo> cachedMetaInfos = new ConcurrentHashMap<String, SpreadSheetMetaInfo>();
	
	/**
	 * Returns whether meta info file modified or not
	 */
	private static boolean checkMetaInfoFileModified() {
		File file = FileHelper.createMetaInfoFileIfNeeded();
		
		if (metainfoFileLasttModified == 0 ||
				metainfoFileLasttModified != file.lastModified()) {
			metainfoFileLasttModified = file.lastModified();
			return true;
		}
		return false;
	}
}