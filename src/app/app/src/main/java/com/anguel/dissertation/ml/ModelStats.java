package com.anguel.dissertation.ml;

import com.anguel.dissertation.ml.smile.validation.metric.ConfusionMatrix;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

// a collection of various stats bundled up nicely
// note: these are generated from test data predictions, not by the tree itself during fitting
@Data
@Builder
public class ModelStats implements Serializable {
    private static final long serialVersionUID = 300L;

    double accuracy;
    double sensitivity;
    double specificity;
    double precision;
    double falseDiscoveryRate;
    double falsePositiveRate;
    double f1score;
    ConfusionMatrix confusionMatrix;
    long trainSize;
    long testSize;
    long anxiousCountTrain;
    long anxiousCountTest;
}
