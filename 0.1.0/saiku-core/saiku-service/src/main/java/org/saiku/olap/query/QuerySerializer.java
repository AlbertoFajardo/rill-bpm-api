/*
 * Copyright (C) 2011 OSBI Ltd
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */
package org.saiku.olap.query;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.olap4j.Axis;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.saiku.olap.dto.SaikuCube;
import org.saiku.olap.util.exception.QueryParseException;

public class QuerySerializer {
    
    private IQuery query;
    Document dom;
	private SaikuCube saikuCube;
    
    public QuerySerializer(IQuery query) {
        this.query = query;
        this.saikuCube = query.getSaikuCube();
    }
    
    public String createXML() throws QueryParseException{
        if (this.query == null)
            throw new QueryParseException("Query object can not be null");
        
        try {
            createDocument();
            createDOMTree();
            
            XMLOutputter serializer = new XMLOutputter();
            Format format = Format.getPrettyFormat();
            StringWriter st = new StringWriter();
            serializer.setFormat(format);
            serializer.output(dom, st);

            return st.getBuffer().toString();
            
        } catch (Exception e) {
            throw new QueryParseException(e.getMessage(),e);
        }
        
    }
    
    private void createDocument() throws ParserConfigurationException {
        dom = new Document();
    }
    
    private void createDOMTree(){

        Element rootEle = new Element("Query");
        
        if (StringUtils.isNotBlank(query.getName())) {
            rootEle.setAttribute("name", query.getName());
        }
        
        if (StringUtils.isNotBlank(query.getType().toString())) {
            rootEle.setAttribute("type", query.getType().toString());
        }

        String cubeName = query.getCube().getUniqueName();

        if (StringUtils.isNotBlank(saikuCube.getConnectionName())) {
            rootEle.setAttribute("connection", saikuCube.getConnectionName());
        }

        if (StringUtils.isNotBlank(cubeName)) {
            rootEle.setAttribute("cube", cubeName);
        }
        
        if (StringUtils.isNotBlank(saikuCube.getCatalogName())) {
            rootEle.setAttribute("catalog", saikuCube.getCatalogName());
        }
        
        if (StringUtils.isNotBlank(saikuCube.getSchemaName())) {
            rootEle.setAttribute("schema", saikuCube.getSchemaName());
        }
        if (IQuery.QueryType.QM.equals(query.getType())) {
        	rootEle = appendQmQuery(rootEle);
        }
        rootEle = appendMdxQuery(rootEle);
        
        dom.setRootElement(rootEle);

    }
    
    private Element appendQmQuery(Element rootElement) {
        
        if (this.query != null) {
            Element qm = new Element("QueryModel");
            
            qm = appendAxes(qm);
            rootElement.addContent(qm);
        }
        return rootElement;
    }

    private Element appendMdxQuery(Element rootElement) {
        Element mdx = new Element("MDX");
        if (StringUtils.isNotBlank(this.query.getMdx())) {
            mdx.setText(this.query.getMdx());

        }
        rootElement.addContent(mdx);
        
        return rootElement;
    }
    
    private Element appendAxes(Element rootElement) {
        
        Element axes = new Element("Axes");
        
        QueryAxis rows =  ((OlapQuery)query).getAxes().get(Axis.ROWS);
        if (rows != null) {
            Element rowsElement = createAxisElement(rows);
            axes.addContent(rowsElement);
        }
        
        QueryAxis columns =  ((OlapQuery)query).getAxes().get(Axis.COLUMNS);
        if (columns != null) {
            Element columnsElement = createAxisElement(columns);
            axes.addContent(columnsElement);
        }
        
        QueryAxis filters =  ((OlapQuery)query).getAxes().get(Axis.FILTER);
        if (filters != null) {
            Element filtersElement = createAxisElement(filters);
            axes.addContent(filtersElement);
        }
        
        QueryAxis pages =  ((OlapQuery)query).getAxes().get(Axis.PAGES);
        if (pages != null) {
            Element pagesElement = createAxisElement(pages);
            axes.addContent(pagesElement);
        }
        
        rootElement.addContent(axes);
        return rootElement;
        
        
    }
    
    private Element createAxisElement(QueryAxis axis) {
        Element axisElement = new Element("Axis");
        axisElement.setAttribute("location",getAxisName(axis));
        
//        if (axis.isNonEmpty() == true) {
            axisElement.setAttribute("nonEmpty", "" + axis.isNonEmpty());
//        }
        
        
        if (axis.getSortOrder() != null) {
            axisElement.setAttribute("sortOrder", axis.getSortOrder().toString());
        }
        
        if (StringUtils.isNotBlank(axis.getSortIdentifierNodeName())) {
            axisElement.setAttribute("sortEvaluationLiteral", axis.getSortIdentifierNodeName());
        }
        
        Element dimensions = new Element("Dimensions");
        
        
        for (QueryDimension dim : axis.getDimensions()) {
            Element d = createDimensionElement(dim);
            dimensions.addContent(d);
        }
        if (axis.getDimensions().size() > 0) {
            axisElement.addContent(dimensions);
        }
        
        return axisElement;
    }
    
    private Element createDimensionElement(QueryDimension dim) {
        Element dimension = new Element("Dimension");
        dimension.setAttribute("name", dim.getDimension().getName());
        if (dim.getSortOrder() != null) {
            dimension.setAttribute("sortOrder", dim.getSortOrder().toString());
        }
        if (dim.getHierarchizeMode() != null) {
            dimension.setAttribute("hierarchizeMode", dim.getHierarchizeMode().toString());
        }
        
        if (dim.getHierarchizeMode() != null) {
            dimension.setAttribute("hierarchyConsistent", "" + dim.isHierarchyConsistent());
        }
        
        Element inclusionsElement = new Element("Inclusions");
        List<Selection> inclusions = dim.getInclusions();
        
        inclusionsElement = createSelectionsElement(inclusionsElement, inclusions);
        dimension.addContent(inclusionsElement);
        
        Element exclusionsElement = new Element("Exclusions");
        List<Selection> exclusions = dim.getExclusions();
        
        inclusionsElement = createSelectionsElement(exclusionsElement, exclusions);
        dimension.addContent(inclusionsElement);
        
        return dimension;
    }
    
    private Element createSelectionsElement(Element rootElement, List<Selection> selections) {
        for (Selection sel : selections) {
            Element selection = new Element("Selection");
            if (sel.getDimension() != null)
                selection.setAttribute("dimension", sel.getDimension().getName());
            if ((sel.getRootElement() instanceof Level)) {
            	selection.setAttribute("type", "level");
            } else if ((sel.getRootElement() instanceof Member)) {
            	selection.setAttribute("type", "member");
            }
            selection.setAttribute("node", sel.getUniqueName());
            selection.setAttribute("operator", sel.getOperator().toString());
            
            if (sel.getSelectionContext() != null && sel.getSelectionContext().size() > 0) {
                Element context = new Element("Context");
                context = createSelectionsElement(context, sel.getSelectionContext());
                selection.addContent(context);
            }
            rootElement.addContent(selection);
        }
        return rootElement;
    }

    public String getAxisName(QueryAxis axis) {
        switch(axis.getLocation().axisOrdinal()) {
        case -1: return "FILTER";
        case  0: return "COLUMNS";
        case  1: return "ROWS";
        case  2: return "PAGES";
        case  3: return "CHAPTERS";
        case  4: return "SECTIONS";
        default: throw new RuntimeException("Unsupported Axis-Ordinal: " + axis.getLocation().axisOrdinal());
        }
    }
    

}
