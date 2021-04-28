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

package com.anguel.dissertation.ml.smile.validation;

import com.anguel.dissertation.ml.smile.classification.Classifier;
import com.anguel.dissertation.ml.smile.classification.DataFrameClassifier;
import com.anguel.dissertation.ml.smile.classification.SoftClassifier;
import com.anguel.dissertation.ml.smile.data.DataFrame;
import com.anguel.dissertation.ml.smile.data.Tuple;
import com.anguel.dissertation.ml.smile.data.formula.Formula;
import com.anguel.dissertation.ml.smile.math.MathEx;
import com.anguel.dissertation.ml.smile.validation.metric.AUC;
import com.anguel.dissertation.ml.smile.validation.metric.Accuracy;
import com.anguel.dissertation.ml.smile.validation.metric.CrossEntropy;
import com.anguel.dissertation.ml.smile.validation.metric.Error;
import com.anguel.dissertation.ml.smile.validation.metric.FScore;
import com.anguel.dissertation.ml.smile.validation.metric.LogLoss;
import com.anguel.dissertation.ml.smile.validation.metric.MatthewsCorrelation;
import com.anguel.dissertation.ml.smile.validation.metric.Precision;
import com.anguel.dissertation.ml.smile.validation.metric.Sensitivity;
import com.anguel.dissertation.ml.smile.validation.metric.Specificity;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Leave-one-out cross validation. LOOCV uses a single observation
 * from the original sample as the validation data, and the remaining
 * observations as the training data. This is repeated such that each
 * observation in the sample is used once as the validation data. This is
 * the same as a K-fold cross-validation with K being equal to the number of
 * observations in the original sample. Leave-one-out cross-validation is
 * usually very expensive from a computational point of view because of the
 * large number of times the training process is repeated.
 *
 * @author Haifeng Li
 */
public interface LOOCV {
    /**
     * Returns the training sample index for each round.
     *
     * @param n the number of samples.
     * @return The index of training instances for each round.
     * The left one of i-th round is i-th sample.
     */
    static int[][] of(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid sample size: " + n);
        }

        int[][] train = new int[n][n - 1];
        for (int i = 0; i < n; i++) {
            int p = 0;
            for (int j = 0; j < i; j++) {
                train[i][p++] = j;
            }

            for (int j = i + 1; j < n; j++) {
                train[i][p++] = j;
            }
        }

        return train;
    }

    /**
     * Runs leave-one-out cross validation tests.
     *
     * @param x       the training data.
     * @param y       the class labels of training data.
     * @param trainer the lambda to train the model.
     * @param <T>     the data type of samples.
     * @param <M>     the model type.
     * @return the validation results.
     */
    static <T, M extends Classifier<T>> ClassificationMetrics classification(T[] x, int[] y, BiFunction<T[], int[], M> trainer) {
        int k = MathEx.unique(y).length;
        int n = x.length;

        int[][] train = LOOCV.of(n);
        int[] prediction = new int[n];
        double[][] posteriori = new double[n][k];
        long fitTime = 0;
        long scoreTime = 0;
        boolean soft = false;

        for (int i = 0; i < n; i++) {
            T[] trainx = MathEx.slice(x, train[i]);
            int[] trainy = MathEx.slice(y, train[i]);

            long start = System.nanoTime();
            M model = trainer.apply(trainx, trainy);
            fitTime += System.nanoTime() - start;

            start = System.nanoTime();
            if (model instanceof SoftClassifier) {
                soft = true;
                prediction[i] = ((SoftClassifier<T>) model).predict(x[i], posteriori[i]);
            } else {
                prediction[i] = model.predict(x[i]);
            }
            scoreTime += System.nanoTime() - start;
        }

        int error = Error.of(y, prediction);
        double accuracy = Accuracy.of(y, prediction);
        if (soft) {
            if (k == 2) {
                double[] probability = Arrays.stream(posteriori).mapToDouble(p -> p[1]).toArray();
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Sensitivity.of(y, prediction),
                        Specificity.of(y, prediction),
                        Precision.of(y, prediction),
                        FScore.F1.score(y, prediction),
                        MatthewsCorrelation.of(y, prediction),
                        AUC.of(y, probability),
                        LogLoss.of(y, probability),
                        CrossEntropy.of(y, posteriori));
            } else {
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN,
                        CrossEntropy.of(y, posteriori));
            }
        } else {
            if (k == 2) {
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Sensitivity.of(y, prediction),
                        Specificity.of(y, prediction),
                        Precision.of(y, prediction),
                        FScore.F1.score(y, prediction),
                        MatthewsCorrelation.of(y, prediction),
                        Double.NaN, Double.NaN, Double.NaN);
            } else {
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN,
                        CrossEntropy.of(y, posteriori));
            }
        }
    }

    /**
     * Runs leave-one-out cross validation tests.
     *
     * @param formula the model formula.
     * @param data    the training data.
     * @param trainer the lambda to train the model.
     * @return the validation results.
     */
    @SuppressWarnings("unchecked")
    static ClassificationMetrics classification(Formula formula, DataFrame data, BiFunction<Formula, DataFrame, DataFrameClassifier> trainer) {
        int[] y = formula.y(data).toIntArray();
        int k = MathEx.unique(y).length;
        int n = y.length;

        int[][] train = LOOCV.of(n);
        int[] prediction = new int[n];
        double[][] posteriori = new double[n][k];
        long fitTime = 0;
        long scoreTime = 0;
        boolean soft = false;

        for (int i = 0; i < n; i++) {
            long start = System.nanoTime();
            DataFrameClassifier model = trainer.apply(formula, data.of(train[i]));
            fitTime += System.nanoTime() - start;

            start = System.nanoTime();
            if (model instanceof SoftClassifier) {
                soft = true;
                prediction[i] = ((SoftClassifier<Tuple>) model).predict(data.get(i), posteriori[i]);
            } else {
                prediction[i] = model.predict(data.get(i));
            }
            scoreTime += System.nanoTime() - start;
        }

        int error = Error.of(y, prediction);
        double accuracy = Accuracy.of(y, prediction);
        if (soft) {
            if (k == 2) {
                double[] probability = Arrays.stream(posteriori).mapToDouble(p -> p[1]).toArray();
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Sensitivity.of(y, prediction),
                        Specificity.of(y, prediction),
                        Precision.of(y, prediction),
                        FScore.F1.score(y, prediction),
                        MatthewsCorrelation.of(y, prediction),
                        AUC.of(y, probability),
                        LogLoss.of(y, probability),
                        CrossEntropy.of(y, posteriori));
            } else {
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN,
                        CrossEntropy.of(y, posteriori));
            }
        } else {
            if (k == 2) {
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Sensitivity.of(y, prediction),
                        Specificity.of(y, prediction),
                        Precision.of(y, prediction),
                        FScore.F1.score(y, prediction),
                        MatthewsCorrelation.of(y, prediction),
                        Double.NaN, Double.NaN, Double.NaN);
            } else {
                return new ClassificationMetrics(
                        fitTime / (n * 1E6),
                        scoreTime / (n * 1E6),
                        n, error, accuracy,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN,
                        CrossEntropy.of(y, posteriori));
            }
        }
    }
}
