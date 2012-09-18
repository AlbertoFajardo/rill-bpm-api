package nu.com.rill.analysis.report.excel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.springframework.util.Assert;
import org.zkoss.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.ConditionalFormatting;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.xssf.usermodel.XSSFConditionalFormattingRule;

public class ConditionalFormattingHelper {

	public static Map<String, String> getCFInWorkbook(Sheet sheet, Cell cell) {
		
		Assert.notNull(sheet);
		Map<String, String> result = new HashMap<String, String>();
		if (cell == null) {
			return result;
		}
		
		int numCF = sheet.getSheetConditionalFormatting().getNumConditionalFormattings();
		if (numCF <= 0) {
			return result;
		}
		
		for (int j = 0; j < numCF; j++) {
			try {
				ConditionalFormatting cf = sheet.getSheetConditionalFormatting().getConditionalFormattingAt(j);
				for (int r = 0; r < cf.getNumberOfRules(); r++) {
					XSSFConditionalFormattingRule rule = (XSSFConditionalFormattingRule) cf.getRule(r);
					CTDxf dxf = rule.getDxf(false);
					if (dxf.getFont() != null) {
						CTColor color = dxf.getFont().getColorArray(0);
						String fontRgb = color.xgetRgb().getStringValue();
						result.put("color", "#" + fontRgb.substring(2));
					}
					if (dxf.getFill() != null) {
						String bgRgb = null;
						bgRgb = dxf.getFill().getPatternFill().getBgColor().xgetRgb().getStringValue();
						result.put("background-color", "#" + bgRgb.substring(2));
					}
					for (CellRangeAddress address : cf.getFormattingRanges()) {
						boolean isInRange = address.isInRange(cell.getRowIndex(), cell.getColumnIndex());
						if (isInRange) {
							if (ComparisonOperator.LT == rule.getComparisonOperation() && 
									new Double(rule.getFormula1()).compareTo(cell.getNumericCellValue()) > 0) {
								return result;
							}
							if (ComparisonOperator.GT == rule.getComparisonOperation() && 
									new Double(rule.getFormula1()).compareTo(cell.getNumericCellValue()) < 0) {
								return result;
							}
						}
					}
				}
			} catch (Exception e) {
				// Ignore
			}
		}
		
		return Collections.emptyMap();
	}
}
