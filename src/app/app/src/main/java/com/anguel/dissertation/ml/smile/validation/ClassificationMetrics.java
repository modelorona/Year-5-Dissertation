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

import java.io.Serializable;

import lombok.Builder;
import lombok.Setter;

@Builder
@Setter
/* The classification validation metrics. */
public class ClassificationMetrics implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The time in milliseconds of fitting the model.
     */
    public double fitTime;
    /**
     * The time in milliseconds of scoring the validation data.
     */
    public double scoreTime;
    /**
     * The validation data size.
     */
    public final int size;
    /**
     * The number of errors.
     */
    public final int error;
    /**
     * The accuracy on validation data.
     */
    public double accuracy;
    /**
     * The sensitivity on validation data.
     */
    public double sensitivity;
    /**
     * The specificity on validation data.
     */
    public double specificity;
    /**
     * The precision on validation data.
     */
    public double precision;
    /**
     * The F-1 score on validation data.
     */
    public double f1;
    /**
     * The Matthews correlation coefficient on validation data.
     */
    public double mcc;
    /**
     * The AUC on validation data.
     */
    public double auc;
    /**
     * The log loss on validation data.
     */
    public double logloss;
    /**
     * The cross entropy on validation data.
     */
    public double crossentropy;

    /**
     * Constructor.
     *
     * @param fitTime   the time in milliseconds of fitting the model.
     * @param scoreTime the time in milliseconds of scoring the validation data.
     * @param size      the validation data size.
     * @param error     the number of errors.
     * @param accuracy  the accuracy on validation data.
     */
    public ClassificationMetrics(double fitTime, double scoreTime, int size, int error, double accuracy) {
        this(fitTime, scoreTime, size, error, accuracy, Double.NaN);
    }

    /**
     * Constructor of multiclass soft classifier validation.
     *
     * @param fitTime      the time in milliseconds of fitting the model.
     * @param scoreTime    the time in milliseconds of scoring the validation data.
     * @param size         the validation data size.
     * @param error        the number of errors.
     * @param accuracy     the accuracy on validation data.
     * @param crossentropy the cross entropy on validation data.
     */
    public ClassificationMetrics(double fitTime, double scoreTime, int size, int error, double accuracy, double crossentropy) {
        this.fitTime = fitTime;
        this.scoreTime = scoreTime;
        this.size = size;
        this.error = error;
        this.accuracy = accuracy;
        this.crossentropy = crossentropy;
        this.sensitivity = Double.NaN;
        this.specificity = Double.NaN;
        this.precision = Double.NaN;
        this.f1 = Double.NaN;
        this.mcc = Double.NaN;
        this.auc = Double.NaN;
        this.logloss = Double.NaN;
    }

    /**
     * Constructor of binary classifier validation.
     *
     * @param fitTime     the time in milliseconds of fitting the model.
     * @param scoreTime   the time in milliseconds of scoring the validation data.
     * @param size        the validation data size.
     * @param error       the number of errors.
     * @param accuracy    the accuracy on validation data.
     * @param sensitivity the sensitivity on validation data.
     * @param specificity the specificity on validation data.
     * @param precision   the precision on validation data.
     * @param f1          the F-1 score on validation data.
     * @param mcc         the Matthews correlation coefficient on validation data.
     */
    public ClassificationMetrics(double fitTime, double scoreTime, int size, int error,
                                 double accuracy, double sensitivity, double specificity,
                                 double precision, double f1, double mcc) {
        this(fitTime, scoreTime, size, error, accuracy, sensitivity, specificity, precision, f1, mcc, Double.NaN, Double.NaN);
    }

    /**
     * Constructor of binary soft classifier validation.
     *
     * @param fitTime     the time in milliseconds of fitting the model.
     * @param scoreTime   the time in milliseconds of scoring the validation data.
     * @param size        the validation data size.
     * @param error       the number of errors.
     * @param accuracy    the accuracy on validation data.
     * @param sensitivity the sensitivity on validation data.
     * @param specificity the specificity on validation data.
     * @param precision   the precision on validation data.
     * @param f1          the F-1 score on validation data.
     * @param mcc         the Matthews correlation coefficient on validation data.
     * @param auc         the AUC on validation data.
     * @param logloss     the log loss on validation data.
     */
    public ClassificationMetrics(double fitTime, double scoreTime, int size, int error,
                                 double accuracy, double sensitivity, double specificity,
                                 double precision, double f1, double mcc, double auc,
                                 double logloss) {
        this.fitTime = fitTime;
        this.scoreTime = scoreTime;
        this.size = size;
        this.error = error;
        this.accuracy = accuracy;
        this.sensitivity = sensitivity;
        this.specificity = specificity;
        this.precision = precision;
        this.f1 = f1;
        this.mcc = mcc;
        this.auc = auc;
        this.logloss = logloss;
        this.crossentropy = logloss;
    }

    /**
     * Constructor.
     *
     * @param fitTime      the time in milliseconds of fitting the model.
     * @param scoreTime    the time in milliseconds of scoring the validation data.
     * @param size         the validation data size.
     * @param error        the number of errors.
     * @param accuracy     the accuracy on validation data.
     * @param sensitivity  the sensitivity on validation data.
     * @param specificity  the specificity on validation data.
     * @param precision    the precision on validation data.
     * @param f1           the F-1 score on validation data.
     * @param mcc          the Matthews correlation coefficient on validation data.
     * @param auc          the AUC on validation data.
     * @param logloss      the log loss on validation data.
     * @param crossentropy the cross entropy on validation data.
     */
    public ClassificationMetrics(double fitTime, double scoreTime, int size, int error,
                                 double accuracy, double sensitivity, double specificity,
                                 double precision, double f1, double mcc, double auc,
                                 double logloss, double crossentropy) {
        this.fitTime = fitTime;
        this.scoreTime = scoreTime;
        this.size = size;
        this.error = error;
        this.accuracy = accuracy;
        this.sensitivity = sensitivity;
        this.specificity = specificity;
        this.precision = precision;
        this.f1 = f1;
        this.mcc = mcc;
        this.auc = auc;
        this.logloss = logloss;
        this.crossentropy = crossentropy;
    }

    // gets all its own values, but replaces the NaN with a json encodable value
    public ClassificationMetrics getSafeSelf() {
        final double safeDouble = -9999.00; // hopefully none of the values below can ever be this...
        if (Double.isNaN(sensitivity)) this.sensitivity = safeDouble;
        if (Double.isNaN(specificity)) this.specificity = safeDouble;
        if (Double.isNaN(precision)) this.precision = safeDouble;
        if (Double.isNaN(f1)) this.f1 = safeDouble;
        if (Double.isNaN(mcc)) this.mcc = safeDouble;
        if (Double.isNaN(auc)) this.auc = safeDouble;
        if (Double.isNaN(logloss)) this.logloss = safeDouble;
        if (Double.isNaN(crossentropy)) this.crossentropy = safeDouble;
        if (Double.isNaN(fitTime)) this.fitTime = safeDouble;
        if (Double.isNaN(scoreTime)) this.scoreTime = safeDouble;
        if (Double.isNaN(accuracy)) this.accuracy = safeDouble;
        return this;
    }

}
