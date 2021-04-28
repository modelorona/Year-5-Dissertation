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

package com.anguel.dissertation.ml.smile.base.cart;

import android.annotation.SuppressLint;

import com.anguel.dissertation.ml.smile.data.Tuple;
import com.anguel.dissertation.ml.smile.data.type.StructField;
import com.anguel.dissertation.ml.smile.data.type.StructType;

/**
 * A node with a ordinal split variable (real-valued or ordinal categorical value).
 *
 * @author Haifeng Li
 */
public class OrdinalNode extends InternalNode {
    private static final long serialVersionUID = 2L;

    /**
     * The split value.
     */
    double value;

    /**
     * Constructor.
     *
     * @param feature    the index of feature column.
     * @param value      the split value.
     * @param score      the split score.
     * @param deviance   the deviance.
     * @param trueChild  the true branch child.
     * @param falseChild the false branch child.
     */
    public OrdinalNode(int feature, double value, double score, double deviance, Node trueChild, Node falseChild) {
        super(feature, score, deviance, trueChild, falseChild);
        this.value = value;
    }

    @Override
    public LeafNode predict(Tuple x) {
        return x.getDouble(feature) <= value ? trueChild.predict(x) : falseChild.predict(x);
    }

    @Override
    public boolean branch(Tuple x) {
        return x.getDouble(feature) <= value;
    }

    @Override
    public OrdinalNode replace(Node trueChild, Node falseChild) {
        return new OrdinalNode(feature, value, score, deviance, trueChild, falseChild);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString(StructType schema, boolean trueBranch) {
        StructField field = schema.field(feature);
        String condition = trueBranch ? "<=" : ">";
        return String.format("%s%s%g", field.name, condition, value);
    }
}
