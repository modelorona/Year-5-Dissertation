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

import com.anguel.dissertation.ml.smile.base.cart.CART;
import com.anguel.dissertation.ml.smile.base.cart.DecisionNode;
import com.anguel.dissertation.ml.smile.base.cart.LeafNode;
import com.anguel.dissertation.ml.smile.base.cart.NominalSplit;
import com.anguel.dissertation.ml.smile.base.cart.OrdinalSplit;
import com.anguel.dissertation.ml.smile.base.cart.Split;
import com.anguel.dissertation.ml.smile.base.cart.SplitRule;
import com.anguel.dissertation.ml.smile.data.DataFrame;
import com.anguel.dissertation.ml.smile.data.Tuple;
import com.anguel.dissertation.ml.smile.data.formula.Formula;
import com.anguel.dissertation.ml.smile.data.measure.Measure;
import com.anguel.dissertation.ml.smile.data.measure.NominalScale;
import com.anguel.dissertation.ml.smile.data.type.StructField;
import com.anguel.dissertation.ml.smile.data.type.StructType;
import com.anguel.dissertation.ml.smile.data.vector.BaseVector;
import com.anguel.dissertation.ml.smile.math.MathEx;
import com.anguel.dissertation.ml.smile.util.IntSet;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Properties;

/**
 * Decision tree. A classification/regression tree can be learned by
 * splitting the training set into subsets based on an attribute value
 * test. This process is repeated on each derived subset in a recursive
 * manner called recursive partitioning. The recursion is completed when
 * the subset at a node all has the same value of the target variable,
 * or when splitting no longer adds value to the predictions.
 * <p>
 * The algorithms that are used for constructing decision trees usually
 * work top-down by choosing a variable at each step that is the next best
 * variable to use in splitting the set of items. "Best" is defined by how
 * well the variable splits the set into homogeneous subsets that have
 * the same value of the target variable. Different algorithms use different
 * formulae for measuring "best". Used by the CART algorithm, Gini impurity
 * is a measure of how often a randomly chosen element from the set would
 * be incorrectly labeled if it were randomly labeled according to the
 * distribution of labels in the subset. Gini impurity can be computed by
 * summing the probability of each item being chosen times the probability
 * of a mistake in categorizing that item. It reaches its minimum (zero) when
 * all cases in the node fall into a single target category. Information gain
 * is another popular measure, used by the ID3, C4.5 and C5.0 algorithms.
 * Information gain is based on the concept of entropy used in information
 * theory. For categorical variables with different number of levels, however,
 * information gain are biased in favor of those attributes with more levels.
 * Instead, one may employ the information gain ratio, which solves the drawback
 * of information gain.
 * <p>
 * Classification and Regression Tree techniques have a number of advantages
 * over many of those alternative techniques.
 * <dl>
 * <dt>Simple to understand and interpret.</dt>
 * <dd>In most cases, the interpretation of results summarized in a tree is
 * very simple. This simplicity is useful not only for purposes of rapid
 * classification of new observations, but can also often yield a much simpler
 * "model" for explaining why observations are classified or predicted in a
 * particular manner.</dd>
 * <dt>Able to handle both numerical and categorical data.</dt>
 * <dd>Other techniques are usually specialized in analyzing datasets that
 * have only one type of variable. </dd>
 * <dt>Tree methods are nonparametric and nonlinear.</dt>
 * <dd>The final results of using tree methods for classification or regression
 * can be summarized in a series of (usually few) logical if-then conditions
 * (tree nodes). Therefore, there is no implicit assumption that the underlying
 * relationships between the predictor variables and the dependent variable
 * are linear, follow some specific non-linear link function, or that they
 * are even monotonic in nature. Thus, tree methods are particularly well
 * suited for data mining tasks, where there is often little a priori
 * knowledge nor any coherent set of theories or predictions regarding which
 * variables are related and how. In those types of data analytics, tree
 * methods can often reveal simple relationships between just a few variables
 * that could have easily gone unnoticed using other analytic techniques.</dd>
 * </dl>
 * One major problem with classification and regression trees is their high
 * variance. Often a small change in the data can result in a very different
 * series of splits, making interpretation somewhat precarious. Besides,
 * decision-tree learners can create over-complex trees that cause over-fitting.
 * Mechanisms such as pruning are necessary to avoid this problem.
 * Another limitation of trees is the lack of smoothness of the prediction
 * surface.
 * <p>
 * Some techniques such as bagging, boosting, and random forest use more than
 * one decision tree for their analysis.
 *
 * @author Haifeng Li
 * @see RandomForest
 */
public class DecisionTree extends CART implements SoftClassifier<Tuple>, DataFrameClassifier {
    private static final long serialVersionUID = 2L;

