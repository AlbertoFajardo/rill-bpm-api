package nu.com.rill.analysis.report.example;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/example")
public class ParamController {
	
	public static final Map<String, String> indMap = new LinkedHashMap<String, String>();
	public static final Map<String, String> lineMap = new LinkedHashMap<String, String>();
	public static final Map<String, String> posMap = new LinkedHashMap<String, String>();
	
	static {
		indMap.put("1", "点击消费");
		indMap.put("2", "新客户数");
		indMap.put("3", "CPM");
		
		lineMap.put("1", "搜索+网盟");
		lineMap.put("2", "游戏");
		lineMap.put("3", "音乐");
		
		posMap.put("1", "高级经理A");
		posMap.put("2", "高级经理B");
		posMap.put("3", "高级经理C");
		posMap.put("4", "高级经理D");
	}
	
	@RequestMapping(value = { "/ind" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void ind(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("value", indMap);
		jsonResult.put("selectedIndex", new Random().nextInt(2));
		ReportEngine.mapper.writeValue(out, jsonResult);
		
		out.flush();
		
	}
	
	@RequestMapping(value = { "/line" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void line(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("value", lineMap);
		jsonResult.put("selectedIndex", "1");
		ReportEngine.mapper.writeValue(out, jsonResult);
		
		out.flush();
		
	}
	
	@RequestMapping(value = { "/pos" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void pos(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("value", posMap);
		jsonResult.put("selectedIndex", "3");
		ReportEngine.mapper.writeValue(out, jsonResult);
		
		out.flush();
		
	}
	
	@RequestMapping(value = { "/type" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void type(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		ServletRequestUtils.getRequiredStringParameter(request, "dateType");
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("1", "按年查看");
		result.put("2", "按月查看");
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("value", result);
		jsonResult.put("selectedIndex", "1");
		ReportEngine.mapper.writeValue(out, jsonResult);
		
		out.flush();
		
	}
}
