/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The matrix package provides comprehensive support for matrix and field data structures in GAMA.
 * 
 * <p>
 * This package contains the core interfaces and factories for working with two-dimensional data structures:
 * </p>
 * 
 * <h2>Main Components</h2>
 * <ul>
 * <li><strong>{@link gama.api.types.matrix.IMatrix}</strong> - The fundamental interface for all matrix types,
 * supporting generic content types and providing operations for element access, mathematical operations, and
 * conversions to other container types.</li>
 * 
 * <li><strong>{@link gama.api.types.matrix.IField}</strong> - A specialized matrix of doubles designed as a
 * lightweight replacement for grids. Fields support spatial operations, cell-based queries, multi-band data, diffusion,
 * and can handle "no-data" values for sparse datasets.</li>
 * 
 * <li><strong>{@link gama.api.types.matrix.GamaMatrixFactory}</strong> - The main entry point for creating matrix and
 * field instances. Provides static factory methods to construct matrices from various sources (values, lists,
 * expressions, other matrices) with automatic type inference and optimization.</li>
 * 
 * <li><strong>{@link gama.api.types.matrix.IMatrixFactory}</strong> - The internal factory interface implemented by
 * the core system. This allows for pluggable matrix implementations while maintaining a consistent API.</li>
 * </ul>
 * 
 * <h2>Matrix Types</h2>
 * <p>
 * Matrices can contain different data types:
 * </p>
 * <ul>
 * <li><strong>Integer matrices</strong> - Optimized storage for integer values</li>
 * <li><strong>Float matrices</strong> - Optimized storage for floating-point values</li>
 * <li><strong>Object matrices</strong> - Generic storage for any GAMA type (agents, geometries, etc.)</li>
 * <li><strong>Fields</strong> - Specialized double matrices with spatial awareness and multi-band support</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li>Two-dimensional addressable containers with {column, row} indexing</li>
 * <li>Matrix arithmetic operations (+, -, *, /)</li>
 * <li>Conversion between container types (lists, maps, matrices)</li>
 * <li>Parallel and sequential matrix initialization</li>
 * <li>Support for raster data import/export</li>
 * <li>Field-specific features: cell shapes, spatial queries, diffusion, multi-band data</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Creating matrices
 * IMatrix&lt;Integer&gt; intMatrix = GamaMatrixFactory.createIntMatrix(3, 3);
 * IMatrix&lt;Double&gt; floatMatrix = GamaMatrixFactory.createFloatMatrix(10, 10);
 * 
 * // Creating from values
 * IMatrix matrix = GamaMatrixFactory.createWithValue(scope, 0, GamaPointFactory.create(5, 5));
 * 
 * // Creating fields
 * IField field = GamaMatrixFactory.createField(scope, 100, 100);
 * field.setNoData(scope, -9999.0);
 * </pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
package gama.api.types.matrix;
