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

package com.anguel.dissertation.ml.smile.util;

import android.annotation.SuppressLint;

import com.anguel.dissertation.ml.smile.math.MathEx;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * String utility functions.
 *
 * @author Haifeng Li
 */
public interface Strings {
    /**
     * Decimal format for floating numbers.
     */
    DecimalFormat decimal = new DecimalFormat("#.####");

    /**
     * Returns true if the string is null or empty.
     *
     * @param s the string.
     * @return true if the string is null or empty.
     */
    static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Left pad a string with a specified character.
     *
     * @param s       the string to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return left padded String or original String if no padding is necessary,
     * null if null String input
     */
    static String leftPad(String s, int size, char padChar) {
        if (s == null)
            return null;

        int pads = size - s.length();
        if (pads <= 0)
            return s; // returns original String when possible

        return fill(padChar, pads).concat(s);
    }

    /**
     * Right pad a string with a specified character.
     *
     * @param s       the string to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return left padded String or original String if no padding is necessary,
     * null if null String input
     */
    static String rightPad(String s, int size, char padChar) {
        if (s == null)
            return null;

        int pads = size - s.length();
        if (pads <= 0)
            return s; // returns original String when possible

        return s.concat(fill(padChar, pads));
    }

    /**
     * Returns the string with a single repeated character to a specific length.
     *
     * @param ch  the character.
     * @param len the length of string.
     * @return the string.
     */
    static String fill(char ch, int len) {
        char[] chars = new char[len];
        Arrays.fill(chars, ch);
        return new String(chars);
    }

    /**
     * Returns the string representation of a floating number without trailing zeros.
     *
     * @param x a real number.
     * @return the string representation.
     */
    static String format(float x) {
        return format(x, false);
    }

    /**
     * Returns the string representation of a floating number.
     *
     * @param x             a real number.
     * @param trailingZeros the flag if removes the trailing zeros.
     * @return the string representation.
     */
    @SuppressLint("DefaultLocale")
    static String format(float x, boolean trailingZeros) {
        if (MathEx.isZero(x, 1E-7f)) {
            return trailingZeros ? "0.0000" : "0";
        }

        float ax = Math.abs(x);
        if (ax >= 1E-3f && ax < 1E7f) {
            return trailingZeros ? String.format("%.4f", x) : decimal.format(x);
        }

        return String.format("%.4e", x);
    }

    /**
     * Returns the string representation of a floating number without trailing zeros.
     *
     * @param x a real number.
     * @return the string representation.
     */
    static String format(double x) {
        return format(x, false);
    }

    /**
     * Returns the string representation of a floating number.
     *
     * @param x             a real number.
     * @param trailingZeros the flag if removes the trailing zeros.
     * @return the string representation.
     */
    @SuppressLint("DefaultLocale")
    static String format(double x, boolean trailingZeros) {
        if (MathEx.isZero(x, 1E-14)) {
            return trailingZeros ? "0.0000" : "0";
        }

        double ax = Math.abs(x);
        if (ax >= 1E-3 && ax < 1E7) {
            return trailingZeros ? String.format("%.4f", x) : decimal.format(x);
        }

        return String.format("%.4e", x);
    }

    /**
     * Returns the string representation of array in format '[1, 2, 3]'.
     *
     * @param a the array.
     * @return the string representation.
     */
    static String toString(int[] a) {
        return Arrays.stream(a).mapToObj(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Returns the string representation of array in format '[1.0, 2.0, 3.0]'.
     *
     * @param a the array.
     * @return the string representation.
     */
    static String toString(float[] a) {
        return IntStream.range(0, a.length).mapToObj(i -> format(a[i])).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Returns the string representation of array in format '[1.0, 2.0, 3.0]'.
     *
     * @param a the array.
     * @return the string representation.
     */
    static String toString(double[] a) {
        return Arrays.stream(a).mapToObj(Strings::format).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Parses a double array in format '[1.0, 2.0, 3.0]'.
     * Returns null if s is null or empty.
     *
     * @param s the string.
     * @return the array.
     */
    static int[] parseIntArray(String s) {
        if (isNullOrEmpty(s)) return null;

        String[] tokens = s.trim().substring(1, s.length() - 1).split(",");
        return Arrays.stream(tokens).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

}
