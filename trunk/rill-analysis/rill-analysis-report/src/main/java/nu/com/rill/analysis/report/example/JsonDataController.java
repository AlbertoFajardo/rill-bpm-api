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
		lineData.add(ServletRequestUtils.getRequiredStringParameter(request, "lineId"));
		result.add(lineData);
		
		List<String> indData = new ArrayList<String>();
		indData.add("分析指标");
		indData.add(ServletRequestUtils.getRequiredStringParameter(request, "indId"));
		result.add(indData);
		
		Random random = new Random();
		List<String> trendTitleData = new ArrayList<String>();
		trendTitleData.add("时间");
		trendTitleData.add("高级经理");
		trendTitleData.add("高级经理" + random.nextInt(100));
		trendTitleData.add("高级经理" + random.nextInt(100));
		trendTitleData.add("高级经理" + random.nextInt(100));
		trendTitleData.add("高级经理" + random.nextInt(100));
		result.add(trendTitleData);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = random.nextInt(10); i < 30; i++) {
			List<String> trendData = new ArrayList<String>();
			Integer randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			date = DateUtils.addDays(date, 1);
			trendData.add(sdf.format(date));
			trendData.add(randomNum.toString());
			randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			trendData.add(randomNum.toString());
			randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			trendData.add(randomNum.toString());
			randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			trendData.add(randomNum.toString());
			randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
			trendData.add(randomNum.toString());
			result.add(trendData);
		}
		
		ReportEngine.mapper.writeValue(out, result);
		
		out.flush();
		
	}
	
}
