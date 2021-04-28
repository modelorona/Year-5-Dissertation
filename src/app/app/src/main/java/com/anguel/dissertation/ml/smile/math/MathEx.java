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

package com.anguel.dissertation.ml.smile.math;

import com.anguel.dissertation.ml.smile.sort.Sort;

import java.util.Arrays;
import java.util.HashSet;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

/**
 * Extra basic numeric functions. The following functions are
 * included:
 * <ul>
 * <li> scalar functions: sqr, factorial, lfactorial, choose, lchoose, log2 are
 * provided.
 * <li> vector functions: min, max, mean, sum, var, sd, cov, L<sub>1</sub> norm,
 * L<sub>2</sub> norm, L<sub>&infin;</sub> norm, normalize, unitize, cor, Spearman
 * correlation, Kendall correlation, distance, dot product, histogram, vector
 * (element-wise) copy, equal, plus, minus, times, and divide.
 * <li> matrix functions: min, max, rowSums, colSums, rowMeans, colMeans, transpose,
 * cov, cor, matrix copy, equals.
 * <li> random functions: random, randomInt, and permutate.
 * <li> Find the root of a univariate function with or without derivative.
 * </uL>
 *
 * @author Haifeng Li
 */
public class MathEx {

    /**
     * This RNG is to generate the seeds for multi-threads.
     * Each thread will use different seed and unlikely generates
     * the correlated sequences with other threads.
     */
    private static final Random seedRNG = new Random();

    /**
     * Used seeds.
     */
    private static final HashSet<Long> seeds = new HashSet<>();

    /**
     * High quality random number generator.
     */
    private static final ThreadLocal<Random> random = new ThreadLocal<Random>() {
        protected synchronized Random initialValue() {
            // For the first RNG, we use the default seed so that we can
            // get repeatable results for random algorithms.
            // Note that this may or may not be the main thread.
            long seed = 19650218L;

            // Make sure other threads not to use the same seed.
            // This is very important for some algorithms such as random forest.
            // Otherwise, all trees of random forest are same except the main thread one.
            if (!seeds.isEmpty()) {
                do {
                    seed = probablePrime(19650218L, 256, seedRNG);
                } while (seeds.contains(seed));
            }

            seeds.add(seed);
            return new Random(seed);
        }
    };

    /**
     * Private constructor to prevent instance creation.
     */
    private MathEx() {

    }

    /**
     * log(2), used in log2().
     */
    private static final double LOG2 = Math.log(2);

    /**
     * Log of base 2.
     *
     * @param x a real number.
     * @return the value <code>log2(x)</code>.
     */
    public static double log2(double x) {
        return Math.log(x) / LOG2;
    }

    /**
     * Returns natural log without underflow.
     *
     * @param x a real number.
     * @return the value <code>log(x)</code>.
     */
    public static double log(double x) {
        double y = -690.7755;
        if (x > 1E-300) {
            y = Math.log(x);
        }
        return y;
    }

    /**
     * Returns true if two double values equals to each other in the system precision.
     *
     * @param a a double value.
     * @param b a double value.
     * @return true if two double values equals to each other in the system precision
     */
    public static boolean equals(double a, double b) {
        if (a == b) {
            return true;
        }

        double absa = abs(a);
        double absb = abs(b);
        return abs(a - b) <= Math.min(absa, absb) * 2.2204460492503131e-16;
    }

