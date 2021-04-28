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

import com.anguel.dissertation.ml.smile.data.measure.CategoricalMeasure;
import com.anguel.dissertation.ml.smile.data.measure.Measure;
import com.anguel.dissertation.ml.smile.data.type.StructField;
import com.anguel.dissertation.ml.smile.data.type.StructType;

import java.io.Serializable;

/**
 * A tuple is an immutable finite ordered list (sequence) of elements.
 * Allows both generic access by ordinal, which will incur boxing overhead
 * for primitives, as well as native primitive access.
 * <p>
 * It is invalid to use the native primitive interface to retrieve a value
 * that is null, instead a user must check `isNullAt` before attempting
 * to retrieve a value that might be null.
 *
 * @author Haifeng Li
 */
public interface Tuple extends Serializable {
    /**
     * Returns the schema of tuple.
     *
     * @return the schema of tuple.
     */
    StructType schema();

    /**
     * Returns the number of elements in the Tuple.
     *
     * @return the number of elements in the Tuple.
     */
    default int length() {
        return schema().length();
    }

    /**
     * Returns the tuple as an array of doubles.
     *
     * @return the tuple as an array of doubles.
     */
    default double[] toArray() {
        return toArray(false, CategoricalEncoder.LEVEL);
    }

    /**
     * Return an array obtained by converting all the variables
     * in a data frame to numeric mode. Missing values/nulls will be
     * encoded as Double.NaN.
     *
     * @param bias    if true, add the first column of all 1's.
     * @param encoder the categorical variable encoder.
     * @return the tuple as an array of doubles.
     */
    default double[] toArray(boolean bias, CategoricalEncoder encoder) {
        int ncol = length();
        StructType schema = schema();

        int n = bias ? 1 : 0;
        for (int i = 0; i < ncol; i++) {
            StructField field = schema.field(i);

            Measure measure = field.measure;
            if (encoder != CategoricalEncoder.LEVEL && measure instanceof CategoricalMeasure) {
                CategoricalMeasure cat = (CategoricalMeasure) measure;

                if (encoder == CategoricalEncoder.DUMMY) {
                    n += cat.size() - 1;
                } else if (encoder == CategoricalEncoder.ONE_HOT) {
                    n += cat.size();
                }
            } else {
                n++;
            }
        }

        double[] array = new double[n];

        int j = 0;
        if (bias) {
            array[j++] = 1.0;
        }

        for (int i = 0; i < ncol; i++) {
            StructField field = schema.field(i);

            Measure measure = field.measure;
            if (encoder != CategoricalEncoder.LEVEL && measure instanceof CategoricalMeasure) {
                CategoricalMeasure cat = (CategoricalMeasure) measure;
                if (encoder == CategoricalEncoder.DUMMY) {
                    int k = cat.factor(getInt(i));
                    if (k > 0) array[j + k - 1] = 1.0;
                    j += cat.size() - 1;
                } else if (encoder == CategoricalEncoder.ONE_HOT) {
                    int k = cat.factor(getInt(i));
                    array[j + k] = 1.0;
                    j += cat.size();
                }
            } else {
                array[j++] = getDouble(i);
            }
        }

        return array;
    }

    /**
     * Returns the value at position i. The value may be null.
     *
     * @param i the index of field.
     * @return the field value.
     */
    default Object apply(int i) {
        return get(i);
    }

    /**
     * Returns the value by field name. The value may be null.
     *
     * @param field the name of field.
     * @return the field value.
     */
    default Object apply(String field) {
        return get(field);
    }

    /**
     * Returns the value at position i. The value may be null.
     *
     * @param i the index of field.
     * @return the field value.
     */
    Object get(int i);

    /**
     * Returns the value by field name. The value may be null.
     *
     * @param field the name of field.
     * @return the field value.
     */
    default Object get(String field) {
        return get(indexOf(field));
    }

    /**
     * Checks whether the value at position i is null.
     *
     * @param i the index of field.
     * @return true if the field value is null.
     */
    default boolean isNullAt(int i) {
        return get(i) == null;
    }

