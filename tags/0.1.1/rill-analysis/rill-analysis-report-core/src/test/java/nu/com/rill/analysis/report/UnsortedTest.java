package nu.com.rill.analysis.report;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mondrian.util.Format;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

import com.hp.gagawa.java.elements.Div;

public class UnsortedTest {

	@Test
	public void test() {
		
		String mdx = "SELECT * FROM [Time].[2011] AND ON";
		System.out.println(mdx.contains("[Time].[2011]"));
		mdx = mdx.replaceAll("[Time].[2011]", "[Time].[2010]");
		System.out.println(mdx);
		mdx = StringUtils.replace(mdx, "[Time].[2011]", "[Time].[2010]");
		System.out.println(mdx);		
		
	}
	
	@Test
	public void testCurrentDateMember() {
		
		final Locale locale = Locale.getDefault();
        final Format format = new Format("[\"Time\"]\\.[yyyy]\\.[\"Q\"q]\\.[mm]\\.[dd]", locale);
        String currDateStr = format.format(new Date());
        System.out.println(currDateStr);
        
        String areaRefrence = "_input!A4:F7";
        System.out.println(areaRefrence.substring(areaRefrence.indexOf("!") + 1));
        
	}
	
	@Test
	public void testREPARAMJSONRESULT() throws Exception {
		
		String json = "{\"model\":{\"recordCount\":0,\"pageCount\":0,\"currentPage\":0,\"fetchSize\":15,\"beginIndex\":-1},\"_RE_DATA_JSON_RESULT\":[],\"status\":0,\"statusInfo\":\"\",\"_RE_PARAM_JSON_RESULT\":{\"value\":{\"1\":\"高级经理A\",\"2\":\"高级经理B\",\"3\":\"高级经理C\",\"4\":\"高级经理D\"},\"selectedIndex\":\"2\"}}";
		
		Map<String, Object> jsonResult = null;
		
		jsonResult = ReportEngine.mapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
		
		if (jsonResult.containsKey("_RE_PARAM_JSON_RESULT")) {
			jsonResult = (Map<String, Object>) jsonResult.get("_RE_PARAM_JSON_RESULT");
		}
		
		System.out.println(jsonResult);
		Assert.assertEquals(jsonResult.size(), 2);
		Assert.assertEquals(((Map<String, Object>) jsonResult.get("value")).size(), 4);
		Assert.assertEquals(((Map<String, String>) jsonResult.get("value")).get("1"), "高级经理A");
		Assert.assertEquals(jsonResult.get("selectedIndex"), "2");
	}
	
	@Test
	public void testREDATAJSONRESULT() throws Exception {
		
		List<List<String>> data = null;
		
		String json = "{\"model\":{\"recordCount\":0,\"pageCount\":0,\"currentPage\":0,\"fetchSize\":15,\"beginIndex\":-1},\"_RE_DATA_JSON_RESULT\":[[\"商业产品线\",\"搜索\"],[\"分析指标\",\"CPM\"],[\"时间\",\"高级经理A\",\"高级经理\"],[\"2012-10-09\",\"3866438\",\"2578934\"],[\"2012-10-10\",\"3286401\",\"3087627\"],[\"2012-10-11\",\"3384874\",\"3329007\"],[\"2012-10-12\",\"3482655\",\"3498274\"],[\"2012-10-13\",\"4164374\",\"3357476\"],[\"2012-10-14\",\"4110706\",\"2481985\"],[\"2012-10-15\",\"2810232\",\"2551658\"],[\"2012-10-16\",\"3643434\",\"3957610\"],[\"2012-10-17\",\"3189620\",\"2754973\"],[\"2012-10-18\",\"2908621\",\"2650047\"],[\"2012-10-19\",\"3021165\",\"3660485\"],[\"2012-10-20\",\"2999697\",\"3688816\"],[\"2012-10-21\",\"3069370\",\"3600192\"],[\"2012-10-22\",\"3023616\",\"4119878\"],[\"2012-10-23\",\"3122089\",\"3216685\"],[\"2012-10-24\",\"3517770\",\"2762872\"],[\"2012-10-25\",\"3630314\",\"4005918\"],[\"2012-10-26\",\"4308110\",\"3448886\"],[\"2012-10-27\",\"4202116\",\"2878357\"],[\"2012-10-28\",\"4240150\",\"2850633\"],[\"2012-10-29\",\"3174415\",\"2709901\"],[\"2012-10-30\",\"3729310\",\"2874552\"],[\"2012-10-31\",\"2678813\",\"2621436\"],[\"2012-11-01\",\"2733308\",\"3772581\"],[\"2012-11-02\",\"3044131\",\"3683957\"],[\"2012-11-03\",\"3285511\",\"3386657\"],[\"2012-11-04\",\"2915909\",\"2867730\"],[\"2012-11-05\",\"3238849\",\"2496186\"]],\"status\":0,\"statusInfo\":\"\",\"_RE_PARAM_JSON_RESULT\":{}}";
		Map<String, Object> jsonResult = null;
		jsonResult = ReportEngine.mapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
		if (jsonResult.containsKey("_RE_DATA_JSON_RESULT")) {
			data = (List<List<String>>) jsonResult.get("_RE_DATA_JSON_RESULT");
		}
		
		if (data == null) {
			data = new ArrayList<List<String>>();
			data.addAll(ReportEngine.mapper.readValue(json, List.class));
		}
	}
	
