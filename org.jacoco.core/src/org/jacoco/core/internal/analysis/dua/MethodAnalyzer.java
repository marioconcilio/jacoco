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
import br.usp.each.saeg.asm.defuse.DefUseFrame;
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
	private final int methodProbeIndex;
	private final boolean[] probes;
	private int[][] basicBlocks;
	private int[] leaders;
	private Variable[] variables;

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
		final int[] lines = getLines();
		final DefUseChain[] duaI = getDuas(methodNode);
		int indexDua = 0;
		for (final DefUseChain defUseChain : duaI) {

			// transform given defusechain to BasicBlock
			final DefUseChain bbchain = toBB(defUseChain);

			if (bbchain != null) {
				final int defLine = lines[defUseChain.def];
				final int useLine = lines[defUseChain.use];
				int targetLines = -1;
				if (defUseChain.target != -1) {
					targetLines = lines[defUseChain.target];
				}
				String varName = getName(defUseChain);

				if (varName == null) {
					varName = "random_" + Math.random();
				}

				final int status = getStatus(indexDua);
				final IDua dua = new Dua(defLine, useLine, targetLines,
						varName, status);
				coverage.addDua(dua);

				indexDua++;
			}
		}
	}

	private DefUseChain toBB(final DefUseChain c) {
		if (DefUseChain.isGlobal(c, leaders, basicBlocks)) {
			return new DefUseChain(leaders[c.def], leaders[c.use],
					c.target == -1 ? -1 : leaders[c.target], c.var);
		}
		return null;
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

	private String getName(final DefUseChain dua) {
		final Variable var = variables[dua.var];
		String name;
		if (var instanceof Field) {
			name = ((Field) var).name;
		} else {
			try {
				name = varName(dua.use, ((Local) var).var, methodNode);
			} catch (final Exception e) {
				name = null;
			}
		}

		return name;
	}

	private String varName(final int insn, final int index, final MethodNode mn) {
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

	private DefUseChain[] getDuas(final MethodNode methodNode) {
		final DefUseAnalyzer analyzer = new DefUseAnalyzer();
		try {
			analyzer.analyze(className, methodNode);
		} catch (final AnalyzerException e) {
			throw new RuntimeException(e);
		}

		final DefUseFrame[] frames = analyzer.getDefUseFrames();
		variables = analyzer.getVariables();
		final int[][] successors = analyzer.getSuccessors();
		final int[][] predecessors = analyzer.getPredecessors();
		basicBlocks = analyzer.getBasicBlocks();
		leaders = analyzer.getLeaders();
		// defuse with instructions
		return new DepthFirstDefUseChainSearch().search(frames, variables,
				successors, predecessors);
	}
}
