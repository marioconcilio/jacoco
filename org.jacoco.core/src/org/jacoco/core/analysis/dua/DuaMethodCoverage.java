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

import java.util.ArrayList;
import java.util.Collection;

public class DuaMethodCoverage implements IDuaMethodCoverage {

	private final int id;
	private final String name;
	private final String desc;
	private final String signature;
	private final Collection<IDua> duas;
	private final boolean isStatic;

	public DuaMethodCoverage(final int id, final String name, final String desc, final String signature, boolean isStatic) {
		super();
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		this.duas = new ArrayList<IDua>();
		this.isStatic=isStatic;
	}

	/**
	 * Add a dua to this method.
	 * 
	 * @param dua
	 *            dua to add
	 */
	public void addDua(final IDua dua) {
		this.duas.add(dua);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public String getSignature() {
		return signature;
	}

	public Collection<IDua> getDuas() {
		return duas;
	}

	@Override
	public String toString() {
		return "DuaMethodCoverage [id=" + id + ", name=" + name + ", desc=" + desc + ", signature=" + signature
				+ ", duas=" + duas + "]";
	}

	public boolean isStaticMethod() {
		return isStatic;
	}

}
