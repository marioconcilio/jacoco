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

public class Dua implements IDua {

	private final int index;

	private final int def;

	private final int use;

	private final int target;

	private final String var;

	private final int status;

	public Dua(final int index, final int def, final int use, final int target,
			final String var, final int status) {
		this.index = index;
		this.def = def;
		this.use = use;
		this.target = target;
		this.var = var;
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.analysis.IDua#getIndex()
	 */
	public int getIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.analysis.IDua#getDef()
	 */
	public int getDef() {
		return def;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.analysis.IDua#getUse()
	 */
	public int getUse() {
		return use;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.analysis.IDua#getTarget()
	 */
	public int getTarget() {
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.analysis.IDua#getVar()
	 */
	public String getVar() {
		return var;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.analysis.IDua#getVar()
	 */
	public int getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "Dua [def=" + def + ", use=" + use + ", target=" + target
				+ ", var=" + var + ", status=" + status + "]";
	}

}
