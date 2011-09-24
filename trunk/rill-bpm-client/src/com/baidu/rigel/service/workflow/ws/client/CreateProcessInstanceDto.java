
package com.baidu.rigel.service.workflow.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createProcessInstanceDto complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createProcessInstanceDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="businessObjectId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="processDefinitionKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="processStarter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startParams" type="{http://activiti.api.ws.workflow.service.rigel.baidu.com/}mapElementsArray" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createProcessInstanceDto", propOrder = {
    "businessObjectId",
    "processDefinitionKey",
    "processStarter",
    "startParams"
})
public class CreateProcessInstanceDto {

    protected String businessObjectId;
    protected String processDefinitionKey;
    protected String processStarter;
    protected MapElementsArray startParams;

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
     * Gets the value of the processStarter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessStarter() {
        return processStarter;
    }

    /**
     * Sets the value of the processStarter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessStarter(String value) {
        this.processStarter = value;
    }

    /**
     * Gets the value of the startParams property.
     * 
     * @return
     *     possible object is
     *     {@link MapElementsArray }
     *     
     */
    public MapElementsArray getStartParams() {
        return startParams;
    }

    /**
     * Sets the value of the startParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link MapElementsArray }
     *     
     */
    public void setStartParams(MapElementsArray value) {
        this.startParams = value;
    }

}
