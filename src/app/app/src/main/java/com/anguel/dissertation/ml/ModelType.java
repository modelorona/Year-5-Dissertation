package com.anguel.dissertation.ml;

public enum ModelType {
    DAILY, // one that's created every 24 hours
    COMBINED, // the result of the current + daily
    OVERALL // the one created at the very end
}
