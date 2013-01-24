package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STConditionalFormattingOperator;
import org.zkoss.poi.ss.usermodel.ConditionalFormattingRule;
import org.zkoss.poi.xssf.model.StylesTable;

public class XSSFConditionalFormattingRule
  implements ConditionalFormattingRule
{
  private final CTCfRule _cfRule;
  private XSSFSheet _sh;

  XSSFConditionalFormattingRule(XSSFSheet sh)
  {
    this._cfRule = CTCfRule.Factory.newInstance();
    this._sh = sh;
  }

  XSSFConditionalFormattingRule(XSSFSheet sh, CTCfRule cfRule) {
    this._cfRule = cfRule;
    this._sh = sh;
  }

  public CTCfRule getCTCfRule() {
    return this._cfRule;
  }

  public CTDxf getDxf(boolean create) {
    StylesTable styles = this._sh.getWorkbook().getStylesSource();
    CTDxf dxf = null;
    if ((styles._getDXfsSize() > 0) && (this._cfRule.isSetDxfId())) {
      int dxfId = (int)this._cfRule.getDxfId();
      dxf = styles.getDxfAt(dxfId);
    }
    if ((create) && (dxf == null)) {
      dxf = CTDxf.Factory.newInstance();
      int dxfId = styles.putDxf(dxf);
      this._cfRule.setDxfId(dxfId - 1);
    }
    return dxf;
  }

  public XSSFBorderFormatting createBorderFormatting()
  {
    CTDxf dxf = getDxf(true);
    CTBorder border;
    if (!dxf.isSetBorder())
      border = dxf.addNewBorder();
    else {
      border = dxf.getBorder();
    }

    return new XSSFBorderFormatting(border);
  }

  public XSSFBorderFormatting getBorderFormatting()
  {
    CTDxf dxf = getDxf(false);
    if ((dxf == null) || (!dxf.isSetBorder())) return null;

    return new XSSFBorderFormatting(dxf.getBorder());
  }

  public XSSFFontFormatting createFontFormatting()
  {
    CTDxf dxf = getDxf(true);
    CTFont font;
    if (!dxf.isSetFont())
      font = dxf.addNewFont();
    else {
      font = dxf.getFont();
    }

    return new XSSFFontFormatting(font);
  }

  public XSSFFontFormatting getFontFormatting()
  {
    CTDxf dxf = getDxf(false);
    if ((dxf == null) || (!dxf.isSetFont())) return null;

    return new XSSFFontFormatting(dxf.getFont());
  }

  public XSSFPatternFormatting createPatternFormatting()
  {
    CTDxf dxf = getDxf(true);
    CTFill fill;
    if (!dxf.isSetFill())
      fill = dxf.addNewFill();
    else {
      fill = dxf.getFill();
    }

    return new XSSFPatternFormatting(fill);
  }

  public XSSFPatternFormatting getPatternFormatting()
  {
    CTDxf dxf = getDxf(false);
    if ((dxf == null) || (!dxf.isSetFill())) return null;

    return new XSSFPatternFormatting(dxf.getFill());
  }

  public byte getConditionType()
  {
    switch (this._cfRule.getType().intValue()) { case 1:
      return 2;
    case 2:
      return 1;
    }
    return 0;
  }

  public byte getComparisonOperation()
  {
    STConditionalFormattingOperator.Enum op = this._cfRule.getOperator();
    if (op == null) return 0;

    switch (op.intValue()) { case 1:
      return 6;
    case 2:
      return 8;
    case 6:
      return 5;
    case 5:
      return 7;
    case 3:
      return 3;
    case 4:
      return 4;
    case 7:
      return 1;
    case 8:
      return 2;
    }
    return 0;
  }

  public String getFormula1()
  {
    return this._cfRule.sizeOfFormulaArray() > 0 ? this._cfRule.getFormulaArray(0) : null;
  }

  public String getFormula2()
  {
    return this._cfRule.sizeOfFormulaArray() == 2 ? this._cfRule.getFormulaArray(1) : null;
  }
}