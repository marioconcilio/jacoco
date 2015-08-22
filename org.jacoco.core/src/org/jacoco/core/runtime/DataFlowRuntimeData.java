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
package org.jacoco.core.runtime;

import org.jacoco.core.data.DataFlowExecutionData;
import org.jacoco.core.data.DataFlowExecutionDataStore;

public class DataFlowRuntimeData extends AbstractRuntimeData {

	public DataFlowRuntimeData() {
		store = new DataFlowExecutionDataStore();
	}

	@Override
	public DataFlowExecutionData getExecutionData(final Long id,
			final String name, final int probecount) {
		synchronized (store) {
			return (DataFlowExecutionData) store.get(id, name, probecount);
		}
	}

}
