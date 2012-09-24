package nu.com.rill.analysis.report.param;

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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

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
			Executions.getCurrent().postEvent(Integer.MAX_VALUE - 1, new Event(Events.ON_USER, tmpParamDiv.getNextSibling(), report));
			return;
		}
		
		paramComponentConstruct(paramDiv, report);
		
		// Initialize parameter components
		paramComponentInit(paramDiv, report);
		
		// Append reset button
		Button reset = new Button("重置");
		reset.setClass("reset-class");
		reset.setWidgetAttribute("fileName", report.getName());
		reset.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
				Executions.getCurrent().sendRedirect("view2.zul?fileName=" + event.getTarget().getWidgetAttribute("fileName"));
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
	
	private void paramComponentConstruct(final Div paramDiv, final Report report) {
		
		final Div userParamDiv = paramDiv;
		Div floatParamDiv = new Div();
		floatParamDiv.setId("floatParamDiv");
		floatParamDiv.setClass("paramDiv-floatParamDiv-class");
		userParamDiv.appendChild(floatParamDiv);
		// Append download button
		Button download = new Button("Download");
		download.setClass("search-class");
		download.setWidgetAttribute("fileName", report.getName());
		
		download.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
			}
		});
		floatParamDiv.appendChild(download);
		
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
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
			
			if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE)) || 
					"select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				final Combobox cb = new Combobox();
				cb.setId(config.get(PARAM_CONFIG.NAME));
				cb.setMold("rounded");
				if (config.containsKey(PARAM_CONFIG.FETCH_URL)) {
					// Fetch content
					cb.addEventListener(Events.ON_CREATE, new EventListener() {
						
						@Override
						public void onEvent(Event event) throws Exception {
							// Reload content event. FIXME: MENGRAN. Deed-loop.
							// 1. Prepare fetch parameters
							Map<String, String> fetchParams = new HashMap<String, String>();
							String dependencies = config.get(PARAM_CONFIG.DEPENDENCIES);
							if (StringUtils.hasText(dependencies)) {
								for (String dep : dependencies.trim().split(" ")) {
									fetchParams.put(dep, userParamDiv.getWidgetAttribute(dep));
								}
							}
							// 2. Fetch result
							Map<String, String> fetchResult = new LinkedHashMap<String, String>();
							String selectedIndex = fetchSelectItems(config.get(PARAM_CONFIG.FETCH_URL), fetchParams, fetchResult);
							// 3. Re-fill items
							cb.getItems().clear();
							for (Entry<String, String> entry : fetchResult.entrySet()) {
								Comboitem ci = cb.appendItem(entry.getValue());
								ci.setValue(entry.getKey());
							}
							cb.setSelectedIndex("".equals(selectedIndex) ? 0 : new Integer(selectedIndex));
							userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), cb.getSelectedItem().getLabel());
						}
					});
				}
				if (config.containsKey(PARAM_CONFIG.DEPENDENCIES)) {
					if (StringUtils.hasText(config.get(PARAM_CONFIG.DEPENDENCIES))) {
						for (String dep : config.get(PARAM_CONFIG.DEPENDENCIES).trim().split(" ")) {
							paramDiv.getFellow(dep).addEventListener(Events.ON_CHANGE, new EventListener() {
								
								@Override
								public void onEvent(Event event) throws Exception {
									Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CREATE, cb));
								}
							});
						}
					}
				}
				cb.addEventListener(Events.ON_CHANGE, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						userParamDiv.setWidgetAttribute(config.get(PARAM_CONFIG.NAME), cb.getSelectedItem().getLabel());
					}
				});
				if (StringUtils.hasLength(paramName)) {
					paramDiv.appendChild(new Label(paramName + " ："));
					paramDiv.appendChild(cb);
					paramDiv.appendChild(new Label(" "));
				} else {
					floatParamDiv.appendChild(cb);
					floatParamDiv.appendChild(new Label(" "));
				}
				
				// Post create event
				Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CREATE, cb));
			}
		
		}
		
	}
	
	private void paramComponentInit(final Div paramDiv, final Report report) {
		
		final Div userParamDiv = paramDiv;
		
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
			final Map<PARAM_CONFIG, String> config = entry.getValue();
			if (config.containsKey(PARAM_CONFIG.RENDER_TYPE)) {
				if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE)) 
					|| "select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CHANGE, userParamDiv.getFellow(config.get(PARAM_CONFIG.NAME))));
				}
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
			String content = ReportEngine.fetchUrl(url, params);
			
			if (!StringUtils.hasText(content)) {
				LOGGER.info("Return empty content when try to fetch url " + url + " using " + ObjectUtils.getDisplayString(params) + " " + cookie);
				return "";
			}
			try {
				Map<String, Object> jsonResult = ReportEngine.mapper.readValue(content, Map.class);
				items.putAll((Map<String, String>) jsonResult.get("value"));
				return jsonResult.get("selectedIndex").toString();
			} catch (Exception e) {
				LOGGER.error("Error when try to parse to JSON " + content, e);
			}
		} finally {
			ReportEngine.registCookie(null);
		}
		
		return "";
	}
	
}
