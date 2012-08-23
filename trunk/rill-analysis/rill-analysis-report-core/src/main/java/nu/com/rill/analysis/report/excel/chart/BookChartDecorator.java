package nu.com.rill.analysis.report.excel.chart;

import java.lang.reflect.Field;
import java.util.ArrayList;

import nu.com.rill.analysis.report.excel.BookDecorator;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.poi.ss.usermodel.Drawing;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.usermodel.charts.AxisCrosses;
import org.zkoss.poi.ss.usermodel.charts.AxisPosition;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartLegend;
import org.zkoss.poi.ss.usermodel.charts.DataSources;
import org.zkoss.poi.ss.usermodel.charts.LegendPosition;
import org.zkoss.poi.ss.usermodel.charts.ScatterChartData;
import org.zkoss.poi.ss.usermodel.charts.ValueAxis;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.zkoss.poi.xssf.usermodel.XSSFChartX;
import org.zkoss.poi.xssf.usermodel.XSSFClientAnchor;
import org.zkoss.poi.xssf.usermodel.XSSFDrawing;
import org.zkoss.poi.xssf.usermodel.XSSFName;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.poi.xssf.usermodel.charts.XSSFChartAxis;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

@BookDecorator
public class BookChartDecorator {

	private static final Log LOGGER = LogFactory.getLog(BookChartDecorator.class);
	
	public void process(Book book) {
		
		int sheetNums = book.getNumberOfSheets();
		for (int i = 0; i < sheetNums; i++) {
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
					int nameSheetIndex = findNamedChartSourceData(book, chartName, arrayEval);
					if (nameSheetIndex < 0) {
						LOGGER.debug("Can not find chart source XSSFName. " + chartName);
						break;
					}
					
			        lineChartChange(book, arrayEval, book.getSheetAt(nameSheetIndex), sheet, chart);
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
	
	private int findNamedChartSourceData(Book book, String chartName, ArrayEval arrayEval) {
		
		int nameSheetIndex = -1;
		for (int j = 0; j < ((XSSFWorkbook) book).getNumberOfNames(); j++) {
			XSSFName name = ((XSSFWorkbook) book).getNameAt(j);
			if (name.getNameName().equals(chartName + "DataSource")) {
				arrayEval = (ArrayEval) book.getCreationHelper().createFormulaEvaluator().evaluateFormulaValueEval(name.getSheetIndex(), name.getRefersToFormula(), false);
				nameSheetIndex = name.getSheetIndex();
			}
		}
		
		return nameSheetIndex;
	}
	
	private void lineChartChange(Workbook wb, ArrayEval arrayEval,
			Sheet cellRangeAddressSheet, Sheet chartSheet,
			XSSFChart originalChart) {
		
		// Initialize axis if need
		final XSSFChart useChart = originalChart;
		ReflectionUtils.doWithFields(useChart.getClass(), new FieldCallback() {
			
			@Override
			public void doWith(Field field) throws IllegalArgumentException,
					IllegalAccessException {
				field.setAccessible(true);
				if (field.get(useChart) == null) {
					field.set(useChart, new ArrayList<XSSFChartAxis>());
				}
			}
		}, new FieldFilter() {
			
			@Override
			public boolean matches(Field field) {
				return field.getName().equals("axis");
			}
		});
		
		Sheet sheet = chartSheet;

		Drawing drawing = sheet.createDrawingPatriarch();

		drawing.deleteChart(new XSSFChartX((XSSFDrawing) drawing,
				(XSSFClientAnchor) originalChart.getPreferredSize(), "",
				originalChart.getChartId()));

		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 15);

		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();
		legend.setPosition(LegendPosition.TOP_RIGHT);

		ScatterChartData data = chart.getChartDataFactory()
				.createScatterChartData();

		ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(
				AxisPosition.BOTTOM);
		ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(
				AxisPosition.LEFT);
		leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

		StringBuilder catStartCell = new StringBuilder(
				CellReference.convertNumToColString(arrayEval.getFirstColumn() + 1))
				.append(arrayEval.getFirstRow() + 1);
		StringBuilder catEndCell = new StringBuilder(
				CellReference.convertNumToColString(arrayEval.getLastColumn()))
				.append(arrayEval.getFirstRow() + 1);
		ChartDataSource<String> cats = DataSources.fromStringCellRange(
				cellRangeAddressSheet,
				CellRangeAddress.valueOf(catStartCell + ":" + catEndCell));

		for (int j = 1; j < arrayEval.getHeight(); ++j) {
//			ChartTextSource title = DataSources.fromString(((StringEval) arrayEval.getValue(j, 0)).getStringValue());
			StringBuilder valStartCell = new StringBuilder(
					CellReference.convertNumToColString(arrayEval
							.getFirstColumn() + 1)).append(j + 1
					+ arrayEval.getFirstRow());
			StringBuilder valEndCell = new StringBuilder(
					CellReference.convertNumToColString(arrayEval
							.getLastColumn())).append(j + 1
					+ arrayEval.getFirstRow());
			ChartDataSource<Number> vals = DataSources.fromNumericCellRange(
					cellRangeAddressSheet,
					CellRangeAddress.valueOf(valStartCell + ":" + valEndCell));
			data.addSerie(cats, vals);
		}

		chart.plot(data, new ChartAxis[] { leftAxis, bottomAxis });

	}
	
}
