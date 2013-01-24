/* FormulaMetaInfo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 25, 2010 6:54:36 AM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.formula;


/**
 * @author Sam
 *
 */
public class FormulaMetaInfo {

	private String category;
	private String function;
	private String expression;
	private String description;
	private int requiredParameter;
	private String multipleParameter;
	
	private int rowIndex;
	private int colIndex;

	/**
	 * @param category
	 * @param function
	 * @param expression
	 * @param description
	 * @param requiredParameter
	 */
	public FormulaMetaInfo(String category, String function, String expression,
			String description, int requiredParameter, String multipleParameterName) {
		this.category = category;
		this.function = function;
		this.expression = expression;
		this.description = description;
		this.requiredParameter = requiredParameter;
		this.multipleParameter = multipleParameterName;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getRequiredParameter() {
		return requiredParameter;
	}
	public void setRequiredParameter(int requiredParameter) {
		this.requiredParameter = requiredParameter;
	}
	
	public boolean isMultipleParameter() {
		return multipleParameter != null;
	}
	public void setMultipleParameter(String multipleParameterName) {
		this.multipleParameter = multipleParameterName;
	}
	public String getMultipleParameter() {
		return multipleParameter;
	}
	public String[] getParameterNames() {
		String arg = expression.substring(expression.indexOf("(") + 1, 
				expression.lastIndexOf(")"));
		String[] args = arg.split(",");
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		return args;
	}
	public int getRowIndex() {
		return rowIndex;
	}
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
	public int getColIndex() {
		return colIndex;
	}
	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}
}
