/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.ws.metro;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.data.PrimitiveStructureDefinition;
import org.activiti.engine.impl.bpmn.data.PrimitiveStructureInstance;
import org.activiti.engine.impl.bpmn.data.StructureInstance;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.bpmn.webservice.Operation;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.webservice.SyncWebServiceClient;
import org.activiti.engine.impl.webservice.WSOperation;
import org.activiti.engine.impl.webservice.WSService;
import org.apache.commons.lang.StringUtils;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ObjectUtils;
import org.xml.sax.SAXException;

import com.sun.xml.ws.api.server.ContainerResolver;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLServiceImpl;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * Use wsimport tools for XML importer implementation.
 * @author mengran
 */
public class WSImportToolImporterImpl implements BeanFactoryAware, InitializingBean {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
    protected Map<QName, WSService> wsServices = new HashMap<QName, WSService>();
    protected Map<QName, DynamicClientDelegateWSOperation> wsOperations = new HashMap<QName, DynamicClientDelegateWSOperation>();
    protected String wsdlLocation;
    protected XSSchemaSet xsSchemaSet;
    protected BeanFactory internalBeanFactory;
//    private ActivitiAccessor activitiAccessor;
        
    public void importFrom(String url, String namespace) {
    	
    	this.importFrom(url);
    }
    
