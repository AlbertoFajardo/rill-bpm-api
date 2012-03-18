package org.rill.bpm.web.statistics;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONWriter;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.web.ScaleoutControllerSupport;
import org.rill.utils.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/statistics/process")
public class ProcessStatisticsController extends ScaleoutControllerSupport {
	
	@RequestMapping(value = { "/3days" }, method = RequestMethod.GET)
	public ModelAndView console(HttpServletRequest request,
			final HttpServletResponse response, ModelMap model) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		final String scaleoutName = model.containsAttribute(SCALE_OUT_TARGET) ? model.get(SCALE_OUT_TARGET).toString() : ProcessEngines.NAME_DEFAULT;
		WorkflowOperations workflowOperations = scaleoutTarget.get(scaleoutName);
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowOperations, ActivitiAccessor.class);
		activitiAccessor.runExtraCommand(new Command<Void>() {

			@Override
			public Void execute(CommandContext commandContext) {
				
				Date now = new Date();
				List<Object> list = new ArrayList<Object>();
				for (int i = 3; i > 0; i--) {
					Map<String, Object> parameterMap = new HashMap<String, Object>();
					Date resultDate = org.apache.commons.lang.time.DateUtils.addDays(now, -i);
					String dateYYYYMMDD = DateUtils.formatDateYYYYMMDD(resultDate);
					Date[] startAndEndDate = DateUtils.getDayStartAndEndDate(resultDate);
					parameterMap.put("timeBegin", startAndEndDate[0]);
					parameterMap.put("timeEnd", startAndEndDate[1]);
					Object runningResult = commandContext.getDbSqlSession().selectOne("selectDayRunningRootHistoricProcessInstanceCnt", parameterMap);
					Object stoppedResult = commandContext.getDbSqlSession().selectOne("selectDayStoppedRootHistoricProcessInstanceCnt", parameterMap);
					JSONArray dayJsonArray = new JSONArray();
					dayJsonArray.put(dateYYYYMMDD.substring(dateYYYYMMDD.lastIndexOf("-") + 1));
					dayJsonArray.put(runningResult);
					dayJsonArray.put(stoppedResult);
					
					// Put into result
					list.add(dayJsonArray);
				}
				JSONWriter jsonWriter = new JSONWriter(out);
				jsonWriter
				.array()
				.value(list)
				.endArray();
				out.flush();
				
				return null;
			}
		});
		
		return null;
	}
}
