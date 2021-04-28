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

import android.annotation.SuppressLint;

import com.anguel.dissertation.ml.smile.data.measure.CategoricalMeasure;
import com.anguel.dissertation.ml.smile.data.measure.Measure;
import com.anguel.dissertation.ml.smile.data.type.DataType;
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
import com.anguel.dissertation.ml.smile.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An immutable collection of data organized into named columns.
 *
 * @author Haifeng Li
 */
public interface DataFrame extends Dataset<Tuple>, Iterable<BaseVector> {
    /**
     * Returns the schema of DataFrame.
     *
     * @return the schema.
     */
    StructType schema();

    /**
     * Returns the column names.
     *
     * @return the column names.
     */
    default String[] names() {
        StructField[] fields = schema().fields();
        return Arrays.stream(fields)
                .map(field -> field.name)
                .collect(java.util.stream.Collectors.toList())
                .toArray(new String[fields.length]);
    }

    /**
     * Returns the column data types.
     *
     * @return the column data types.
     */
    default DataType[] types() {
        StructField[] fields = schema().fields();
        return Arrays.stream(fields)
                .map(field -> field.type)
                .collect(java.util.stream.Collectors.toList())
                .toArray(new DataType[fields.length]);
    }


    /**
     * Returns the column's level of measurements.
     *
     * @return the column's level of measurements.
     */
    default Measure[] measures() {
        StructField[] fields = schema().fields();
        return Arrays.stream(fields)
                .map(field -> field.measure)
                .toArray(Measure[]::new);
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows.
     */
    default int nrow() {
        return size();
    }

    /**
     * Returns the number of columns.
     *
     * @return the number of columns.
     */
    int ncol();

    /**
     * Returns the structure of data frame.
     *
     * @return the structure of data frame.
     */
    default DataFrame structure() {
        List<BaseVector> vectors = Arrays.asList(
                Vector.of("Column", String.class, names()),
                Vector.of("Type", DataType.class, types()),
                Vector.of("Measure", Measure.class, measures())
        );

        return new DataFrameImpl(vectors);
    }

    /**
     * Returns the cell at (i, j).
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     */
    default Object get(int i, int j) {
        return get(i).get(j);
    }

    /**
     * Returns the cell at (i, j).
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     */
    default Object get(int i, String column) {
        return get(i).get(column);
    }

    /**
     * Returns a new data frame with row indexing.
     *
     * @param index the row indices.
     * @return the data frame of selected rows.
     */
    default DataFrame of(int... index) {
        return new IndexDataFrame(this, index);
    }

    /**
     * Returns a new data frame with boolean indexing.
     *
     * @param index the boolean index.
     * @return the data frame of selected rows.
     */
    default DataFrame of(boolean... index) {
        return of(IntStream.range(0, index.length).filter(i -> index[i]).toArray());
    }

    /**
     * Copies the specified range into a new data frame.
     *
     * @param from the initial index of the range to be copied, inclusive
     * @param to   the final index of the range to be copied, exclusive.
     * @return the data frame of selected range of rows.
     */
    default DataFrame slice(int from, int to) {
        return IntStream.range(from, to).mapToObj(this::get).collect(Collectors.collect());
    }

    /**
     * Returns the value at position (i, j) as a primitive boolean.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default boolean getBoolean(int i, int j) {
        return get(i).getBoolean(j);
    }

    /**
     * Returns the field value as a primitive boolean.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default boolean getBoolean(int i, String column) {
        return get(i).getBoolean(column);
    }

    /**
     * Returns the value at position (i, j) as a primitive byte.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default byte getByte(int i, int j) {
        return get(i).getByte(j);
    }

    /**
     * Returns the field value as a primitive byte.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default byte getByte(int i, String column) {
        return get(i).getByte(column);
    }

    /**
     * Returns the value at position (i, j) as a primitive short.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default short getShort(int i, int j) {
        return get(i).getShort(j);
    }

    /**
     * Returns the field value as a primitive short.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default short getShort(int i, String column) {
        return get(i).getShort(column);
    }

    /**
     * Returns the value at position (i, j) as a primitive int.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default int getInt(int i, int j) {
        return get(i).getInt(j);
    }

    /**
     * Returns the field value as a primitive int.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default int getInt(int i, String column) {
        return get(i).getInt(column);
    }

    /**
     * Returns the value at position (i, j) as a primitive long.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default long getLong(int i, int j) {
        return get(i).getLong(j);
    }

    /**
     * Returns the field value as a primitive long.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default long getLong(int i, String column) {
        return get(i).getLong(column);
    }

    /**
     * Returns the value at position (i, j) as a primitive float.
     * Throws an exception if the type mismatches or if the value is null.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default float getFloat(int i, int j) {
        return get(i).getFloat(j);
    }

    /**
     * Returns the field value as a primitive float.
     * Throws an exception if the type mismatches or if the value is null.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default float getFloat(int i, String column) {
        return get(i).getFloat(column);
    }

    /**
     * Returns the value at position (i, j) as a primitive double.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default double getDouble(int i, int j) {
        return get(i).getDouble(j);
    }

    /**
     * Returns the field value as a primitive double.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException   when data type does not match.
     * @throws NullPointerException when value is null.
     */
    default double getDouble(int i, String column) {
        return get(i).getDouble(column);
    }

    /**
     * Returns the value at position (i, j) as a String object.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the cell value.
     * @throws ClassCastException when data type does not match.
     */
    default String getString(int i, int j) {
        return get(i).getString(j);
    }

    /**
     * Returns the field value as a String object.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the cell value.
     * @throws ClassCastException when data type does not match.
     */
    default String getString(int i, String column) {
        return get(i).getString(column);
    }

    /**
     * Returns the string representation of the value at position (i, j).
     *
     * @param i the row index.
     * @param j the column index.
     * @return the string representation of cell value.
     */
    default String toString(int i, int j) {
        Object o = get(i, j);
        if (o == null) return "null";

        if (o instanceof String) {
            return (String) o;
        } else {
            return schema().field(j).toString(o);
        }
    }

    /**
     * Returns the string representation of the field value.
     *
     * @param i      the row index.
     * @param column the column name.
     * @return the string representation of cell value.
     */
    default String toString(int i, String column) {
        return toString(i, indexOf(column));
    }

    /**
     * Returns the index of a given column name.
     *
     * @param column the column name.
     * @return the index of column.
     * @throws IllegalArgumentException when a field `name` does not exist.
     */
    int indexOf(String column);

    /**
     * Selects column based on the column name and return it as a Column.
     *
     * @param column the column name.
     * @return the column vector.
     */
    default BaseVector apply(String column) {
        return column(column);
    }

    /**
     * Selects column using an enum value.
     *
     * @param column the field enum.
     * @return the column vector.
     */
    default BaseVector apply(Enum<?> column) {
        return column(column.toString());
    }

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    BaseVector column(int i);

    /**
     * Selects column based on the column name.
     *
     * @param column the column name.
     * @return the column vector.
     */
    default BaseVector column(String column) {
        return column(indexOf(column));
    }

    /**
     * Selects column using an enum value.
     *
     * @param column the column name.
     * @return the column vector.
     */
    default BaseVector column(Enum<?> column) {
        return column(indexOf(column.toString()));
    }

    /**
     * Selects column based on the column index.
     *
     * @param i   the column index.
     * @param <T> the data type of column.
     * @return the column vector.
     */
    <T> Vector<T> vector(int i);

    /**
     * Selects column based on the column name.
     *
     * @param column the column name.
     * @param <T>    the data type of column.
     * @return the column vector.
     */
    default <T> Vector<T> vector(String column) {
        return vector(indexOf(column));
    }

    /**
     * Selects column using an enum value.
     *
     * @param column the column name.
     * @param <T>    the data type of column.
     * @return the column vector.
     */
    default <T> Vector<T> vector(Enum<?> column) {
        return vector(indexOf(column.toString()));
    }

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    BooleanVector booleanVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    CharVector charVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    ByteVector byteVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    ShortVector shortVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    IntVector intVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    LongVector longVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    FloatVector floatVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    DoubleVector doubleVector(int i);

    /**
     * Selects column based on the column index.
     *
     * @param i the column index.
     * @return the column vector.
     */
    StringVector stringVector(int i);

    /**
     * Returns a new DataFrame with selected columns.
     *
     * @param columns the column indices.
     * @return a new DataFrame with selected columns.
     */
    DataFrame select(int... columns);

    /**
     * Returns a new DataFrame without selected columns.
     *
     * @param columns the column indices.
     * @return a new DataFrame without selected columns.
     */
    DataFrame drop(int... columns);

    /**
     * Merges data frames horizontally by columns.
     *
     * @param dataframes the data frames to merge.
     * @return a new data frame that combines this DataFrame
     * with one more more other DataFrames by columns.
     */
    DataFrame merge(DataFrame... dataframes);

    /**
     * Merges vectors with this data frame.
     *
     * @param vectors the vectors to merge.
     * @return a new data frame that combines this DataFrame
     * with one more more additional vectors.
     */
    DataFrame merge(BaseVector... vectors);

    /**
     * Unions data frames vertically by rows.
     *
     * @param dataframes the data frames to union.
     * @return a new data frame that combines all the rows.
     */
    DataFrame union(DataFrame... dataframes);

    /**
     * Return an array obtained by converting all the variables
     * in a data frame to numeric mode and then binding them together
     * as the columns of a matrix. Missing values/nulls will be
     * encoded as Double.NaN. No bias term and uses level encoding
     * for categorical variables.
     *
     * @return the numeric array.
     */
    default double[][] toArray() {
        return toArray(false, CategoricalEncoder.LEVEL);
    }

    /**
     * Return an array obtained by converting all the variables
     * in a data frame to numeric mode and then binding them together
     * as the columns of a matrix. Missing values/nulls will be
     * encoded as Double.NaN.
     *
     * @param bias    if true, add the first column of all 1's.
     * @param encoder the categorical variable encoder.
     * @return the numeric array.
     */
    default double[][] toArray(boolean bias, CategoricalEncoder encoder) {
        int nrow = nrow();
        int ncol = ncol();
        StructType schema = schema();

        ArrayList<String> colNames = new ArrayList<>();
        if (bias) colNames.add("Intercept");
        for (int j = 0; j < ncol; j++) {
            StructField field = schema.field(j);

            Measure measure = field.measure;
            if (encoder != CategoricalEncoder.LEVEL && measure instanceof CategoricalMeasure) {
                CategoricalMeasure cat = (CategoricalMeasure) measure;
                int n = cat.size();

                if (encoder == CategoricalEncoder.DUMMY) {
                    for (int k = 1; k < n; k++) {
                        colNames.add(String.format("%s_%s", field.name, cat.level(k)));
                    }
                } else if (encoder == CategoricalEncoder.ONE_HOT) {
                    for (int k = 0; k < n; k++) {
                        colNames.add(String.format("%s_%s", field.name, cat.level(k)));
                    }
                }
            } else {
                colNames.add(field.name);
            }
        }

        double[][] matrix = new double[nrow][colNames.size()];

        int j = 0;
        if (bias) {
            j++;
            for (int i = 0; i < nrow; i++) {
                matrix[i][0] = 1.0;
            }
        }

        for (int col = 0; col < ncol; col++) {
            StructField field = schema.field(col);

            Measure measure = field.measure;
            if (encoder != CategoricalEncoder.LEVEL && measure instanceof CategoricalMeasure) {
                CategoricalMeasure cat = (CategoricalMeasure) measure;
                if (encoder == CategoricalEncoder.DUMMY) {
                    for (int i = 0; i < nrow; i++) {
                        int k = cat.factor(getInt(i, col));
                        if (k > 0) matrix[i][j + k - 1] = 1.0;
                    }
                    j += cat.size() - 1;
                } else if (encoder == CategoricalEncoder.ONE_HOT) {
                    for (int i = 0; i < nrow; i++) {
                        int k = cat.factor(getInt(i, col));
                        matrix[i][j + k] = 1.0;
                    }
                    j += cat.size();
                }
            } else {
                for (int i = 0; i < nrow; i++) {
                    matrix[i][j] = getDouble(i, col);
                }
                j++;
            }
        }

        return matrix;
    }

    /**
     * Returns the string representation of top rows.
     *
     * @param numRows the number of rows to show
     * @return the string representation of top rows.
     */
    default String toString(int numRows) {
        return toString(numRows, true);
    }

    /**
     * Returns the string representation of top rows.
     *
     * @param numRows  Number of rows to show
     * @param truncate Whether truncate long strings and align cells right.
     * @return the string representation of top rows.
     */
    @SuppressLint("DefaultLocale")
    default String toString(final int numRows, final boolean truncate) {
        StringBuilder sb = new StringBuilder(schema().toString());
        sb.append('\n');

        boolean hasMoreData = size() > numRows;
        String[] names = names();
        int numCols = names.length;
        int maxColWidth;
        switch (numCols) {
            case 1:
                maxColWidth = 78;
                break;
            case 2:
                maxColWidth = 38;
                break;
            default:
                maxColWidth = 20;
        }
        // To be used in lambda.
        final int maxColumnWidth = maxColWidth;

        // Initialize the width of each column to a minimum value of '3'
        int[] colWidths = new int[numCols];
        for (int i = 0; i < numCols; i++) {
            colWidths[i] = Math.max(names[i].length(), 3);
        }

        // For array values, replace Seq and Array with square brackets
        // For cells that are beyond maxColumnWidth characters, truncate it with "..."
        List<String[]> rows = stream().limit(numRows).map(row -> {
            String[] cells = new String[numCols];
            for (int i = 0; i < numCols; i++) {
                String str = row.toString(i);
                cells[i] = (truncate && str.length() > maxColumnWidth) ? str.substring(0, maxColumnWidth - 3) + "..." : str;
            }
            return cells;
        }).collect(java.util.stream.Collectors.toList());

        // Compute the width of each column
        for (String[] row : rows) {
            for (int i = 0; i < numCols; i++) {
                colWidths[i] = Math.max(colWidths[i], row[i].length());
            }
        }

        // Create SeparateLine
        String sep = IntStream.of(colWidths)
                .mapToObj(w -> Strings.fill('-', w))
                .collect(java.util.stream.Collectors.joining("+", "+", "+\n"));
        sb.append(sep);

        // column names
        StringBuilder header = new StringBuilder();
        header.append('|');
        for (int i = 0; i < numCols; i++) {
            if (truncate) {
                header.append(Strings.leftPad(names[i], colWidths[i], ' '));
            } else {
                header.append(Strings.rightPad(names[i], colWidths[i], ' '));
            }
            header.append('|');
        }
        header.append('\n');
        sb.append(header.toString());
        sb.append(sep);

        // data
        for (String[] row : rows) {
            StringBuilder line = new StringBuilder();
            line.append('|');
            for (int i = 0; i < numCols; i++) {
                if (truncate) {
                    line.append(Strings.leftPad(row[i], colWidths[i], ' '));
                } else {
                    line.append(Strings.rightPad(row[i], colWidths[i], ' '));
                }
                line.append('|');
            }
            line.append('\n');
            sb.append(line.toString());
        }

        sb.append(sep);

        // For Data that has more than "numRows" records
        if (hasMoreData) {
            int rest = size() - numRows;
            if (rest > 0) {
                String rowsString = (rest == 1) ? "row" : "rows";
                sb.append(String.format("%d more %s...\n", rest, rowsString));
            }
        }

        return sb.toString();
    }

    /**
     * Creates a DataFrame from a set of vectors.
     *
     * @param vectors The column vectors.
     * @return the data frame.
     */
    static DataFrame of(BaseVector... vectors) {
        return new DataFrameImpl(vectors);
    }

    /**
     * Creates a DataFrame from a 2-dimensional array.
     *
     * @param data  The data array.
     * @param names the name of columns.
     * @return the data frame.
     */
    static DataFrame of(double[][] data, String... names) {
        int p = data[0].length;
        if (names == null || names.length == 0) {
            names = IntStream.range(1, p + 1).mapToObj(i -> "V" + i).toArray(String[]::new);
        }

        DoubleVector[] vectors = new DoubleVector[p];
        for (int j = 0; j < p; j++) {
            double[] x = new double[data.length];
            for (int i = 0; i < x.length; i++) {
                x[i] = data[i][j];
            }
            vectors[j] = DoubleVector.of(names[j], x);
        }
        return DataFrame.of(vectors);
    }

    /**
     * Creates a DataFrame from a 2-dimensional array.
     *
     * @param data  The data array.
     * @param names the name of columns.
     * @return the data frame.
     */
    static DataFrame of(float[][] data, String... names) {
        int p = data[0].length;
        if (names == null || names.length == 0) {
            names = IntStream.range(1, p + 1).mapToObj(i -> "V" + i).toArray(String[]::new);
        }

        FloatVector[] vectors = new FloatVector[p];
        for (int j = 0; j < p; j++) {
            float[] x = new float[data.length];
            for (int i = 0; i < x.length; i++) {
                x[i] = data[i][j];
            }
            vectors[j] = FloatVector.of(names[j], x);
        }
        return DataFrame.of(vectors);
    }

    /**
     * Creates a DataFrame from a 2-dimensional array.
     *
     * @param data  The data array.
     * @param names the name of columns.
     * @return the data frame.
     */
    static DataFrame of(int[][] data, String... names) {
        int p = data[0].length;
        if (names == null || names.length == 0) {
            names = IntStream.range(1, p + 1).mapToObj(i -> "V" + i).toArray(String[]::new);
        }

        IntVector[] vectors = new IntVector[p];
        for (int j = 0; j < p; j++) {
            int[] x = new int[data.length];
            for (int i = 0; i < x.length; i++) {
                x[i] = data[i][j];
            }
            vectors[j] = IntVector.of(names[j], x);
        }
        return DataFrame.of(vectors);
    }

    /**
     * Creates a DataFrame from a stream of tuples.
     *
     * @param data   The data stream.
     * @param schema The schema of tuple.
     * @return the data frame.
     */
    static DataFrame of(Stream<? extends Tuple> data, StructType schema) {
        return new DataFrameImpl(data, schema);
    }

    /**
     * Creates a DataFrame from a set of tuples.
     *
     * @param data The data collection.
     * @return the data frame.
     */
    static DataFrame of(List<? extends Tuple> data) {
        return new DataFrameImpl(data);
    }

    /**
     * Creates a DataFrame from a set of tuples.
     *
     * @param data   The data collection.
     * @param schema The schema of tuple.
     * @return the data frame.
     */
    static DataFrame of(List<? extends Tuple> data, StructType schema) {
        return new DataFrameImpl(data, schema);
    }

    /**
     * Stream collectors.
     */
    interface Collectors {
        /**
         * Returns a stream collector that accumulates tuples into a DataFrame.
         *
         * @return the stream collector.
         */
        static Collector<Tuple, List<Tuple>, DataFrame> collect() {
            return Collector.of(
                    // supplier
                    ArrayList::new,
                    // accumulator
                    List::add,
                    // combiner
                    (c1, c2) -> {
                        c1.addAll(c2);
                        return c1;
                    },
                    // finisher
                    DataFrame::of
            );
        }
    }
}
