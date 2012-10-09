package nu.com.rill.analysis.report.param;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nu.com.rill.analysis.report.ReportManager;
import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.zkoss.zhtml.Input;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zss.app.zul.Zssapp;
import org.zkoss.zss.model.Book;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;

import com.foo.ecuiZk.PopSelect;
import com.foo.ecuiZk.Select;

public class ParamDivCtrl extends GenericForwardComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Log LOGGER = LogFactory.getLog(ParamDivCtrl.class);
	
	private Div paramDiv;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		String fileName = Executions.getCurrent().getParameter("fileName");
		final ReportManager reportMgr = (ReportManager) SpringUtil.getBean("reportMgr");
		
		final Report report = reportMgr.getReport(fileName);
		// Construct parameter components
		for (Object entry : Executions.getCurrent().getParameterMap().entrySet()) {
			@SuppressWarnings("unchecked")
			Entry<String, String[]> e = (Entry<String, String[]>) entry;
			// Update report parameters only. Don't support new parameters. 
			if (report.getParams() != null) {
				for (Map<PARAM_CONFIG, String> ele : report.getParams().values()) {
					if (e.getKey().equals(ele.get(PARAM_CONFIG.NAME))) {
						ele.put(PARAM_CONFIG.VALUE, e.getValue()[0]);
					}
				}
				if (report.getParams().containsKey(e.getKey())) {
					report.getParams().get(e.getKey()).put(PARAM_CONFIG.VALUE, e.getValue()[0]);
				}
			}
		}
		final Div tmpParamDiv = paramDiv;
		if (CollectionUtils.isEmpty(report.getParams())) {
			// Not contains any parameter
			LOGGER.debug("Not contains any parameter and open it directly.");
			// Post search action event
			Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_USER, tmpParamDiv.getNextSibling(), report));
			return;
		}
		
		paramComponentConstruct(paramDiv, report);
		
		// Initialize parameter components
		paramComponentInit(paramDiv, report, false);
		
		// Append reset button
		Button reset = new Button("重置");
		reset.setClass("reset-class");
		reset.setWidgetAttribute("fileName", report.getName());
		reset.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
				
				// Initialize parameter components
				paramComponentInit(tmpParamDiv, report, true);
			}
		});
		paramDiv.appendChild(reset);
				
		// Append search button
		Button search = new Button("查询");
		search.setClass("search-class");
		search.setWidgetAttribute("fileName", report.getName());
		
		search.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
				Zssapp app = (Zssapp) tmpParamDiv.getNextSibling();
				for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
					if (tmpParamDiv.getWidgetAttribute(entry.getValue().get(ReportEngine.PARAM_CONFIG.NAME)) != null) {
						entry.getValue().put(PARAM_CONFIG.VALUE, tmpParamDiv.getWidgetAttribute(entry.getValue().get(ReportEngine.PARAM_CONFIG.NAME)));
					}
				}
				app.setReport(report);
			}
		});
		paramDiv.appendChild(search);
		
		paramDiv.setClass("paramDiv-class");
		
		// Post search action event
		Executions.getCurrent().postEvent(Integer.MAX_VALUE - 1, new Event(Events.ON_CLICK, search));
		
	}
	
	private class SelectOnCreate {
		
		private Select target;
		
		public SelectOnCreate(Select target) {
			super();
			this.target = target;
		}
		
		public void resetSelectedIndex() {
			
			Assert.notEmpty(this.target.getItems());
			ArrayList<HashMap<String, String>> items = this.target.getItems();
			for (int i = 0; i < items.size(); i++) {
				HashMap<String, String> item = items.get(i);
				if (i == 0) {
					item.put("selected", "selected");
				} else {
					item.remove("selected");
				}
			}
			this.target.setItems(items);
			
			paramDiv.setWidgetAttribute(target.getId(), items.get(new Integer(0)).get("value"));
		}

		public void onCreate(Entry<String, Map<PARAM_CONFIG, String>> entryParam, Boolean reset) {
			
			if (reset == null) {
				reset = false;
			}
			
			final Map<PARAM_CONFIG, String> config = entryParam.getValue();
			
			// Reload content event. FIXME: MENGRAN. Deed-loop.
			// 1. Prepare fetch parameters
			Map<String, String> fetchParams = new HashMap<String, String>();
			String dependencies = config.get(PARAM_CONFIG.DEPENDENCIES);
			if (StringUtils.hasText(dependencies)) {
				for (String dep : dependencies.trim().split(" ")) {
					fetchParams.put(dep, paramDiv.getWidgetAttribute(dep));
				}
			}
			// 2. Fetch result
			Map<String, String> fetchResult = new LinkedHashMap<String, String>();
			String selectedIndex = fetchSelectItems(config.get(PARAM_CONFIG.FETCH_URL), fetchParams, fetchResult);
			if (reset || !StringUtils.hasText(selectedIndex)) {
				selectedIndex = "0";
			}
			
			// 3. Re-fill items
			ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
			int i = -1;
			for (Entry<String, String> entry : fetchResult.entrySet()) {
				i++;
				HashMap<String, String> item1 = new HashMap<String, String>();
				item1.put("value", entry.getKey());
				item1.put("text", entry.getValue());
				if (new Integer(i).toString().equals(selectedIndex)) {
					item1.put("selected", "selected");
				}
				items.add(item1);
			}
			target.setItems(items);
			
			paramDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), items.get(new Integer(selectedIndex)).get("value"));
		}
	}
	
	private class PopSelectOnCreate {
		
		private PopSelect target;
		
		public PopSelectOnCreate(PopSelect target) {
			super();
			this.target = target;
		}
		
		public void resetSelectedIndex() {
			
			Assert.notEmpty(this.target.getItems());
			ArrayList<ArrayList<String>> items = this.target.getItems();
			String value = "";
			for (int i = 0; i < items.size(); i++) {
				List<String> item = items.get(i);
				if (i == 0) {
					item.set(2, "true");
					value = item.get(0);
				} else {
					item.set(2, "false");
				}
			}
			this.target.setItems(items);
			
			paramDiv.setWidgetAttribute(target.getId(), value);
		}

		public void onCreate(Entry<String, Map<PARAM_CONFIG, String>> entryParam, Boolean reset) {
			
			if (reset == null) {
				reset = false;
			}
			
//			HashMap<String, String> options = new HashMap<String, String>();
//			options.put("butWidth", "400px");
//			options.put("panelWidth", "350px");
			
			final Map<PARAM_CONFIG, String> config = entryParam.getValue();
			
			// Reload content event. FIXME: MENGRAN. Deed-loop.
			// 1. Prepare fetch parameters
			Map<String, String> fetchParams = new HashMap<String, String>();
			String dependencies = config.get(PARAM_CONFIG.DEPENDENCIES);
			if (StringUtils.hasText(dependencies)) {
				for (String dep : dependencies.trim().split(" ")) {
					fetchParams.put(dep, paramDiv.getWidgetAttribute(dep));
				}
			}
			// 2. Fetch result
			Map<String, String> fetchResult = new LinkedHashMap<String, String>();
			String selectedIndex = fetchSelectItems(config.get(PARAM_CONFIG.FETCH_URL), fetchParams, fetchResult);
			if (reset || !StringUtils.hasText(selectedIndex)) {
				selectedIndex = "0";
			}
			
			// 3. Re-fill items
			ArrayList<ArrayList<String>> items = new ArrayList<ArrayList<String>>();
			int i = -1;
			String value = "";
			for (Entry<String, String> entry : fetchResult.entrySet()) {
				i++;
				ArrayList<String> item1 = new ArrayList<String>();
				item1.add(entry.getKey());
				item1.add(entry.getValue());
				for (String select : selectedIndex.split(",")) {
					if (select.equals(new Integer(i).toString())) {
						item1.add("true");
						value += entry.getKey();
					}else {
						item1.add("false");
					}
				}
				item1.add(i == 0 ? "true" : "false");
				items.add(item1);
			}
			target.setItems(items);
			
			paramDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), value);
		}
	}
	
	
	private void paramComponentConstruct(final Div paramDiv, final Report report) {
		
		final Div userParamDiv = paramDiv;
		Div floatParamDiv = new Div();
		floatParamDiv.setId("floatParamDiv");
		floatParamDiv.setClass("paramDiv-floatParamDiv-class");
		userParamDiv.appendChild(floatParamDiv);
		// Append download button
		Button download = new Button("下载");
		download.setClass("download-class");
		download.setWidgetAttribute("fileName", report.getName());
		
		download.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
				Zssapp app = (Zssapp) userParamDiv.getNextSibling();
				Book book = app.getSpreadsheet().getBook();
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					book.write(baos);
					Filedownload.save(baos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", report.getName());
				} catch (Exception e) {
					// Ignore~~
				}
			}
		});
		floatParamDiv.appendChild(download);
		
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
			final Entry<String, Map<PARAM_CONFIG, String>> paramEntry = entry;
			final String paramName = entry.getKey();
			final Map<PARAM_CONFIG, String> config = entry.getValue();
			if (!config.containsKey(PARAM_CONFIG.RENDER_TYPE)) {
				continue;
			}

			if ("provided".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				Input input = new Input();
				input.setId(config.get(PARAM_CONFIG.NAME));
				input.setVisible(false);
				input.setValue(config.get(PARAM_CONFIG.VALUE));
				paramDiv.appendChild(input);
				userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), input.getValue());
			}
			
			if ("calendar".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				Date now = new Date();
				if (config.get(PARAM_CONFIG.VALUE) != null) {
					try {
						String value = config.get(PARAM_CONFIG.VALUE);
						if ("yestoday".equals(value.toLowerCase())) {
							now = DateUtils.addDays(now, -1);
						} else if ("today".equals(value.toLowerCase())) {
							// Do nothing
						} else {
							// From parameter
							now = new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).parse(config.get(PARAM_CONFIG.VALUE));
						}
					} catch (ParseException e) {
						// Ignore
						LOGGER.warn(e);
					}
				}
				final Datebox db = new Datebox(now);
				userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).format(now));
				db.setId(config.get(PARAM_CONFIG.NAME));
				db.setFormat("long");
				db.setWidgetAttribute(PARAM_CONFIG.RENDER_TYPE.name(), config.get(PARAM_CONFIG.RENDER_TYPE));
				paramDiv.appendChild(new Label(paramName + " ："));
				paramDiv.appendChild(db);
				paramDiv.appendChild(new Label(" "));
				db.addEventListener(Events.ON_CHANGE, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).format(db.getValue()));
					}
				});
			}
			
			if ("select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				final Select cb = new Select();
				final SelectOnCreate soc = new SelectOnCreate(cb);
				cb.setId(config.get(PARAM_CONFIG.NAME));
				if (config.containsKey(PARAM_CONFIG.FETCH_URL)) {
					// Fetch content
					soc.onCreate(entry, false);
				}
				
				if (config.containsKey(PARAM_CONFIG.DEPENDENCIES)) {
					if (StringUtils.hasText(config.get(PARAM_CONFIG.DEPENDENCIES))) {
						for (String dep : config.get(PARAM_CONFIG.DEPENDENCIES).trim().split(" ")) {
							paramDiv.getFellow(dep).addEventListener(Events.ON_CHANGE, new EventListener() {
								
								@Override
								public void onEvent(Event event) throws Exception {
									soc.onCreate(paramEntry, (Boolean) event.getData());
								}
							});
						}
					}
				}
				cb.addEventListener(Events.ON_CHANGE, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						for (HashMap<String, String> item : cb.getItems()) {
							if (item.get("value").equals(cb.getValue())) {
								userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), cb.getValue());
							}
						}
					}
				});
				if (StringUtils.hasLength(paramName)) {
					paramDiv.appendChild(new Label(" "));
					Label l = new Label(paramName + " ：");
					l.setClass("param-label-class");
					paramDiv.appendChild(l);
					paramDiv.appendChild(cb);
					paramDiv.appendChild(new Label(" "));
				} else {
					floatParamDiv.appendChild(new Label(" "));
					floatParamDiv.appendChild(cb);
					floatParamDiv.appendChild(new Label(" "));
				}
				
			}
			
			if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				
				final PopSelect ps = new PopSelect();
				final PopSelectOnCreate psoc = new PopSelectOnCreate(ps);
				ps.setId(config.get(PARAM_CONFIG.NAME));
				if (config.containsKey(PARAM_CONFIG.FETCH_URL)) {
					// Fetch content
					psoc.onCreate(entry, false);
				}
				
				if (config.containsKey(PARAM_CONFIG.DEPENDENCIES)) {
					if (StringUtils.hasText(config.get(PARAM_CONFIG.DEPENDENCIES))) {
						for (String dep : config.get(PARAM_CONFIG.DEPENDENCIES).trim().split(" ")) {
							paramDiv.getFellow(dep).addEventListener(Events.ON_CHANGE, new EventListener() {
								
								@Override
								public void onEvent(Event event) throws Exception {
									psoc.onCreate(paramEntry, (Boolean) event.getData());
								}
							});
						}
					}
				}
				ps.addEventListener(Events.ON_CHANGE, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						String select = "";
						if (!CollectionUtils.isEmpty(ps.getValue())) {
							for (String value : ps.getValue()) {
								select += value + ",";
//								for (ArrayList<String> item : ps.getItems()) {
//									if (item.get(0).equals(value)) {
//										select += item.get(1) + ",";
//									}
//								}
							}
							userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), select.substring(0, select.length() - 1));
						}
						
					}
				});
				if (StringUtils.hasLength(paramName)) {
					paramDiv.appendChild(new Label(" "));
					Label l = new Label(paramName + " ：");
					l.setClass("param-label-class");
					paramDiv.appendChild(l);
					paramDiv.appendChild(ps);
					paramDiv.appendChild(new Label(" "));
				} else {
					floatParamDiv.appendChild(new Label(" "));
					floatParamDiv.appendChild(ps);
					floatParamDiv.appendChild(new Label(" "));
				}
			}
		
		}
		
	}
	
	private void paramComponentInit(final Div paramDiv, final Report report, boolean reset) {
		
		final Div userParamDiv = paramDiv;
		
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
			final Map<PARAM_CONFIG, String> config = entry.getValue();
			if (config.containsKey(PARAM_CONFIG.RENDER_TYPE)) {
				if ("select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					if (reset) {
						SelectOnCreate soc = new SelectOnCreate((Select) userParamDiv.getFellow(config.get(PARAM_CONFIG.NAME)));
						soc.resetSelectedIndex();
					}
					Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CHANGE, userParamDiv.getFellow(config.get(PARAM_CONFIG.NAME)), reset));
				}
				if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					if (reset) {
						PopSelectOnCreate psoc = new PopSelectOnCreate((PopSelect) userParamDiv.getFellow(config.get(PARAM_CONFIG.NAME)));
						psoc.resetSelectedIndex();
					}
					Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CHANGE, userParamDiv.getFellow(config.get(PARAM_CONFIG.NAME)), reset));
				}
				// FIXME: What we should do when calendar reset.
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private String fetchSelectItems(String url , Map<String, String> params, Map<String, String> items) {
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : params.entrySet()) {
			formparams.add(new NameValuePair(entry.getKey(), entry.getValue()));
		}
		
		String cookie = Executions.getCurrent().getHeader("Cookie");
		cookie = StringUtils.replace(cookie, "JSESSIONID=", "IGNORE_JSESSIONID=");
		
		try {
			ReportEngine.registCookie(cookie);
			LOGGER.debug("Fetch url with params: " + ObjectUtils.getDisplayString(params));
			String content = ReportEngine.fetchUrl(url, params);
			
			if (!StringUtils.hasText(content)) {
				LOGGER.info("Return empty content when try to fetch url " + url + " using " + ObjectUtils.getDisplayString(params) + " " + cookie);
				return "";
			}
			try {
				Map<String, Object> jsonResult = null;
				jsonResult = ReportEngine.mapper.readValue(content, new TypeReference<Map<String, Object>>() {
				});
				
				if (jsonResult.containsKey("_RE_PARAM_JSON_RESULT")) {
					jsonResult = (Map<String, Object>) jsonResult.get("_RE_PARAM_JSON_RESULT");
				}
				if (jsonResult != null) {
					items.putAll((Map<String, String>) jsonResult.get("value"));
				}
				
				return jsonResult == null ? "" : jsonResult.get("selectedIndex").toString();
			} catch (Exception e) {
				LOGGER.error("Error when try to parse to JSON " + content, e);
			}
		} finally {
			ReportEngine.registCookie(null);
		}
		
		return "";
	}
	
}
