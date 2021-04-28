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

package com.anguel.dissertation.ml.smile.data;

import com.anguel.dissertation.ml.smile.data.type.StructType;
import com.anguel.dissertation.ml.smile.data.vector.BaseVector;
import com.anguel.dissertation.ml.smile.data.vector.BooleanVector;
import com.anguel.dissertation.ml.smile.data.vector.ByteVector;
import com.anguel.dissertation.ml.smile.data.vector.CharVector;
import com.anguel.dissertation.ml.smile.data.vector.DoubleVector;
import com.anguel.dissertation.ml.smile.data.vector.FloatVector;
import com.anguel.dissertation.ml.smile.data.vector.IntVector;
import com.anguel.dissertation.ml.smile.data.vector.LongVector;
import com.anguel.dissertation.ml.smile.data.vector.ShortVector;
import com.anguel.dissertation.ml.smile.data.vector.StringVector;
import com.anguel.dissertation.ml.smile.data.vector.Vector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A data frame with a new index instead of the default [0, n) row index.
 *
 * @author Haifeng Li
 */
public class IndexDataFrame implements DataFrame {
    /**
     * The underlying data frame.
     */
    private final DataFrame df;
    /**
     * The row index.
     */
    private final int[] index;

    /**
     * Constructor.
     *
     * @param df    The underlying data frame.
     * @param index The row index.
     */
    public IndexDataFrame(DataFrame df, int[] index) {
        this.df = df;
        this.index = index;
    }

    @Override
    public StructType schema() {
        return df.schema();
    }

    @Override
    public String toString() {
        return toString(10, true);
    }

    @Override
    public Iterator<BaseVector> iterator() {
        return df.iterator();
    }

    @Override
    public int indexOf(String name) {
        return df.indexOf(name);
    }

    @Override
    public int size() {
        return index.length;
    }

    @Override
    public int ncol() {
        return df.ncol();
    }

    @Override
    public Object get(int i, int j) {
        return df.get(index[i], j);
    }

    @Override
    public Stream<Tuple> stream() {
        return Arrays.stream(index).mapToObj(df::get);
    }

    @Override
    public BaseVector column(int i) {
        return df.column(i).get(index);
    }

    @Override
    public <T> Vector<T> vector(int i) {
        return df.<T>vector(i).get(index);
    }

    @Override
    public BooleanVector booleanVector(int i) {
        return df.booleanVector(i).get(index);
    }

    @Override
    public CharVector charVector(int i) {
        return df.charVector(i).get(index);
    }

    @Override
    public ByteVector byteVector(int i) {
        return df.byteVector(i).get(index);
    }

    @Override
    public ShortVector shortVector(int i) {
        return df.shortVector(i).get(index);
    }

    @Override
    public IntVector intVector(int i) {
        return df.intVector(i).get(index);
    }

    @Override
    public LongVector longVector(int i) {
        return df.longVector(i).get(index);
    }

    @Override
    public FloatVector floatVector(int i) {
        return df.floatVector(i).get(index);
    }

    @Override
    public DoubleVector doubleVector(int i) {
        return df.doubleVector(i).get(index);
    }

    @Override
    public StringVector stringVector(int i) {
        return df.stringVector(i).get(index);
    }

    @Override
    public DataFrame select(int... cols) {
        return new IndexDataFrame(df.select(cols), index);
    }

    @Override
    public DataFrame drop(int... cols) {
        return new IndexDataFrame(df.drop(cols), index);
    }

    @Override
    public DataFrame merge(DataFrame... dataframes) {
        return null;
    }

    @Override
    public DataFrame merge(BaseVector... vectors) {
        return null;
    }

    @Override
    public DataFrame union(DataFrame... dataframes) {
        return null;
    }

    @Override
    public Tuple get(int i) {
        return df.get(index[i]);
    }
}
