
package com.baidu.rigel.service.workflow.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for completeTaskInstanceDto complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="completeTaskInstanceDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="engineTaskInstanceId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="workflowParams" type="{http://activiti.api.ws.workflow.service.rigel.baidu.com/}mapElementsArray" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "completeTaskInstanceDto", propOrder = {
    "engineTaskInstanceId",
    "operator",
    "workflowParams"
})
public class CompleteTaskInstanceDto {

    protected String engineTaskInstanceId;
    protected String operator;
    protected MapElementsArray workflowParams;

    /**
     * Gets the value of the engineTaskInstanceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEngineTaskInstanceId() {
        return engineTaskInstanceId;
    }

    /**
     * Sets the value of the engineTaskInstanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEngineTaskInstanceId(String value) {
        this.engineTaskInstanceId = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

    /**
     * Gets the value of the workflowParams property.
     * 
     * @return
     *     possible object is
     *     {@link MapElementsArray }
     *     
     */
    public MapElementsArray getWorkflowParams() {
        return workflowParams;
    }

    /**
     * Sets the value of the workflowParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link MapElementsArray }
     *     
     */
    public void setWorkflowParams(MapElementsArray value) {
        this.workflowParams = value;
    }

}
