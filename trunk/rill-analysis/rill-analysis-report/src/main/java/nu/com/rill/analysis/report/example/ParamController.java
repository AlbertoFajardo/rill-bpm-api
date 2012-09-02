package nu.com.rill.analysis.report.example;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/example")
public class ParamController {
	
	@RequestMapping(value = { "/ind" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void ind(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("点击消费", "点击消费");
		result.put("新客户数", "新客户数");
		result.put("CPM", "CPM");
		ReportEngine.mapper.writeValue(out, result);
		
		out.flush();
		
	}
	
	@RequestMapping(value = { "/line" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void line(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("搜索", "搜索");
		result.put("网盟", "网盟");
		result.put("游戏", "游戏");
		ReportEngine.mapper.writeValue(out, result);
		
		out.flush();
		
	}
	
	@RequestMapping(value = { "/pos" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void pos(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("高级经理A", "高级经理A");
		result.put("高级经理B", "高级经理B");
		ReportEngine.mapper.writeValue(out, result);
		
		out.flush();
		
	}
}
