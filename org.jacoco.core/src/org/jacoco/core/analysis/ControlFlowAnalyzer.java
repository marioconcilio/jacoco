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
package org.jacoco.core.analysis;

import org.jacoco.core.data.AbstractExecutionDataStore;
import org.jacoco.core.data.ControlFlowExecutionData;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.analysis.line.ClassAnalyzer;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * An {@link ControlFlowAnalyzer} instance processes a set of Java class files and
 * calculates coverage data for them. For each class file the result is reported
 * to a given {@link ICoverageVisitor} instance. In addition the
 * {@link ControlFlowAnalyzer} requires a {@link AbstractExecutionDataStore} instance that
 * holds the execution data for the classes to analyze. The {@link ControlFlowAnalyzer}
 * offers several methods to analyze classes from a variety of sources.
 */
public class ControlFlowAnalyzer extends AbstractAnalyzer {

	private final AbstractExecutionDataStore executionData;

	private final ICoverageVisitor coverageVisitor;

	private final StringPool stringPool;

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param executionData
	 *            execution data
	 * @param coverageVisitor
	 *            the output instance that will coverage data for every analyzed
	 *            class
	 */
	public ControlFlowAnalyzer(final AbstractExecutionDataStore executionData,
			final ICoverageVisitor coverageVisitor) {
		this.executionData = executionData;
		this.coverageVisitor = coverageVisitor;
		this.stringPool = new StringPool();
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @param className
	 *            VM name of the class
	 * @return ASM visitor to write class definition to
	 */
	private ClassVisitor createAnalyzingVisitor(final long classid,
			final String className) {
		final ControlFlowExecutionData data = executionData.get(classid);
		final boolean[] probes;
		final boolean noMatch;
		if (data == null) {
			probes = null;
			noMatch = executionData.contains(className);
		} else {
			probes = data.getProbes();
			noMatch = false;
		}
		final ClassAnalyzer analyzer = new ClassAnalyzer(classid, noMatch,
				probes, stringPool) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				coverageVisitor.visitCoverage(getCoverage());
			}
		};
		return new ClassProbesAdapter(analyzer, false);
	}

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 */
	@Override
	public void analyzeClass(final ClassReader reader) {
		final ClassVisitor visitor = createAnalyzingVisitor(
				CRC64.checksum(reader.b), reader.getClassName());
		reader.accept(visitor, 0);
	}
}
