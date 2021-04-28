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

package com.anguel.dissertation.ml.smile.data.type;

import java.math.BigDecimal;
import java.util.List;

//import java.sql.JDBCType;

/**
 * To get a specific data type, users should use singleton objects
 * and factory methods in this class.
 *
 * @author Haifeng Li
 */
public class DataTypes {
    /**
     * Boolean data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.BooleanType BooleanType = com.anguel.dissertation.ml.smile.data.type.BooleanType.instance;
    /**
     * Char data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.CharType CharType = com.anguel.dissertation.ml.smile.data.type.CharType.instance;
    /**
     * Byte data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ByteType ByteType = com.anguel.dissertation.ml.smile.data.type.ByteType.instance;
    /**
     * Short data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ShortType ShortType = com.anguel.dissertation.ml.smile.data.type.ShortType.instance;
    /**
     * Integer data type.
     */
    public static IntegerType IntegerType = com.anguel.dissertation.ml.smile.data.type.IntegerType.instance;
    /**
     * Long data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.LongType LongType = com.anguel.dissertation.ml.smile.data.type.LongType.instance;
    /**
     * Float data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.FloatType FloatType = com.anguel.dissertation.ml.smile.data.type.FloatType.instance;
    /**
     * Double data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.DoubleType DoubleType = com.anguel.dissertation.ml.smile.data.type.DoubleType.instance;
    /**
     * Decimal data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.DecimalType DecimalType = com.anguel.dissertation.ml.smile.data.type.DecimalType.instance;
    /**
     * String data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.StringType StringType = com.anguel.dissertation.ml.smile.data.type.StringType.instance;
    /**
     * Plain Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType ObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.instance;
    /**
     * Boolean Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType BooleanObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.BooleanObjectType;
    /**
     * Char Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType CharObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.CharObjectType;
    /**
     * Byte Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType ByteObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.ByteObjectType;
    /**
     * Short Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType ShortObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.ShortObjectType;
    /**
     * Integer Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType IntegerObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.IntegerObjectType;
    /**
     * Long Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType LongObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.LongObjectType;
    /**
     * Float Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType FloatObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.FloatObjectType;
    /**
     * Double Object data type.
     */
    public static com.anguel.dissertation.ml.smile.data.type.ObjectType DoubleObjectType = com.anguel.dissertation.ml.smile.data.type.ObjectType.DoubleObjectType;
    /**
     * Boolean Array data type.
     */
    public static ArrayType BooleanArrayType = ArrayType.BooleanArrayType;
    /**
     * Char Array data type.
     */
    public static ArrayType CharArrayType = ArrayType.CharArrayType;
    /**
     * Byte Array data type.
     */
    public static ArrayType ByteArrayType = ArrayType.ByteArrayType;
    /**
     * Short Array data type.
     */
    public static ArrayType ShortArrayType = ArrayType.ShortArrayType;
    /**
     * Integer Array data type.
     */
    public static ArrayType IntegerArrayType = ArrayType.IntegerArrayType;
    /**
     * Long Array data type.
     */
    public static ArrayType LongArrayType = ArrayType.LongArrayType;
    /**
     * Float Array data type.
     */
    public static ArrayType FloatArrayType = ArrayType.FloatArrayType;
    /**
     * Double Array data type.
     */
    public static ArrayType DoubleArrayType = ArrayType.DoubleArrayType;

    /**
     * Creates an object data type of a given class.
     *
     * @param clazz the object class.
     * @return the object data type.
     */
    public static DataType object(Class<?> clazz) {
        if (clazz == Integer.class) return IntegerObjectType;
        if (clazz == Long.class) return LongObjectType;
        if (clazz == Float.class) return FloatObjectType;
        if (clazz == Double.class) return DoubleObjectType;
        if (clazz == Boolean.class) return BooleanObjectType;
        if (clazz == Character.class) return CharObjectType;
        if (clazz == Byte.class) return ByteObjectType;
        if (clazz == Short.class) return ShortObjectType;
        if (clazz == BigDecimal.class) return DecimalType;
        if (clazz == String.class) return StringType;
        return new ObjectType(clazz);
    }

    /**
     * Creates an array data type.
     *
     * @param type the data type of array elements.
     * @return the array data type.
     */
    public static ArrayType array(DataType type) {
        if (type == IntegerType) return IntegerArrayType;
        if (type == LongType) return LongArrayType;
        if (type == FloatType) return FloatArrayType;
        if (type == DoubleType) return DoubleArrayType;
        if (type == BooleanType) return BooleanArrayType;
        if (type == CharType) return CharArrayType;
        if (type == ByteType) return ByteArrayType;
        if (type == ShortType) return ShortArrayType;
        return new ArrayType(type);
    }

    /**
     * Creates a struct data type.
     *
     * @param fields the struct fields.
     * @return the struct data type.
     */
    public static StructType struct(StructField... fields) {
        return new StructType(fields);
    }

    /**
     * Creates a struct data type.
     *
     * @param fields the struct fields.
     * @return the struct data type.
     */
    public static StructType struct(List<StructField> fields) {
        return new StructType(fields);
    }

}
