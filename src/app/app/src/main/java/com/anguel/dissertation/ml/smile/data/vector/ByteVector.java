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

import android.annotation.SuppressLint;

import com.anguel.dissertation.ml.smile.data.type.DataType;
import com.anguel.dissertation.ml.smile.data.type.DataTypes;
import com.anguel.dissertation.ml.smile.data.type.StructField;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An immutable byte vector.
 *
 * @author Haifeng Li
 */
public interface ByteVector extends BaseVector<Byte, Integer, IntStream> {
    @Override
    default DataType type() {
        return DataTypes.ByteType;
    }

    @Override
    byte[] array();

    @Override
    ByteVector get(int... index);

    @Override
    default short getShort(int i) {
        return getByte(i);
    }

    @Override
    default int getInt(int i) {
        return getByte(i);
    }

    @Override
    default long getLong(int i) {
        return getByte(i);
    }

    @Override
    default float getFloat(int i) {
        return getByte(i);
    }

    @Override
    default double getDouble(int i) {
        return getByte(i);
    }

    /**
     * Returns the string representation of vector.
     *
     * @param n the number of elements to show.
     * @return the string representation of vector.
     */
    default String toString(int n) {
        @SuppressLint("DefaultLocale") String suffix = n >= size() ? "]" : String.format(", ... %,d more]", size() - n);
        return stream().limit(n).mapToObj(field()::toString).collect(Collectors.joining(", ", "[", suffix));
    }

    /**
     * Creates a named byte vector.
     *
     * @param name   the name of vector.
     * @param vector the data of vector.
     * @return the vector.
     */
    static ByteVector of(String name, byte[] vector) {
        return new ByteVectorImpl(name, vector);
    }

    /**
     * Creates a named byte vector.
     *
     * @param field  the struct field of vector.
     * @param vector the data of vector.
     * @return the vector.
     */
    static ByteVector of(StructField field, byte[] vector) {
        return new ByteVectorImpl(field, vector);
    }
}