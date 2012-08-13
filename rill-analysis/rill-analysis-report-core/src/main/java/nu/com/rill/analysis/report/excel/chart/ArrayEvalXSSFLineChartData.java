package nu.com.rill.analysis.report.excel.chart;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.ss.usermodel.charts.DataSources;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.zkoss.poi.xssf.usermodel.charts.XSSFLineChartData;

public class ArrayEvalXSSFLineChartData extends XSSFLineChartData {
	
	public ArrayEvalXSSFLineChartData(Sheet sheet, ArrayEval arrayEval) {
		super();
		
		StringBuilder catStartCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn() + 1)).append(arrayEval.getFirstRow() + 1);
		StringBuilder catEndCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(arrayEval.getFirstRow() + 1);
		ChartDataSource<String> cats = DataSources.fromStringCellRange(sheet, CellRangeAddress.valueOf(catStartCell + ":" + catEndCell));
		
		for (int j = 1; j < arrayEval.getHeight(); ++j) {
			ChartTextSource title = DataSources.fromString(((StringEval) arrayEval.getValue(j, 0)).getStringValue());
			StringBuilder valStartCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn() + 1)).append(j + 1 + arrayEval.getFirstRow());
			StringBuilder valEndCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(j + 1 + arrayEval.getFirstRow());
			ChartDataSource<Number> vals = DataSources.fromNumericCellRange(sheet, CellRangeAddress.valueOf(valStartCell + ":" + valEndCell));
	    	addSerie(title, cats, vals);
		}
		
	}

	public void fillChart(Chart chart, ChartAxis... axis) {
		
        CTPlotArea plotArea = ((XSSFChart) chart).getCTChart().getPlotArea();
        for (int i = 0; i < plotArea.getLineChartList().size(); i++) {
        	plotArea.removeLineChart(i);
        }
        
        super.fillChart(chart, axis);
	}
	
}
