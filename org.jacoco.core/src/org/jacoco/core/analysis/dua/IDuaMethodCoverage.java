/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis.dua;

import java.util.Collection;

/**
 * Coverage data of a single method.
 */
public interface IDuaMethodCoverage {

	/**
	 * Returns the id of this node. The first method id is equals to 1, the
	 * second equals to 2, and so on.
	 * 
	 * @return name of this node
	 */
	public int getId();

	/**
	 * Returns the name of this node.
	 * 
	 * @return name of this node
	 */
	public String getName();

	/**
	 * Returns the parameter description of the method.
	 * 
	 * @return parameter description
	 */
	public String getDesc();

	/**
	 * Returns the generic signature of the method if defined.
	 * 
	 * @return generic signature or <code>null</code>
	 */
	public String getSignature();

	/**
	 * Returns the duas included in this method.
	 * 
	 * @return duas of this method
	 */
	public Collection<IDua> getDuas();
	
	
	public boolean isStaticMethod();

}