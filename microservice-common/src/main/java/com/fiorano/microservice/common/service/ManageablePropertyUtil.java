/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.service;

import com.fiorano.openesb.microservice.ccp.event.common.data.ConfigurationProperty;
import com.fiorano.services.common.annotations.PropertyLeaf;
import com.fiorano.services.common.annotations.PropertyNode;
import com.fiorano.services.common.configuration.NamedConfigConstants;
import com.fiorano.services.common.configuration.NamedConfiguration;
import com.fiorano.services.common.jaxb.JAXBUtil;
import com.fiorano.util.StringUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: poorna
 * Date: Jun 21, 2008
 * Time: 11:13:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ManageablePropertyUtil {

    private final static Object[] OBJECT = new Object[0];

    public static void  setManageableProperties(Object propertyNode, Map<String, ConfigurationProperty> manageableProperties) {

        if(propertyNode == null || manageableProperties == null || manageableProperties.size() == 0) {
            return;
        }

        Method[] methods = propertyNode.getClass().getMethods();

        List<Method> annotatedMethods = new ArrayList<Method>();

        for (Method method : methods) {
            if (method.getAnnotations().length != 0) {
                annotatedMethods.add(method);
            }
        }

        Collections.sort(annotatedMethods, new AnnotationComparator());

        for (Method method : annotatedMethods) {
            Annotation[] annots = method.getAnnotations();

                for (Annotation annot : annots) {
                    if (PropertyNode.class.getName().equalsIgnoreCase(annot.annotationType().getName())) {
                        try {
                            Object pNode = method.invoke(propertyNode, OBJECT);
                            if(pNode != null) {
                                if (pNode instanceof NamedConfiguration) {
                                    String name = ((PropertyNode) annot).name();
                                    if (!StringUtil.isEmpty(name)) {
                                        setManageablePropForNamedConfig(name, pNode, manageableProperties);
                                    } else {
                                        setManageableProperties(pNode, manageableProperties);
                                    }
                                } else {
                                    setManageableProperties(pNode, manageableProperties);
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } else if ((PropertyLeaf.class.getName().equalsIgnoreCase(annot.annotationType().getName()))
                               && (method.getName().startsWith("set"))) {

                        PropertyLeaf resLeaf = (PropertyLeaf) annot;
                        String name = resLeaf.name();
                        String type = resLeaf.type();
                        ConfigurationProperty property = (ConfigurationProperty) ((Map) manageableProperties).get(name);
                        String value = property != null ? property.getValue() : null;
                        if (value != null) {
                            Object object = null;
                            try {
                                object = (Class.forName(type)).getConstructor(String.class).newInstance(value.toString());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (object != null)
                                    method.invoke(propertyNode, object);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                }
        }
    }

    private static void setManageablePropForNamedConfig(String propertyName, Object propertyNode,
                                                          Map<String, ConfigurationProperty> manageableProperties) {

        if (propertyNode instanceof NamedConfiguration) {

            ConfigurationProperty property = manageableProperties.get(propertyName);

            String value = property != null ? property.getValue() : null;
            if (value == null) {
                return;
            }

            String[] tokens = StringUtil.getTokens(value, "\n", true);
            if (tokens.length > 1) {
                try {
                    NamedConfiguration object = (NamedConfiguration) JAXBUtil.unmarshal(value);
                    JAXBUtil.copyProperties(object, propertyNode);
                  //  if (propertyNode instanceof PasswordEncryptionConfiguration) {
                  //      ((PasswordEncryptionConfiguration) propertyNode).setDecrypted(true);
                   // }
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                NamedConfiguration configuration = (NamedConfiguration) propertyNode;
                configuration.setConfigName(value);
            }

            return;
        }
    }

    public static Map<String, ConfigurationProperty> getManageableProperties(Object propertyNode) {
        Map<String, ConfigurationProperty> manageableProperties = new HashMap<String, ConfigurationProperty>();
        populateManageableProperties(propertyNode, manageableProperties);
        return manageableProperties;
    }

    public static void populateManageableProperties(Object propertyNode, Map<String, ConfigurationProperty> manageableProperties) {

        if (manageableProperties == null || propertyNode == null) {
            return;
        }
        Method[] methods = propertyNode.getClass().getMethods();

        for (Method method : methods) {
            Annotation[] annots = method.getAnnotations();

            for (Annotation annot : annots) {
                if (PropertyNode.class.getName().equalsIgnoreCase(annot.annotationType().getName())) {
                    try {
                        Object pNode = method.invoke(propertyNode, OBJECT);
                        if(pNode != null) {
                            if (pNode instanceof NamedConfiguration) {
                                String name = ((PropertyNode) annot).name();
                                if (!StringUtil.isEmpty(name)) {
                                    populateManageablePropForNamedConfig(name, pNode, manageableProperties);
                                } else {
                                    populateManageableProperties(pNode, manageableProperties);
                                }
                            } else {
                                populateManageableProperties(pNode, manageableProperties);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else if (PropertyLeaf.class.getName().equalsIgnoreCase(annot.annotationType().getName())
                        && !(method.getName().startsWith("set"))) {
                    PropertyLeaf resLeaf = (PropertyLeaf) annot;
                    ConfigurationProperty propertyObject = new ConfigurationProperty();
                    propertyObject.setName(resLeaf.name());
                    propertyObject.setType(resLeaf.type());
                    propertyObject.setEncrypted("yes".equalsIgnoreCase(resLeaf.isEncrypted()));
                    Object value = null;

                    try {
                        value = method.invoke(propertyNode, OBJECT);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (value != null) {
                        propertyObject.setValue(value.toString());
                    }
                    manageableProperties.put(resLeaf.name(), propertyObject);
                }
            }
        }
    }

    private static void populateManageablePropForNamedConfig(
            String propertyName, Object propertyNode, Map<String, ConfigurationProperty> manageableProperties) {

        if (propertyNode instanceof NamedConfiguration) {

            NamedConfiguration configuration = (NamedConfiguration) propertyNode;
            ConfigurationProperty propertyObject = new ConfigurationProperty();
            propertyObject.setName(propertyName);
            propertyObject.setType("NamedObject");
            propertyObject.setEncrypted(false);

            String value = null;

            if (!(StringUtil.isEmpty(configuration.getConfigName()))) {
                value = configuration.getConfigName();
                if (NamedConfigConstants.SERVICE_CONFIGURATION_CATEGORY.equals(
                        configuration.getObjectCategory().toString())) {
                    propertyObject.setConfigurationType(NamedConfigConstants.COMPONENT_CONFIG_TYPE);
                } else {
                    propertyObject.setConfigurationType(NamedConfigConstants.RESOURCE_CONFIG_TYPE);
                }
            } else {
                try {
                    value = JAXBUtil.marshal(configuration);
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }

            if (value != null) {
                propertyObject.setValue(value);
            }

            manageableProperties.put(propertyName, propertyObject);

            return;
        }
    }

    private static class AnnotationComparator implements Comparator<Method> {

        public int compare(Method method1, Method method2) {

            PropertyLeaf propertyLeaf1 = method1.getAnnotation(PropertyLeaf.class);
            PropertyLeaf propertyLeaf2 = method2.getAnnotation(PropertyLeaf.class);

            if (propertyLeaf1 != null && propertyLeaf2 != null) {
                return propertyLeaf1.index() - propertyLeaf2.index();
            } else if (propertyLeaf1 != null) {
                return 1;
            } else if (propertyLeaf2 != null) {
                return -1;
            }

            return -1;
        }
    }
}


