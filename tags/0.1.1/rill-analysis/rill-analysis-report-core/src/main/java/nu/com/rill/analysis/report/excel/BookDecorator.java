package nu.com.rill.analysis.report.excel;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.xssf.usermodel.XSSFName;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

public interface BookDecorator {

	void process(Book book);
	
	
	public abstract class RefreshDataSourceBookDecorator<T> implements BookDecorator {
		
		protected final Log LOGGER = LogFactory.getLog(this.getClass());
		
		protected final Object[] findNamedSourceData(Book book, String chartName) {
			
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
		
		protected abstract Map<String, T> obtainRefreshTarget(XSSFSheetImpl sheet);
		protected abstract void doRefresh(Workbook wb, ArrayEval arrayEval,
				Sheet cellRangeAddressSheet, Sheet refreshTargetSheet,
				T t);
		
		public final void process(Book book) {
			
			int sheetNums = book.getNumberOfSheets();
			for (int i = 0; i < sheetNums; i++) {
				XSSFSheetImpl sheet = (XSSFSheetImpl) book.getSheetAt(i);
				// Find pivot table
				Map<String, T> refreshTargets = obtainRefreshTarget(sheet);
				if (CollectionUtils.isEmpty(refreshTargets)) {
					LOGGER.debug("Sheet do not contain any refresh target. " + sheet);
					continue;
				}
				
				// Handle pivot table one by one
				for(Entry<String, T> entry : refreshTargets.entrySet()) {
					// source data
					String ptName = entry.getKey();
					
					Object[] retrunValue = findNamedSourceData(book, ptName);
					int nameSheetIndex = (Integer) retrunValue[0];
					ArrayEval arrayEval = (ArrayEval) retrunValue[1];
					if (nameSheetIndex < 0) {
						LOGGER.debug("Can not find pivot table source XSSFName. " + ptName);
						break;
					}
					
					doRefresh(book, arrayEval, book.getSheetAt(nameSheetIndex), sheet, entry.getValue());
				}
			}
		}
	}
}
