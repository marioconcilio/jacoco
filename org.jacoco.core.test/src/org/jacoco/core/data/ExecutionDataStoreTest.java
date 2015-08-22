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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ControlFlowExecutionDataStore}.
 */
public class ExecutionDataStoreTest implements IExecutionDataVisitor {

	private ControlFlowExecutionDataStore store;

	private Map<Long, ControlFlowExecutionData> dataOutput;

	@Before
	public void setup() {
		store = new ControlFlowExecutionDataStore();
		dataOutput = new HashMap<Long, ControlFlowExecutionData>();
	}

	@Test
	public void testEmpty() {
		assertNull(store.get(123));
		assertFalse(store.contains("org/jacoco/example/Foo"));
		store.accept(this);
		assertEquals(Collections.emptyMap(), dataOutput);
	}

	@Test
	public void testPut() {
		final boolean[] probes = new boolean[] { false, false, true };
		store.put(new ControlFlowExecutionData(1000, "Sample", probes));
		final ControlFlowExecutionData data = store.get(1000);
		assertSame(probes, data.getProbes());
		assertTrue(store.contains("Sample"));
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
				dataOutput);
	}

	@Test
	public void testGetContents() {
		final boolean[] probes = new boolean[] {};
		final ControlFlowExecutionData a = new ControlFlowExecutionData(1000, "A", probes);
		store.put(a);
		final ControlFlowExecutionData aa = new ControlFlowExecutionData(1000, "A", probes);
		store.put(aa);
		final ControlFlowExecutionData b = new ControlFlowExecutionData(1001, "B", probes);
		store.put(b);
		final Set<ControlFlowExecutionData> actual = new HashSet<ControlFlowExecutionData>(
				store.getContents());
		final Set<ControlFlowExecutionData> expected = new HashSet<ControlFlowExecutionData>(
				Arrays.asList(a, b));
		assertEquals(expected, actual);
	}

	@Test
	public void testGetWithoutCreate() {
		final ControlFlowExecutionData data = new ControlFlowExecutionData(1000, "Sample",
				new boolean[] {});
		store.put(data);
		assertSame(data, store.get(1000));
	}

	@Test
	public void testGetWithCreate() {
		final Long id = Long.valueOf(1000);
		final ControlFlowExecutionData data = store.get(id, "Sample", 3);
		assertEquals(1000, data.getId());
		assertEquals("Sample", data.getName());
		assertEquals(3, data.getProbes().length);
		assertFalse(data.getProbes()[0]);
		assertFalse(data.getProbes()[1]);
		assertFalse(data.getProbes()[2]);
		assertSame(data, store.get(id, "Sample", 3));
		assertTrue(store.contains("Sample"));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative1() {
		final boolean[] data = new boolean[] { false, false, true };
		store.put(new ControlFlowExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Other", 3);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative2() {
		final boolean[] data = new boolean[] { false, false, true };
		store.put(new ControlFlowExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Sample", 4);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutNegative() {
		final boolean[] data = new boolean[0];
		store.put(new ControlFlowExecutionData(1000, "Sample1", data));
		store.put(new ControlFlowExecutionData(1000, "Sample2", data));
	}

	@Test
	public void testMerge() {
		final boolean[] data1 = new boolean[] { false, true, false, true };
		store.visitClassExecution(new ControlFlowExecutionData(1000, "Sample", data1));
		final boolean[] data2 = new boolean[] { false, true, true, false };
		store.visitClassExecution(new ControlFlowExecutionData(1000, "Sample", data2));

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertTrue(result[2]);
		assertTrue(result[3]);
	}

	@Test(expected = IllegalStateException.class)
	public void testMergeNegative() {
		final boolean[] data1 = new boolean[] { false, false };
		store.visitClassExecution(new ControlFlowExecutionData(1000, "Sample", data1));
		final boolean[] data2 = new boolean[] { false, false, false };
		store.visitClassExecution(new ControlFlowExecutionData(1000, "Sample", data2));
	}

	@Test
	public void testSubtract() {
		final boolean[] data1 = new boolean[] { false, true, false, true };
		store.put(new ControlFlowExecutionData(1000, "Sample", data1));
		final boolean[] data2 = new boolean[] { false, false, true, true };
		store.subtract(new ControlFlowExecutionData(1000, "Sample", data2));

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertFalse(result[2]);
		assertFalse(result[3]);
	}

	@Test
	public void testSubtractOtherId() {
		final boolean[] data1 = new boolean[] { false, true };
		store.put(new ControlFlowExecutionData(1000, "Sample1", data1));
		final boolean[] data2 = new boolean[] { true, true };
		store.subtract(new ControlFlowExecutionData(2000, "Sample2", data2));

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);

		assertNull(store.get(2000));
	}

	@Test
	public void testSubtractStore() {
		final boolean[] data1 = new boolean[] { false, true, false, true };
		store.put(new ControlFlowExecutionData(1000, "Sample", data1));

		final ControlFlowExecutionDataStore store2 = new ControlFlowExecutionDataStore();
		final boolean[] data2 = new boolean[] { false, false, true, true };
		store2.put(new ControlFlowExecutionData(1000, "Sample", data2));

		store.subtract(store2);

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertFalse(result[2]);
		assertFalse(result[3]);
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[] data1 = new boolean[] { true, true, false };
		store.put(new ControlFlowExecutionData(1000, "Sample", data1));
		store.reset();
		final boolean[] data2 = store.get(1000).getProbes();
		assertNotNull(data2);
		assertFalse(data2[0]);
		assertFalse(data2[1]);
		assertFalse(data2[2]);
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(final ControlFlowExecutionData data) {
		dataOutput.put(Long.valueOf(data.getId()), data);
	}

}
