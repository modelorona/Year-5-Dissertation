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

package com.anguel.dissertation.ml.smile.data.measure;

import java.io.Serializable;

/**
 * Level of measurement or scale of measure is a classification that
 * describes the nature of information within the values assigned to
 * variables. Psychologist Stanley Smith Stevens developed the best-known
 * classification with four levels, or scales, of measurement: nominal,
 * ordinal, interval, and ratio. Each scale of measurement has certain
 * properties which in turn determines the appropriateness for use of
 * certain statistical analyses.
 *
 * @author Haifeng Li
 */
public interface Measure extends Serializable {

    /**
     * Returns a measurement value object represented by the argument string s.
     *
     * @param s a string.
     * @return the parsed value.
     */
    Number valueOf(String s) throws NumberFormatException;

    /**
     * Returns the string representation of an object in the measure.
     *
     * @param o an object.
     * @return the string representation
     */
    String toString(Object o);
}
