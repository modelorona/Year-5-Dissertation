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

import com.anguel.dissertation.ml.smile.data.type.DataTypes;
import com.anguel.dissertation.ml.smile.data.type.StructField;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * A simple implementation of DataFrame that store columnar data in single machine's memory.
 *
 * @author Haifeng Li
 */
class DataFrameImpl implements DataFrame, Serializable {
    private static final long serialVersionUID = 2L;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataFrameImpl.class);

    /**
     * DataFrame schema.
     */
    private final StructType schema;
    /**
     * The column vectors.
     */
    private final List<BaseVector> columns;
    /**
     * The number of rows.
     */
    private final int size;

    /**
     * Constructor.
     *
     * @param columns The columns of data frame.
     */
    public DataFrameImpl(Collection<BaseVector> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Empty collection of columns");
        }

        this.columns = new ArrayList<>(columns);

        StructField[] fields = columns.stream()
                .map(BaseVector::field)
                .toArray(StructField[]::new);
        this.schema = DataTypes.struct(fields);

        Set<String> set = new HashSet<>();
        for (BaseVector v : columns) {
            if (!set.add(v.name())) {
                throw new IllegalArgumentException(String.format("Duplicated column name: %s", v.name()));
            }
        }

        BaseVector first = columns.iterator().next();
        this.size = first.size();
        for (BaseVector v : columns) {
            if (v.size() != first.size()) {
                throw new IllegalArgumentException(String.format("Column %s size %d != %d", v.name(), v.size(), first.size()));
            }
        }
    }

    /**
     * Constructor.
     *
     * @param data The data stream.
     */
    public DataFrameImpl(Stream<? extends Tuple> data, StructType schema) {
        this(data.collect(java.util.stream.Collectors.toList()), schema);
    }

    /**
     * Constructor.
     *
     * @param data The data collection.
     */
    public DataFrameImpl(List<? extends Tuple> data) {
        this(data, data.get(0).schema());
    }

    /**
     * Constructor.
     *
     * @param data The data collection.
     */
    public DataFrameImpl(List<? extends Tuple> data, StructType schema) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Empty tuple collections");
        }

        this.size = data.size();
        this.schema = schema;
        StructField[] fields = schema.fields();
        this.columns = new ArrayList<>(fields.length);

        for (int j = 0; j < fields.length; j++) {
            int i = 0;
            StructField field = fields[j];
            switch (field.type.id()) {
                case Integer: {
                    int[] values = new int[size];
                    for (Tuple datum : data) values[i++] = datum.getInt(j);
                    IntVector vector = IntVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Long: {
                    long[] values = new long[size];
                    for (Tuple datum : data) values[i++] = datum.getLong(j);
                    LongVector vector = LongVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Double: {
                    double[] values = new double[size];
                    for (Tuple datum : data) values[i++] = datum.getDouble(j);
                    DoubleVector vector = DoubleVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Float: {
                    float[] values = new float[size];
                    for (Tuple datum : data) values[i++] = datum.getFloat(j);
                    FloatVector vector = FloatVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Boolean: {
                    boolean[] values = new boolean[size];
                    for (Tuple datum : data) values[i++] = datum.getBoolean(j);
                    BooleanVector vector = BooleanVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Byte: {
                    byte[] values = new byte[size];
                    for (Tuple datum : data) values[i++] = datum.getByte(j);
                    ByteVector vector = ByteVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Short: {
                    short[] values = new short[size];
                    for (Tuple datum : data) values[i++] = datum.getShort(j);
                    ShortVector vector = ShortVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case Char: {
                    char[] values = new char[size];
                    for (Tuple datum : data) values[i++] = datum.getChar(j);
                    CharVector vector = CharVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                case String: {
                    String[] values = new String[size];
                    for (Tuple datum : data) values[i++] = datum.getString(j);
                    StringVector vector = StringVector.of(field, values);
                    columns.add(vector);
                    break;
                }

                default: {
                    Object[] values = new Object[size];
                    for (Tuple datum : data) values[i++] = datum.get(j);
                    Vector<?> vector = Vector.of(field, values);
                    columns.add(vector);
                }
            }
        }
    }

    /**
     * Constructor.
     *
     * @param vectors The column vectors.
     */
    public DataFrameImpl(BaseVector... vectors) {
        this(Arrays.asList(vectors));
    }

    @Override
    public StructType schema() {
        return schema;
    }

    @Override
    public String toString() {
        return toString(10, true);
    }

    @Override
    public Iterator<BaseVector> iterator() {
        return columns.iterator();
    }

    @Override
    public int indexOf(String name) {
        return schema.indexOf(name);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int ncol() {
        return columns.size();
    }

    @Override
    public Object get(int i, int j) {
        return columns.get(j).get(i);
    }

    @Override
    public Stream<Tuple> stream() {
        Spliterator<Tuple> spliterator = new DatasetSpliterator<>(this, Spliterator.ORDERED);
        return java.util.stream.StreamSupport.stream(spliterator, true);
    }

    @Override
    public BaseVector column(int i) {
        return columns.get(i);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Vector<T> vector(int i) {
        return (Vector<T>) columns.get(i);
    }

    @Override
    public BooleanVector booleanVector(int i) {
        return (BooleanVector) columns.get(i);
    }

    @Override
    public CharVector charVector(int i) {
        return (CharVector) columns.get(i);
    }

    @Override
    public ByteVector byteVector(int i) {
        return (ByteVector) columns.get(i);
    }

    @Override
    public ShortVector shortVector(int i) {
        return (ShortVector) columns.get(i);
    }

    @Override
    public IntVector intVector(int i) {
        return (IntVector) columns.get(i);
    }

    @Override
    public LongVector longVector(int i) {
        return (LongVector) columns.get(i);
    }

    @Override
    public FloatVector floatVector(int i) {
        return (FloatVector) columns.get(i);
    }

    @Override
    public DoubleVector doubleVector(int i) {
        return (DoubleVector) columns.get(i);
    }

    @Override
    public StringVector stringVector(int i) {
        return (StringVector) columns.get(i);
    }

    @Override
    public DataFrame select(int... cols) {
        List<BaseVector> sub = new ArrayList<>();
        for (int col : cols) {
            sub.add(columns.get(col));
        }

        return new DataFrameImpl(sub);
    }

    @Override
    public DataFrame drop(int... cols) {
        List<BaseVector> sub = new ArrayList<>(columns);
        List<BaseVector> drops = new ArrayList<>();
        for (int col : cols) {
            drops.add(columns.get(col));
        }
        sub.removeAll(drops);

        return new DataFrameImpl(sub);
    }

    @Override
    public DataFrame merge(DataFrame... dataframes) {
        for (DataFrame df : dataframes) {
            if (df.size() != size()) {
                throw new IllegalArgumentException("Merge data frames with different size: " + size() + " vs " + df.size());
            }
        }

        List<BaseVector> all = new ArrayList<>(columns);
        for (DataFrame df : dataframes) {
            for (int i = 0; i < df.ncol(); i++) {
                all.add(df.column(i));
            }
        }

        return new DataFrameImpl(all);
    }

    @Override
    public DataFrame merge(BaseVector... vectors) {
        for (BaseVector vector : vectors) {
            if (vector.size() != size()) {
                throw new IllegalArgumentException("Merge data frames with different size: " + size() + " vs " + vector.size());
            }
        }

        List<BaseVector> columns = new ArrayList<>(this.columns);
        Collections.addAll(columns, vectors);
        return new DataFrameImpl(columns);
    }

    @Override
    public DataFrame union(DataFrame... dataframes) {
        for (DataFrame df : dataframes) {
            if (!schema.equals(df.schema())) {
                throw new IllegalArgumentException("Union data frames with different schema: " + schema + " vs " + df.schema());
            }
        }

        int nrow = nrow();
        for (DataFrame df : dataframes) {
            nrow += df.nrow();
        }

        // Single line solution
        // Stream.of(a, b).flatMap(Stream::of).toArray(Object[]::new)
        // It doesn't work for boolean, byte, char, short though.
        Object[] vectors = new Object[ncol()];
        for (int i = 0; i < vectors.length; i++) {
            BaseVector column = columns.get(i);
            switch (column.type().id()) {
                case Boolean:
                    vectors[i] = new boolean[nrow];
                    break;
                case Char:
                    vectors[i] = new char[nrow];
                    break;
                case Byte:
                    vectors[i] = new byte[nrow];
                    break;
                case Short:
                    vectors[i] = new short[nrow];
                    break;
                case Integer:
                    vectors[i] = new int[nrow];
                    break;
                case Long:
                    vectors[i] = new long[nrow];
                    break;
                case Float:
                    vectors[i] = new float[nrow];
                    break;
                case Double:
                    vectors[i] = new double[nrow];
                    break;
                default:
                    vectors[i] = new Object[nrow];
            }
            System.arraycopy(column.array(), 0, vectors[i], 0, nrow());
        }

        int destPos = nrow();
        for (DataFrame df : dataframes) {
            for (int i = 0; i < vectors.length; i++) {
                System.arraycopy(df.column(i).array(), 0, vectors[i], destPos, df.nrow());
            }
            destPos += df.nrow();
        }

        List<BaseVector> data = new ArrayList<>();
        for (int i = 0; i < vectors.length; i++) {
            BaseVector column = columns.get(i);
            switch (column.type().id()) {
                case Boolean:
                    data.add(BooleanVector.of(column.name(), (boolean[]) vectors[i]));
                    break;
                case Char:
                    data.add(CharVector.of(column.name(), (char[]) vectors[i]));
                    break;
                case Byte:
                    data.add(ByteVector.of(column.name(), (byte[]) vectors[i]));
                    break;
                case Short:
                    data.add(ShortVector.of(column.name(), (short[]) vectors[i]));
                    break;
                case Integer:
                    data.add(IntVector.of(column.name(), (int[]) vectors[i]));
                    break;
                case Long:
                    data.add(LongVector.of(column.name(), (long[]) vectors[i]));
                    break;
                case Float:
                    data.add(FloatVector.of(column.name(), (float[]) vectors[i]));
                    break;
                case Double:
                    data.add(DoubleVector.of(column.name(), (double[]) vectors[i]));
                    break;
                default:
                    data.add(Vector.of(column.name(), column.type(), (Object[]) vectors[i]));
            }
        }

        return new DataFrameImpl(data);
    }

    @Override
    public Tuple get(int i) {
        return new DataFrameRow(i);
    }

    /**
     * A row in data frame.
     */
    class DataFrameRow implements Tuple {
        /**
         * Row index.
         */
        private final int i;

        /**
         * Constructor.
         *
         * @param i the index of row.
         */
        DataFrameRow(int i) {
            this.i = i;
        }

        @Override
        public StructType schema() {
            return schema;
        }

        @Override
        public Object get(int j) {
            return DataFrameImpl.this.get(i, j);
        }

        @Override
        public boolean getBoolean(int j) {
            return ((BooleanVector) columns.get(j)).getBoolean(i);
        }

        @Override
        public char getChar(int j) {
            return ((CharVector) columns.get(j)).getChar(i);
        }

        @Override
        public byte getByte(int j) {
            return columns.get(j).getByte(i);
        }

        @Override
        public short getShort(int j) {
            return columns.get(j).getShort(i);
        }

        @Override
        public int getInt(int j) {
            return columns.get(j).getInt(i);
        }

        @Override
        public long getLong(int j) {
            return columns.get(j).getLong(i);
        }

        @Override
        public float getFloat(int j) {
            return columns.get(j).getFloat(i);
        }

        @Override
        public double getDouble(int j) {
            return columns.get(j).getDouble(i);
        }

        @Override
        public String toString() {
            return schema.toString(this);
        }
    }
}
