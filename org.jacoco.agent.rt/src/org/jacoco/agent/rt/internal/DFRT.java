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
package org.jacoco.agent.rt.internal;

import org.jacoco.core.runtime.DataFlowRuntimeData;

public final class DFRT {

	private static DataFlowRuntimeData DATA;

	static {
		DATA = (DataFlowRuntimeData) Agent.getInstance().getData();
	}

	private DFRT() {
		// No instances
	}

	public static void init() {
	}

	public static long[] getData(final long classId, final String className,
			final int size) {
		return DATA.getExecutionData(classId, className, size).getLongProbes();
	}

}
