package nu.com.rill.analysis.report.param;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.springframework.util.StringUtils;
import org.zkoss.zhtml.Input;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zss.app.file.SpreadSheetMetaInfo;
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
	
	private Div paramDiv;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		String fileName = Executions.getCurrent().getParameter("fileName");
		// FIXME: MENGRAN. Not thread-safe.
		final SpreadSheetMetaInfo ssmi = SpreadSheetMetaInfo.getMetaInfos().get(fileName);
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : ssmi.getReportParams().entrySet()) {
			String paramName = entry.getKey();
			final Map<PARAM_CONFIG, String> config = entry.getValue();
			// FIXME: MENGRAN. Refactor by visitor pattern
			if (config.containsKey(PARAM_CONFIG.RENDER_TYPE)) {
				if ("calendar".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					Date now = new Date();
					final Datebox db = new Datebox(now);
					config.put(PARAM_CONFIG.VALUE, new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).format(now));
					db.setId(paramName);
					db.setFormat("long");
					db.setWidgetAttribute(PARAM_CONFIG.RENDER_TYPE.name(), config.get(PARAM_CONFIG.RENDER_TYPE));
					paramDiv.appendChild(new Label(paramName + " ："));
					paramDiv.appendChild(db);
					db.addEventListener(Events.ON_CHANGE, new EventListener() {
						
						@Override
						public void onEvent(Event event) throws Exception {
							config.put(PARAM_CONFIG.VALUE, new SimpleDateFormat(config.get(PARAM_CONFIG.FORMAT)).format(db.getValue()));
						}
					});
				} else if ("provided".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					Input input = new Input();
					input.setId(paramName);
					input.setVisible(false);
					// FIXME: MENGRAN. Call provided parameter resolver
					input.setValue("admin");
//					paramDiv.appendChild(new Label(paramName));
					paramDiv.appendChild(input);
					config.put(PARAM_CONFIG.VALUE, input.getValue());
				} else if ("multiselect".equals(config.get(PARAM_CONFIG.RENDER_TYPE)) || "select".equals(config.get(PARAM_CONFIG.RENDER_TYPE))) {
					final Combobox cb = new Combobox();
					cb.setId(paramName);
					cb.setMold("rounded");
					cb.setWidgetAttribute(PARAM_CONFIG.DEPENDENCIES.name(), config.get(PARAM_CONFIG.DEPENDENCIES));
					cb.setWidgetAttribute(PARAM_CONFIG.RENDER_TYPE.name(), config.get(PARAM_CONFIG.RENDER_TYPE));
					if (config.containsKey(PARAM_CONFIG.FETCH_URL)) {
						// Fetch content
						cb.addEventListener(Events.ON_CREATE, new EventListener() {
							
							@Override
							public void onEvent(Event event) throws Exception {
								// Reload content event. FIXME: MENGRAN. Deed-loop. FIXME: MENGRAN. Refactor by visitor pattern
								// 1. Prepare fetch parameters
								Map<String, String> fetchParams = new HashMap<String, String>();
								String dependencies = config.get(PARAM_CONFIG.DEPENDENCIES);
								if (StringUtils.hasText(dependencies)) {
									for (String dep : dependencies.trim().split(" ")) {
										String type = paramDiv.getFellow(dep).getWidgetAttribute(PARAM_CONFIG.RENDER_TYPE.name());
										if ("calendar".equals(type)) {
											Datebox depDb = (Datebox) paramDiv.getFellow(dep);
											fetchParams.put(dep, new SimpleDateFormat("yyyy-MM-dd").format(depDb.getValue()));
										}
										if ("provided".equals(type)) {
											Input depInput = (Input) paramDiv.getFellow(dep);
											fetchParams.put(dep, depInput.getValue());
										}
										if ("multiselect".equals(type)) {
											Combobox depCb = (Combobox) paramDiv.getFellow(dep);
											if (depCb.getSelectedItem() == null) {
												return;
											}
											fetchParams.put(dep, depCb.getSelectedItem().getValue().toString());
										}
									}
								}
								// 2. Fetch result
								Map<String, String> fetchResult = fetch(config.get(PARAM_CONFIG.FETCH_URL), fetchParams);
								// 3. Re-fill items
								cb.getItems().clear();
								for (Entry<String, String> entry : fetchResult.entrySet()) {
									Comboitem ci = cb.appendItem(entry.getValue());
									ci.setValue(entry.getKey());
								}
								
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
							config.put(PARAM_CONFIG.VALUE, cb.getSelectedItem().getValue().toString());
						}
					});
					paramDiv.appendChild(new Label(paramName + " ："));
					paramDiv.appendChild(cb);
					Executions.getCurrent().postEvent(Integer.MAX_VALUE, new Event(Events.ON_CREATE, cb));
				}
			}
		}
		
		if (!ssmi.getReportParams().isEmpty()) {
			// Append search button
			Button search = new Button("查询");
			search.setClass("paramDiv_btn");
			search.setWidgetAttribute("fileName", ssmi.getFileName());
			final Div tmpParamDiv = paramDiv;
			search.addEventListener(Events.ON_CLICK, new EventListener() {
				
				@Override
				public void onEvent(Event event) throws Exception {
					Zssapp app = (Zssapp) tmpParamDiv.getNextSibling();
					app.setSrc(ssmi.getSrc());
				}
			});
			paramDiv.appendChild(search);
			
			// Append reset button
			Button reset = new Button("重置");
			reset.setClass("paramDiv_btn");
			reset.setWidgetAttribute("fileName", ssmi.getFileName());
			reset.addEventListener(Events.ON_CLICK, new EventListener() {
				
				@Override
				public void onEvent(Event event) throws Exception {
	//				Executions.getCurrent().sendRedirect("view2.zul?fileName=" + event.getTarget().getWidgetAttribute("fileName"));
				}
			});
			paramDiv.appendChild(reset);
		}
		
	}
	
	private Map<String, String> fetch(String url , Map<String, String> params) {
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		if (url.equals("ind.action")) {
			result.put("点击消费", "点击消费");
			result.put("新客户数", "新客户数");
			result.put("CPM", "CPM");
		}
		if (url.equals("line.action")) {
			result.put("搜索", "搜索");
			result.put("网盟", "网盟");
			result.put("游戏", "游戏");
		}
		if (url.equals("pos.action")) {
			result.put("高级经理A", "高级经理A");
			result.put("高级经理B", "高级经理B");
		}
		return result;
	}
	
}
