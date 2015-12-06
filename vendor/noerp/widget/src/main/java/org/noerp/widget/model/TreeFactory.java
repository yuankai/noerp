/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.noerp.widget.model;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.noerp.base.location.FlexibleLocation;
import org.noerp.base.util.UtilXml;
import org.noerp.base.util.cache.UtilCache;
import org.noerp.entity.Delegator;
import org.noerp.service.LocalDispatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * Widget Library - Tree factory class
 */
public class TreeFactory {

    public static final String module = TreeFactory.class.getName();

    public static final UtilCache<String, Map<String, ModelTree>> treeLocationCache = UtilCache.createUtilCache("widget.tree.locationResource", 0, 0, false);

    public static ModelTree getTreeFromLocation(String resourceName, String treeName, Delegator delegator, LocalDispatcher dispatcher)
            throws IOException, SAXException, ParserConfigurationException {
        Map<String, ModelTree> modelTreeMap = treeLocationCache.get(resourceName);
        if (modelTreeMap == null) {
            synchronized (TreeFactory.class) {
                modelTreeMap = treeLocationCache.get(resourceName);
                if (modelTreeMap == null) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    if (loader == null) {
                        loader = TreeFactory.class.getClassLoader();
                    }

                    URL treeFileUrl = null;
                    treeFileUrl = FlexibleLocation.resolveLocation(resourceName); //, loader);
                    Document treeFileDoc = UtilXml.readXmlDocument(treeFileUrl, true, true);
                    modelTreeMap = readTreeDocument(treeFileDoc, delegator, dispatcher, resourceName);
                    treeLocationCache.put(resourceName, modelTreeMap);
                }
            }
        }

        ModelTree modelTree = modelTreeMap.get(treeName);
        if (modelTree == null) {
            throw new IllegalArgumentException("Could not find tree with name [" + treeName + "] in class resource [" + resourceName + "]");
        }
        return modelTree;
    }

    public static Map<String, ModelTree> readTreeDocument(Document treeFileDoc, Delegator delegator, LocalDispatcher dispatcher, String treeLocation) {
        Map<String, ModelTree> modelTreeMap = new HashMap<String, ModelTree>();
        if (treeFileDoc != null) {
            // read document and construct ModelTree for each tree element
            Element rootElement = treeFileDoc.getDocumentElement();
            for (Element treeElement: UtilXml.childElementList(rootElement, "tree")) {
                ModelTree modelTree = new ModelTree(treeElement, treeLocation);
                modelTreeMap.put(modelTree.getName(), modelTree);
            }
        }
        return modelTreeMap;
    }
}