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

package com.anguel.dissertation.ml.smile.validation.metric;

import java.io.Serializable;

/**
 * An abstract interface to measure the classification performance.
 *
 * @author Haifeng Li
 */
public interface ClassificationMetric extends Serializable {
    /**
     * Returns a score to measure the quality of classification.
     *
     * @param truth      the true class labels.
     * @param prediction the predicted class labels.
     * @return the metric.
     */
    double score(int[] truth, int[] prediction);
}
