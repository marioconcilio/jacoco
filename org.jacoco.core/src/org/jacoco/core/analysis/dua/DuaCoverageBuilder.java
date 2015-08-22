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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

///**
// * Builder for hierarchical {@link ICoverageNode} structures from single
// * {@link IClassCoverage} nodes. The nodes are feed into the builder through its
// * {@link ICoverageVisitor} interface. Afterwards the aggregated data can be
// * obtained with {@link #getClasses()}, {@link #getSourceFiles()} or
// * {@link #getBundle(String)} in the following hierarchy:
// * 
// * <pre>
// * {@link IBundleCoverage}
// * +-- {@link IPackageCoverage}*
// *     +-- {@link IClassCoverage}*
// *     +-- {@link ISourceFileCoverage}*
// * </pre>
// */
public class DuaCoverageBuilder implements IDuaCoverageVisitor {

	private final Map<String, IDuaClassCoverage> classes;

	// private final Map<String, ISourceFileCoverage> sourcefiles;

	/**
	 * Create a new builder.
	 * 
	 */
	public DuaCoverageBuilder() {
		this.classes = new HashMap<String, IDuaClassCoverage>();
		// this.sourcefiles = new HashMap<String, ISourceFileCoverage>();
	}

	/**
	 * Returns all class nodes currently contained in this builder.
	 * 
	 * @return all class nodes
	 */
	public Collection<IDuaClassCoverage> getClasses() {
		return Collections.unmodifiableCollection(classes.values());
	}

	public void visitCoverage(final IDuaClassCoverage coverage) {
		//SYSO System.out.println("DuaCoverageBuilder.visitCoverage");
		// Only consider classes that contain at least one method:
		if (coverage.getMethods().size() > 0) {
			//SYSO System.out.println("DuaCoverageBuilder.visitCoverage #methods > 0");
			final String name = coverage.getName();
			final IDuaClassCoverage dup = classes.put(name, coverage);
			if (dup != null && dup.getId() != coverage.getId()) {
				throw new IllegalStateException(
						"Can't add different class with same name: " + name);
			}
		}
	}

}