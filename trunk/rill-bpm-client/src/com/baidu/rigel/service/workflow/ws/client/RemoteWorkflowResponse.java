
package com.baidu.rigel.service.workflow.ws.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteWorkflowResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteWorkflowResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="businessObjectId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineProcessInstanceId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineTaskInstanceIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="processDefinitionKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="processInstanceEnd" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteWorkflowResponse", propOrder = {
    "businessObjectId",
    "engineProcessInstanceId",
    "engineTaskInstanceIds",
    "processDefinitionKey",
    "processInstanceEnd"
})
public class RemoteWorkflowResponse {

    protected String businessObjectId;
    protected String engineProcessInstanceId;
    @XmlElement(nillable = true)
    protected List<String> engineTaskInstanceIds;
    protected String processDefinitionKey;
    protected boolean processInstanceEnd;

    /**
     * Gets the value of the businessObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusinessObjectId() {
        return businessObjectId;
    }

    /**
     * Sets the value of the businessObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusinessObjectId(String value) {
        this.businessObjectId = value;
    }

    /**
     * Gets the value of the engineProcessInstanceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEngineProcessInstanceId() {
        return engineProcessInstanceId;
    }

    /**
     * Sets the value of the engineProcessInstanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEngineProcessInstanceId(String value) {
        this.engineProcessInstanceId = value;
    }

    /**
     * Gets the value of the engineTaskInstanceIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the engineTaskInstanceIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEngineTaskInstanceIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getEngineTaskInstanceIds() {
        if (engineTaskInstanceIds == null) {
            engineTaskInstanceIds = new ArrayList<String>();
        }
        return this.engineTaskInstanceIds;
    }

    /**
     * Gets the value of the processDefinitionKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    /**
     * Sets the value of the processDefinitionKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessDefinitionKey(String value) {
        this.processDefinitionKey = value;
    }

    /**
     * Gets the value of the processInstanceEnd property.
     * 
     */
    public boolean isProcessInstanceEnd() {
        return processInstanceEnd;
    }

    /**
     * Sets the value of the processInstanceEnd property.
     * 
     */
    public void setProcessInstanceEnd(boolean value) {
        this.processInstanceEnd = value;
    }

}
