package nu.com.rill.analysis.report.example;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/example")
public class JsonDataController {
	
	@RequestMapping(value = { "/data" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void data(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> lineData = new ArrayList<String>();
		lineData.add("商业产品线");
		lineData.add(ParamController.lineMap.get(ServletRequestUtils.getRequiredStringParameter(request, "lineId")));
		result.add(lineData);
		
		List<String> indData = new ArrayList<String>();
		indData.add("分析指标");
		indData.add(ParamController.indMap.get(ServletRequestUtils.getRequiredStringParameter(request, "indId")));
		result.add(indData);
		
		String[] posIds = ServletRequestUtils.getRequiredStringParameter(request, "posIds").split(",");
		
		Random random = new Random();
		List<String> trendTitleData = new ArrayList<String>();
		trendTitleData.add("时间");
		for (String posId : posIds) {
			trendTitleData.add(ParamController.posMap.get(posId));
		}
		result.add(trendTitleData);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = random.nextInt(10); i < 30 + random.nextInt(100); i++) {
			List<String> trendData = new ArrayList<String>();
			Integer randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			date = DateUtils.addDays(date, 1);
			trendData.add(sdf.format(date));
			for (String posId : posIds) {
				trendData.add(randomNum.toString());
				randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			}
			result.add(trendData);
		}
		
		ReportEngine.mapper.writeValue(out, result);
		
		out.flush();
		
	}
	
	@RequestMapping(value = { "/rtdata" }, method = {RequestMethod.GET, RequestMethod.POST})
	public void rtdata(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> lineData = new ArrayList<String>();
		lineData.add("商业产品线");
		lineData.add(ParamController.lineMap.get(ServletRequestUtils.getRequiredStringParameter(request, "lineId")));
		result.add(lineData);
		
		List<String> indData = new ArrayList<String>();
		indData.add("分析指标");
		indData.add(ParamController.indMap.get(ServletRequestUtils.getRequiredStringParameter(request, "indId")));
		result.add(indData);
		
		List<String> trendTitleData = new ArrayList<String>();
		trendTitleData.add("时间");
		trendTitleData.add("今天");
		trendTitleData.add("昨天");
		trendTitleData.add("上周同期");
		result.add(trendTitleData);
		
		for (int i = 0; i < 24; i++) {
			List<String> trendData = new ArrayList<String>();
			Integer randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			trendData.add(i + ":00");
			for (String a : "1,2,3".split(",")) {
				trendData.add(randomNum.toString());
				randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			}
			result.add(trendData);
		}
		
		ReportEngine.mapper.writeValue(out, result);
		
		out.flush();
		
	}
	
}
