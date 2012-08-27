package nu.com.rill.analysis.report.excel.pivottable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nu.com.rill.analysis.report.excel.BookDecorator.RefreshDataSourceBookDecorator;

import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.PivotTable;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFPivotTable;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

public class BookPivottableDecorator extends RefreshDataSourceBookDecorator<PivotTable> {

	@Override
	protected Map<String, PivotTable> obtainRefreshTarget(XSSFSheetImpl sheet) {
		
		List<PivotTable> pivotTables = sheet.getPivotTables();
		Map<String, PivotTable> result = new LinkedHashMap<String, PivotTable>();
		for (PivotTable pt : pivotTables) {
			result.put(pt.getName(), pt);
		}
		return result;
	}

	@Override
	protected void doRefresh(Workbook wb, ArrayEval arrayEval,
			Sheet cellRangeAddressSheet, Sheet refreshTargetSheet, PivotTable t) {
		
		this.pivotTableChange(wb, arrayEval, cellRangeAddressSheet, refreshTargetSheet, t);
	}

	private void pivotTableChange(Workbook wb, ArrayEval arrayEval,
			Sheet cellRangeAddressSheet, Sheet pivotTableSheet,
			PivotTable pt) {
		
		// Refresh pivot table
		StringBuilder leftTopCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn())).append(arrayEval.getFirstRow() + 1);
		StringBuilder rightBottomCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(arrayEval.getLastRow() + 1);
		AreaReference ar = new AreaReference(cellRangeAddressSheet.getSheetName() + "!" + leftTopCell + ":" + rightBottomCell);
		((XSSFPivotTable) pt).setSheetSource(ar);
		
	}
	
}
