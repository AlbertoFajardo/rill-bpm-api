package nu.com.rill.analysis.report.example;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.BookHelper;
import org.zkoss.zss.model.impl.ExcelImporter;

public class ParamControllerTest {

	@Test
	public void test() throws Exception {
		
		ObjectMapper om = new ObjectMapper();
		StringWriter out = new StringWriter();
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("1", "点击消费");
		result.put("2", "新客户数");
		result.put("3", "CPM");
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("value", result);
		jsonResult.put("selectedIndex", 2);
		om.writeValue(out, jsonResult);
		
		System.out.println(out.toString());
		
		Map<String, Object> parseResult = om.readValue(out.toString(), Map.class);
		Assert.assertTrue(parseResult.size() == 2);
		Assert.assertEquals(parseResult.get("selectedIndex").toString(), "2");
		Assert.assertTrue(parseResult.get("value") instanceof Map);
		Assert.assertTrue(((Map<String, String>) parseResult.get("value")).size() == 3);
	}
	
	@Test
	public void testluopan() {
		
		
		ClassPathResource cpr = new ClassPathResource("test-luopan.xlsx");
		try {
			Book book = new ExcelImporter().imports(cpr.getInputStream(), "test-luopan.xlsx");
			
			Cell c = book.getSheet("_input").getRow(5).getCell(1);
			Assert.assertTrue(BookHelper.getCellValue(c) != null);
			Assert.assertNotNull(c.getNumericCellValue());
			
			DecimalFormat myFormatter = new DecimalFormat(c.getCellStyle().getDataFormatString());
			System.out.println(myFormatter.format(c.getNumericCellValue()));
			BigDecimal bd = new BigDecimal(myFormatter.format(c.getNumericCellValue()).replaceAll(",", ""));
			System.out.println(bd);
			
			
			Assert.assertTrue("1234.00".contains("."));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	

}
