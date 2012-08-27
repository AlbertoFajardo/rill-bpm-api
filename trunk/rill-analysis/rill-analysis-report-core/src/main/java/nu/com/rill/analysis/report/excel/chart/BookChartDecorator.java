package nu.com.rill.analysis.report.excel.chart;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import nu.com.rill.analysis.report.excel.BookDecorator.RefreshDataSourceBookDecorator;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STGrouping;
import org.springframework.util.ReflectionUtils;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.DataSources;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.zkoss.poi.xssf.usermodel.XSSFDrawing;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

public class BookChartDecorator extends RefreshDataSourceBookDecorator<XSSFChart> {

	@Override
	protected Map<String, XSSFChart> obtainRefreshTarget(XSSFSheetImpl sheet) {
		
		Map<String, XSSFChart> result = new LinkedHashMap<String, XSSFChart>();
		XSSFDrawing drawing = sheet.getDrawingPatriarch();
		if (drawing == null) {
			LOGGER.debug("Sheet do not contain any drawing. " + sheet);
			return result;
		}
		
		// Handle chart one by one
		for(XSSFChart chart : drawing.getCharts()) {
			// source data
			String chartName = drawing.getCTDrawing().getTwoCellAnchorList().get(0).getGraphicFrame().getNvGraphicFramePr().getCNvPr().getName();
			result.put(chartName, chart);
		}
		return result;
	}

	@Override
	protected void doRefresh(Workbook wb, ArrayEval arrayEval,
			Sheet cellRangeAddressSheet, Sheet refreshTargetSheet, XSSFChart t) {
		
		switch (t.getChartType()) {
			case Line:
				CTLineChart c = t.getCTChart().getPlotArea().getLineChartArray(0);
				c.getGrouping().setVal(STGrouping.STANDARD);
				
				int serSize = c.getSerList().size();
				StringBuilder catStartCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn() + 1)).append(arrayEval.getFirstRow() + 1);
				StringBuilder catEndCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(arrayEval.getFirstRow() + 1);
				ChartDataSource<String> cats = DataSources.fromStringCellRange(cellRangeAddressSheet, CellRangeAddress.valueOf(catStartCell + ":" + catEndCell));
				
				for (int i = 1; i < arrayEval.getHeight(); i++) {
					CellReference tx = new CellReference(cellRangeAddressSheet.getSheetName(), arrayEval.getFirstRow() + i, arrayEval.getFirstColumn(), false, false);
					StringBuilder valStartCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn() + 1)).append(i + 1 + arrayEval.getFirstRow());
					StringBuilder valEndCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(i + 1 + arrayEval.getFirstRow());
					ChartDataSource<Number> vals = DataSources.fromNumericCellRange(cellRangeAddressSheet, CellRangeAddress.valueOf(valStartCell + ":" + valEndCell));
					
					if ((i - 1) < serSize) {
						c.getSerList().get(i - 1).getTx().getStrRef().setF(tx.formatAsString());
						c.getSerList().get(i - 1).getTx().getStrRef().unsetStrCache();
						c.getSerList().get(i - 1).getCat().unsetNumRef();
						c.getSerList().get(i - 1).getCat().addNewStrRef().setF(cats.getFormulaString());
						c.getSerList().get(i - 1).getVal().getNumRef().setF(vals.getFormulaString());
						c.getSerList().get(i - 1).getVal().getNumRef().unsetNumCache();
					} else {
						CTLineSer ser = c.addNewSer();
						CTSerTx newTx = ser.addNewTx();
						newTx.addNewStrRef().setF(tx.formatAsString());
						ser.addNewCat().addNewNumRef().setF(cats.getFormulaString());
						ser.addNewVal().addNewNumRef().setF(vals.getFormulaString());
					}
				}
			break;
		}
		
		Method commit = ReflectionUtils.findMethod(t.getClass(), "commit");
		commit.setAccessible(true);
		ReflectionUtils.invokeMethod(commit, t);
	}

}
