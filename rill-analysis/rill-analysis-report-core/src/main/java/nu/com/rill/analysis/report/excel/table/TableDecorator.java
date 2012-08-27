package nu.com.rill.analysis.report.excel.table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nu.com.rill.analysis.report.excel.BookDecorator;
import nu.com.rill.analysis.report.excel.BookDecorator.RefreshDataSourceBookDecorator;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFTable;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

public class TableDecorator extends RefreshDataSourceBookDecorator<XSSFTable> implements BookDecorator {
	
	@Override
	protected Map<String, XSSFTable> obtainRefreshTarget(XSSFSheetImpl sheet) {
		
		List<XSSFTable> tables = sheet.getTables();
		Map<String, XSSFTable> result = new LinkedHashMap<String, XSSFTable>();
		for (XSSFTable t : tables) {
			result.put(t.getName(), t);
		}
		
		return result;
	}

	@Override
	protected void doRefresh(Workbook wb, ArrayEval arrayEval,
			Sheet cellRangeAddressSheet, Sheet refreshTargetSheet, XSSFTable t) {
		
		// Refresh table
		StringBuilder leftTopCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn())).append(arrayEval.getFirstRow() + 1);
		StringBuilder rightBottomCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(arrayEval.getLastRow() + 1);
//		AreaReference ar = new AreaReference(cellRangeAddressSheet.getSheetName() + "!" + leftTopCell + ":" + rightBottomCell);
		
		CTTable ctTable = t.getCTTable();
	}

}
