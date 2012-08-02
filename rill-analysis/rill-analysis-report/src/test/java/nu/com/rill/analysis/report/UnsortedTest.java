package nu.com.rill.analysis.report;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import mondrian.olap.Id;
import mondrian.olap.Util;
import mondrian.util.Format;

import org.junit.Test;
import org.springframework.util.StringUtils;

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
        
	}

}
