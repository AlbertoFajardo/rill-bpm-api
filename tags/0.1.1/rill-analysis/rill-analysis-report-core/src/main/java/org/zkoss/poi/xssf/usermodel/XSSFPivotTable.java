package org.zkoss.poi.xssf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColItems;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTI;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTItems;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTLocation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotTableDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRowFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRowItems;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTX;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.PivotTableDefinitionDocument;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STAxis;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.openxml4j.opc.TargetMode;
import org.zkoss.poi.openxml4j.opc.internal.MemoryPackagePart;
import org.zkoss.poi.ss.usermodel.Calculation;
import org.zkoss.poi.ss.usermodel.DataField;
import org.zkoss.poi.ss.usermodel.PivotCache;
import org.zkoss.poi.ss.usermodel.PivotCache.CacheRecord;
import org.zkoss.poi.ss.usermodel.PivotField;
import org.zkoss.poi.ss.usermodel.PivotField.Item;
import org.zkoss.poi.ss.usermodel.PivotField.Item.Type;
import org.zkoss.poi.ss.usermodel.PivotTable;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.util.ItemInfo;

public class XSSFPivotTable extends POIXMLDocumentPart
  implements PivotTable
{
  private CTPivotTableDefinition _pivotTableDefinition;
  private XSSFPivotCache _pivotCache;
  private LinkedHashMap<String, PivotField> _pivotFields;
  private PackagePart part;

  XSSFPivotTable()
  {
    onDocumentCreate();
  }

  protected void onDocumentCreate() {
    this._pivotTableDefinition = CTPivotTableDefinition.Factory.newInstance();

    this._pivotTableDefinition.setApplyNumberFormats(false);
    this._pivotTableDefinition.setApplyBorderFormats(false);
    this._pivotTableDefinition.setApplyFontFormats(false);
    this._pivotTableDefinition.setApplyPatternFormats(false);
    this._pivotTableDefinition.setApplyAlignmentFormats(false);
    this._pivotTableDefinition.setApplyWidthHeightFormats(true);
    this._pivotTableDefinition.setDataCaption("Values");
    this._pivotTableDefinition.setShowCalcMbrs(false);
    this._pivotTableDefinition.setUseAutoFormatting(true);
    this._pivotTableDefinition.setItemPrintTitles(true);
    this._pivotTableDefinition.setIndent(0L);
    this._pivotTableDefinition.setOutlineData(true);
    this._pivotTableDefinition.setMultipleFieldFilters(false);
  }

  public XSSFPivotTable(PackagePart part, PackageRelationship rel, List<PivotCache> pivotCaches) throws IOException, XmlException {
    InputStream in = part.getInputStream();
    this._pivotTableDefinition = PivotTableDefinitionDocument.Factory.parse(in).getPivotTableDefinition();
    this.part = part;
    
    long cacheId = getCacheId();
    for (PivotCache e : pivotCaches)
      if (e.getCacheId() == cacheId) {
        this._pivotCache = ((XSSFPivotCache)e);
        break;
      }
  }
  
  public void setSheetSource(AreaReference ar) {
	  
	  this._pivotCache.setSheetSource(ar);
	  this.setPivotCache(this._pivotCache);
//	  this._pivotTableDefinition.getLocation().setRef("A4:D7");
	  	// Init pivot fields
		this.initPivotFields();
		  
	  try {
		this.commit();
	} catch (IOException e) {
		// Ignore
	}
  }

  protected void commit() throws IOException
  {
    XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
    xmlOptions.setSaveSyntheticDocumentElement(new QName(CTPivotTableDefinition.type.getName().getNamespaceURI(), "pivotTableDefinition"));
    Map map = new HashMap();
    map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
    xmlOptions.setSaveSuggestedPrefixes(map);

    PackagePart part = getPackagePart() == null ? this.part : getPackagePart();
    clearMemoryPackagePart(part);

    OutputStream out = part.getOutputStream();
    this._pivotTableDefinition.save(out, xmlOptions);
    out.close();
  }

  private void clearMemoryPackagePart(PackagePart part) {
    if ((part instanceof MemoryPackagePart))
      ((MemoryPackagePart)part).clear();
  }

  public CTPivotTableDefinition getPivotTableDefinition()
  {
    return this._pivotTableDefinition;
  }

  private String nextReleationId() {
    int idx = 1;
    String rId = "rId" + idx;
    while (getRelationById(rId) != null) {
      idx++; rId = "rId" + idx;
    }
    return rId;
  }

  public void setPivotCache(PivotCache pivotCache) {
    this._pivotCache = ((XSSFPivotCache)pivotCache);

//    getPackagePart().addRelationship(this._pivotCache.getPackagePart().getPartName(), TargetMode.INTERNAL, XSSFRelation.PIVOT_CACHE_DEFINITION.getRelation());
//    addRelation(nextReleationId(), this._pivotCache);

    this._pivotTableDefinition.setCacheId(pivotCache.getCacheId());

    this._pivotTableDefinition.setUpdatedVersion((short) 1);
    this._pivotTableDefinition.setMinRefreshableVersion(this._pivotCache.getMinRefreshableVersion());
    this._pivotTableDefinition.setCreatedVersion((short) 1);
  }

  public long getCacheId()
  {
    return this._pivotTableDefinition.getCacheId();
  }

  public List<PivotField> getColumnFields()
  {
    CTColFields colFields = this._pivotTableDefinition.getColFields();
    if (colFields == null) {
      return Collections.emptyList();
    }

    List list = new ArrayList();
    List pivotFields = getPivotFields();
    for (CTField f : colFields.getFieldList()) {
      int x = f.getX();
      if (x >= 0) {
        list.add(pivotFields.get(x));
      }
    }
    return list;
  }

  private static CTColFields initColFields(CTPivotTableDefinition pivotTableDefinition) {
    CTColFields ctColFields = pivotTableDefinition.addNewColFields();
    CTRowItems rowItems = pivotTableDefinition.getRowItems();
    if (rowItems == null) {
      rowItems = pivotTableDefinition.addNewRowItems();
      rowItems.addNewI();
      rowItems.setCount(1L);
    }
    return ctColFields;
  }

  public void setColumnField(PivotField field)
  {
    int idx = getPivotFieldIndex(field, getPivotFields());
    if (idx < 0) {
      throw new IllegalArgumentException("Can't find PivotField");
    }

    boolean typeChanged = false;
    PivotField.Type type = field.getType();
    if (type != null) {
      switch (type.ordinal()) {
      case 1:
        typeChanged = true;
        removeRowField(idx, field);
        break;
      case 2:
        typeChanged = true;
        removeDataField(idx, field);
      }
    }
    else {
      typeChanged = true;
    }

    if (typeChanged)
    {
      field.setType(PivotField.Type.COLUMN);

      CTColFields colFields = this._pivotTableDefinition.getColFields();
      if (colFields == null) {
        colFields = initColFields(this._pivotTableDefinition);
      }

      int count = (int)colFields.getCount();
      List dataFields = getDataFields();
      boolean insrtDataCol = false;
      if ((dataFields.size() > 1) && (!getDataOnRows())) {
        insrtDataCol = true;

        int lastIdx = count - 1;
        if (lastIdx >= 0) {
          CTField f = (CTField)colFields.getFieldList().get(lastIdx);
          if (f.getX() == -2) {
            colFields.removeField(lastIdx);
          }
        }
      }

      colFields.addNewField().setX(idx);

      if (insrtDataCol) {
        colFields.addNewField().setX(-2);
      }
      colFields.setCount(colFields.getFieldList().size());
    }
  }

  public void setDataCaption(String caption) {
    this._pivotTableDefinition.setDataCaption(caption);
  }

  public String getDataCaption()
  {
    return this._pivotTableDefinition.getDataCaption();
  }

  public List<DataField> getDataFields()
  {
    CTDataFields dataFields = this._pivotTableDefinition.getDataFields();
    if (dataFields == null) {
      return Collections.emptyList();
    }

    ArrayList list = new ArrayList();
    List pivotFields = getPivotFields();
    List<CTDataField> dataFieldList = dataFields.getDataFieldList();
    for (CTDataField dataField : dataFieldList) {
      PivotField pivotField = (PivotField)pivotFields.get((int)dataField.getFld());
      list.add(new XSSFDataField(dataField, pivotField));
    }
    return list;
  }

  public void setDataField(PivotField field, String name, Calculation subtotal)
  {
    CTDataFields dataFields = this._pivotTableDefinition.getDataFields();
    if (dataFields == null) {
      dataFields = this._pivotTableDefinition.addNewDataFields();
    }
    CTDataField dataField = dataFields.addNewDataField();
    dataField.setName(name);
    int idx = getPivotFieldIndex(field, getPivotFields());
    if (idx < 0) {
      throw new IllegalArgumentException("Can't find PivotField");
    }
//    field.setType(PivotField.Type.DATA);
    dataField.setFld(idx);
    dataField.setSubtotal(XSSFDataField.getSubtotalType(subtotal));

    dataField.setBaseField(0);
    dataField.setBaseItem(0L);
    dataFields.setCount(dataFields.getDataFieldList().size());

    CTRowItems rowItems = this._pivotTableDefinition.getRowItems();
    if (rowItems == null) {
      rowItems = this._pivotTableDefinition.addNewRowItems();
      rowItems.addNewI();
      rowItems.setCount(rowItems.getIList().size());
    }
    CTColItems colItems = this._pivotTableDefinition.getColItems();
    if (colItems == null) {
      colItems = this._pivotTableDefinition.addNewColItems();
      colItems.addNewI();
      colItems.setCount(colItems.getIList().size());
    }
  }

  public void setFirstHeaderRow(int row) {
    getCTLocation().setFirstHeaderRow(row);
  }

  public CTLocation getCTLocation() {
    CTLocation location = this._pivotTableDefinition.getLocation();
    if (location == null) {
      return this._pivotTableDefinition.addNewLocation();
    }
    return location;
  }

  public CTColFields getCTColFields()
  {
    CTColFields colFields = this._pivotTableDefinition.getColFields();
    if (colFields == null) {
      return initColFields(this._pivotTableDefinition);
    }
    return colFields;
  }

  public CTRowFields getCTRowFields()
  {
    CTRowFields rowFields = this._pivotTableDefinition.getRowFields();
    if (rowFields == null) {
      return initRowFields(this._pivotTableDefinition);
    }
    return rowFields;
  }

  public void setFirstData(int row, int col) {
    CTLocation ctLocation = getCTLocation();
    ctLocation.setFirstDataRow(row);
    ctLocation.setFirstDataCol(col);
  }

  public void setLocationRef(AreaReference ref) {
    getCTLocation().setRef(ref.formatAsString());
  }

  public AreaReference getLocationRef() {
    return new AreaReference(this._pivotTableDefinition.getLocation().getRef());
  }

  public CellReference getFirstDataRef() {
    CellReference firstCellRef = getLocationRef().getFirstCell();

    CTLocation l = getCTLocation();
    int col = firstCellRef.getCol() + (int)l.getFirstDataCol();
    int row = firstCellRef.getRow() + (int)l.getFirstDataRow();

    return new CellReference(row, col);
  }

  public void setGrandTotalCaption(String caption)
  {
    this._pivotTableDefinition.setGrandTotalCaption(caption);
  }

  public String getGrandTotalCaption()
  {
    return this._pivotTableDefinition.getGrandTotalCaption();
  }

  public void setName(String name)
  {
    this._pivotTableDefinition.setName(name);
  }

  public String getName()
  {
    return this._pivotTableDefinition.getName();
  }

  public PivotCache getPivotCache()
  {
    return this._pivotCache;
  }

  public PivotField getPivotField(String name)
  {
    List<PivotField> pivotFields = getPivotFields();
    for (PivotField f : pivotFields) {
      String fName = f.getName();
      if (fName.equalsIgnoreCase(name)) {
        return f;
      }
    }
    return null;
  }

  public List<PivotField> getPivotFields()
  {
    if (this._pivotFields == null) {
      initPivotFields();
    }
    return new ArrayList(this._pivotFields.values());
  }

  private void initPivotFields() {
    this._pivotFields = new LinkedHashMap();
    CTPivotFields pivotFields = this._pivotTableDefinition.getPivotFields();
    if (pivotFields == null) {
      return;
    }
    int size = pivotFields.getPivotFieldList().size();
    CTDataFields dataFields = this._pivotTableDefinition.getDataFields();
    int dataFieldsSize = dataFields.sizeOfDataFieldArray();
    Map<String, Calculation> needAddToDataField = new LinkedHashMap<String, Calculation>();
    int rowSize = 0;
    for (int i = 0; i < this._pivotCache.getFields().size(); i++) {
    	if (i < size) {
    		CTPivotField f = pivotFields.getPivotFieldList().get(i);
    		if (STAxis.AXIS_ROW.equals(f.getAxis())) {
    			rowSize++;
        		f.unsetItems();
        		CTItems items = f.addNewItems();
        		for (int s = 0; s < this._pivotCache.getFields().get(i).getSharedItems().size(); s++) {
        			items.addNewItem().setX(s);
        		}
        		items.setCount(this._pivotCache.getFields().get(i).getSharedItems().size());
        	} else {
        		this._pivotTableDefinition.getDataFields().getDataFieldArray(i - rowSize).setName(this._pivotCache.getFields().get(i).getName());
        	}
          XSSFPivotField pf = new XSSFPivotField(f, (PivotCache.CacheField)this._pivotCache.getFields().get(i), this);
          this._pivotFields.put(pf.getName(), pf);
    	} else {
    		// FIXME: Need copy from
    		CTPivotField f = pivotFields.addNewPivotField();
    		f.setDataField(true);
    		f.setShowAll(false);
    		f.setDefaultSubtotal(false);
    		f.setAxis(STAxis.AXIS_VALUES);
    		f.unsetAxis();
          XSSFPivotField pf = new XSSFPivotField(f, (PivotCache.CacheField)this._pivotCache.getFields().get(i), this);
          this._pivotFields.put(pf.getName(), pf);
          needAddToDataField.put(pf.getName(), Calculation.valueOf(dataFields.getDataFieldArray((i-size)%dataFieldsSize).getSubtotal().toString().toUpperCase()));
    	}
    }
    // Need add to data field
    for (Entry<String, Calculation> entry : needAddToDataField.entrySet()) {
    	this.setDataField(this._pivotFields.get(entry.getKey()), entry.getKey(), entry.getValue());
    }
    this._pivotTableDefinition.getPivotFields().setCount(this._pivotFields.size());
    
    // Need re-set row items
    List<List<ItemInfo>> rowItems = new ArrayList<List<ItemInfo>>();
    for (int depth = 0; depth < getRowFields().size(); depth++) {
    	PivotField pf = getRowFields().get(depth);
    	List<ItemInfo> ti = new ArrayList<ItemInfo>();
    	for (int index = 0; index < pf.getItems().size(); index++) {
    		ti.add(new ItemInfo(Type.BLANK, pf.getItems().get(index).getValue(), depth, index));
    	}
    	rowItems.add(ti);
    }
    this.setRowItems(rowItems);
  }

  private int getPivotFieldIndex(PivotField pivotField, List<PivotField> from) {
    int i = 0;
    for (PivotField pf : from) {
      if (pf.equals(pivotField)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  public List<PivotField> getRowFields()
  {
    CTRowFields rowFields = this._pivotTableDefinition.getRowFields();
    if (rowFields == null) {
      return Collections.emptyList();
    }

    List list = new ArrayList();
    List pivotFields = getPivotFields();
    for (CTField f : rowFields.getFieldList()) {
      int idx = f.getX();
      if (idx >= 0) {
        list.add(pivotFields.get(idx));
      }
    }
    return list;
  }

  private static CTRowFields initRowFields(CTPivotTableDefinition pivotTableDefinition) {
    CTRowFields rowFields = pivotTableDefinition.addNewRowFields();
    CTColItems colItems = pivotTableDefinition.getColItems();
    if (colItems == null) {
      colItems = pivotTableDefinition.addNewColItems();
      colItems.addNewI();
      colItems.setCount(1L);
    }
    return rowFields;
  }

  public Sheet getSheet() {
    return (Sheet)getParent();
  }

  private ItemInfo getNonDataTypeItem(List<ItemInfo> items) {
    for (int i = 0; i < items.size(); i++) {
      ItemInfo info = (ItemInfo)items.get(i);
      if ((info != null) && (info.getType() != PivotField.Item.Type.DATA)) {
        return info;
      }
    }
    return null;
  }

  public void setColumnItems(List<List<ItemInfo>> items) {
    CTColItems colItems = this._pivotTableDefinition.getColItems();
    if (colItems != null) {
      this._pivotTableDefinition.unsetColItems();
    }
    colItems = this._pivotTableDefinition.addNewColItems();

    IndexMapper indexMapper = new IndexMapper(getColumnFields());
    DataFieldIndexMapper dataIndexMapper = null;
    List dataFields = getDataFields();
    if ((!getDataOnRows()) && (dataFields.size() > 1)) {
      dataIndexMapper = new DataFieldIndexMapper(dataFields);
    }

    int itemSize = items.size();
    for (int c = 0; c < itemSize; c++)
    {
      List col = (List)items.get(c);
      ItemInfo info = getNonDataTypeItem(col);
      if (info != null) {
        CTI ctI = colItems.addNewI();
        if (info.getDepth() > 0) {
          ctI.setR(info.getDepth());
        }

        PivotField.Item.Type type = info.getType();
        CTX ctX = ctI.addNewX();
        ctI.setT(XSSFPivotField.XSSFItem.getCTItemType(type));

        if (type == PivotField.Item.Type.GRAND) {
          int index = info.getIndex();
          if (index > 0)
            ctX.setV(index);
        }
        else {
          int index = indexMapper.getShareItemIndex(info.getDepth(), info.getValue());
          if (index > 0)
            ctX.setV(index);
        }
      }
      else
      {
        boolean setRowIndex = false;
        Integer firstRowIndex = null;
        CTI ctI = colItems.addNewI();
        for (int i = 0; i < col.size(); i++) {
          ItemInfo item = (ItemInfo)col.get(i);
          if (item != null) {
            if (firstRowIndex == null) {
              firstRowIndex = Integer.valueOf(i);
            }
            int depth = item.getDepth();
            if (depth >= 0) {
              CTX ctX = ctI.addNewX();
              int idx = indexMapper.getShareItemIndex(i, item.getValue());
              if (idx < 0)
                throw new IllegalArgumentException("can't find item: " + item.getValue());
              if (idx > 0)
                ctX.setV(idx);
            }
            else {
              int idx = dataIndexMapper.getIndex((String)item.getValue());
              if (idx < 0) {
                throw new IllegalArgumentException("can't find item: " + item.getValue());
              }

              CTX ctX = ctI.addNewX();
              if (idx > 0) {
                ctI.setI(idx);
                ctX.setV(idx);
              }
            }
          } else {
            setRowIndex = true;
          }
        }
        if ((setRowIndex) && (firstRowIndex != null)) {
          ctI.setR(firstRowIndex.intValue());
        }
      }
    }
    colItems.setCount(colItems.getIList().size());
  }

  public void setRowItems(List<List<ItemInfo>> items)
  {
    CTRowItems rowItems = this._pivotTableDefinition.getRowItems();
    if (rowItems != null) {
      this._pivotTableDefinition.unsetRowItems();
    }
    rowItems = this._pivotTableDefinition.addNewRowItems();

    IndexMapper indexMapper = new IndexMapper(getRowFields());
    DataFieldIndexMapper dataIndexMapper = null;
    List dataFields = getDataFields();
    if ((getDataOnRows()) && (dataFields.size() > 1)) {
      dataIndexMapper = new DataFieldIndexMapper(dataFields);
    }

    int itemSize = items.size();
    for (int i = 0; i < itemSize; i++) {
      List<ItemInfo> row = items.get(i);
      for (ItemInfo info : row) {
	      if (info != null) {
	        CTI ctI = rowItems.addNewI();
	        if (info.getDepth() > 0) {
	          ctI.setR(info.getDepth());
	        }
	
	        PivotField.Item.Type type = info.getType();
	        CTX ctX = ctI.addNewX();
//	        ctI.setT(XSSFPivotField.XSSFItem.getCTItemType(type));
	
	        if (type == PivotField.Item.Type.GRAND) {
	          int index = info.getIndex();
	          if (index > 0)
	            ctX.setV(index);
	        }
	        else {
	          int index = indexMapper.getShareItemIndex(info.getDepth(), info.getValue());
	          if (index > 0)
	            ctX.setV(index);
	        }
	      }
	      else {
	        boolean setColumnIndex = false;
	        Integer firstColumnIndex = null;
	        CTI ctI = rowItems.addNewI();
	
	        for (int c = 0; c < row.size(); c++) {
	          ItemInfo item = (ItemInfo)row.get(c);
	          if (item != null) {
	            if (firstColumnIndex == null) {
	              firstColumnIndex = Integer.valueOf(c);
	            }
	
	            int depth = item.getDepth();
	            if (depth >= 0) {
	              CTX ctX = ctI.addNewX();
	              int idx = indexMapper.getShareItemIndex(c, item.getValue());
	              if (idx < 0)
	                throw new IllegalArgumentException("can't find item: " + item.getValue());
	              if (idx > 0)
	                ctX.setV(idx);
	            }
	            else {
	              int idx = dataIndexMapper.getIndex((String)item.getValue());
	              if (idx < 0) {
	                throw new IllegalArgumentException("can't find item: " + item.getValue());
	              }
	
	              CTX ctX = ctI.addNewX();
	              if (idx > 0) {
	                ctI.setI(idx);
	                ctX.setV(idx);
	              }
	            }
	          } else {
	            setColumnIndex = true;
	          }
	        }
	        if ((setColumnIndex) && (firstColumnIndex != null)) {
	          ctI.setR(firstColumnIndex.intValue());
	        }
	      }
      }
    }
    rowItems.setCount(rowItems.getIList().size());
  }

  public void setRowField(PivotField field) {
    int idx = getPivotFieldIndex(field, getPivotFields());
    if (idx < 0) {
      throw new IllegalArgumentException("Can't find PivotField");
    }

    boolean typeChanged = false;
    PivotField.Type type = field.getType();
    if (type != null) {
      switch (type.ordinal()) {
      case 3:
        typeChanged = true;
        removeColumnField(idx, field);
        break;
      case 2:
        typeChanged = true;
        removeDataField(idx, field);
      }
    }
    else {
      typeChanged = true;
    }

    if (typeChanged)
    {
      field.setType(PivotField.Type.ROW);

      CTRowFields rowFields = this._pivotTableDefinition.getRowFields();
      if (rowFields == null) {
        rowFields = initRowFields(this._pivotTableDefinition);
      }

      int count = (int)rowFields.getCount();
      List dataFields = getDataFields();
      boolean insrtDataRow = false;
      if ((dataFields.size() > 1) && (getDataOnRows())) {
        insrtDataRow = true;

        int lastIdx = count - 1;
        if (lastIdx >= 0) {
          CTField f = (CTField)rowFields.getFieldList().get(lastIdx);
          if (f.getX() == -2) {
            rowFields.removeField(lastIdx);
          }
        }
      }

      rowFields.addNewField().setX(idx);

      if (insrtDataRow) {
        rowFields.addNewField().setX(-2);
      }
      rowFields.setCount(rowFields.getFieldList().size());
    }
  }

  private void removeDataField(int idx, PivotField field) {
    CTDataField dataField = null;
    CTDataFields dataFields = this._pivotTableDefinition.getDataFields();

    int i = 0;
    for (CTDataField d : dataFields.getDataFieldList()) {
      if (d.getFld() == idx) {
        dataField = d;
        break;
      }
      i++;
    }

    if (dataField != null) {
      dataFields.removeDataField(i);
      dataFields.setCount(dataFields.getDataFieldList().size());
    }
  }

  private void removeRowField(int idx, PivotField field) {
    CTField ctField = null;
    CTRowFields rowFields = this._pivotTableDefinition.getRowFields();

    int i = 0;
    for (CTField f : rowFields.getFieldList()) {
      if (f.getX() == idx) {
        ctField = f;
        break;
      }
    }

    if (ctField != null) {
      rowFields.removeField(i);
      rowFields.setCount(rowFields.getFieldList().size());
    }
  }

  private void removeColumnField(int idx, PivotField field) {
    CTField ctField = null;
    CTColFields colFields = this._pivotTableDefinition.getColFields();

    int i = 0;
    for (CTField f : colFields.getFieldList()) {
      if (f.getX() == idx) {
        ctField = f;
        break;
      }
      i++;
    }

    if (ctField != null) {
      colFields.removeField(i);
      colFields.setCount(colFields.getFieldList().size());
    }
  }

  public void setDataOnRows(boolean dataOnRows)
  {
    this._pivotTableDefinition.setDataOnRows(dataOnRows);
  }

  public boolean getDataOnRows()
  {
    return this._pivotTableDefinition.getDataOnRows();
  }

  public String getRowHeaderCaption()
  {
    return this._pivotTableDefinition.getRowHeaderCaption();
  }

  public void setRowHeaderCaption(String caption)
  {
    this._pivotTableDefinition.setRowHeaderCaption(caption);
  }

  public void setOutline(boolean outline)
  {
    this._pivotTableDefinition.setOutline(outline);
  }

  public boolean getOutline()
  {
    return this._pivotTableDefinition.getOutline();
  }

  public void setOutlineData(boolean outlineData)
  {
    this._pivotTableDefinition.setOutlineData(outlineData);
  }

  public boolean getOutlineData()
  {
    return this._pivotTableDefinition.getOutlineData();
  }

  private class DataFieldIndexMapper
  {
    HashMap<String, Integer> _mapper = new HashMap();

    DataFieldIndexMapper(List<DataField> dataFields) { for (int i = 0; i < dataFields.size(); i++) {
        DataField f = (DataField)dataFields.get(i);
        this._mapper.put(f.getName(), Integer.valueOf(i));
      } }

    int getIndex(String str)
    {
      Integer index = (Integer)this._mapper.get(str);
      if (index != null) {
        return index.intValue();
      }
      return -1;
    }
  }

  private class IndexMapper
  {
    List<HashMap<Object, Integer>> _mapper = new ArrayList();

    IndexMapper(List<PivotField> fields)
    {
      for (PivotField pivotField : fields) {
        HashMap map = new HashMap();
        this._mapper.add(map);
        List items = pivotField.getItems();
        for (int j = 0; j < items.size(); j++) {
          PivotField.Item item = (PivotField.Item)items.get(j);
          Object value = item.getValue();
          if ((value instanceof Calendar))
            map.put(((Calendar)value).getTime(), Integer.valueOf(j));
          else if ((value instanceof Number))
            map.put(Double.valueOf(((Number)value).doubleValue()), Integer.valueOf(j));
          else
            map.put(value != null ? value.toString() : null, Integer.valueOf(j));
        }
      }
    }

    int getShareItemIndex(int fieldIndex, Object key)
    {
      HashMap map = (HashMap)this._mapper.get(fieldIndex);

      Integer index = (Integer)map.get(key);
      if (index != null) {
        return index.intValue();
      }

      return -1;
    }
  }
}