    /**
     * The splitting rule.
     */
    private final SplitRule rule;
    /**
     * The number of classes.
     */
    private final int k;
    /**
     * The class label encoder.
     */
    private IntSet labels;

    /**
     * The dependent variable.
     */
    private transient final int[] y;

    @Override
    protected double impurity(LeafNode node) {
        return ((DecisionNode) node).impurity(rule);
    }

    @Override
    protected LeafNode newNode(int[] nodeSamples) {
        int[] count = new int[k];
        for (int i : nodeSamples) {
            count[y[i]] += samples[i];
        }
        return new DecisionNode(count);
    }

    @Override
    protected Optional<Split> findBestSplit(LeafNode leaf, int j, double impurity, int lo, int hi) {
        DecisionNode node = (DecisionNode) leaf;
        BaseVector xj = x.column(j);
        int[] falseCount = new int[k];

        Split split = null;
        double splitScore = 0.0;
        int splitTrueCount = 0;
        int splitFalseCount = 0;

        Measure measure = schema.field(j).measure;
        if (measure instanceof NominalScale) {
            int splitValue = -1;

            NominalScale scale = (NominalScale) measure;
            int m = scale.size();
            int[][] trueCount = new int[m][k];

            for (int i = lo; i < hi; i++) {
                int o = index[i];
                trueCount[xj.getInt(o)][y[o]] += samples[o];
            }

            for (int l : scale.values()) {
                int tc = (int) MathEx.sum(trueCount[l]);
                int fc = node.size() - tc;

                // If either side is too small, skip this value.
                if (tc < nodeSize || fc < nodeSize) {
                    continue;
                }

                for (int q = 0; q < k; q++) {
                    falseCount[q] = node.count()[q] - trueCount[l][q];
                }

                double gain = impurity - (double) tc / node.size() * DecisionNode.impurity(rule, tc, trueCount[l]) - (double) fc / node.size() * DecisionNode.impurity(rule, fc, falseCount);

                // new best split
                if (gain > splitScore) {
                    splitValue = l;
                    splitTrueCount = tc;
                    splitFalseCount = fc;
                    splitScore = gain;
                }
            }

            if (splitScore > 0.0) {
                final int value = splitValue;
                split = new NominalSplit(leaf, j, splitValue, splitScore, lo, hi, splitTrueCount, splitFalseCount, (int o) -> xj.getInt(o) == value);
            }
        } else {
            double splitValue = 0.0;
            int[] trueCount = new int[k];
            int[] orderj = order[j];

            int first = orderj[lo];
            double prevx = xj.getDouble(first);
            int prevy = y[first];

            for (int i = lo; i < hi; i++) {
                int tc = 0;
                int fc = 0;

                int o = orderj[i];
                int yi = y[o];
                double xij = xj.getDouble(o);

                if (yi != prevy && !MathEx.isZero(xij - prevx, 1E-7)) {
                    tc = (int) MathEx.sum(trueCount);
                    fc = node.size() - tc;
                }

                // If either side is empty, skip this value.
                if (tc >= nodeSize && fc >= nodeSize) {
                    for (int l = 0; l < k; l++) {
                        falseCount[l] = node.count()[l] - trueCount[l];
                    }

                    double gain = impurity - (double) tc / node.size() * DecisionNode.impurity(rule, tc, trueCount) - (double) fc / node.size() * DecisionNode.impurity(rule, fc, falseCount);

                    // new best split
                    if (gain > splitScore) {
                        splitValue = (xij + prevx) / 2;
                        splitTrueCount = tc;
                        splitFalseCount = fc;
                        splitScore = gain;
                    }
                }

                prevx = xij;
                prevy = yi;
                trueCount[prevy] += samples[o];
            }

            if (splitScore > 0.0) {
                final double value = splitValue;
                split = new OrdinalSplit(leaf, j, splitValue, splitScore, lo, hi, splitTrueCount, splitFalseCount, (int o) -> xj.getDouble(o) <= value);
            }
        }

        return Optional.ofNullable(split);
    }

