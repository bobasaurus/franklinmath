/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
 */
package franklinmath.util;

import java.util.*;
import java.io.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 *
 * @author Allen Jordan
 */
public class FunctionInformation {

    protected Vector<FunctionInfo> functionList;
    protected Vector<String> categoryList;

    public static class FunctionInfo {

        public String name,  category,  description,  exampleInput,  exampleResult;
        public boolean isMathFunction;
    }

    public FunctionInformation(String filename) throws IOException {
        //initialize an empty list of function information
        functionList = new Vector<FunctionInfo>();
        //initialize an empty list of function categories
        categoryList = new Vector<String>();

        try {
            //Read in the XML function list.  The java DOM API is fairly complex and difficult.  Might eventually switch back to the JDOM library.  
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //load the function xml file
            Document document = builder.parse(new File(filename));

            //starting from the root element of the document, parse out all functions
            Element root = document.getDocumentElement();
            NodeList functionNodeList = root.getElementsByTagName("function");
            for (int i = 0; i < functionNodeList.getLength(); i++) {
                //node representing a function
                org.w3c.dom.Node node = functionNodeList.item(i);
                if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    throw new IOException("XML function list parsing error; invalid node type");
                }
                if (!node.getNodeName().equals("function")) {
                    throw new IOException("XML function list parsing error; invalid element name");
                }

                //storage for an individual function's information
                FunctionInfo info = new FunctionInfo();

                //parse out information specific to the function
                NodeList functionDataList = node.getChildNodes();
                for (int j = 0; j < functionDataList.getLength(); j++) {
                    org.w3c.dom.Node dataNode = functionDataList.item(j);
                    String nodeName = dataNode.getNodeName();

                    if (nodeName.equals("name")) {
                        info.name = dataNode.getTextContent();
                    } else if (nodeName.equals("category")) {
                        info.category = dataNode.getTextContent();
                        //update the unique category list
                        if (!categoryList.contains(info.category)) {
                            categoryList.add(info.category);
                        }
                    } else if (nodeName.equals("is_math_function")) {
                        info.isMathFunction = (dataNode.getTextContent().equals("true")) ? true : false;
                    } else if (nodeName.equals("description")) {
                        info.description = dataNode.getTextContent();
                    } else if (nodeName.equals("example")) {
                        NodeList exampleDataList = dataNode.getChildNodes();
                        for (int k = 0; k < exampleDataList.getLength(); k++) {
                            org.w3c.dom.Node exampleNode = exampleDataList.item(k);
                            String exampleNodeName = exampleNode.getNodeName();
                            if (exampleNodeName.equals("input")) {
                                info.exampleInput = exampleNode.getTextContent();
                            } else if (exampleNodeName.equals("result")) {
                                info.exampleResult = exampleNode.getTextContent();
                            }
                        }
                    }
                }

                //save the function information in a list
                functionList.add(info);

            }

        } catch (ParserConfigurationException ex) {
            throw new IOException(ex.toString());
        } catch (org.xml.sax.SAXException ex) {
            throw new IOException(ex.toString());
        }

        //sort the list of unique categories
        Collections.sort(categoryList);

        if (functionList.size() == 0) {
            throw new IOException("XML function list parsing error; no functions were loaded");
        }
    }

    /**
     * Get the list of function information.  
     * @return  A vector containing information about each function.  
     */
    public Vector<FunctionInfo> GetFunctionList() {
        return functionList;
    }

    /**
     * Get the list of unique function categories.  
     * @return  A vector containing the unique function category names.  
     */
    public Vector<String> GetCategoryList() {
        return categoryList;
    }
}
