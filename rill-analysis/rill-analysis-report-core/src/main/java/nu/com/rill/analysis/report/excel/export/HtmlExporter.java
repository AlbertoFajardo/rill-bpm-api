package nu.com.rill.analysis.report.excel.export;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.Range;
import org.zkoss.zss.model.Ranges;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.BookHelper;
import org.zkoss.zss.ui.impl.Utils;

import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tr;

/**
 * <STRONG>NOT Thread-safe</STRONG>
 * 
 * @author mengran
 *
 */
public class HtmlExporter {
	
	public static final String BLANK = "&nbsp;";
	
	private Workbook wb;
	private Sheet sheet;
	
	private Html html = new Html();
	private Body body = new Body();
	private Head head = new Head();
	
	private int rowCnt = -1;
	private int columnCnt = -1;
	
	public HtmlExporter(Workbook wb) {
		super();
		this.wb = wb;
		Assert.notNull(wb);
		
		html.appendChild(head);
		html.appendChild(body);
		Meta contextTypeMeta = new Meta("text/html;charset=utf-8");
		contextTypeMeta.setHttpEquiv("Content-Type");
		head.appendChild(contextTypeMeta);
	}

	public String export() {
		
		// Start parse work-book and generate HTML.
		// FIXME: MENGRAN. Support first sheet export only at this version.
		sheet = wb.getSheetAt(0);
		
		// Calculate table column count and row count
		rowCnt = sheet.getPhysicalNumberOfRows();
		for (Row r : sheet) {
			if (r != null && r.getPhysicalNumberOfCells() > columnCnt) {
				columnCnt = r.getPhysicalNumberOfCells();
			}
		}
		
		// Append one sheet
		body.appendChild(renderTable(sheet));
		
		return html.write();
	}
	
	private Table renderTable(Sheet sheet) {
		
		// Render table one by one
		Table table = new Table();
		for (Row r : sheet) {
			Tr tr = renderTr(r);
			table.appendChild(tr);
		}
		
		return table;
	}
	
	private void appRowStyle(Tr tr, Row r) {
		
		int height = Utils.getRowHeightInPx((Worksheet) sheet, r);
		tr.setAttribute("height", height + "px");
	}
	
	private Tr renderTr(Row r) {
		
		Tr tr = new Tr();
//		appRowStyle(tr, r);
		
		for (int i = 0; i < columnCnt; i++) {
			tr.appendChild(renderTd(r, r == null ? null : r.getCell(i), i));
		}
		
		return tr;
	}
	
	
	private int getCellColspan(Cell c) {
		
		final int sz = c.getSheet().getNumMergedRegions();
		for(int j = sz - 1; j >= 0; --j) {
			final CellRangeAddress addr = c.getSheet().getMergedRegion(j);
			if (addr.getFirstRow() != addr.getLastRow()) {
				throw new UnsupportedOperationException("Not support multi-row merge-regions at this version.");
			}
			if (addr.getFirstColumn() == c.getColumnIndex() && addr.getFirstRow() == c.getRowIndex()) {
				return addr.getNumberOfCells();
			}
		}
			
		return 1;
	} 
	
	private void appCellStyle(Td td, Cell c, Row r, int colIndex) {
		
		String textCssStyle = "";
		String fontCssStyle = "";
		if (c != null) {
			textCssStyle = BookHelper.getTextCSSStyle((Book) wb, c);
			fontCssStyle = BookHelper.getFontCSSStyle(c, BookHelper.getFont(c));
			
			td.setColspan(getCellColspan(c) + "");
		}
		
		td.setStyle(textCssStyle + fontCssStyle);
		
	}
	
	private Td renderTd(Row r, Cell c, int colIndex) {
		
		Td td = new Td();
		appCellStyle(td, c, r, colIndex);
		
		int height = Utils.getRowHeightInPx((Worksheet) sheet, r);
		int width = Utils.getColumnWidthInPx((Worksheet) sheet, colIndex);
		
		Div div = new Div();
		if (c == null || (c.getCellType() == Cell.CELL_TYPE_STRING && !StringUtils.hasText(c.getStringCellValue()))) {
			div.appendText(BLANK);
			div.setStyle("height: " + height + "px; width: " + width + "px");
			td.appendChild(div);
		} else {
			Range range = Ranges.range((Worksheet) c.getSheet(), c.getRowIndex(), c.getColumnIndex());
			td.appendText(ObjectUtils.getDisplayString(range.getValue()));
		}
		
		return td;
	}
	
}
