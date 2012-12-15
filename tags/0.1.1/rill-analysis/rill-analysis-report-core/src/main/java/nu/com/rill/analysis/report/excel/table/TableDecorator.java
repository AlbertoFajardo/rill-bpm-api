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
import org.zkoss.zss.model.Range;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.XSSFSheetImpl;
import org.zkoss.zss.ui.Rect;
import org.zkoss.zss.ui.impl.Utils;

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
//		StringBuilder leftTopCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getFirstColumn())).append(arrayEval.getFirstRow() + 1);
//		StringBuilder rightBottomCell = new StringBuilder(CellReference.convertNumToColString(arrayEval.getLastColumn())).append(arrayEval.getLastRow() + 1);
//		AreaReference ar = new AreaReference(cellRangeAddressSheet.getSheetName() + "!" + leftTopCell + ":" + rightBottomCell);
//		System.out.println(leftTopCell + " " + rightBottomCell);
		
		CTTable ctTable = t.getCTTable();
		AreaReference ar = new AreaReference(refreshTargetSheet.getSheetName() + "!" + ctTable.getRef());
		// Demo data row(Below header row)
		Rect srcRect = new Rect(ar.getFirstCell().getCol(), ar.getFirstCell().getRow() + new Long(ctTable.getHeaderRowCount()).intValue(), 
				ar.getLastCell().getCol(),
				ar.getLastCell().getRow());
		
		if (arrayEval.getHeight() > 2) {
			Rect descRect = (Rect) srcRect.cloneSelf();
			descRect.setTop(srcRect.getTop() + 1);
			descRect.setBottom(srcRect.getBottom() + arrayEval.getHeight() - 2);
			Utils.pasteSpecial((Worksheet) refreshTargetSheet, srcRect, (Worksheet) refreshTargetSheet, 
					descRect.getTop(), descRect.getLeft(), descRect.getBottom(), descRect.getRight(), 
					Range.PASTE_ALL, Range.PASTEOP_NONE, false, false);
			
			StringBuilder leftTopCell = new StringBuilder(CellReference.convertNumToColString(ar.getFirstCell().getCol())).append(ar.getFirstCell().getRow() + 1);
			StringBuilder rightBottomCell = new StringBuilder(CellReference.convertNumToColString(ar.getLastCell().getCol())).append(descRect.getBottom() + 1);

			ctTable.setRef(leftTopCell + ":" + rightBottomCell);
		}
		
	}

}
