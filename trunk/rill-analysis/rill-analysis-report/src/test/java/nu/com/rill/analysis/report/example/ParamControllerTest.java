package nu.com.rill.analysis.report.example;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.junit.Assert;
import org.junit.Test;

public class ParamControllerTest {

	@Test
	public void test() throws Exception {
		
		StringWriter out = new StringWriter();
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("1", "点击消费");
		result.put("2", "新客户数");
		result.put("3", "CPM");
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("value", result);
		jsonResult.put("selectedIndex", 2);
		ReportEngine.mapper.writeValue(out, jsonResult);
		
		System.out.println(out.toString());
		
		Map<String, Object> parseResult = ReportEngine.mapper.readValue(out.toString(), Map.class);
		Assert.assertTrue(parseResult.size() == 2);
		Assert.assertEquals(parseResult.get("selectedIndex").toString(), "2");
		Assert.assertTrue(parseResult.get("value") instanceof Map);
		Assert.assertTrue(((Map<String, String>) parseResult.get("value")).size() == 3);
	}

}
