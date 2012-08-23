package org.zkoss.poi.xssf.usermodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDateTime;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumber;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCacheDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRecord;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSharedItems;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTString;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheetSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.PivotCacheDefinitionDocument;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STSourceType;
import org.zkoss.poi.POIXMLDocument;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.openxml4j.exceptions.InvalidFormatException;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.openxml4j.opc.internal.MemoryPackagePart;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.DateUtil;
import org.zkoss.poi.ss.usermodel.PivotCache;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.util.AreaReference;

public class XSSFPivotCache extends POIXMLDocumentPart
  implements PivotCache
{
  private long _cacheId;
  private CTPivotCacheDefinition _pivotCacheDefinition;
  private XSSFPivotCacheRecords _pivotCacheRecords;

  public XSSFPivotCache()
  {
    onDocumentCreate();
  }

  protected void onDocumentCreate()
  {
    this._pivotCacheDefinition = CTPivotCacheDefinition.Factory.newInstance();
    this._pivotCacheDefinition.addNewCacheSource();

    short ver = 3;
    this._pivotCacheDefinition.setCreatedVersion(ver);
    this._pivotCacheDefinition.setRefreshedVersion(ver);
    this._pivotCacheDefinition.setMinRefreshableVersion(ver);
  }

  public XSSFPivotCache(PackagePart part, PackageRelationship rel) {
    super(part, rel);
  }

  public XSSFPivotCache(long cacheId, POIXMLDocumentPart parent, PackagePart part, PackageRelationship rel) throws IOException {
    super(parent, part, rel);

    this._cacheId = cacheId;
    onDocumentRead();
  }

  protected void onDocumentRead() throws IOException
  {
    try {
      PackagePart part = getPackagePart();
      PackageRelationship rel = getPackageRelationship();
      this._pivotCacheDefinition = PivotCacheDefinitionDocument.Factory.parse(part.getInputStream()).getPivotCacheDefinition();

      PackageRelationship relationship = part.getRelationshipsByType(XSSFRelation.PIVOT_CACHE_RECORDS.getRelation()).getRelationshipByID(rel.getId());
      this._pivotCacheRecords = new XSSFPivotCacheRecords(part.getRelatedPart(relationship), relationship);
    } catch (IOException e) {
      throw new POIXMLException(e);
    } catch (XmlException e) {
      throw new POIXMLException(e);
    } catch (InvalidFormatException e) {
      throw new POIXMLException(e);
    }
  }

  protected void commit()
    throws IOException
  {
    XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
    xmlOptions.setSaveSyntheticDocumentElement(new QName(CTPivotCacheDefinition.type.getName().getNamespaceURI(), "pivotCacheDefinition"));
    Map map = new HashMap();
    map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
    xmlOptions.setSaveSuggestedPrefixes(map);

    PackagePart part = getPackagePart();
    clearMemoryPackagePart(part);

    this._pivotCacheDefinition.setId(this._pivotCacheRecords.getPackageRelationship().getId());

    OutputStream out = part.getOutputStream();
    this._pivotCacheDefinition.save(out, xmlOptions);
    out.close();
  }

  private void clearMemoryPackagePart(PackagePart part) {
    if ((part instanceof MemoryPackagePart))
      ((MemoryPackagePart)part).clear();
  }

  public Workbook getWorkbook()
  {
    return (Workbook)getParent();
  }

  public void setCacheId(long cacheId) {
    this._cacheId = cacheId;
  }

  public long getCacheId() {
    return this._cacheId;
  }

  public List<PivotCache.CacheField> getFields() {
    CTCacheFields cacheFields = this._pivotCacheDefinition.getCacheFields();
    if (cacheFields == null) {
      return Collections.emptyList();
    }

    ArrayList list = new ArrayList();
    for (CTCacheField e : cacheFields.getCacheFieldList()) {
      list.add(new XSSFCacheField(e));
    }

    return list;
  }

  public List<PivotCache.CacheRecord> getRecords() {
    List<CTRecord> records = this._pivotCacheRecords.getRows();
    if (records == null) {
      return Collections.emptyList();
    }

    CTCacheFields cacheFields = this._pivotCacheDefinition.getCacheFields();
    List list = new ArrayList();
    for (CTRecord record : records) {
      list.add(new XSSFCacheRecord(record, cacheFields.getCacheFieldList()));
    }
    return list;
  }

  private static String setSheetSource(AreaReference areaRef, CTCacheSource cacheSource)
  {
    CTWorksheetSource sheetSource = null;

    STSourceType.Enum type = cacheSource.getType();
    if (type != STSourceType.WORKSHEET) {
      cacheSource.setType(STSourceType.WORKSHEET);
      sheetSource = cacheSource.addNewWorksheetSource();
    } else {
      sheetSource = cacheSource.getWorksheetSource();
    }

    String sheetName = areaRef.getLastCell().getSheetName();
    String ref = areaRef.formatAsString();
    ref = ref.substring(sheetName.length() + 1);

    sheetSource.setSheet(sheetName);
    sheetSource.setRef(ref);

    return sheetName;
  }

  private static Cell getCell(int row, int col, Sheet sheet) {
    Row r = sheet.getRow(row);
    return r != null ? r.getCell(col) : null;
  }

  private static String getCellText(Cell cell) {
    if (cell == null) {
      return null;
    }

    int type = cell.getCellType();
    if (type == 1)
      return cell.getStringCellValue();
    if ((type == 2) && (cell.getCachedFormulaResultType() == 1)) {
      return cell.getRichStringCellValue().getString();
    }
    return null;
  }

  private static ArrayList<FieldInfo> prepareFields(int row, int lCol, int rCol, Sheet sheet) {
    ArrayList fields = new ArrayList();
    for (int i = lCol; i <= rCol; i++) {
      String field = getCellText(getCell(row, i, sheet));
      if ((field == null) || (field.length() == 0)) {
        throw new IllegalArgumentException("PivotTable field name should not be empty");
      }
      fields.add(new FieldInfo(field));
    }

    return fields;
  }

  public void setSheetSource(AreaReference sourceRef) {
    if (this._pivotCacheRecords == null) {
      int pivotCacheRecordNumber = 1;
      this._pivotCacheRecords = ((XSSFPivotCacheRecords)createRelationship(XSSFRelation.PIVOT_CACHE_RECORDS, XSSFFactory.getInstance(), pivotCacheRecordNumber));
    }

    CTCacheFields cacheFields = this._pivotCacheDefinition.getCacheFields();
    if (cacheFields == null) {
      cacheFields = this._pivotCacheDefinition.addNewCacheFields();
    }

    String refreshedBy = ((POIXMLDocument)getWorkbook()).getProperties().getCoreProperties().getCreator();
    if ((refreshedBy != null) && (!refreshedBy.isEmpty())) {
      this._pivotCacheDefinition.setRefreshedBy(refreshedBy);
    }
    this._pivotCacheDefinition.setRefreshedDate(DateUtil.getExcelDate(Calendar.getInstance().getTime()));

    String sheetName = setSheetSource(sourceRef, this._pivotCacheDefinition.getCacheSource());
    if ((sheetName == null) || (sheetName.length() == 0)) {
      throw new IllegalArgumentException("AreaReference shall has source sheet");
    }
    Sheet sheet = getWorkbook().getSheet(sheetName);

    int tRow = sourceRef.getFirstCell().getRow();
    int lCol = sourceRef.getFirstCell().getCol();
    int bRow = sourceRef.getLastCell().getRow();
    int rCol = sourceRef.getLastCell().getCol();
    if (bRow - tRow + 1 < 2) {
      throw new IllegalArgumentException("PivotTable requires at least two rows of source data.");
    }

    List fields = prepareFields(tRow, lCol, rCol, sheet);

    for (int r = tRow + 1; r <= bRow; r++)
    {
      Iterator iter = fields.iterator();
      for (int col = lCol; col <= rCol; col++) {
        Cell cell = getCell(r, col, sheet);
        FieldInfo field = (FieldInfo)iter.next();
        field.add(cell);
      }
    }

    createCacheRecords(fields, createSharedItems(fields, cacheFields), this._pivotCacheRecords);
    cacheFields.setCount(fields.size());
    this._pivotCacheDefinition.getCacheSource().getWorksheetSource().setRef(sourceRef.formatAsString().substring(sourceRef.formatAsString().indexOf("!") + 1));
    this._pivotCacheDefinition.setRecordCount(((FieldInfo)fields.get(0)).size());
    this._pivotCacheDefinition.setRefreshOnLoad(true);
    
    try {
    	this.commit();
    	this._pivotCacheRecords.commit();
//    	this._pivotCacheRecords.commit();
    } catch (Exception e) {
    	// Ignore
    }
  }

  private static void newCell(Object src, CTRecord record, int updateIndex, boolean createOrUpdate) {
    if ((src instanceof Calendar)) {
    	if (createOrUpdate)
      record.addNewD().setV((Calendar)src);
    	else 
    		record.getDArray(updateIndex).setV((Calendar)src);
    } else if ((src instanceof Number)) {
    	if (createOrUpdate)
      record.addNewN().setV(((Number)src).doubleValue());
    	else 
    		record.getNArray(updateIndex).setV(((Number)src).doubleValue());
    } else if ((src instanceof String)) {
    	if (createOrUpdate)
    	record.addNewS().setV((String)src);
    	else 
    		record.getSArray(updateIndex).setV((String)src);
    }
    else if (src == null)
      record.addNewM();
  }

  private static void createCacheRecords(List<FieldInfo> fields, HashMap<Integer, CTSharedItems> shareItems, XSSFPivotCacheRecords cacheRecords)
  {
    IndexMapper indexMapper = new IndexMapper(shareItems);

    int rowSize = ((FieldInfo)fields.get(0)).size();
    cacheRecords.setCount(rowSize);
    int cacheRecordSize = cacheRecords.getRows().size();
    int xSize = cacheRecords.getRows().get(0).getXList().size();
    for (int s = 0; s < cacheRecordSize; s++) {
    	cacheRecords.getRows().remove(0);
    }
    for (int i = 0; i < rowSize; i++)
    {
//    	if (i < cacheRecordSize) {
//    		CTRecord r = cacheRecords.getRows().get(i);
//    	      for (int j = 0; j < fields.size(); j++) {
//    	        FieldInfo field = (FieldInfo)fields.get(j);
//    	        Object obj = field.getValue(i);
//    	        
//    	        Integer idx = indexMapper.getShareItemIndex(j, obj);
//    	        if (j < xSize) {
//    	        	// Ignore X
//    	        	r.getXList().get(j).setV(idx.longValue());
//    	        } else {
//    	        	newCell(obj, r, j - xSize, false);
//    	        }
//    	      }
//    	} else {
    		CTRecord r = cacheRecords.addNewRow();
  	      for (int j = 0; j < fields.size(); j++) {
  	        FieldInfo field = (FieldInfo)fields.get(j);
  	        Object obj = field.getValue(i);
  	        
  	        int idx = indexMapper.getShareItemIndex(j, obj);
  	      if (j < xSize) {
  	          r.addNewX().setV(idx);
  	        } else
  	          newCell(obj, r, j, true);
  	      }
//		}
      
    }
  }

  private static void setupSharedItemsProperties(FieldInfo field, CTSharedItems sharedItems)
  {
    if (field.containsBlank) {
      sharedItems.setContainsBlank(true);
    }

    if (field.containsDate) {
      if (!field.isContainsSemiMixedTypes()) {
        sharedItems.setContainsSemiMixedTypes(false);
      }
      if (!field.containsNonDate) {
        sharedItems.setContainsNonDate(false);
      }
      sharedItems.setContainsDate(true);
      if (!field.containsString) {
        sharedItems.setContainsString(false);
      }
      if (field.isContainsMixedTypes()) {
        sharedItems.setContainsMixedTypes(true);
      }
      sharedItems.setMinDate(field.minDate);
      sharedItems.setMaxDate(field.maxDate);
    }
    else if (field.containsNumber) {
      sharedItems.setContainsString(field.containsString);
      sharedItems.setContainsNumber(true);
      if (field.isContainsMixedTypes())
        sharedItems.setContainsMixedTypes(true);
      else if (!field.isContainsSemiMixedTypes()) {
        sharedItems.setContainsSemiMixedTypes(false);
      }
      sharedItems.setMinValue(field.minNumber.doubleValue());
      sharedItems.setMaxValue(field.maxNumber.doubleValue());
      if (field.containsInteger)
        sharedItems.setContainsInteger(true);
    }
    else if (field.isMissing()) {
      sharedItems.setContainsNonDate(false);
      sharedItems.setContainsString(false);
      sharedItems.setContainsBlank(true);
    }
  }

  private static void createSharedItems(FieldInfo field, CTSharedItems sharedItems)
  {
    int size = 0;
    if (field.containsBlank) {
      sharedItems.addNewM();
      size++;
    }
    if (field.containsDate) {
      for (Calendar cal : field.getSharedDates()) {
        sharedItems.addNewD().setV(cal);
      }
      size += field.getSharedDates().size();
    }
    if (field.containsNumber) {
      for (Double num : field.getSharedNumbers()) {
        sharedItems.addNewN().setV(num.doubleValue());
      }
      size += field.getSharedNumbers().size();
    }
    if (field.containsString) {
    	int temp = sharedItems.sizeOfSArray();
    	for (int i = 0 ; i < temp; i++) {
            sharedItems.removeS(0);
          }
    	for (String s : field.getSharedStrings()) {
        sharedItems.addNewS().setV(s);
      }
      size += field.getSharedStrings().size();
    }
    sharedItems.setCount(size);
  }

  private static HashMap<Integer, CTSharedItems> createSharedItems(List<FieldInfo> fields, CTCacheFields cacheFields) {
    HashMap shareItems = new HashMap();

    int itemIndex = 0;
    int size = cacheFields.getCacheFieldList().size();
    for (FieldInfo fieldInfo : fields) {
    	int matchCFIndex = -1;
		if (itemIndex < size && fieldInfo.fieldName.equals(cacheFields.getCacheFieldList().get(itemIndex).getName())) {
			matchCFIndex = itemIndex;
		}
    	if (matchCFIndex < 0) {
    		// Means create
    		CTCacheField ctCacheField = cacheFields.addNewCacheField();
    	      ctCacheField.setName(fieldInfo.fieldName);
    	      ctCacheField.setNumFmtId(fieldInfo.formatId);

    	      CTSharedItems ctSharedItems = ctCacheField.addNewSharedItems();
    	      setupSharedItemsProperties(fieldInfo, ctSharedItems);
    	      createSharedItems(fieldInfo, ctSharedItems);
    	      shareItems.put(Integer.valueOf(itemIndex++), ctSharedItems);
    	} else {
    		// Means update 
    		CTCacheField ctCacheField = cacheFields.getCacheFieldArray(matchCFIndex);
    		ctCacheField.setName(fieldInfo.fieldName);
  	      	ctCacheField.setNumFmtId(fieldInfo.formatId);
  	      	
  	      CTSharedItems ctSharedItems = ctCacheField.getSharedItems();
//	      setupSharedItemsProperties(fieldInfo, ctSharedItems);
	      createSharedItems(fieldInfo, ctSharedItems);
	      shareItems.put(Integer.valueOf(itemIndex++), ctSharedItems);
    	}

    }
    
    cacheFields.setCount(shareItems.size());

    return shareItems;
  }

  public PivotCache.SheetSource getSheetSource() {
    CTCacheSource source = this._pivotCacheDefinition.getCacheSource();
    STSourceType.Enum type = source.getType();
    if (type == STSourceType.WORKSHEET) {
      return new SheetSourceImpl(source.getWorksheetSource());
    }

    return null;
  }

  public short getRefreshedVersion() {
    return this._pivotCacheDefinition.getRefreshedVersion();
  }

  public short getMinRefreshableVersion() {
    return this._pivotCacheDefinition.getMinRefreshableVersion();
  }

  public short getCreatedVersion() {
    return this._pivotCacheDefinition.getCreatedVersion();
  }

  private static class IndexMapper
  {
    List<HashMap<Object, Integer>> _mapper = new ArrayList();

    IndexMapper(HashMap<Integer, CTSharedItems> shareItems)
    {
      int itemSize = shareItems.size();
      HashMap map;
      int idx;
      for (int i = 0; i < itemSize; i++) {
        map = new HashMap();
        this._mapper.add(map);

        CTSharedItems ctSharedItems = (CTSharedItems)shareItems.get(Integer.valueOf(i));

        idx = 0;

        if (ctSharedItems.getContainsBlank()) {
          map.put(null, Integer.valueOf(idx++));
        }

        if (ctSharedItems.getContainsDate()) {
          List<CTDateTime> dList = ctSharedItems.getDList();
          for (CTDateTime d : dList) {
            map.put(d.getV(), Integer.valueOf(idx++));
          }
        }

        if (ctSharedItems.getContainsNumber()) {
          List<CTNumber> nList = ctSharedItems.getNList();
          for (CTNumber n : nList) {
            map.put(Double.valueOf(n.getV()), Integer.valueOf(idx++));
          }
        }

        if (ctSharedItems.getContainsString()) {
          List<CTString> sList = ctSharedItems.getSList();
          for (CTString s : sList)
            map.put(s.getV(), Integer.valueOf(idx++));
        }
      }
    }

    Integer getShareItemIndex(int colIndex, Object key)
    {
      HashMap map = (HashMap)this._mapper.get(colIndex);

      Integer index = (Integer)map.get(key);
      if (index != null) {
        return index;
      }

      return -1;
    }
  }

  private static class FieldInfo
  {
    String fieldName;
    boolean containsNonDate;
    boolean containsDate;
    boolean containsString;
    boolean containsNumber;
    boolean containsBlank;
    boolean containsInteger = true;
    Calendar minDate;
    Calendar maxDate;
    Double minNumber;
    Double maxNumber;
    int formatId;
    LinkedHashSet<Double> sharedNumbers;
    LinkedHashSet<String> sharedStrings;
    LinkedHashSet<Calendar> sharedDates;
    ArrayList<Cell> cells = new ArrayList();
    ArrayList<Object> values = new ArrayList();

    FieldInfo(String name) {
      this.fieldName = name;
    }

    boolean isInteger(Double src) {
      return Math.floor(src.doubleValue()) == src.doubleValue();
    }

    boolean isDateFormat(Cell cell) {
      if (cell == null) return false;

      CellStyle style = cell.getCellStyle();
      if (style == null) return false;
      return DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString());
    }

    boolean isValidDate(Cell cell) {
      return DateUtil.isCellDateFormatted(cell);
    }

    boolean isNumber(Cell cell) {
      int cellType = cell.getCellType();
      return (cellType == 0) || ((cellType == 2) && (cell.getCachedFormulaResultType() == 0));
    }

    boolean isString(Cell cell)
    {
      int cellType = cell.getCellType();
      return (cellType == 1) || ((cellType == 2) && (cell.getCachedFormulaResultType() == 1));
    }

    void add(Cell cell)
    {
      this.cells.add(cell);

      if (cell != null) {
        if (isNumber(cell)) {
          this.formatId = cell.getCellStyle().getDataFormat();
          if (isDateFormat(cell)) {
            this.containsDate = true;
            if (!isValidDate(cell)) {
              this.containsNonDate = true;

              this.values.add(Double.valueOf(cell.getNumericCellValue()));
            } else {
              XmlCalendar c = new XmlCalendar(DateUtil.getJavaDate(cell.getNumericCellValue()));
              c.clear(15);

              getSharedDates().add(c);
              this.values.add(c);

              if (this.minDate == null)
                this.minDate = c;
              else {
                this.minDate = getMinDate(c, this.minDate);
              }
              if (this.maxDate == null)
                this.maxDate = c;
              else
                this.maxDate = getMaxDate(c, this.maxDate);
            }
          }
          else {
            this.containsNumber = true;
            Double num = Double.valueOf(cell.getNumericCellValue());
            if (!isInteger(num)) {
              this.containsInteger = false;
            }

            getSharedNumbers().add(num);
            this.values.add(num);

            if (this.minNumber == null)
              this.minNumber = num;
            else
              this.minNumber = Double.valueOf(Math.min(num.doubleValue(), this.minNumber.doubleValue()));
            if (this.maxNumber == null)
              this.maxNumber = num;
            else
              this.maxNumber = Double.valueOf(Math.max(num.doubleValue(), this.maxNumber.doubleValue()));
          }
        } else if (isString(cell)) {
          this.containsString = true;
          String str = XSSFPivotCache.getCellText(cell);

          getSharedStrings().add(str);
          this.values.add(str);
        } else if (cell.getCellType() == 3) {
          this.values.add(null);
        }
      } else {
        this.containsBlank = true;
        this.values.add(null);
      }
    }

    private Calendar getMaxDate(Calendar c1, Calendar c2) {
      int cmp = c1.compareTo(c2);
      return cmp >= 0 ? c1 : c2;
    }

    private Calendar getMinDate(Calendar c1, Calendar c2) {
      int cmp = c1.compareTo(c2);
      return cmp <= 0 ? c1 : c2;
    }

    LinkedHashSet<Double> getSharedNumbers() {
      if (this.sharedNumbers == null) {
        this.sharedNumbers = new LinkedHashSet();
      }
      return this.sharedNumbers;
    }

    LinkedHashSet<Calendar> getSharedDates() {
      if (this.sharedDates == null) {
        this.sharedDates = new LinkedHashSet();
      }
      return this.sharedDates;
    }

    LinkedHashSet<String> getSharedStrings() {
      if (this.sharedStrings == null) {
        this.sharedStrings = new LinkedHashSet();
      }
      return this.sharedStrings;
    }

    Object getValue(int index) {
      return this.values.get(index);
    }

    int size() {
      return this.cells.size();
    }

    boolean isMissing() {
      for (Iterator i$ = this.values.iterator(); i$.hasNext(); ) { Object o = i$.next();
        if (o != null) {
          return false;
        }
      }
      return true;
    }

    boolean isContainsSemiMixedTypes() {
      int bTrue = 0;
      if (this.containsBlank) bTrue++;
      if (this.containsDate) bTrue++;
      if (this.containsString) bTrue++;
      if (this.containsNumber) bTrue++;
      return bTrue > 1;
    }

    boolean isContainsMixedTypes() {
      int bTrue = 0;

      if (this.containsDate) {
        bTrue++;
        if (this.containsNonDate) bTrue++;
      }
      if (this.containsString) bTrue++;
      if (this.containsNumber) bTrue++;

      return bTrue > 1;
    }
  }

  private class SheetSourceImpl
    implements PivotCache.SheetSource
  {
    private final CTWorksheetSource _source;

    SheetSourceImpl(CTWorksheetSource source)
    {
      this._source = source;
    }

    public String getName() {
      return this._source.getSheet();
    }

    public String getRef() {
      return this._source.getRef();
    }
  }
}