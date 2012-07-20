package nu.com.rill.analysis.report;

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

}
