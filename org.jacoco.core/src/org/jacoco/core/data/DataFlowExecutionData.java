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
package org.jacoco.core.data;

import java.util.Arrays;

import br.usp.each.saeg.commons.BitSetIterator;
import br.usp.each.saeg.commons.BitSetUtils;

public class DataFlowExecutionData extends ControlFlowExecutionData {

	private final long[] longProbes;

	public DataFlowExecutionData(final long id, final String name,
			final long[] probes) {
		super(id, name, new boolean[] {});
		this.longProbes = probes;
	}

	public DataFlowExecutionData(final long id, final String name,
			final int probeCount) {
		super(id, name, new boolean[] {});
		this.longProbes = new long[probeCount];
	}

	@Override
	public boolean[] getProbes() {
		final boolean[] booleanProbes = new boolean[longProbes.length * 64];
		final BitSetIterator it = new BitSetIterator(
				BitSetUtils.valueOf(longProbes));
		while (it.hasNext()) {
			booleanProbes[it.next()] = true;
		}
		this.probes = booleanProbes;
		return booleanProbes;
	}

	public long[] getLongProbes() {
		return longProbes;
	}

	/**
	 * Sets all probes to <code>false</code>.
	 */
	@Override
	public void reset() {
		Arrays.fill(probes, false);
		Arrays.fill(longProbes, 0);
	}

}