    /**
     * Constructor. Learns a classification tree for AdaBoost and Random Forest.
     *
     * @param x        the data frame of the explanatory variable.
     * @param y        the response variables.
     * @param response the metadata of response variable.
     * @param k        the number of classes.
     * @param maxDepth the maximum depth of the tree.
     * @param maxNodes the maximum number of leaf nodes in the tree.
     * @param nodeSize the minimum size of leaf nodes.
     * @param mtry     the number of input variables to pick to split on at each
     *                 node. It seems that sqrt(p) give generally good performance,
     *                 where p is the number of variables.
     * @param rule     the splitting rule.
     * @param samples  the sample set of instances for stochastic learning.
     *                 samples[i] is the number of sampling for instance i.
     * @param order    the index of training values in ascending order. Note
     *                 that only numeric attributes need be sorted.
     */
    public DecisionTree(DataFrame x, int[] y, StructField response, int k, SplitRule rule, int maxDepth, int maxNodes, int nodeSize, int mtry, int[] samples, int[][] order) {
        super(x, response, maxDepth, maxNodes, nodeSize, mtry, samples, order);
        this.k = k;
        this.y = y;
        this.rule = rule;

        final int[] count = new int[k];
        int n = x.size();
        for (int i = 0; i < n; i++) {
            count[y[i]] += this.samples[i];
        }

        LeafNode node = new DecisionNode(count);
        this.root = node;

        Optional<Split> split = findBestSplit(node, 0, index.length, new boolean[x.ncol()]);

        if (maxNodes == Integer.MAX_VALUE) {
            // deep-first split
            split.ifPresent(s -> split(s, null));
        } else {
            // best-first split
            PriorityQueue<Split> queue = new PriorityQueue<>(2 * maxNodes, Split.comparator.reversed());
            split.ifPresent(queue::add);

            for (int leaves = 1; leaves < this.maxNodes && !queue.isEmpty(); ) {
                if (split(queue.poll(), queue)) leaves++;
            }
        }

        // merge the sister leaves that produce the same output.
        this.root = this.root.merge();

        clear();
    }

    /**
     * Learns a classification tree.
     *
     * @param formula a symbolic description of the model to be fitted.
     * @param data    the data frame of the explanatory and response variables.
     * @return the model.
     */
    public static DecisionTree fit(Formula formula, DataFrame data) {
        return fit(formula, data, new Properties());
    }

    /**
     * Learns a classification tree.
     * The hyper-parameters in <code>prop</code> include
     * <ul>
     * <li><code>smile.cart.split.rule</code>
     * <li><code>smile.cart.node.size</code>
     * <li><code>smile.cart.max.nodes</code>
     * </ul>
     *
     * @param formula a symbolic description of the model to be fitted.
     * @param data    the data frame of the explanatory and response variables.
     * @param prop    the hyper-parameters.
     * @return the model.
     */
    public static DecisionTree fit(Formula formula, DataFrame data, Properties prop) {
        SplitRule rule = SplitRule.valueOf(prop.getProperty("smile.cart.split.rule", "GINI"));
        int maxDepth = Integer.parseInt(prop.getProperty("smile.cart.max.depth", "20"));
        int maxNodes = Integer.parseInt(prop.getProperty("smile.cart.max.nodes", String.valueOf(data.size() / 5)));
        int nodeSize = Integer.parseInt(prop.getProperty("smile.cart.node.size", "5"));
        return fit(formula, data, rule, maxDepth, maxNodes, nodeSize);
    }

    /**
     * Learns a classification tree.
     *
     * @param formula  a symbolic description of the model to be fitted.
     * @param data     the data frame of the explanatory and response variables.
     * @param rule     the splitting rule.
     * @param maxDepth the maximum depth of the tree.
     * @param maxNodes the maximum number of leaf nodes in the tree.
     * @param nodeSize the minimum size of leaf nodes.
     * @return the model.
     */
    public static DecisionTree fit(Formula formula, DataFrame data, SplitRule rule, int maxDepth, int maxNodes, int nodeSize) {
        formula = formula.expand(data.schema());
        DataFrame x = formula.x(data);
        BaseVector y = formula.y(data);
        ClassLabels codec = ClassLabels.fit(y);

        DecisionTree tree = new DecisionTree(x, codec.y, y.field(), codec.k, rule, maxDepth, maxNodes, nodeSize, -1, null, null);
        tree.formula = formula;
        tree.labels = codec.labels;
        return tree;
    }

    @Override
    public int predict(Tuple x) {
        DecisionNode leaf = (DecisionNode) root.predict(predictors(x));
        int y = leaf.output();
        return labels == null ? y : labels.valueOf(y);
    }

    /**
     * Predicts the class label of an instance and also calculate a posteriori
     * probabilities. The posteriori estimation is based on sample distribution
     * in the leaf node. It is not accurate at all when be used in a single tree.
     * It is mainly used by RandomForest in an ensemble way.
     */
    @Override
    public int predict(Tuple x, double[] posteriori) {
        DecisionNode leaf = (DecisionNode) root.predict(predictors(x));
        leaf.posteriori(posteriori);
        int y = leaf.output();
        return labels == null ? y : labels.valueOf(y);
    }

    /**
     * Returns null if the tree is part of ensemble algorithm.
     */
    @Override
    public Formula formula() {
        return formula;
    }

    @Override
    public StructType schema() {
        return schema;
    }

}
