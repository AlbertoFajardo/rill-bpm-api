package nu.com.rill.analysis.report.param;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
			if (report.getParams() != null && report.getParams().containsKey(e.getKey())) {
				Map<PARAM_CONFIG, String> config = report.getParams().get(e.getKey());
				config.put(PARAM_CONFIG.VALUE, e.getValue()[0]);
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
		paramComponentInit(paramDiv, report);
		
		// Append search button
		Button search = new Button("查询");
		search.setWidgetAttribute("fileName", report.getName());
		
		search.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
				Zssapp app = (Zssapp) tmpParamDiv.getNextSibling();
				for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
					if (tmpParamDiv.getWidgetAttribute(entry.getKey()) != null) {
						entry.getValue().put(PARAM_CONFIG.VALUE, tmpParamDiv.getWidgetAttribute(entry.getKey()));
					}
				}
				app.setReport(report);
			}
		});
		paramDiv.appendChild(search);
		
		// Append reset button
		Button reset = new Button("重置");
		reset.setClass("paramDiv_btn");
		reset.setWidgetAttribute("fileName", report.getName());
		reset.addEventListener(Events.ON_CLICK, new EventListener() {
			
			@Override
			public void onEvent(Event event) throws Exception {
				Executions.getCurrent().sendRedirect("view2.zul?fileName=" + event.getTarget().getWidgetAttribute("fileName"));
			}
		});
		paramDiv.appendChild(reset);
		
		paramDiv.setClass("paramDiv-class");
		
		// Post search action event
		Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CLICK, search));
		
	}
	
	private void paramComponentConstruct(final Div paramDiv, final Report report) {
		
		final Div userParamDiv = paramDiv;
		
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
			final String paramName = entry.getKey();
			final Map<PARAM_CONFIG, String> config = entry.getValue();
			if (!config.containsKey(PARAM_CONFIG.RENDER_TYPE)) {
				continue;
			}

			if ("provided".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				Input input = new Input();
				input.setId(paramName);
				input.setVisible(false);
				input.setValue(fetchProvided(paramName, report));
				paramDiv.appendChild(input);
				userParamDiv.setWidgetAttribute(paramName, input.getValue());
			}
			
			if ("calendar".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				Date now = new Date();
				final Datebox db = new Datebox(now);
				userParamDiv.setWidgetAttribute(paramName, new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).format(now));
				db.setId(paramName);
				db.setFormat("long");
				db.setWidgetAttribute(PARAM_CONFIG.RENDER_TYPE.name(), config.get(PARAM_CONFIG.RENDER_TYPE));
				paramDiv.appendChild(new Label(paramName + " ："));
				paramDiv.appendChild(db);
				db.addEventListener(Events.ON_CHANGE, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						userParamDiv.setWidgetAttribute(paramName, new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).format(db.getValue()));
					}
				});
			}
			
			if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE)) || 
					"select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
				final Combobox cb = new Combobox();
				cb.setId(paramName);
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
							Map<String, String> fetchResult = fetchSelectItems(config.get(PARAM_CONFIG.FETCH_URL), fetchParams);
							// 3. Re-fill items
							cb.getItems().clear();
							for (Entry<String, String> entry : fetchResult.entrySet()) {
								Comboitem ci = cb.appendItem(entry.getValue());
								ci.setValue(entry.getKey());
							}
							cb.setSelectedIndex(0);
							userParamDiv.setWidgetAttribute(paramName, cb.getSelectedItem().getValue().toString());
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
						userParamDiv.setWidgetAttribute(paramName, cb.getSelectedItem().getValue().toString());
					}
				});
				paramDiv.appendChild(new Label(paramName + " ："));
				paramDiv.appendChild(cb);
				// Post create event
				Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CREATE, cb));
			}
		
		}
		
	}
	
	private void paramComponentInit(final Div paramDiv, final Report report) {
		
		final Div userParamDiv = paramDiv;
		
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : report.getParams().entrySet()) {
			final String paramName = entry.getKey();
			final Map<PARAM_CONFIG, String> config = entry.getValue();
			if (config.containsKey(PARAM_CONFIG.RENDER_TYPE)) {
				if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE)) 
					|| "select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CHANGE, userParamDiv.getFellow(paramName)));
				}
			}
		}
		
	}
	
	private String fetchProvided(String name, final Report report) {
		
		Assert.notNull(name);
		// First fetch from report parameter
//		if (report.getParams().containsKey(name) && 
//				report.getParams().get(name).get(PARAM_CONFIG.VALUE) != null) {
//			return report.getParams().get(name).get(PARAM_CONFIG.VALUE);
//		}
		// Second fetch from cookie.
		
		return "admin";
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> fetchSelectItems(String url , Map<String, String> params) {
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : params.entrySet()) {
			formparams.add(new NameValuePair(entry.getKey(), entry.getValue()));
		}
		
		String cookie = Executions.getCurrent().getHeader("Cookie");
		cookie = StringUtils.replace(cookie, "JSESSIONID=", "IGNORE_JSESSIONID=");
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		String content = ReportEngine.fetchUrl(url, params, cookie);
		if (!StringUtils.hasText(content)) {
			LOGGER.info("Return empty content when try to fetch url " + url + " using " + ObjectUtils.getDisplayString(params) + " " + cookie);
			return result;
		}
		try {
			result.putAll(ReportEngine.mapper.readValue(content, Map.class));
		} catch (Exception e) {
			LOGGER.error("Error when try to parse to JSON " + content, e);
		}
		
		return result;
	}
	
}
