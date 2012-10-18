package nu.com.rill.analysis.report.excel.export;

import org.springframework.util.Assert;
import org.zkoss.poi.ss.usermodel.Workbook;

import com.hp.gagawa.java.elements.Html;

public class HtmlExporter {
	
	private Workbook wb;
	
	private Html html = new Html();
	
	public HtmlExporter(Workbook wb) {
		super();
		this.wb = wb;
		Assert.notNull(wb);
	}

	public String export() {
		
		// Start parse work-book and generate HTML.
		// FIXME: MENGRAN. Need working...
		wb.toString();
		
		return html.write();
	}
	
}
