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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.AbstractExecutionDataStore;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.Pack200Streams;
import org.objectweb.asm.ClassReader;

/**
 * An {@link AbstractAnalyzer} instance processes a set of Java class files and
 * calculates coverage data for them. For each class file the result is reported
 * to a given {@link ICoverageVisitor} instance. In addition the
 * {@link AbstractAnalyzer} requires a {@link AbstractExecutionDataStore}
 * instance that holds the execution data for the classes to analyze. The
 * {@link AbstractAnalyzer} offers several methods to analyze classes from a
 * variety of sources.
 */
public abstract class AbstractAnalyzer {

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 */
	public abstract void analyzeClass(final ClassReader reader);

	/**
	 * Analyzes the class definition from a given in-memory buffer.
	 * 
	 * @param buffer
	 *            class definitions
	 * @param name
	 *            a name used for exception messages
	 * @throws IOException
	 *             if the class can't be analyzed
	 */
	public void analyzeClass(final byte[] buffer, final String name)
			throws IOException {
		try {
			analyzeClass(new ClassReader(buffer));
		} catch (final RuntimeException cause) {
			throw analyzerError(name, cause);
		}
	}

	/**
	 * Analyzes the class definition from a given input stream.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param name
	 *            a name used for exception messages
	 * @throws IOException
	 *             if the stream can't be read or the class can't be analyzed
	 */
	public void analyzeClass(final InputStream input, final String name)
			throws IOException {
		try {
			analyzeClass(new ClassReader(input));
		} catch (final RuntimeException e) {
			throw analyzerError(name, e);
		}
	}

	private IOException analyzerError(final String name,
			final RuntimeException cause) {
		final IOException ex = new IOException(String.format(
				"Error while analyzing class %s.", name));
		ex.initCause(cause);
		return ex;
	}

	/**
	 * Analyzes all classes found in the given input stream. The input stream
	 * may either represent a single class file, a ZIP archive, a Pack200
	 * archive or a gzip stream that is searched recursively for class files.
	 * All other content types are ignored.
	 * 
	 * @param input
	 *            input data
	 * @param name
	 *            a name used for exception messages
	 * @return number of class files found
	 * @throws IOException
	 *             if the stream can't be read or a class can't be analyzed
	 */
	public int analyzeAll(final InputStream input, final String name)
			throws IOException {
		final ContentTypeDetector detector = new ContentTypeDetector(input);
		switch (detector.getType()) {
		case ContentTypeDetector.CLASSFILE:
			analyzeClass(detector.getInputStream(), name);
			return 1;
		case ContentTypeDetector.ZIPFILE:
			return analyzeZip(detector.getInputStream(), name);
		case ContentTypeDetector.GZFILE:
			return analyzeGzip(detector.getInputStream(), name);
		case ContentTypeDetector.PACK200FILE:
			return analyzePack200(detector.getInputStream(), name);
		default:
			return 0;
		}
	}

	/**
	 * Analyzes all class files contained in the given file or folder. Class
	 * files as well as ZIP files are considered. Folders are searched
	 * recursively.
	 * 
	 * @param file
	 *            file or folder to look for class files
	 * @return number of class files found
	 * @throws IOException
	 *             if the file can't be read or a class can't be analyzed
	 */
	public int analyzeAll(final File file) throws IOException {
		int count = 0;
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				count += analyzeAll(f);
			}
		} else {
			final InputStream in = new FileInputStream(file);
			try {
				count += analyzeAll(in, file.getPath());
			} finally {
				in.close();
			}
		}
		return count;
	}

	/**
	 * Analyzes all classes from the given class path. Directories containing
	 * class files as well as archive files are considered.
	 * 
	 * @param path
	 *            path definition
	 * @param basedir
	 *            optional base directory, if <code>null</code> the current
	 *            working directory is used as the base for relative path
	 *            entries
	 * @return number of class files found
	 * @throws IOException
	 *             if a file can't be read or a class can't be analyzed
	 */
	public int analyzeAll(final String path, final File basedir)
			throws IOException {
		int count = 0;
		final StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		while (st.hasMoreTokens()) {
			count += analyzeAll(new File(basedir, st.nextToken()));
		}
		return count;
	}

	private int analyzeZip(final InputStream input, final String name)
			throws IOException {
		final ZipInputStream zip = new ZipInputStream(input);
		ZipEntry entry;
		int count = 0;
		while ((entry = zip.getNextEntry()) != null) {
			count += analyzeAll(zip, name + "@" + entry.getName());
		}
		return count;
	}

	private int analyzeGzip(final InputStream input, final String name)
			throws IOException {
		return analyzeAll(new GZIPInputStream(input), name);
	}

	private int analyzePack200(final InputStream input, final String name)
			throws IOException {
		return analyzeAll(Pack200Streams.unpack(input), name);
	}

}