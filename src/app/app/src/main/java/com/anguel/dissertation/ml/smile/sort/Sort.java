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

package com.anguel.dissertation.ml.smile.sort;

/**
 * Sort algorithm trait that includes useful static functions
 * such as swap and swift up/down used in many sorting algorithms.
 *
 * @author Haifeng Li
 */
public interface Sort {

    /**
     * Swap two positions.
     *
     * @param x the array.
     * @param i the index of array element.
     * @param j the index of other element.
     */
    static void swap(int[] x, int i, int j) {
        int a = x[i];
        x[i] = x[j];
        x[j] = a;
    }

    /**
     * Swap two positions.
     *
     * @param x the array.
     * @param i the index of array element.
     * @param j the index of other element.
     */
    static void swap(float[] x, int i, int j) {
        float a = x[i];
        x[i] = x[j];
        x[j] = a;
    }

    /**
     * Swap two positions.
     *
     * @param x the array.
     * @param i the index of array element.
     * @param j the index of other element.
     */
    static void swap(double[] x, int i, int j) {
        double a;
        a = x[i];
        x[i] = x[j];
        x[j] = a;
    }

    /**
     * Swap two positions.
     *
     * @param x the array.
     * @param i the index of array element.
     * @param j the index of other element.
     */
    static void swap(Object[] x, int i, int j) {
        Object a;
        a = x[i];
        x[i] = x[j];
        x[j] = a;
    }
}