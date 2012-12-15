package nu.com.rill.analysis.report.excel.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nu.com.rill.analysis.report.REException;
import nu.com.rill.analysis.report.excel.DataRetriever;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.zss.model.Worksheet;

public class JsonDataRetriever implements DataRetriever {

	public final Log LOGGER = LogFactory.getLog(this.getClass());
	
	@Override
	public boolean supportType(DATA_TYPE dt) {
		
		return DATA_TYPE.json.equals(dt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveData(Worksheet dataSheet, Map<String, String> reportParams) {
		
		Row row = dataSheet.getRow(dataSheet.getFirstRowNum());
		String url = row.getCell(row.getFirstCellNum()).getStringCellValue();
		String result = ReportEngine.fetchUrl(reportParams.get(ReportEngine.URL) + url, reportParams);
		
		List<List<String>> data = null;
		try {
			Map<String, Object> jsonResult = new LinkedHashMap<String, Object>();
			try {
				jsonResult = ReportEngine.mapper.readValue(result, new TypeReference<Map<String, Object>>() {
				});
			} catch (JsonMappingException e) {
				// Ignore 
				LOGGER.debug("Fail to read value as Map<String, Object>, ignore it." + result);
			}
			if (jsonResult.containsKey("_RE_DATA_JSON_RESULT")) {
				data = (List<List<String>>) jsonResult.get("_RE_DATA_JSON_RESULT");
			}
			
			if (data == null) {
				data = new ArrayList<List<String>>();
				data.addAll(ReportEngine.mapper.readValue(result, List.class));
			}
			
			int i = row.getRowNum() + 1;
			for (List<String> element : data) {
				// Handle row one by one.
				Row currentRow = null;
				if (dataSheet.getRow(i) == null) {
					currentRow = ReportEngine.copyRow(dataSheet, i - 1, i);
				} else {
					currentRow = dataSheet.getRow(i);
				}
				for (int j = 0; j < element.size(); j++) {
					Cell currentCell = null;
					int cellType = Cell.CELL_TYPE_STRING;
					if (currentRow.getCell(j) == null) {
						currentCell = currentRow.createCell(j);
						currentCell.setCellStyle(currentRow.getCell(j - 1).getCellStyle());
						currentCell.setCellType(currentRow.getCell(j - 1).getCellType());
						cellType = currentRow.getCell(j - 1).getCellType();
					} else {
						currentCell = currentRow.getCell(j);
						cellType = currentRow.getCell(j).getCellType();
					}
					if (Cell.CELL_TYPE_NUMERIC == cellType) {
						currentCell.setCellValue(new Double(element.get(j)));
					}
					if (Cell.CELL_TYPE_STRING == cellType) {
						currentCell.setCellValue(element.get(j));
					}
				}
				i++;
			}
		} catch (Exception e) {
			LOGGER.error("Error when try to parse to JSON " + result, e);
			throw new REException("仅允许响应application/json数据.", e);
		}
		
		return;
	}

}