    /**
     * Returns true if n is probably prime, false if it's definitely composite.
     * This implements Miller-Rabin primality test.
     *
     * @param n   an odd integer to be tested for primality
     * @param k   a parameter that determines the accuracy of the test
     * @param rng random number generator
     * @return true if n is probably prime, false if it's definitely composite.
     */
    private static boolean isProbablePrime(long n, int k, Random rng) {
        if (n <= 1 || n == 4)
            return false;
        if (n <= 3)
            return true;

        // Find r such that n = 2^d * r + 1 for some r >= 1
        int s = 0;
        long d = n - 1;
        while (d % 2 == 0) {
            s++;
            d = d / 2;
        }

        for (int i = 0; i < k; i++) {
            long a = 2 + rng.nextLong() % (n - 4);
            long x = power(a, d, n);
            if (x == 1 || x == n - 1)
                continue;

            int r = 0;
            for (; r < s; r++) {
                x = (x * x) % n;
                if (x == 1) return false;
                if (x == n - 1) break;
            }

            // None of the steps made x equal n-1.
            if (r == s) return false;
        }
        return true;
    }

    /**
     * Modular exponentiation <code>x<sup>y</sup> % p</code>.
     *
     * @param x the base.
     * @param y the exponent.
     * @param p the modular.
     * @return the modular exponentation.
     */
    private static long power(long x, long y, long p) {
        long res = 1;      // Initialize result
        x = x % p;  // Update x if it is more than or
        // equal to p
        while (y > 0) {
            // If y is odd, multiply x with result
            if ((y & 1) == 1) res = (res * x) % p;

            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }
        return res;
    }

