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

import com.anguel.dissertation.ml.smile.data.type.StructField;

/**
 * An immutable string vector.
 *
 * @author Haifeng Li
 */
class StringVectorImpl extends VectorImpl<String> implements StringVector {

    /**
     * Constructor.
     */
    public StringVectorImpl(String name, String[] vector) {
        super(name, String.class, vector);
    }

    /**
     * Constructor.
     */
    public StringVectorImpl(StructField field, String[] vector) {
        super(field.name, field.type, vector);

        if (field.measure != null) {
            throw new IllegalArgumentException(String.format("Invalid measure %s for %s", field.measure, type()));
        }
    }

    @Override
    public StringVector get(int... index) {
        String[] v = new String[index.length];
        for (int i = 0; i < index.length; i++) v[i] = get(index[i]);
        return new StringVectorImpl(field(), v);
    }

}