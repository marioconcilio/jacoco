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

public final class ControlFlowExecutionDataStore extends AbstractExecutionDataStore {

	@Override
	public ControlFlowExecutionData get(final Long id, final String name,
			final int probecount) {
		ControlFlowExecutionData entry = (ControlFlowExecutionData) entries.get(id);
		if (entry == null) {
			entry = new ControlFlowExecutionData(id.longValue(), name, probecount);
			entries.put(id, entry);
			names.add(name);
		} else {
			entry.assertCompatibility(id.longValue(), name, probecount);
		}
		return entry;
	}
}