    /**
     * The log of factorial of n.
     *
     * @param n a positive integer.
     * @return the log of factorial .
     */
    public static double lfactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(String.format("n has to be non-negative: %d", n));
        }

        double f = 0.0;
        for (int i = 2; i <= n; i++) {
            f += Math.log(i);
        }

        return f;
    }

    /**
     * The n choose k. Returns 0 if n is less than k.
     *
     * @param n the total number of objects in the set.
     * @param k the number of choosing objects from the set.
     * @return the number of combinations.
     */
    public static double choose(int n, int k) {
        if (n < 0 || k < 0) {
            throw new IllegalArgumentException(String.format("Invalid n = %d, k = %d", n, k));
        }

        if (n < k) {
            return 0.0;
        }

        return floor(0.5 + exp(lchoose(n, k)));
    }

    /**
     * The log of n choose k.
     *
     * @param n the total number of objects in the set.
     * @param k the number of choosing objects from the set.
     * @return the log of the number of combinations.
     */
    public static double lchoose(int n, int k) {
        if (k < 0 || k > n) {
            throw new IllegalArgumentException(String.format("Invalid n = %d, k = %d", n, k));
        }

        return lfactorial(n) - lfactorial(k) - lfactorial(n - k);
    }

    /**
     * Initialize the random number generator with a seed.
     *
     * @param seed the RNG seed.
     */
    public static void setSeed(long seed) {
        if (seeds.isEmpty()) {
            seedRNG.setSeed(seed);
            seeds.clear();
        }

        random.get().setSeed(seed);
        seeds.add(seed);
    }

    /**
     * Returns a probably prime number.
     *
     * @param n   the returned value should be greater than n.
     * @param k   a parameter that determines the accuracy of the primality test.
     * @param rng the random number generator.
     * @return a probably prime number greater than n.
     */
    private static long probablePrime(long n, int k, Random rng) {
        long seed = n + rng.nextInt(899999963); // The largest prime less than 9*10^8
        for (int i = 0; i < 4096; i++) {
            if (isProbablePrime(seed, k, rng)) break;
            seed = n + rng.nextInt(899999963);
        }

        return seed;
    }

    /**
     * Given a set of n probabilities, generate a random number in [0, n).
     *
     * @param prob probabilities of size n. The prob argument can be used to
     *             give a vector of weights for obtaining the elements of the vector being
     *             sampled. They need not sum to one, but they should be non-negative and
     *             not all zero.
     * @return a random integer in [0, n).
     */
    public static int random(double[] prob) {
        int[] ans = random(prob, 1);
        return ans[0];
    }

    /**
     * Given a set of m probabilities, draw with replacement a set of n random
     * number in [0, m).
     *
     * @param prob probabilities of size n. The prob argument can be used to
     *             give a vector of weights for obtaining the elements of the vector being
     *             sampled. They need not sum to one, but they should be non-negative and
     *             not all zero.
     * @param n    the number of random numbers.
     * @return random numbers in range of [0, m).
     */
    public static int[] random(double[] prob, int n) {
        // set up alias table
        double[] q = new double[prob.length];
        for (int i = 0; i < prob.length; i++) {
            q[i] = prob[i] * prob.length;
        }

        // initialize a with indices
        int[] a = new int[prob.length];
        for (int i = 0; i < prob.length; i++) {
            a[i] = i;
        }

        // set up H and L
        int[] HL = new int[prob.length];
        int head = 0;
        int tail = prob.length - 1;
        for (int i = 0; i < prob.length; i++) {
            if (q[i] >= 1.0) {
                HL[head++] = i;
            } else {
                HL[tail--] = i;
            }
        }

        while (head != 0 && tail != prob.length - 1) {
            int j = HL[tail + 1];
            int k = HL[head - 1];
            a[j] = k;
            q[k] += q[j] - 1;
            tail++;                                  // remove j from L
            if (q[k] < 1.0) {
                HL[tail--] = k;                      // add k to L
                head--;                              // remove k
            }
        }

        // generate sample
        int[] ans = new int[n];
        for (int i = 0; i < n; i++) {
            double rU = random() * prob.length;

            int k = (int) (rU);
            rU -= k;  /* rU becomes rU-[rU] */

            if (rU < q[k]) {
                ans[i] = k;
            } else {
                ans[i] = a[k];
            }
        }

        return ans;
    }

    /**
     * Generate a random number in [0, 1).
     *
     * @return a random number.
     */
    public static double random() {
        return random.get().nextDouble();
    }

    /**
     * Generate n random numbers in [0, 1).
     *
     * @param n the number of random numbers.
     * @return the random numbers.
     */
    public static double[] random(int n) {
        double[] x = new double[n];
        random.get().nextDoubles(x);
        return x;
    }

    /**
     * Generate a uniform random number in the range [lo, hi).
     *
     * @param lo lower limit of range
     * @param hi upper limit of range
     * @return a uniform random number in the range [lo, hi)
     */
    public static double random(double lo, double hi) {
        return random.get().nextDouble(lo, hi);
    }

    /**
     * Generate uniform random numbers in the range [lo, hi).
     *
     * @param lo lower limit of range
     * @param hi upper limit of range
     * @param n  the number of random numbers.
     * @return uniform random numbers in the range [lo, hi)
     */
    public static double[] random(double lo, double hi, int n) {
        double[] x = new double[n];
        random.get().nextDoubles(x, lo, hi);
        return x;
    }

    /**
     * Returns a random integer in [0, n).
     *
     * @param n the upper bound of random number.
     * @return a random integer.
     */
    public static int randomInt(int n) {
        return random.get().nextInt(n);
    }

    /**
     * Returns a random integer in [lo, hi).
     *
     * @param lo lower limit of range
     * @param hi upper limit of range
     * @return a uniform random number in the range [lo, hi)
     */
    public static int randomInt(int lo, int hi) {
        int w = hi - lo;
        return lo + random.get().nextInt(w);
    }

    /**
     * Returns a permutation of <code>(0, 1, 2, ..., n-1)</code>.
     *
     * @param n the upper bound.
     * @return the permutation of <code>(0, 1, 2, ..., n-1)</code>.
     */
    public static int[] permutate(int n) {
        return random.get().permutate(n);
    }

    /**
     * Permutates an array.
     *
     * @param x the array.
     */
    public static void permutate(int[] x) {
        random.get().permutate(x);
    }

    /**
     * Combines the arguments to form a vector.
     *
     * @param x the vector elements.
     * @return the vector.
     */
    public static int[] c(int... x) {
        return x;
    }

    /**
     * Combines the arguments to form a vector.
     *
     * @param x the vector elements.
     * @return the vector.
     */
    public static float[] c(float... x) {
        return x;
    }

    /**
     * Combines the arguments to form a vector.
     *
     * @param x the vector elements.
     * @return the vector.
     */
    public static double[] c(double... x) {
        return x;
    }

    /**
     * Combines the arguments to form a vector.
     *
     * @param x the vector elements.
     * @return the vector.
     */
    public static String[] c(String... x) {
        return x;
    }

    /**
     * Concatenates multiple vectors into one.
     *
     * @param list the vectors.
     * @return the concatenated vector.
     */
    public static int[] c(int[]... list) {
        int n = 0;
        for (int[] x : list) n += x.length;
        int[] y = new int[n];
        int pos = 0;
        for (int[] x : list) {
            System.arraycopy(x, 0, y, pos, x.length);
            pos += x.length;
        }
        return y;
    }

    /**
     * Concatenates multiple vectors into one.
     *
     * @param list the vectors.
     * @return the concatenated vector.
     */
    public static float[] c(float[]... list) {
        int n = 0;
        for (float[] x : list) n += x.length;
        float[] y = new float[n];
        int pos = 0;
        for (float[] x : list) {
            System.arraycopy(x, 0, y, pos, x.length);
            pos += x.length;
        }
        return y;
    }

    /**
     * Concatenates multiple vectors into one.
     *
     * @param list the vectors.
     * @return the concatenated vector.
     */
    public static double[] c(double[]... list) {
        int n = 0;
        for (double[] x : list) n += x.length;
        double[] y = new double[n];
        int pos = 0;
        for (double[] x : list) {
            System.arraycopy(x, 0, y, pos, x.length);
            pos += x.length;
        }
        return y;
    }

    /**
     * Concatenates multiple vectors into one array of strings.
     *
     * @param list the vectors.
     * @return the concatenated vector.
     */
    public static String[] c(String[]... list) {
        int n = 0;
        for (String[] x : list) n += x.length;
        String[] y = new String[n];
        int pos = 0;
        for (String[] x : list) {
            System.arraycopy(x, 0, y, pos, x.length);
            pos += x.length;
        }
        return y;
    }

    /**
     * Returns a slice of data for given indices.
     *
     * @param data  the array.
     * @param index the indices of selected elements.
     * @param <E>   the data type of elements.
     * @return the selected elements.
     */
    public static <E> E[] slice(E[] data, int[] index) {
        int n = index.length;

        @SuppressWarnings("unchecked")
        E[] x = (E[]) java.lang.reflect.Array.newInstance(data.getClass().getComponentType(), n);

        for (int i = 0; i < n; i++) {
            x[i] = data[index[i]];
        }

        return x;
    }

    /**
     * Returns a slice of data for given indices.
     *
     * @param data  the array.
     * @param index the indices of selected elements.
     * @return the selected elements.
     */
    public static int[] slice(int[] data, int[] index) {
        int n = index.length;
        int[] x = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = data[index[i]];
        }

        return x;
    }

    /**
     * Determines if the polygon contains the point.
     *
     * @param polygon the vertices of polygon.
     * @param point   the point.
     * @return true if the Polygon contains the point.
     */
    public static boolean contains(double[][] polygon, double[] point) {
        return contains(polygon, point[0], point[1]);
    }

    /**
     * Determines if the polygon contains the point.
     *
     * @param polygon the vertices of polygon.
     * @param x       the x coordinate of point.
     * @param y       the y coordinate of point.
     * @return true if the Polygon contains the point.
     */
    public static boolean contains(double[][] polygon, double x, double y) {
        if (polygon.length <= 2) {
            return false;
        }

        int hits = 0;

        int n = polygon.length;
        double lastx = polygon[n - 1][0];
        double lasty = polygon[n - 1][1];
        double curx, cury;

        // Walk the edges of the polygon
        for (int i = 0; i < n; lastx = curx, lasty = cury, i++) {
            curx = polygon[i][0];
            cury = polygon[i][1];

            if (cury == lasty) {
                continue;
            }

            double leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }

    /**
     * Reverses the order of the elements in the specified array.
     *
     * @param a an array to reverse.
     */
    public static void reverse(int[] a) {
        int i = 0, j = a.length - 1;
        while (i < j) {
            Sort.swap(a, i++, j--);  // code for swap not shown, but easy enough
        }
    }

    /**
     * Reverses the order of the elements in the specified array.
     *
     * @param a the array to reverse.
     */
    public static void reverse(float[] a) {
        int i = 0, j = a.length - 1;
        while (i < j) {
            Sort.swap(a, i++, j--);  // code for swap not shown, but easy enough
        }
    }

    /**
     * Reverses the order of the elements in the specified array.
     *
     * @param a the array to reverse.
     */
    public static void reverse(double[] a) {
        int i = 0, j = a.length - 1;
        while (i < j) {
            Sort.swap(a, i++, j--);  // code for swap not shown, but easy enough
        }
    }

    /**
     * Reverses the order of the elements in the specified array.
     *
     * @param a   the array to reverse.
     * @param <T> the data type of array elements.
     */
    public static <T> void reverse(T[] a) {
        int i = 0, j = a.length - 1;
        while (i < j) {
            Sort.swap(a, i++, j--);
        }
    }

    /**
     * Returns the maximum of 3 integer numbers.
     *
     * @param a a number.
     * @param b a number.
     * @param c a number.
     * @return the maximum.
     */
    public static int max(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * Returns the maximum of 4 float numbers.
     *
     * @param a a number.
     * @param b a number.
     * @param c a number.
     * @return the maximum.
     */
    public static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * Returns the maximum of 4 double numbers.
     *
     * @param a a number.
     * @param b a number.
     * @param c a number.
     * @return the maximum.
     */
    public static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * Returns the maximum of 4 integer numbers.
     *
     * @param a a number.
     * @param b a number.
     * @param c a number.
     * @param d a number.
     * @return the maximum.
     */
    public static int max(int a, int b, int c, int d) {
        return Math.max(Math.max(Math.max(a, b), c), d);
    }

    /**
     * Returns the maximum of 4 float numbers.
     *
     * @param a a number.
     * @param b a number.
     * @param c a number.
     * @param d a number.
     * @return the maximum.
     */
    public static float max(float a, float b, float c, float d) {
        return Math.max(Math.max(Math.max(a, b), c), d);
    }

    /**
     * Returns the maximum of 4 double numbers.
     *
     * @param a a number.
     * @param b a number.
     * @param c a number.
     * @param d a number.
     * @return the maximum.
     */
    public static double max(double a, double b, double c, double d) {
        return Math.max(Math.max(Math.max(a, b), c), d);
    }

    /**
     * Returns the minimum value of an array.
     *
     * @param x the array.
     * @return the minimum.
     */
    public static int min(int[] x) {
        int min = x[0];

        for (int n : x) {
            if (n < min) {
                min = n;
            }
        }

        return min;
    }

    /**
     * Returns the index of minimum value of an array.
     *
     * @param x the array.
     * @return the index of minimum.
     */
    public static int whichMin(int[] x) {
        int min = x[0];
        int which = 0;

        for (int i = 1; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
                which = i;
            }
        }

        return which;
    }

    /**
     * Returns the maximum value of an array.
     *
     * @param x the array.
     * @return the index of maximum.
     */
    public static int max(int[] x) {
        int max = x[0];

        for (int n : x) {
            if (n > max) {
                max = n;
            }
        }

        return max;
    }

    /**
     * Returns the maximum value of an array.
     *
     * @param x the array.
     * @return the index of maximum.
     */
    public static float max(float[] x) {
        float max = Float.NEGATIVE_INFINITY;

        for (float n : x) {
            if (n > max) {
                max = n;
            }
        }

        return max;
    }

    /**
     * Returns the maximum value of an array.
     *
     * @param x the array.
     * @return the index of maximum.
     */
    public static double max(double[] x) {
        double max = Double.NEGATIVE_INFINITY;

        for (double n : x) {
            if (n > max) {
                max = n;
            }
        }

        return max;
    }

    /**
     * Returns the index of maximum value of an array.
     *
     * @param x the array.
     * @return the index of maximum.
     */
    public static int whichMax(int[] x) {
        int max = x[0];
        int which = 0;

        for (int i = 1; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                which = i;
            }
        }

        return which;
    }

    /**
     * Returns the index of maximum value of an array.
     *
     * @param x the array.
     * @return the index of maximum.
     */
    public static int whichMax(double[] x) {
        double max = Double.NEGATIVE_INFINITY;
        int which = 0;

        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                which = i;
            }
        }

        return which;
    }

    /**
     * Returns the maximum of a matrix.
     *
     * @param matrix the matrix.
     * @return the maximum.
     */
    public static int max(int[][] matrix) {
        int max = matrix[0][0];

        for (int[] x : matrix) {
            for (int y : x) {
                if (max < y) {
                    max = y;
                }
            }
        }

        return max;
    }

    /**
     * Returns the maximum of a matrix.
     *
     * @param matrix the matrix.
     * @return the maximum.
     */
    public static double max(double[][] matrix) {
        double max = Double.NEGATIVE_INFINITY;

        for (double[] x : matrix) {
            for (double y : x) {
                if (max < y) {
                    max = y;
                }
            }
        }

        return max;
    }


    /**
     * Returns the column means of a matrix.
     *
     * @param matrix the matrix.
     * @return the column means.
     */
    public static double[] colMeans(double[][] matrix) {
        double[] x = matrix[0].clone();

        for (int i = 1; i < matrix.length; i++) {
            for (int j = 0; j < x.length; j++) {
                x[j] += matrix[i][j];
            }
        }

        scale(1.0 / matrix.length, x);

        return x;
    }


    /**
     * Returns the sum of an array.
     *
     * @param x the array.
     * @return the sum.
     */
    public static long sum(int[] x) {
        long sum = 0;

        for (int n : x) {
            sum += n;
        }

        return (int) sum;
    }

    /**
     * Returns the sum of an array.
     *
     * @param x the array.
     * @return the sum.
     */
    public static double sum(float[] x) {
        double sum = 0.0;

        for (float n : x) {
            sum += n;
        }

        return sum;
    }

    /**
     * Returns the sum of an array.
     *
     * @param x the array.
     * @return the sum.
     */
    public static double sum(double[] x) {
        double sum = 0.0;

        for (double n : x) {
            sum += n;
        }

        return sum;
    }

    /**
     * Returns the mean of an array.
     *
     * @param x the array.
     * @return the mean.
     */
    public static double mean(int[] x) {
        return (double) sum(x) / x.length;
    }

    /**
     * Returns the mean of an array.
     *
     * @param x the array.
     * @return the mean.
     */
    public static double mean(float[] x) {
        return sum(x) / x.length;
    }

    /**
     * Returns the mean of an array.
     *
     * @param x the array.
     * @return the mean.
     */
    public static double mean(double[] x) {
        return sum(x) / x.length;
    }

    /**
     * Returns the variance of an array.
     *
     * @param x the array.
     * @return the variance.
     */
    public static double var(int[] x) {
        if (x.length < 2) {
            throw new IllegalArgumentException("Array length is less than 2.");
        }

        double sum = 0.0;
        double sumsq = 0.0;
        for (int xi : x) {
            sum += xi;
            sumsq += xi * xi;
        }

        int n = x.length - 1;
        return sumsq / n - (sum / x.length) * (sum / n);
    }


    /**
     * Returns the variance of an array.
     *
     * @param x the array.
     * @return the variance.
     */
    public static double var(double[] x) {
        if (x.length < 2) {
            throw new IllegalArgumentException("Array length is less than 2.");
        }

        double sum = 0.0;
        double sumsq = 0.0;
        for (double xi : x) {
            sum += xi;
            sumsq += xi * xi;
        }

        int n = x.length - 1;
        return sumsq / n - (sum / x.length) * (sum / n);
    }

    /**
     * Returns the standard deviation of an array.
     *
     * @param x the array.
     * @return the standard deviation.
     */
    public static double sd(int[] x) {
        return sqrt(var(x));
    }


    /**
     * Returns the standard deviation of an array.
     *
     * @param x the array.
     * @return the standard deviation.
     */
    public static double sd(double[] x) {
        return sqrt(var(x));
    }

    /**
     * L<sub>1</sub> vector norm.
     *
     * @param x a vector.
     * @return L<sub>1</sub> norm.
     */
    public static double norm1(double[] x) {
        double norm = 0.0;

        for (double n : x) {
            norm += abs(n);
        }

        return norm;
    }


    /**
     * Unitize an array so that L<sub>1</sub> norm of x is 1.
     *
     * @param x the vector.
     */
    public static void unitize1(double[] x) {
        double n = norm1(x);

        for (int i = 0; i < x.length; i++) {
            x[i] /= n;
        }
    }

    /**
     * Check if x element-wisely equals y with default epsilon 1E-7.
     *
     * @param x an array.
     * @param y an array.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(float[] x, float[] y) {
        return equals(x, y, 1.0E-7f);
    }

    /**
     * Check if x element-wisely equals y in given precision.
     *
     * @param x       an array.
     * @param y       an array.
     * @param epsilon a number close to zero.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(float[] x, float[] y, float epsilon) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));
        }

        for (int i = 0; i < x.length; i++) {
            if (abs(x[i] - y[i]) > epsilon) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if x element-wisely equals y with default epsilon 1E-10.
     *
     * @param x an array.
     * @param y an array.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(double[] x, double[] y) {
        return equals(x, y, 1.0E-10);
    }

    /**
     * Check if x element-wisely equals y in given precision.
     *
     * @param x       an array.
     * @param y       an array.
     * @param epsilon a number close to zero.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(double[] x, double[] y, double epsilon) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));
        }

        if (epsilon <= 0.0) {
            throw new IllegalArgumentException("Invalid epsilon: " + epsilon);
        }

        for (int i = 0; i < x.length; i++) {
            if (abs(x[i] - y[i]) > epsilon) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if x element-wisely equals y with default epsilon 1E-7.
     *
     * @param x a two-dimensional array.
     * @param y a two-dimensional array.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(float[][] x, float[][] y) {
        return equals(x, y, 1.0E-7f);
    }

    /**
     * Check if x element-wisely equals y in given precision.
     *
     * @param x       a two-dimensional array.
     * @param y       a two-dimensional array.
     * @param epsilon a number close to zero.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(float[][] x, float[][] y, float epsilon) {
        if (x.length != y.length || x[0].length != y[0].length) {
            throw new IllegalArgumentException(String.format("Matrices have different rows: %d x %d vs %d x %d", x.length, x[0].length, y.length, y[0].length));
        }

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                if (abs(x[i][j] - y[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if x element-wisely equals y with default epsilon 1E-10.
     *
     * @param x a two-dimensional array.
     * @param y a two-dimensional array.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(double[][] x, double[][] y) {
        return equals(x, y, 1.0E-10);
    }

    /**
     * Check if x element-wisely equals y in given precision.
     *
     * @param x       a two-dimensional array.
     * @param y       a two-dimensional array.
     * @param epsilon a number close to zero.
     * @return true if x element-wisely equals y.
     */
    public static boolean equals(double[][] x, double[][] y, double epsilon) {
        if (x.length != y.length || x[0].length != y[0].length) {
            throw new IllegalArgumentException(String.format("Matrices have different rows: %d x %d vs %d x %d", x.length, x[0].length, y.length, y[0].length));
        }

        if (epsilon <= 0.0) {
            throw new IllegalArgumentException("Invalid epsilon: " + epsilon);
        }

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                if (abs(x[i][j] - y[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests if a floating number is zero in given precision.
     *
     * @param x       a real number.
     * @param epsilon a number close to zero.
     * @return true if x is zero in <code>epsilon</code> precision.
     */
    public static boolean isZero(float x, float epsilon) {
        return abs(x) < epsilon;
    }


    /**
     * Tests if a floating number is zero in given precision.
     *
     * @param x       a real number.
     * @param epsilon a number close to zero.
     * @return true if x is zero in <code>epsilon</code> precision.
     */
    public static boolean isZero(double x, double epsilon) {
        return abs(x) < epsilon;
    }

    /**
     * Swap two elements of an array.
     *
     * @param x an array.
     * @param i the index of first element.
     * @param j the index of second element.
     */
    public static void swap(int[] x, int i, int j) {
        int s = x[i];
        x[i] = x[j];
        x[j] = s;
    }


    /**
     * Copy x into y.
     *
     * @param x the input matrix.
     * @param y the output matrix.
     */
    public static void copy(int[][] x, int[][] y) {
        if (x.length != y.length || x[0].length != y[0].length) {
            throw new IllegalArgumentException(String.format("Matrices have different rows: %d x %d vs %d x %d", x.length, x[0].length, y.length, y[0].length));
        }

        for (int i = 0; i < x.length; i++) {
            System.arraycopy(x[i], 0, y[i], 0, x[i].length);
        }
    }

    /**
     * Deep copy x into y.
     *
     * @param x the input matrix.
     * @param y the output matrix.
     */
    public static void copy(float[][] x, float[][] y) {
        if (x.length != y.length || x[0].length != y[0].length) {
            throw new IllegalArgumentException(String.format("Matrices have different rows: %d x %d vs %d x %d", x.length, x[0].length, y.length, y[0].length));
        }

        for (int i = 0; i < x.length; i++) {
            System.arraycopy(x[i], 0, y[i], 0, x[i].length);
        }
    }

    /**
     * Deep copy x into y.
     *
     * @param x the input matrix.
     * @param y the output matrix.
     */
    public static void copy(double[][] x, double[][] y) {
        if (x.length != y.length || x[0].length != y[0].length) {
            throw new IllegalArgumentException(String.format("Matrices have different rows: %d x %d vs %d x %d", x.length, x[0].length, y.length, y[0].length));
        }

        for (int i = 0; i < x.length; i++) {
            System.arraycopy(x[i], 0, y[i], 0, x[i].length);
        }
    }

    /**
     * Element-wise sum of two arrays y = x + y.
     *
     * @param x a vector.
     * @param y avector.
     */
    public static void add(double[] y, double[] x) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));
        }

        for (int i = 0; i < x.length; i++) {
            y[i] += x[i];
        }
    }

    /**
     * Scale each element of an array by a constant x = a * x.
     *
     * @param a the scale factor.
     * @param x the input and output vector.
     */
    public static void scale(double a, double[] x) {
        for (int i = 0; i < x.length; i++) {
            x[i] *= a;
        }
    }

    /**
     * Find unique elements of vector.
     *
     * @param x an integer array.
     * @return the same values as in x but with no repetitions.
     */
    public static int[] unique(int[] x) {
        return Arrays.stream(x).distinct().toArray();
    }

    /**
     * Find unique elements of vector.
     *
     * @param x an array of strings.
     * @return the same values as in x but with no repetitions.
     */
    public static String[] unique(String[] x) {
        return Arrays.stream(x).distinct().toArray(String[]::new);
    }

}
