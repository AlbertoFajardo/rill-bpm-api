package nu.com.rill.analysis.report.excel.pivottable;

import java.util.List;

import nu.com.rill.analysis.report.excel.BookDecorator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.PivotTable;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFName;
import org.zkoss.poi.xssf.usermodel.XSSFPivotTable;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

@BookDecorator
public class BookPivottableDecorator {

	private static final Log LOGGER = LogFactory.getLog(BookPivottableDecorator.class);
	
	public void process(Book book) {
		
		int sheetNums = book.getNumberOfSheets();
		for (int i = 0; i < sheetNums; i++) {
			XSSFSheetImpl sheet = (XSSFSheetImpl) book.getSheetAt(i);
			// Find pivot table
			List<PivotTable> pivotTables = sheet.getPivotTables();
			if (CollectionUtils.isEmpty(pivotTables)) {
				LOGGER.debug("Sheet do not contain any pivottable. " + sheet);
				continue;
			}
			
			// Handle pivot table one by one
			int pivotTableSize = pivotTables.size();
			for(int j = 0; j < pivotTableSize; j++) {
				PivotTable pt = pivotTables.get(j);
				// source data
				String ptName = pt.getName();
				
				Object[] retrunValue = findNamedSourceData(book, ptName);
				int nameSheetIndex = (Integer) retrunValue[0];
				ArrayEval arrayEval = (ArrayEval) retrunValue[1];
				if (nameSheetIndex < 0) {
					LOGGER.debug("Can not find pivot table source XSSFName. " + ptName);
					break;
				}
				
		        pivotTableChange(book, arrayEval, book.getSheetAt(nameSheetIndex), sheet, pt);
				
			}
		}
	}
	
	// FIXME: MENGRAN. abstract to common class
	private Object[] findNamedSourceData(Book book, String chartName) {
		
		int nameSheetIndex = -1;
		ArrayEval arrayEval = null;
		for (int j = 0; j < ((XSSFWorkbook) book).getNumberOfNames(); j++) {
			XSSFName name = ((XSSFWorkbook) book).getNameAt(j);
			if (name.getNameName().equals(chartName + "DataSource")) {
				arrayEval = (ArrayEval) book.getCreationHelper().createFormulaEvaluator().evaluateFormulaValueEval(name.getSheetIndex(), name.getRefersToFormula(), false);
				nameSheetIndex = name.getSheetIndex();
			}
		}
		
		return new Object[] {new Integer(nameSheetIndex), arrayEval};
	}
	
	private void pivotTableChange(Workbook wb, ArrayEval arrayEval,
			Sheet cellRangeAddressSheet, Sheet pivotTableSheet,
			PivotTable pt) {
		
		// Refresh pivot table
		StringBuilder leftTopCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn())).append(arrayEval.getFirstRow() + 1);
		StringBuilder rightBottomCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(arrayEval.getLastRow() + 1);
		AreaReference ar = new AreaReference(cellRangeAddressSheet.getSheetName() + "!" + leftTopCell + ":" + rightBottomCell);
		((XSSFPivotTable) pt).setSheetSource(ar);
//		PivotTable newPt = XSSFPivotTableHelpers.instance.getHelper().createPivotTable(new CellReference(rightBottomCell.toString()), 
//				"new" + pt.getName(), pt.getPivotCache(), pivotTableSheet);
		
	}
	
}
