
package com.baidu.rigel.service.workflow.ws.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.baidu.rigel.service.workflow.ws.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CompleteTaskInstance_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "completeTaskInstance");
    private final static QName _GetEngineProcessInstanceIdByBOId_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "getEngineProcessInstanceIdByBOId");
    private final static QName _CreateProcessInstanceResponse_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "createProcessInstanceResponse");
    private final static QName _CompleteTaskInstanceDto_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "completeTaskInstanceDto");
    private final static QName _GetEngineProcessInstanceIdByBOIdResponse_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "getEngineProcessInstanceIdByBOIdResponse");
    private final static QName _RemoteWorkflowResponse_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "remoteWorkflowResponse");
    private final static QName _CompleteTaskInstanceResponse_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "completeTaskInstanceResponse");
    private final static QName _CreateProcessInstance_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "createProcessInstance");
    private final static QName _CreateProcessInstanceDto_QNAME = new QName("http://activiti.api.ws.workflow.service.rigel.baidu.com/", "createProcessInstanceDto");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.baidu.rigel.service.workflow.ws.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RemoteWorkflowResponse }
     * 
     */
    public RemoteWorkflowResponse createRemoteWorkflowResponse() {
        return new RemoteWorkflowResponse();
    }

    /**
     * Create an instance of {@link CreateProcessInstanceDto }
     * 
     */
    public CreateProcessInstanceDto createCreateProcessInstanceDto() {
        return new CreateProcessInstanceDto();
    }

    /**
     * Create an instance of {@link CompleteTaskInstance }
     * 
     */
    public CompleteTaskInstance createCompleteTaskInstance() {
        return new CompleteTaskInstance();
    }

    /**
     * Create an instance of {@link CompleteTaskInstanceDto }
     * 
     */
    public CompleteTaskInstanceDto createCompleteTaskInstanceDto() {
        return new CompleteTaskInstanceDto();
    }

    /**
     * Create an instance of {@link CreateProcessInstance }
     * 
     */
    public CreateProcessInstance createCreateProcessInstance() {
        return new CreateProcessInstance();
    }

    /**
     * Create an instance of {@link GetEngineProcessInstanceIdByBOIdResponse }
     * 
     */
    public GetEngineProcessInstanceIdByBOIdResponse createGetEngineProcessInstanceIdByBOIdResponse() {
        return new GetEngineProcessInstanceIdByBOIdResponse();
    }

    /**
     * Create an instance of {@link CreateProcessInstanceResponse }
     * 
     */
    public CreateProcessInstanceResponse createCreateProcessInstanceResponse() {
        return new CreateProcessInstanceResponse();
    }

    /**
     * Create an instance of {@link MapElements }
     * 
     */
    public MapElements createMapElements() {
        return new MapElements();
    }

    /**
     * Create an instance of {@link CompleteTaskInstanceResponse }
     * 
     */
    public CompleteTaskInstanceResponse createCompleteTaskInstanceResponse() {
        return new CompleteTaskInstanceResponse();
    }

    /**
     * Create an instance of {@link MapElementsArray }
     * 
     */
    public MapElementsArray createMapElementsArray() {
        return new MapElementsArray();
    }

    /**
     * Create an instance of {@link GetEngineProcessInstanceIdByBOId }
     * 
     */
    public GetEngineProcessInstanceIdByBOId createGetEngineProcessInstanceIdByBOId() {
        return new GetEngineProcessInstanceIdByBOId();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteTaskInstance }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "completeTaskInstance")
    public JAXBElement<CompleteTaskInstance> createCompleteTaskInstance(CompleteTaskInstance value) {
        return new JAXBElement<CompleteTaskInstance>(_CompleteTaskInstance_QNAME, CompleteTaskInstance.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEngineProcessInstanceIdByBOId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "getEngineProcessInstanceIdByBOId")
    public JAXBElement<GetEngineProcessInstanceIdByBOId> createGetEngineProcessInstanceIdByBOId(GetEngineProcessInstanceIdByBOId value) {
        return new JAXBElement<GetEngineProcessInstanceIdByBOId>(_GetEngineProcessInstanceIdByBOId_QNAME, GetEngineProcessInstanceIdByBOId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateProcessInstanceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "createProcessInstanceResponse")
    public JAXBElement<CreateProcessInstanceResponse> createCreateProcessInstanceResponse(CreateProcessInstanceResponse value) {
        return new JAXBElement<CreateProcessInstanceResponse>(_CreateProcessInstanceResponse_QNAME, CreateProcessInstanceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteTaskInstanceDto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "completeTaskInstanceDto")
    public JAXBElement<CompleteTaskInstanceDto> createCompleteTaskInstanceDto(CompleteTaskInstanceDto value) {
        return new JAXBElement<CompleteTaskInstanceDto>(_CompleteTaskInstanceDto_QNAME, CompleteTaskInstanceDto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEngineProcessInstanceIdByBOIdResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "getEngineProcessInstanceIdByBOIdResponse")
    public JAXBElement<GetEngineProcessInstanceIdByBOIdResponse> createGetEngineProcessInstanceIdByBOIdResponse(GetEngineProcessInstanceIdByBOIdResponse value) {
        return new JAXBElement<GetEngineProcessInstanceIdByBOIdResponse>(_GetEngineProcessInstanceIdByBOIdResponse_QNAME, GetEngineProcessInstanceIdByBOIdResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoteWorkflowResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "remoteWorkflowResponse")
    public JAXBElement<RemoteWorkflowResponse> createRemoteWorkflowResponse(RemoteWorkflowResponse value) {
        return new JAXBElement<RemoteWorkflowResponse>(_RemoteWorkflowResponse_QNAME, RemoteWorkflowResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteTaskInstanceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "completeTaskInstanceResponse")
    public JAXBElement<CompleteTaskInstanceResponse> createCompleteTaskInstanceResponse(CompleteTaskInstanceResponse value) {
        return new JAXBElement<CompleteTaskInstanceResponse>(_CompleteTaskInstanceResponse_QNAME, CompleteTaskInstanceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateProcessInstance }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "createProcessInstance")
    public JAXBElement<CreateProcessInstance> createCreateProcessInstance(CreateProcessInstance value) {
        return new JAXBElement<CreateProcessInstance>(_CreateProcessInstance_QNAME, CreateProcessInstance.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateProcessInstanceDto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://activiti.api.ws.workflow.service.rigel.baidu.com/", name = "createProcessInstanceDto")
    public JAXBElement<CreateProcessInstanceDto> createCreateProcessInstanceDto(CreateProcessInstanceDto value) {
        return new JAXBElement<CreateProcessInstanceDto>(_CreateProcessInstanceDto_QNAME, CreateProcessInstanceDto.class, null, value);
    }

}