    /**
     * Returns the value at position i as a primitive boolean.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default boolean getBoolean(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive boolean.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default boolean getBoolean(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a primitive byte.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default char getChar(int i) {
        return getAs(i);
    }

    /**
     * Returns the value at position i as a primitive byte.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default byte getByte(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive byte.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default byte getByte(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a primitive short.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default short getShort(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive short.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default short getShort(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a primitive int.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default int getInt(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive int.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default int getInt(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a primitive long.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default long getLong(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive long.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default long getLong(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a primitive float.
     * Throws an exception if the type mismatches or if the value is null.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default float getFloat(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive float.
     * Throws an exception if the type mismatches or if the value is null.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default float getFloat(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a primitive double.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default double getDouble(int i) {
        return getAs(i);
    }

    /**
     * Returns the field value as a primitive double.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default double getDouble(String field) {
        return getAs(field);
    }

    /**
     * Returns the value at position i as a String object.
     *
     * @param i the index of field.
     * @return the field value.
     * @throws ClassCastException when data type does not match.
     */
    default String getString(int i) {
        Object obj = get(i);
        return obj == null ? null : schema().field(i).toString(obj);
    }

    /**
     * Returns the field value as a String object.
     *
     * @param field the name of field.
     * @return the field value.
     * @throws ClassCastException when data type does not match.
     */
    default String getString(String field) {
        return getString(indexOf(field));
    }

    /**
     * Returns the string representation of the value at position i.
     *
     * @param i the index of field.
     * @return the string representation of field value.
     */
    default String toString(int i) {
        Object o = get(i);
        if (o == null) return "null";

        if (o instanceof String) {
            return (String) o;
        } else {
            return schema().field(i).toString(o);
        }
    }

    /**
     * Returns the string representation of the field value.
     *
     * @param field the name of field.
     * @return the string representation of field value.
     */
    default String toString(String field) {
        return toString(indexOf(field));
    }

    /**
     * Returns the value at position i.
     * For primitive types if value is null it returns 'zero value' specific for primitive
     * ie. 0 for Int - use isNullAt to ensure that value is not null
     *
     * @param i   the index of field.
     * @param <T> the data type of field.
     * @return the field value.
     * @throws ClassCastException when data type does not match.
     */
    @SuppressWarnings("unchecked")
    default <T> T getAs(int i) {
        return (T) get(i);
    }

    /**
     * Returns the value of a given fieldName.
     * For primitive types if value is null it returns 'zero value' specific for primitive
     * ie. 0 for Int - use isNullAt to ensure that value is not null
     *
     * @param field the name of field.
     * @param <T>   the data type of field.
     * @return the field value.
     * @throws UnsupportedOperationException when schema is not defined.
     * @throws IllegalArgumentException      when fieldName do not exist.
     * @throws ClassCastException            when data type does not match.
     */
    default <T> T getAs(String field) {
        return getAs(indexOf(field));
    }

    /**
     * Returns the index of a given field name.
     *
     * @param field the name of field.
     * @return the field index.
     * @throws IllegalArgumentException when a field `name` does not exist.
     */
    default int indexOf(String field) {
        return schema().indexOf(field);
    }

    /**
     * Returns an object array based tuple.
     *
     * @param row    the object array.
     * @param schema the schema of tuple.
     * @return the tuple.
     */
    static Tuple of(Object[] row, StructType schema) {
        return new AbstractTuple() {
            @Override
            public Object get(int i) {
                return row[i];
            }

            @Override
            public StructType schema() {
                return schema;
            }
        };
    }

    /**
     * Returns a double array based tuple.
     *
     * @param row    the double array.
     * @param schema the schema of tuple.
     * @return the tuple.
     */
    static Tuple of(double[] row, StructType schema) {
        return new AbstractTuple() {
            @Override
            public Object get(int i) {
                return row[i];
            }

            @Override
            public double getDouble(int i) {
                return row[i];
            }

            @Override
            public StructType schema() {
                return schema;
            }
        };
    }

}