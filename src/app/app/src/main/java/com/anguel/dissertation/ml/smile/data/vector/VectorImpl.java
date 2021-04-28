/*
 * Copyright (c) 2010-2020 Haifeng Li. All rights reserved.
 *
 * Smile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Smile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Smile.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.anguel.dissertation.ml.smile.data.vector;

import com.anguel.dissertation.ml.smile.data.measure.CategoricalMeasure;
import com.anguel.dissertation.ml.smile.data.measure.Measure;
import com.anguel.dissertation.ml.smile.data.measure.NumericalMeasure;
import com.anguel.dissertation.ml.smile.data.type.DataType;
import com.anguel.dissertation.ml.smile.data.type.DataTypes;
import com.anguel.dissertation.ml.smile.data.type.StructField;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * An immutable vector.
 *
 * @author Haifeng Li
 */
class VectorImpl<T> implements Vector<T> {
    /**
     * The name of vector.
     */
    private final String name;
    /**
     * The data type of vector.
     */
    private final DataType type;
    /**
     * Optional measure.
     */
    private final Measure measure;
    /**
     * The vector data.
     */
    private final T[] vector;

    /**
     * Constructor.
     */
    public VectorImpl(String name, Class<?> clazz, T[] vector) {
        this.name = name;
        this.type = DataTypes.object(clazz);
        this.measure = null;
        this.vector = vector;
    }

    /**
     * Constructor.
     */
    public VectorImpl(String name, DataType type, T[] vector) {
        this.name = name;
        this.type = type;
        this.measure = null;
        this.vector = vector;
    }

    /**
     * Constructor.
     */
    public VectorImpl(StructField field, T[] vector) {
        if (field.measure != null) {
            if ((field.type.isIntegral() && field.measure instanceof NumericalMeasure) ||
                    (field.type.isFloating() && field.measure instanceof CategoricalMeasure) ||
                    (!field.type.isIntegral() && !field.type.isFloating())) {
                throw new IllegalArgumentException(String.format("Invalid measure %s for %s", field.measure, type()));
            }
        }

        this.name = field.name;
        this.type = field.type;
        this.measure = field.measure;
        this.vector = vector;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataType type() {
        return type;
    }

    @Override
    public Measure measure() {
        return measure;
    }

    @Override
    public Object array() {
        return vector;
    }

    @Override
    public T get(int i) {
        return vector[i];
    }

    @Override
    public Vector<T> get(int... index) {
        @SuppressWarnings("unchecked")
        T[] v = (T[]) java.lang.reflect.Array.newInstance(vector.getClass().getComponentType(), index.length);
        for (int i = 0; i < index.length; i++) v[i] = vector[index[i]];
        return new VectorImpl<>(field(), v);
    }

    @Override
    public int size() {
        return vector.length;
    }

    @Override
    public Stream<T> stream() {
        return Arrays.stream(vector);
    }

    @Override
    public String toString() {
        return toString(10);
    }

    @Override
    public T[] toArray() {
        return vector;
    }

    @Override
    public double[] toDoubleArray() {
        if (!type.isNumeric()) throw new UnsupportedOperationException(name() + ":" + type());
        return stream().mapToDouble(d -> d == null ? Double.NaN : ((Number) d).doubleValue()).toArray();
    }

    @Override
    public double[] toDoubleArray(double[] a) {
        if (!type.isNumeric()) throw new UnsupportedOperationException(name() + ":" + type());
        for (int i = 0; i < vector.length; i++) {
            Number n = (Number) vector[i];
            a[i] = n == null ? Double.NaN : n.doubleValue();
        }
        return a;
    }

    @Override
    public int[] toIntArray() {
        if (!type.isIntegral()) throw new UnsupportedOperationException(name() + ":" + type());
        return stream().mapToInt(d -> d == null ? Integer.MIN_VALUE : ((Number) d).intValue()).toArray();
    }

    @Override
    public int[] toIntArray(int[] a) {
        if (!type.isIntegral()) throw new UnsupportedOperationException(name() + ":" + type());
        for (int i = 0; i < vector.length; i++) {
            Number n = (Number) vector[i];
            a[i] = n == null ? Integer.MIN_VALUE : n.intValue();
        }
        return a;
    }

}