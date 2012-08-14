package nu.com.rill.analysis.report.excel.chart;

import nu.com.rill.analysis.report.excel.BookDecorator;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.zkoss.poi.xssf.usermodel.XSSFDrawing;
import org.zkoss.poi.xssf.usermodel.XSSFName;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

@BookDecorator
public class BookChartDecorator {

	private static final Log LOGGER = LogFactory.getLog(BookChartDecorator.class);
	
	public void process(Book book) {
		
		for (int i = 0; i < book.getNumberOfSheets(); i++) {
			XSSFSheetImpl sheet = (XSSFSheetImpl) book.getSheetAt(i);
			// Find chart
			XSSFDrawing drawing = sheet.getDrawingPatriarch();
			if (drawing == null) {
				LOGGER.debug("Sheet do not contain any drawing. " + sheet);
				continue;
			}
			
			// Handle chart one by one
			for(XSSFChart chart : drawing.getCharts()) {
				switch (chart.getChartType()) {
				case Area3D:
//					data = fillCategoryData(new XSSFArea3DChartData());
					break;
				case Area:
//					data = fillCategoryData(new XSSFAreaChartData());
					break;
				case Bar3D:
//					data = fillCategoryData(new XSSFBar3DChartData());
					//((XSSFBar3DChartData) data).setGrouping(ChartGrouping.STANDARD);
					break;
				case Column3D:
//					data = fillCategoryData(new XSSFColumn3DChartData());
					//((XSSFBar3DChartData) data).setGrouping(ChartGrouping.STANDARD);
					break;
				case Bar:
//					data = fillCategoryData(new XSSFBarChartData());
					//((XSSFBarChartData) data).setGrouping(ChartGrouping.STANDARD);
					break;
				case Column:
//					data = fillCategoryData(new XSSFColumnChartData());
					//((XSSFBarChartData) data).setGrouping(ChartGrouping.STANDARD);
					break;
				case Bubble:
					throw new UnsupportedOperationException();
				case Doughnut:
//					data = fillCategoryData(new XSSFDoughnutChartData());
					break;
				case Line3D:
//					data = fillCategoryData(new XSSFLine3DChartData());
					break;
				case Line:
					// source data
					String chartName = drawing.getCTDrawing().getTwoCellAnchorList().get(0).getGraphicFrame().getNvGraphicFramePr().getCNvPr().getName();
					ArrayEval arrayEval = null;
					int nameSheetIndex = -1;
					for (int j = 0; j < ((XSSFWorkbook) book).getNumberOfNames(); j++) {
						XSSFName name = ((XSSFWorkbook) book).getNameAt(j);
						if (name.getNameName().equals(chartName + "DataSource")) {
							arrayEval = (ArrayEval) book.getCreationHelper().createFormulaEvaluator().evaluateFormulaValueEval(name.getSheetIndex(), name.getRefersToFormula(), false);
							nameSheetIndex = name.getSheetIndex();
						}
					}
					if (arrayEval == null || nameSheetIndex < 0) {
						LOGGER.debug("Can not find chart source XSSFName. " + chartName);
						break;
					}
					
//					// Clone chart data
					ArrayEvalXSSFLineChartData lineChartData = new ArrayEvalXSSFLineChartData(book.getSheetAt(nameSheetIndex), arrayEval);
					chart.plot(lineChartData, new ChartAxis[0]);
//					lineChartData.fillChart(chart, new ChartAxis[0]);
//					ClientAnchor anchor = new XSSFClientAnchor(0, 0, pxToEmu(200), pxToEmu(200), arrayEval.getFirstColumn(), arrayEval.getFirstRow(), arrayEval.getLastColumn(), arrayEval.getLastRow());
//					Ranges.range(sheet).addChart(anchor, lineChartData, ChartType.Line, ChartGrouping.STANDARD, LegendPosition.RIGHT);
					break;
				case Pie3D:
//					data = fillCategoryData(new XSSFPie3DChartData());
					break;
				case OfPie:
//					break;
					throw new UnsupportedOperationException();
				case Pie:
//					data = fillCategoryData(new XSSFPieChartData());
					break;
				case Radar:
					throw new NotImplementedException("Radar data not impl");
				case Scatter:
					// FIXME: MENGRAN. Comment temporary
//					data = fillXYData(new XSSFScatChartData());
					break;
				case Stock:
//					data = fillCategoryData(new XSSFStockChartData());
//					break;
					throw new UnsupportedOperationException();
				case Surface3D:
//					break;
					throw new UnsupportedOperationException();
				case Surface:
//					break;
					throw new UnsupportedOperationException();
				}
				
			}
		}
	}
	
}
