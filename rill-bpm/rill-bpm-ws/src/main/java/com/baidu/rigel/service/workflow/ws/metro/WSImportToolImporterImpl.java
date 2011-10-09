/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.metro;


import com.sun.xml.ws.api.server.ContainerResolver;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLServiceImpl;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import org.activiti.engine.impl.bpmn.data.StructureDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.webservice.WSOperation;
import org.activiti.engine.impl.webservice.WSService;
import org.xml.sax.SAXException;

/**
 * Use wsimport tools for XML importer implementation.
 * @author mengran
 */
public class WSImportToolImporterImpl implements XMLImporter {

    protected Map<String, WSService> wsServices = new HashMap<String, WSService>();
    protected Map<String, WSOperation> wsOperations = new HashMap<String, WSOperation>();
    protected Map<String, StructureDefinition> structures = new HashMap<String, StructureDefinition>();
    protected String wsdlLocation;
    protected String namespace;

    public WSImportToolImporterImpl() {
        this.namespace = "";
    }

    public void importFrom(Element element, BpmnParse parse) {
        this.namespace = element.attribute("namespace") == null ? "" : element.attribute("namespace") + ":";
        this.importFrom(element.attribute("location"));
        this.transferImportsToParse(parse);
    }

    private void transferImportsToParse(BpmnParse parse) {
        if (parse != null) {
            for (StructureDefinition structure : this.structures.values()) {
                parse.addStructure(structure);
            }
            for (WSService service : this.wsServices.values()) {
                parse.addService(service);
            }
            for (WSOperation operation : this.wsOperations.values()) {
                parse.addOperation(operation);
            }
        }
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
        } catch (ServiceConfigurationError e) {
            throw new WebServiceException(e);
        }
    }

    public void importFrom(String url, String namespace) {
    	
    	this.namespace = namespace == null ? "" : namespace + ":";
    	this.importFrom(url);
    }
    
    public void importFrom(String url) {
        this.wsServices.clear();
        this.wsOperations.clear();
        this.structures.clear();
        
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
                this.wsServices.put(this.namespace + wsService.getName(), wsService);
            }
            
            // FIXME: Comment it for i don't know what it means
//            this.importTypes(model.getPortTypes());
        } catch (MalformedURLException ex) {
            Logger.getLogger(WSImportToolImporterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private WSService importService(WSDLServiceImpl service) {
        String name = service.getName().getLocalPart();
        String location = "";

        WSService wsService = new WSService(this.namespace + name, location, this.wsdlLocation);
        
        for (WSDLPortImpl port : service.getPorts()) {
            location = port.getLocation().getPublicId();
            for (WSDLBoundOperationImpl operation : port.getBinding().getBindingOperations()) {
                WSOperation wsOperation = this.importOperation(operation, wsService);
                wsService.addOperation(wsOperation);

                this.wsOperations.put(this.namespace + operation.getName().getLocalPart(), wsOperation);
            }
        }
        
        return wsService;
    }

    private WSOperation importOperation(WSDLBoundOperationImpl operation, WSService service) {
        WSOperation wsOperation = new WSOperation(this.namespace + operation.getName().getLocalPart(), operation.getName().getLocalPart(), service);
        return wsOperation;
    }

    public Collection<StructureDefinition> getStructures() {
        return this.structures.values();
    }

    public Collection<WSService> getServices() {
        return this.wsServices.values();
    }

    public Collection<WSOperation> getOperations() {
        return this.wsOperations.values();
    }
}
