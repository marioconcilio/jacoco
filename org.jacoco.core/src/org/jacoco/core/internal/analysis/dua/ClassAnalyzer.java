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
package org.jacoco.core.internal.analysis.dua;

import java.util.List;

import org.jacoco.core.analysis.dua.DuaClassCoverage;
import org.jacoco.core.analysis.dua.IDuaMethodCoverage;
import org.jacoco.core.internal.analysis.StringPool;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer {

	private final long classid;
	private final boolean probes[];
	private final List<MethodNode> methods;
	private final StringPool stringPool;
	private int methodProbeIndex = 0;

	private final DuaClassCoverage coverage;

	/**
	 * Creates a new analyzer that builds coverage data for a class.
	 * 
	 * @param classNode
	 *            node of the class
	 * @param probes
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 */
	public ClassAnalyzer(final ClassNode classNode, final boolean[] probes,
			final StringPool stringPool) {
		this.classid = classNode.name.hashCode();
		this.methods = classNode.methods;
		this.probes = probes;
		this.stringPool = stringPool;
		final String[] interfaces = classNode.interfaces
				.toArray(new String[classNode.interfaces.size()]);
		this.coverage = new DuaClassCoverage(stringPool.get(classNode.name),
				classid, stringPool.get(classNode.signature),
				stringPool.get(classNode.superName), stringPool.get(interfaces));
	}

	/**
	 * Returns the coverage data for this class after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this class
	 */
	public DuaClassCoverage getCoverage() {
		return coverage;
	}

	/**
	 * Visits the header of the class.
	 */
	public void visit() {
		int methodId = 0;
		for (final MethodNode method : methods) {
			// Does not instrument:
			// 1. Abstract methods
			if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
				continue;
			}
			// 2. Static class initialization
			if (method.name.equals("<clinit>")) {
				continue;
			}

			visitMethod(method, methodId++);
		}
	}

	/**
	 * Visits the source of the class.
	 * 
	 * @param source
	 *            the name of the source file from which the class was compiled.
	 */
	public void visitSource(final String source) {
		this.coverage.setSourceFileName(stringPool.get(source));
	}

	/**
	 * Visits a method of the class.
	 * 
	 * @param methodNode
	 *            method Node
	 * @param methodId
	 *            method Id
	 */
	public void visitMethod(final MethodNode methodNode, final int methodId) {
		final MethodAnalyzer methodAnalyzer = new MethodAnalyzer(methodId,
				coverage.getName(), methodNode, probes, methodProbeIndex);
		methodAnalyzer.visit();

		final IDuaMethodCoverage methodCoverage = methodAnalyzer.getCoverage();
		if (methodAnalyzer.duasBBsize > 0) {
			// Only consider methods that actually contain code
			coverage.addMethod(methodCoverage);
		}

		methodProbeIndex += ((methodAnalyzer.duasBBsize + 63) / 64) * 64;
	}

}
