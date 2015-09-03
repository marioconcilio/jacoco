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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.dua.Dua;
import org.jacoco.core.analysis.dua.DuaMethodCoverage;
import org.jacoco.core.analysis.dua.IDua;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseChain;
import br.usp.each.saeg.asm.defuse.DepthFirstDefUseChainSearch;
import br.usp.each.saeg.asm.defuse.Field;
import br.usp.each.saeg.asm.defuse.Local;
import br.usp.each.saeg.asm.defuse.Variable;

/**
 * A {@link MethodProbesVisitor} that analyzes which statements and branches of
 * a method has been executed based on given probe data.
 */
public class MethodAnalyzer {

	private final DuaMethodCoverage coverage;
	private final MethodNode methodNode;
	private final String className;
	private final boolean[] probes;
	private final int methodProbeIndex;

	/**
	 * New Method analyzer for the given probe data.
	 * 
	 * @param methodId
	 *            method Id
	 * @param className
	 *            class Name
	 * @param methodNode
	 *            method node
	 * 
	 * @param probes
	 *            recorded probe date of the containing class or
	 *            <code>null</code> if the class is not executed at all
	 * @param methodProbeIndex
	 */
	public MethodAnalyzer(final int methodId, final String className,
			final MethodNode methodNode, final boolean[] probes,
			final int methodProbeIndex) {
		super();
		this.className = className;
		this.methodNode = methodNode;
		this.probes = probes;
		this.methodProbeIndex = methodProbeIndex;
		this.coverage = new DuaMethodCoverage(methodId, methodNode.name,
				methodNode.desc, methodNode.signature,
				((methodNode.access & Opcodes.ACC_STATIC) != 0));
	}

	/**
	 * Returns the coverage data for this method after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this method
	 */
	public DuaMethodCoverage getCoverage() {
		return coverage;
	}

	/**
	 * Visits a method of the class.
	 */
	public void visit() {
		final DefUseAnalyzer analyzer = getAnalyzer();
		final DefUseChain[] duas = getDuas(analyzer);
		final Variable[] variables = analyzer.getVariables();
		final int[] lines = getLines();

		for (int i = 0; i < duas.length; i++) {
			final DefUseChain dua = duas[i];

			// def
			final int defLine = lines[dua.def];

			// use
			final int useLine = lines[dua.use];

			// target
			int targetLine = -1;
			if (dua.target != -1) {
				targetLine = lines[dua.target];
			}

			// var
			String varName = getVarName(dua, variables[dua.var]);
			if (varName == null) {
				varName = "random_" + Math.random();
			}

			// status
			final int status = getStatus(i);

			final IDua finalDua = new Dua(defLine, useLine, targetLine,
					varName, status);
			coverage.addDua(finalDua);
		}

	}

	private DefUseAnalyzer getAnalyzer() {
		final DefUseAnalyzer analyzer = new DefUseAnalyzer();
		try {
			analyzer.analyze(className, methodNode);
		} catch (final AnalyzerException e) {
			throw new RuntimeException(e);
		}
		return analyzer;
	}

	private DefUseChain[] getDuas(final DefUseAnalyzer analyzer) {
		final DefUseChain[] chains = new DepthFirstDefUseChainSearch().search(
				analyzer.getDefUseFrames(), analyzer.getVariables(),
				analyzer.getSuccessors(), analyzer.getPredecessors());

		final DefUseChain[] duas = DefUseChain.toBasicBlock(chains,
				analyzer.getLeaders(), analyzer.getBasicBlocks());
		return duas;
	}

	private int getStatus(final int i) {
		int status = ICounter.NOT_COVERED;
		if (probes[methodProbeIndex + i]) {
			status = ICounter.FULLY_COVERED;
		}
		return status;
	}

	private int[] getLines() {
		final int[] lines = new int[methodNode.instructions.size()];
		for (int i = 0; i < lines.length; i++) {
			if (methodNode.instructions.get(i) instanceof LineNumberNode) {
				final LineNumberNode insn = (LineNumberNode) methodNode.instructions
						.get(i);
				lines[methodNode.instructions.indexOf(insn.start)] = insn.line;
			}
		}

		int line = 1;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == 0) {
				lines[i] = line;
			} else {
				line = lines[i];
			}
		}
		return lines;
	}

	private String getVarName(final DefUseChain dua, final Variable var) {
		if (var instanceof Field) {
			return ((Field) var).name;
		} else {
			try {
				return getName(dua.use, ((Local) var).var, methodNode);
			} catch (final Exception e) {
				return null;
			}
		}
	}

	private String getName(final int insn, final int index, final MethodNode mn) {
		for (final LocalVariableNode local : mn.localVariables) {
			if (local.index == index) {
				final int start = mn.instructions.indexOf(local.start);
				final int end = mn.instructions.indexOf(local.end);
				if (insn >= start && insn < end) {
					return local.name;
				}
			}
		}
		throw new RuntimeException("Variable not found");
	}

}
