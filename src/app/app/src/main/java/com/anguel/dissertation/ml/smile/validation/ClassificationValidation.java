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
import com.anguel.dissertation.ml.smile.validation.metric.ConfusionMatrix;
import com.anguel.dissertation.ml.smile.validation.metric.CrossEntropy;
import com.anguel.dissertation.ml.smile.validation.metric.Error;
import com.anguel.dissertation.ml.smile.validation.metric.FScore;
import com.anguel.dissertation.ml.smile.validation.metric.LogLoss;
import com.anguel.dissertation.ml.smile.validation.metric.MatthewsCorrelation;
import com.anguel.dissertation.ml.smile.validation.metric.Precision;
import com.anguel.dissertation.ml.smile.validation.metric.Sensitivity;
import com.anguel.dissertation.ml.smile.validation.metric.Specificity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Classification model validation results.
 *
 * @param <M> the model type.
 * @author Haifeng
 */
public class ClassificationValidation<M> implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The model.
     */
    public final M model;
    /**
     * The true class labels of validation data.
     */
    public final int[] truth;
    /**
     * The model prediction.
     */
    public final int[] prediction;
    /**
     * The posteriori probability of prediction if the model is a soft classifier.
     */
    public final double[][] posteriori;
    /**
     * The confusion matrix.
     */
    public final ConfusionMatrix confusion;
    /**
     * The classification metrics.
     */
    public final ClassificationMetrics metrics;

    /**
     * Constructor.
     *
     * @param model      the model.
     * @param truth      the ground truth.
     * @param prediction the predictions.
     * @param fitTime    the time in milliseconds of fitting the model.
     * @param scoreTime  the time in milliseconds of scoring the validation data.
     */
    public ClassificationValidation(M model, int[] truth, int[] prediction, double fitTime, double scoreTime) {
        this(model, truth, prediction, null, fitTime, scoreTime);
    }

    /**
     * Constructor of soft classifier validation..
     *
     * @param model      the model.
     * @param truth      the ground truth.
     * @param prediction the predictions.
     * @param posteriori the posteriori probabilities of predictions.
     * @param fitTime    the time in milliseconds of fitting the model.
     * @param scoreTime  the time in milliseconds of scoring the validation data.
     */
    public ClassificationValidation(M model, int[] truth, int[] prediction, double[][] posteriori, double fitTime, double scoreTime) {
        this.model = model;
        this.truth = truth;
        this.prediction = prediction;
        this.posteriori = posteriori;
        this.confusion = ConfusionMatrix.of(truth, prediction);

        int k = MathEx.unique(truth).length;
        if (k == 2) {
            if (posteriori == null) {
                metrics = new ClassificationMetrics(fitTime, scoreTime, truth.length,
                        Error.of(truth, prediction),
                        Accuracy.of(truth, prediction),
                        Sensitivity.of(truth, prediction),
                        Specificity.of(truth, prediction),
                        Precision.of(truth, prediction),
                        FScore.F1.score(truth, prediction),
                        MatthewsCorrelation.of(truth, prediction)
                );
            } else {
                double[] probability = Arrays.stream(posteriori).mapToDouble(p -> p[1]).toArray();
                metrics = new ClassificationMetrics(fitTime, scoreTime, truth.length,
                        Error.of(truth, prediction),
                        Accuracy.of(truth, prediction),
                        Sensitivity.of(truth, prediction),
                        Specificity.of(truth, prediction),
                        Precision.of(truth, prediction),
                        FScore.F1.score(truth, prediction),
                        MatthewsCorrelation.of(truth, prediction),
                        AUC.of(truth, probability),
                        LogLoss.of(truth, probability)
                );
            }
        } else {
            if (posteriori == null) {
                metrics = new ClassificationMetrics(fitTime, scoreTime, truth.length,
                        Error.of(truth, prediction),
                        Accuracy.of(truth, prediction));
            } else {
                metrics = new ClassificationMetrics(fitTime, scoreTime, truth.length,
                        Error.of(truth, prediction),
                        Accuracy.of(truth, prediction),
                        CrossEntropy.of(truth, posteriori)
                );
            }
        }
    }

    @Override
    public String toString() {
        return metrics.toString();
    }

    /**
     * Trains and validates a model on a train/validation split.
     *
     * @param x       the training data.
     * @param y       the class labels of training data.
     * @param testx   the validation data.
     * @param testy   the class labels of validation data.
     * @param trainer the lambda to train the model.
     * @param <T>     the data type of samples.
     * @param <M>     the model type.
     * @return the validation results.
     */
    public static <T, M extends Classifier<T>> ClassificationValidation<M> of(T[] x, int[] y, T[] testx, int[] testy, BiFunction<T[], int[], M> trainer) {
        int k = MathEx.unique(y).length;
        long start = System.nanoTime();
        M model = trainer.apply(x, y);
        double fitTime = (System.nanoTime() - start) / 1E6;

        start = System.nanoTime();
        if (model instanceof SoftClassifier) {
            double[][] posteriori = new double[testx.length][k];
            int[] prediction = ((SoftClassifier<T>) model).predict(testx, posteriori);
            double scoreTime = (System.nanoTime() - start) / 1E6;

            return new ClassificationValidation<>(model, testy, prediction, posteriori, fitTime, scoreTime);
        } else {
            int[] prediction = model.predict(testx);
            double scoreTime = (System.nanoTime() - start) / 1E6;

            return new ClassificationValidation<>(model, testy, prediction, fitTime, scoreTime);
        }
    }

    /**
     * Trains and validates a model on multiple train/validation split.
     *
     * @param bags    the data splits.
     * @param x       the training data.
     * @param y       the class labels.
     * @param trainer the lambda to train the model.
     * @param <T>     the data type of samples.
     * @param <M>     the model type.
     * @return the validation results.
     */
    public static <T, M extends Classifier<T>> ClassificationValidations<M> of(Bag[] bags, T[] x, int[] y, BiFunction<T[], int[], M> trainer) {
        List<ClassificationValidation<M>> rounds = new ArrayList<>(bags.length);

        for (Bag bag : bags) {
            T[] trainx = MathEx.slice(x, bag.samples);
            int[] trainy = MathEx.slice(y, bag.samples);
            T[] testx = MathEx.slice(x, bag.oob);
            int[] testy = MathEx.slice(y, bag.oob);

            rounds.add(of(trainx, trainy, testx, testy, trainer));
        }

        return new ClassificationValidations<>(rounds);
    }

    /**
     * Trains and validates a model on a train/validation split.
     *
     * @param formula the model formula.
     * @param train   the training data.
     * @param test    the validation data.
     * @param trainer the lambda to train the model.
     * @param <M>     the model type.
     * @return the validation results.
     */
    @SuppressWarnings("unchecked")
    public static <M extends DataFrameClassifier> ClassificationValidation<M> of(Formula formula, DataFrame train, DataFrame test, BiFunction<Formula, DataFrame, M> trainer) {
        int[] y = formula.y(train).toIntArray();
        int[] testy = formula.y(test).toIntArray();

        int k = MathEx.unique(y).length;
        long start = System.nanoTime();
        M model = trainer.apply(formula, train);
        double fitTime = (System.nanoTime() - start) / 1E6;

        start = System.nanoTime();
        int n = test.nrow();
        int[] prediction = new int[n];
        if (model instanceof SoftClassifier) {
            double[][] posteriori = new double[n][k];
            for (int i = 0; i < n; i++) {
                prediction[i] = ((SoftClassifier<Tuple>) model).predict(test.get(i), posteriori[i]);
            }
            double scoreTime = (System.nanoTime() - start) / 1E6;

            return new ClassificationValidation<>(model, testy, prediction, posteriori, fitTime, scoreTime);
        } else {
            for (int i = 0; i < n; i++) {
                prediction[i] = model.predict(test.get(i));
            }
            double scoreTime = (System.nanoTime() - start) / 1E6;

            return new ClassificationValidation<>(model, testy, prediction, fitTime, scoreTime);
        }
    }

    /**
     * Trains and validates a model on multiple train/validation split.
     *
     * @param bags    the data splits.
     * @param formula the model formula.
     * @param data    the data.
     * @param trainer the lambda to train the model.
     * @param <M>     the model type.
     * @return the validation results.
     */
    public static <M extends DataFrameClassifier> ClassificationValidations<M> of(Bag[] bags, Formula formula, DataFrame data, BiFunction<Formula, DataFrame, M> trainer) {
        List<ClassificationValidation<M>> rounds = new ArrayList<>(bags.length);

        for (Bag bag : bags) {
            rounds.add(of(formula, data.of(bag.samples), data.of(bag.oob), trainer));
        }

        return new ClassificationValidations<>(rounds);
    }
}
