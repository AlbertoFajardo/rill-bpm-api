package nu.com.rill.analysis.report.excel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfvo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.springframework.util.Assert;
import org.zkoss.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.ConditionalFormatting;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.ui.impl.Utils;

public class ConditionalFormattingHelper {
	
	private static final Log LOGGER = LogFactory.getLog(ConditionalFormattingHelper.class);
	
	public static final String HIDDENTEXT4ICONSET = "HiddenText4IconSet";
	
	private static void processIconSet(Map<String, String> result, XSSFConditionalFormattingRule rule, Cell cell) {
		
		CTCfRule ctRule = rule.getCTCfRule();
		if (!ctRule.isSetIconSet() && ctRule.getIconSet().xgetIconSet().getStringValue().equals("3Arrows")) {
			return;
		}
		
		result.put("HiddenText4IconSet", "true");
		CTCfvo vo = ctRule.getIconSet().getCfvoList().get(0);
		result.put("background-repeat", "no-repeat");
		int width = Utils.getColumnWidthInPx((Worksheet) cell.getSheet(), cell.getColumnIndex());
		int height = Utils.getRowHeightInPx((Worksheet) cell.getSheet(), cell.getRow());
		result.put("background-position", "0px " + (height -10) / 2 + "px");
		String prefix = "";
		String path = "";
		try {
			prefix = Executions.getCurrent().getContextPath() + "/images";
			result.put("padding-left", (width * 4) + "px");
		} catch (Exception e) {
			// Ignore
			prefix = ".";
		}
		if (new Double(vo.getVal()).compareTo(cell.getNumericCellValue()) < 0) {
			// Up
			path = prefix + "/arrow-up.png";
		}
		if (new Double(vo.getVal()).compareTo(cell.getNumericCellValue()) > 0) {
			// Down
			path = prefix + "/arrow-down.png";
		}
		result.put("background-image", "url('" + path + "')");
		
	}
	
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
					
					for (CellRangeAddress address : cf.getFormattingRanges()) {
						boolean isInRange = address.isInRange(cell.getRowIndex(), cell.getColumnIndex());
						if (isInRange) {
							CTDxf dxf = rule.getDxf(false);
							if (dxf != null) {
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
								if (ComparisonOperator.LT == rule.getComparisonOperation() && 
										new Double(rule.getFormula1()).compareTo(cell.getNumericCellValue()) > 0) {
									return result;
								}
								if (ComparisonOperator.GT == rule.getComparisonOperation() && 
										new Double(rule.getFormula1()).compareTo(cell.getNumericCellValue()) < 0) {
									return result;
								}
							} else {
								// iconSet processing
								processIconSet(result, rule, cell);
								
								return result;
							}
						}
					}
				}
			} catch (Exception e) {
				// Ignore
				LOGGER.warn(e);
			}
		}
		
		return Collections.emptyMap();
	}
}
