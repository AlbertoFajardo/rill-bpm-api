package nu.com.rill.analysis.report.excel.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.com.rill.analysis.report.excel.ConditionalFormattingHelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.usermodel.ZssChartX;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.BookHelper;
import org.zkoss.zss.model.impl.DrawingManager;
import org.zkoss.zss.ui.impl.CellFormatHelper;
import org.zkoss.zss.ui.impl.MergeMatrixHelper;
import org.zkoss.zss.ui.impl.Utils;
import org.zkoss.zssex.model.impl.DrawingManagerImpl;
import org.zkoss.zssex.ui.widget.ChartWidget;
import org.zkoss.zssex.ui.widget.PicChart;
import org.zkoss.zssex.util.ChartHelper;

import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Img;
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
	private MergeMatrixHelper mmhelper;
	
	private Html html = new Html();
	private Body body = new Body();
	private Head head = new Head();
	
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
		// FIXME: MENGRAN. Do not support frozen row/column at this version.
		int fzr = 0;
		int fzc = 0;
		final int sz = sheet.getNumMergedRegions();
		final List<int[]> mergeRanges = new ArrayList<int[]>(sz);
		for(int j = sz - 1; j >= 0; --j) {
			final CellRangeAddress addr = sheet.getMergedRegion(j);
			mergeRanges.add(new int[] {addr.getFirstColumn(), addr.getFirstRow(), addr.getLastColumn(), addr.getLastRow()});
		}
		mmhelper = new MergeMatrixHelper(mergeRanges, fzr, fzc);
		
		// Calculate table column count
		for (Row r : sheet) {
			if (r != null && r.getPhysicalNumberOfCells() > columnCnt) {
				columnCnt = r.getPhysicalNumberOfCells();
			}
		}
		
		// Append one sheet
		body.appendChild(renderTable(sheet));
		
		// Generate chart image
		DrawingManager dm = new DrawingManagerImpl(sheet);
		List<ZssChartX> charts = dm.getChartXs();
		if (!CollectionUtils.isEmpty(charts)) {
			int zindex = 200;
			for (ZssChartX chartX : charts) {
				ChartWidget chartwgt = new ChartWidget((Worksheet) sheet, chartX, zindex, ChartHelper.CHART_LIB_TYPE_PIC_CHART);
				PicChart picChart = (PicChart) chartwgt.inner();
				byte[] bytes = picChart.getEngine().drawChart(picChart);
				String imgName = "";
				try {
				File tmpImage = File.createTempFile("htmlExporter" + System.currentTimeMillis(), ".png");
				FileUtils.writeByteArrayToFile(tmpImage, bytes);
				
				imgName = tmpImage.getName();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// New DIV to hold it.
				Div div = new Div();
				int top = 0;
				int left = 0;
				for (int i = 0; i <= chartwgt.getRow(); i++) {
					top += Utils.getRowHeightInPx((Worksheet) sheet, sheet.getRow(i));
				}
				for (int i = 0; i <= chartwgt.getColumn(); i++) {
					left += Utils.getColumnWidthInPx((Worksheet) sheet, i);
				}
				div.setStyle("position: absolute; height: " + chartwgt.getHeight() + "; width: " + chartwgt.getWidth() + ";"
						+ "padding-top: " + chartwgt.getTop() + "px; padding-left: " + chartwgt.getLeft() + "px;"
						+ "top: " + top + "px; left: " + left + "px;");
				Img img = new Img("", "./" + imgName);
				div.appendChild(img);
				body.appendChild(div);
			}
		}
		
		return html.write();
	}
	
	private Table renderTable(Sheet sheet) {
		
		// Render table one by one
		Table table = new Table();
		
		int height = 0;
		for (Row r : sheet) {
			Tr tr = renderTr(r);
			table.appendChild(tr);
			height +=Utils.getRowHeightInPx((Worksheet) sheet, r);
		}
		table.setCellpadding("0");
		table.setCellspacing("0");
		table.setAttribute("height", height + "px");
		return table;
	}
	
	private Tr renderTr(Row r) {
		
		Tr tr = new Tr();
		
		for (int i = 0; i < columnCnt; i++) {
			Td td = renderTd(r, r == null ? null : r.getCell(i), i);
			tr.appendChild(td);
			
			int step = StringUtils.hasText(td.getColspan()) ? new Integer(td.getColspan()) : 1;
			i = i + step - 1;
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
	
	private void appCellStyle(Td td, Cell c, Row r, int colIndex, Div div) {
		
		String textCssStyle = "";
		String fontCssStyle = "";
		String htmlStyle = "";
		if (c != null) {
			CellFormatHelper cfh = new CellFormatHelper((Worksheet) sheet, c.getRowIndex(), c.getColumnIndex(), mmhelper);
			htmlStyle = cfh.getHtmlStyle();
			textCssStyle = BookHelper.getTextCSSStyle((Book) wb, c);
			fontCssStyle = BookHelper.getFontCSSStyle(c, BookHelper.getFont(c));
			
			td.setColspan(getCellColspan(c) + "");
			
			div.setStyle(cfh.getInnerHtmlStyle());
		}
		
		td.setStyle(htmlStyle + textCssStyle + fontCssStyle);
		
	}
	
	private Td renderTd(Row r, Cell c, int colIndex) {
		
		Td td = new Td();
		
		int height = Utils.getRowHeightInPx((Worksheet) sheet, r);
		int width = Utils.getColumnWidthInPx((Worksheet) sheet, colIndex);
		Div div = new Div();
		appCellStyle(td, c, r, colIndex, div);
		if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK || (c.getCellType() == Cell.CELL_TYPE_STRING && !StringUtils.hasText(c.getStringCellValue()))) {
			div.appendText(BLANK);
			div.setStyle("height: " + height + "px; width: " + width + "px; font-size: 0");
			td.appendChild(div);
		} else {
//			Range range = Ranges.range((Worksheet) c.getSheet(), c.getRowIndex(), c.getColumnIndex());
			div.appendText(div.getStyle().contains(ConditionalFormattingHelper.HIDDENTEXT4ICONSET) ? BLANK : BookHelper.getCellText(c));
			td.appendChild(div);
		}
		
		return td;
	}
	
}
