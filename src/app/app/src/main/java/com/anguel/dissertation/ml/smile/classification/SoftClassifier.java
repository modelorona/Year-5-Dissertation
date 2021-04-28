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

package com.anguel.dissertation.ml.smile.classification;

/**
 * Soft classifiers calculate a posteriori probabilities besides the class
 * label of an instance.
 *
 * @param <T> the type of input object
 * @author Haifeng Li
 */
public interface SoftClassifier<T> extends Classifier<T> {
    /**
     * Predicts the class label of an instance and also calculate a posteriori
     * probabilities. Classifiers may NOT support this method since not all
     * classification algorithms are able to calculate such a posteriori
     * probabilities.
     *
     * @param x          an instance to be classified.
     * @param posteriori a posteriori probabilities on output.
     * @return the predicted class label
     */
    int predict(T x, double[] posteriori);

    /**
     * Predicts the class labels of an array of instances.
     *
     * @param x          the instances to be classified.
     * @param posteriori a posteriori probabilities on output.
     * @return the predicted class labels.
     */
    default int[] predict(T[] x, double[][] posteriori) {
        int n = x.length;
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            y[i] = predict(x[i], posteriori[i]);
        }
        return y;
    }
}