	@Test
	public void testListJson() throws Exception {
		
		List<List<String>> data = null;
		
		String json = "[[\"商业产品线\",\"搜索\"],[\"分析指标\",\"CPM\"],[\"时间\",\"高级经理A\",\"高级经理\"],[\"2012-10-09\",\"3866438\",\"2578934\"],[\"2012-10-10\",\"3286401\",\"3087627\"],[\"2012-10-11\",\"3384874\",\"3329007\"],[\"2012-10-12\",\"3482655\",\"3498274\"],[\"2012-10-13\",\"4164374\",\"3357476\"],[\"2012-10-14\",\"4110706\",\"2481985\"],[\"2012-10-15\",\"2810232\",\"2551658\"],[\"2012-10-16\",\"3643434\",\"3957610\"],[\"2012-10-17\",\"3189620\",\"2754973\"],[\"2012-10-18\",\"2908621\",\"2650047\"],[\"2012-10-19\",\"3021165\",\"3660485\"],[\"2012-10-20\",\"2999697\",\"3688816\"],[\"2012-10-21\",\"3069370\",\"3600192\"],[\"2012-10-22\",\"3023616\",\"4119878\"],[\"2012-10-23\",\"3122089\",\"3216685\"],[\"2012-10-24\",\"3517770\",\"2762872\"],[\"2012-10-25\",\"3630314\",\"4005918\"],[\"2012-10-26\",\"4308110\",\"3448886\"],[\"2012-10-27\",\"4202116\",\"2878357\"],[\"2012-10-28\",\"4240150\",\"2850633\"],[\"2012-10-29\",\"3174415\",\"2709901\"],[\"2012-10-30\",\"3729310\",\"2874552\"],[\"2012-10-31\",\"2678813\",\"2621436\"],[\"2012-11-01\",\"2733308\",\"3772581\"],[\"2012-11-02\",\"3044131\",\"3683957\"],[\"2012-11-03\",\"3285511\",\"3386657\"],[\"2012-11-04\",\"2915909\",\"2867730\"],[\"2012-11-05\",\"3238849\",\"2496186\"]]";
		Map<String, Object> jsonResult = new LinkedHashMap<String, Object>();
		try {
			jsonResult = ReportEngine.mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonMappingException e) {
			// Ignore 
		}
		if (jsonResult.containsKey("_RE_DATA_JSON_RESULT")) {
			data = (List<List<String>>) jsonResult.get("_RE_DATA_JSON_RESULT");
		}
		
		if (data == null) {
			data = new ArrayList<List<String>>();
			data.addAll(ReportEngine.mapper.readValue(json, List.class));
		}
	}
	
	@Test
	public void testContains() throws Exception {
		
		String ip = "application/json";
		String ip2 = "application/json;charset=UTF-8";

		Assert.assertTrue(ip2.contains(ip));
	}
	
	@Test
	public void testHash() throws Exception {
		
		String ip = "10.38.100.27";
		System.out.println(InetAddress.getByName(ip).hashCode() + 7800);
		
		ip = "10.50.133.26";
		System.out.println(InetAddress.getByName(ip).hashCode() + 7800);
	}
	
	@Test
	public void testTickInterval() {
		
		int labelCnt = 30;
		int width = 780;
		
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
		
		labelCnt = 90;
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
		
		labelCnt = 6;
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
		
		labelCnt = 10;
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
		
		labelCnt = 30;
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
		
		labelCnt = 11;
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
		
		labelCnt = 12;
		System.out.println(width + " " + labelCnt + " " + ((labelCnt * 130 / width) <= 1 ? 1 : (labelCnt * 130 / width)));
	}
	
	@Test
	public void testFindUrlInBackGroupdStyle() {
		String style = "background-url: url('./somedir/some.png')";
		int startIndex = style.indexOf("url('");
		int endIndex = style.indexOf(".png", startIndex);
		String url = style.substring(startIndex + 5, endIndex + 4);
		
		Assert.assertEquals("./somedir/some.png", url);
	}

}