    public void importFrom(String url) {
        this.wsServices.clear();
        this.wsOperations.clear();
        
        this.wsdlLocation = url;

        // Use JAX-WS Provider to pase wsdl
        URL wsdl = null;
        try {
            wsdl = new URL(url);
            Source source = new StreamSource(wsdl.toExternalForm());
            URL sourceURL = source.getSystemId() == null ? null : new URL(source.getSystemId());
            WSDLModelImpl model = parseWSDL(sourceURL, source);
            for (Entry<QName, WSDLServiceImpl> entry : model.getServices().entrySet()) {
                WSService wsService = this.importService(entry.getValue());
                this.wsServices.put(entry.getKey(), wsService);
            }
            
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "Exception occurred when import WS", ex);
            throw new ProcessException(ex);
        }
    }

    public void importSchema(String location, String xsdIndex) throws Exception {
        
    	xsdIndex = StringUtils.isEmpty(xsdIndex) ? "1" : new Integer(xsdIndex).toString();
    	String xsdLocation = location.replaceAll("wsdl", "xsd=" + xsdIndex);
    	UrlResource xjc = new UrlResource(xsdLocation);
        XSOMParser parser = new XSOMParser();
        parser.parse(xjc.getInputStream());
        xsSchemaSet = parser.getResult();
    }

    /**
     * Copy from WSServiceDelegate
     * 
     * Parses the WSDL and builds {@link com.sun.xml.ws.api.model.wsdl.WSDLModel}.
     * @param wsdlDocumentLocation
     *      Either this or <tt>wsdl</tt> parameter must be given.
     *      Null location means the system won't be able to resolve relative references in the WSDL,
     */
    private WSDLModelImpl parseWSDL(URL wsdlDocumentLocation, Source wsdlSource) {
        try {
            return RuntimeWSDLParser.parse(wsdlDocumentLocation, wsdlSource, XmlUtil.createDefaultCatalogResolver(),
                    true, ContainerResolver.getInstance().getContainer(), ServiceFinder.find(WSDLParserExtension.class).toArray());
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        } catch (SAXException e) {
            throw new WebServiceException(e);
        }
    }

    private WSService importService(WSDLServiceImpl service) {
        
        DynamicJaxwsClient dynamicJaxwsClient = new DynamicJaxwsClient();
        WSService wsService = new WSService(service.getName().toString(), this.wsdlLocation, dynamicJaxwsClient);
        dynamicJaxwsClient.setTarget(wsService);
        
        for (WSDLPortImpl port : service.getPorts()) {
            for (WSDLBoundOperationImpl operation : port.getBinding().getBindingOperations()) {
                DynamicClientDelegateWSOperation wsOperation = this.importOperation(operation, dynamicJaxwsClient);
                wsService.addOperation(wsOperation);

                this.wsOperations.put(operation.getName(), wsOperation);
            }
        }
        
        return wsService;
    }
    
    public static class DynamicJaxwsClient implements SyncWebServiceClient {

    	private static final Logger logger = Logger.getLogger(DynamicJaxwsClient.class.getName());
    	
    	private WSService target;
    	private AtomicReference<Dispatch<SOAPMessage>> dynamicJaxwsClient = new AtomicReference<Dispatch<SOAPMessage>>();
		
		public final WSService getTarget() {
			return target;
		}

		public final void setTarget(WSService target) {
			this.target = target;
		}

		private Dispatch<SOAPMessage> init() throws Exception {
			
			QName serviceQName = QName.valueOf(this.getTarget().getName());
			Service serviceStub = Service.create(new URL(this.target.getLocation()), serviceQName);
			// FIXME: MENGRAN. Unsafe code.
			QName portName = serviceStub.getPorts().next();
//			serviceStub.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, null);
			
			Dispatch<SOAPMessage> forReturn = serviceStub.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
			
			return forReturn;
		}

		@Override
		public Object[] send(String methodName, Object[] arguments)
				throws Exception {
			
			dynamicJaxwsClient.compareAndSet(null, init());
			
			/** Create SOAPMessage request. **/
			// compose a request message
			MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

			// Create a message.  This example works with the SOAPPART.
			SOAPMessage request = mf.createMessage();
			SOAPPart part = request.getSOAPPart();

			// Obtain the SOAPEnvelope and header and body elements.
			SOAPEnvelope env = part.getEnvelope();
			// Construct the message payload.
			QName methodQName = QName.valueOf(methodName);
//			env.addNamespaceDeclaration("ns1", methodQName.getNamespaceURI());
//			SOAPHeader header = env.getHeader();
			SOAPBody body = env.getBody();
			
			StringBuilder sb = new StringBuilder(" - Invoke method: " + methodQName.toString());
			SOAPElement operation = body.addChildElement(methodQName.getLocalPart(), "ns1", methodQName.getNamespaceURI());
			for (int i = 0; i < arguments.length; i++) {
				SOAPElement value = operation.addChildElement("arg" + i);
				value.addTextNode(ObjectUtils.getDisplayString(arguments[i]));
				sb.append(" -- method argument: " + value.getNodeName() + " " + value.getNodeValue());
			}
			request.saveChanges();
			
			// FIXME: MENGRAN. Not supported return value at this version.
			/** Invoke the service endpoint. SOAPMessage response =  **/
			SOAPMessage returnMessage = dynamicJaxwsClient.get().invoke(request);
			sb.append(" -- return value:" + returnMessage.getContentDescription());
			logger.info(sb.toString());
			
			return null;
		}
    	
    }
    
    public class DynamicClientDelegateWSOperation extends WSOperation {

    	private DynamicJaxwsClient djc;
    	
		public final DynamicJaxwsClient getDjc() {
			return djc;
		}

		public final void setDjc(DynamicJaxwsClient djc) {
			this.djc = djc;
		}

		public DynamicClientDelegateWSOperation(String id,
				String operationName, WSService service) {
			super(id, operationName, service);
		}

		@Override
		public MessageInstance sendFor(MessageInstance message,
				Operation operation) {
			
			try {
				this.djc.send(this.getId(), message.getStructureInstance().toArray());
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception occurred when call WS use dynamic client.", e);
			}
			
			// FIXME: MENGRAN. return null at this version.
			return null;
		}
		
		public MessageInstance generateInMessage(ActivityExecution execution) {
			
			QName operationQName = QName.valueOf(this.getId());
			XSComplexType type = xsSchemaSet.getComplexType(operationQName.getNamespaceURI(), operationQName.getLocalPart());
			if (type == null) {
				throw new ProcessException("Support complexType invoke only at this version." + this.getName());
			}
			
			List<PrimitiveStructureInstance> listInstance = new ArrayList<PrimitiveStructureInstance>();
			
	        XSContentType contentType = type.getContentType();
	        XSParticle particle = contentType.asParticle();
	        if(particle != null) {
	            XSTerm term = particle.getTerm();
	            if(term.isModelGroup()){
	                XSModelGroup xsModelGroup = term.asModelGroup();
	                XSParticle[] particles = xsModelGroup.getChildren();
	                for(int i = 0; i < particles.length; i++){
	                	XSParticle p = particles[i];
	                    XSTerm pterm = p.getTerm();
	                    if(pterm.isElementDecl()){ //xs:element inside complex type
	                        // FIXME: MENGRAN. Refactor with Visitor Design Pattern
	                    	if (listInstance.size() < 1 && "string".equals(pterm.asElementDecl().getType().getName())) {
	                    		PrimitiveStructureDefinition definition = new PrimitiveStructureDefinition(pterm.asElementDecl().getName(), String.class);
	                    		StructureInstance instance = definition.createInstance();
	                    		instance.loadFrom(new Object[] {execution.getProcessInstanceId()});
	                    		listInstance.add((PrimitiveStructureInstance) instance);
	                        } else if (listInstance.size() < 2 && "string".equals(pterm.asElementDecl().getType().getName())) {
	                        	PrimitiveStructureDefinition definition = new PrimitiveStructureDefinition(pterm.asElementDecl().getName(), String.class);
	                    		StructureInstance instance = definition.createInstance();
	                    		instance.loadFrom(new Object[] {execution.getActivity().getId()});
	                    		listInstance.add((PrimitiveStructureInstance) instance);
	                        } else {
	                        	throw new UnsupportedOperationException("Supported string type arguments at this version. " + pterm.asElementDecl().getType().getName());
	                        }
	                    }
	                }
	            }
	        }
	        
	        
			// FIXME: MENGRAN.
			return new MessageInstance(null, new ItemInstance(null, new ComplexPrimitiveStructureInstance(listInstance)));
		}
    	
    }
    
    public class ComplexPrimitiveStructureInstance implements StructureInstance {
    	
    	private List<PrimitiveStructureInstance> listStructureInstance; 
    	
		public ComplexPrimitiveStructureInstance(
				List<PrimitiveStructureInstance> listStructureInstance) {
			super();
			this.listStructureInstance = listStructureInstance;
		}

		@Override
		public Object[] toArray() {
			
			Object[] forReturn = new Object[listStructureInstance.size()];
			for (int i = 0; i < forReturn.length; i++) {
				forReturn[i] = listStructureInstance.get(i).getPrimitive();
			}
			
			return forReturn;
		}

		@Override
		public void loadFrom(Object[] array) {
			
			throw new UnsupportedOperationException();
		}
    	
    }
    private DynamicClientDelegateWSOperation importOperation(WSDLBoundOperationImpl operation, DynamicJaxwsClient serviceClient) {
    	DynamicClientDelegateWSOperation wsOperation = new DynamicClientDelegateWSOperation(operation.getName().toString(), operation.getName().getLocalPart(), serviceClient.getTarget());
    	wsOperation.setDjc(serviceClient);
    	
        return wsOperation;
    }

    public Collection<WSService> getServices() {
        return this.wsServices.values();
    }

    public Collection<DynamicClientDelegateWSOperation> getOperations() {
        return this.wsOperations.values();
    }
    
    public DynamicClientDelegateWSOperation getOperationByName(String name) {
    	
    	if (StringUtils.isEmpty(name)) {
    		return null;
    	}
    	QName operationQName = QName.valueOf(name);
    	return this.wsOperations.get(operationQName);
    }

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.internalBeanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		// Manually inject
//		WorkflowOperations workflowAccessor = this.internalBeanFactory.getBean(WorkflowOperations.class);
//		activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
	}
	
	
}
