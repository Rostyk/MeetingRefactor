package com.innovaturelabs.xml.stream.events;

import com.innovaturelabs.xml.namespace.QName;

/**
 * An interface that contains information about a namespace.
 * Namespaces are accessed from a StartElement.
 *
 * @version 1.0
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @see StartElement
 */
public interface Namespace extends Attribute {

  /**
   * Gets the prefix, returns "" if this is a default
   * namespace declaration.
   */
  public String getPrefix();
  
  /**
   * Gets the uri bound to the prefix of this namespace
   */
  public String getNamespaceURI();

  /**
   * returns true if this attribute declares the default namespace 
   */
  public boolean isDefaultNamespaceDeclaration();
}
