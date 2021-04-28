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

package com.anguel.dissertation.ml.smile.data.formula;

import com.anguel.dissertation.ml.smile.data.DataFrame;
import com.anguel.dissertation.ml.smile.data.Tuple;
import com.anguel.dissertation.ml.smile.data.type.DataTypes;
import com.anguel.dissertation.ml.smile.data.type.StructField;
import com.anguel.dissertation.ml.smile.data.type.StructType;
import com.anguel.dissertation.ml.smile.data.vector.BaseVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The model fitting formula in a compact symbolic form.
 * An expression of the form {@code y ~ model} is interpreted as a
 * specification that the response y is modelled by a linear predictor
 * specified symbolically by model. Such a model consists of a series
 * of terms separated by {@code +} operators. The terms themselves
 * consist of variable and factor names separated by {@code ::} operators.
 * Such a term is interpreted as the interaction of all the variables and
 * factors appearing in the term. The special term {@code "."} means
 * all columns not otherwise in the formula in the context of a data frame.
 * <p>
 * In addition to {@code +} and {@code ::}, a number of other operators
 * are useful in model formulae. The {@code &&} operator denotes factor
 * crossing: {@code a && b} interpreted as {@code a+b+a::b}. The {@code ^}
 * operator indicates crossing to the specified degree. For example
 * {@code (a+b+c)^2} is identical to {@code :(a+b+c)*(a+b+c)} which in turn
 * expands to a formula containing the main effects for {@code a},
 * {@code b} and {@code c} together with their second-order interactions.
 * The {@code -} operator removes the specified terms, so that
 * {@code (a+b+c)^2 - a::b} is identical to {@code a + b + c + b::c + a::c}.
 * It can also used to remove the intercept term: when fitting a linear model
 * {@code y ~ x - 1} specifies a line through the origin. A model with
 * no intercept can be also specified as {@code y ~ x + 0}.
 * <p>
 * While formulae usually involve just variable and factor names, they
 * can also involve arithmetic expressions. The formula
 * {@code log(y) ~ a + log(x)}, for example, is legal.
 * <p>
 * Note that the operators {@code ~}, {@code +}, {@code ::}, {@code ^}
 * are only available in Scala API.
 *
 * @author Haifeng Li
 */
public class Formula implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The left-hand side of formula.
     */
    private final Term response;
    /**
     * The right-hand side of formula.
     */
    private final Term[] predictors;
    /**
     * The formula-schema binding.
     */
    private transient ThreadLocal<Binding> binding;

    /**
     * The formula-schema binding.
     */
    private static class Binding {
        /**
         * The input schema.
         */
        StructType inputSchema;
        /**
         * The output schema with response variable and predictors.
         */
        StructType yxschema;
        /**
         * The output schema with only predictors.
         */
        StructType xschema;
        /**
         * The response variable and predictors.
         */
        Feature[] yx;
        /**
         * The predictors.
         */
        Feature[] x;
    }

    /**
     * Constructor.
     *
     * @param response   the left-hand side of formula, i.e. dependent variable.
     * @param predictors the right-hand side of formula, i.e. independent/predictor variables.
     */
    public Formula(Term response, Term... predictors) {
        if (response instanceof Dot || response instanceof FactorCrossing) {
            throw new IllegalArgumentException("The response variable cannot be '.' or FactorCrossing.");
        }

        this.response = response;
        this.predictors = predictors;
    }

    /**
     * Returns the predictors.
     *
     * @return the predictors.
     */
    public Term[] predictors() {
        return predictors;
    }

    /**
     * Returns the response term.
     *
     * @return the response term.
     */
    public Term response() {
        return response;
    }

    @Override
    public String toString() {
        String r = response == null ? "" : response.toString();
        String p = Arrays.stream(predictors).map(predictor -> {
            String s = predictor.toString();
            if (!s.startsWith("- ")) s = "+ " + s;
            return s;
        }).collect(Collectors.joining(" "));

        if (p.startsWith("+ ")) p = p.substring(2);
        return String.format("%s ~ %s", r, p);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Formula)) return false;
        Formula f = (Formula) o;
        if (predictors.length != f.predictors.length) return false;
        if (!String.valueOf(response).equals(String.valueOf(f.response))) return false;
        for (int i = 0; i < predictors.length; i++) {
            if (!String.valueOf(predictors[i]).equals(String.valueOf(f.predictors[i]))) return false;
        }
        return true;
    }

    /**
     * Factory method. The predictors will be all the columns not otherwise
     * in the formula in the context of a data frame.
     *
     * @param lhs the left-hand side of formula, i.e. dependent variable.
     * @return the formula.
     */
    public static Formula lhs(String lhs) {
        return lhs(new Variable(lhs));
    }

    /**
     * Factory method. The predictors will be all the columns not otherwise
     * in the formula in the context of a data frame.
     *
     * @param lhs the left-hand side of formula, i.e. dependent variable.
     * @return the formula.
     */
    public static Formula lhs(Term lhs) {
        return new Formula(lhs, new Dot());
    }

    /**
     * Factory method.
     *
     * @param response   the left-hand side of formula, i.e. dependent variable.
     * @param predictors the right-hand side of formula, i.e. independent/predictor variables.
     * @return the formula.
     */
    public static Formula of(String response, String... predictors) {
        return new Formula(
                new Variable(response),
                Arrays.stream(predictors).map(predictor -> {
                    if (predictor.equals(".")) return new Dot();
                    if (predictor.equals("1")) return new Intercept(true);
                    if (predictor.equals("0")) return new Intercept(false);
                    return new Variable(predictor);
                }).toArray(Term[]::new)
        );
    }

    /**
     * Factory method.
     *
     * @param response   the left-hand side of formula, i.e. dependent variable.
     * @param predictors the right-hand side of formula, i.e. independent/predictor variables.
     * @return the formula.
     */
    public static Formula of(String response, Term... predictors) {
        return new Formula(new Variable(response), predictors);
    }

    /**
     * Factory method.
     *
     * @param response   the left-hand side of formula, i.e. dependent variable.
     * @param predictors the right-hand side of formula, i.e. independent/predictor variables.
     * @return the formula.
     */
    public static Formula of(Term response, Term... predictors) {
        return new Formula(response, predictors);
    }

    /**
     * Expands the Dot and FactorCrossing terms on the given schema.
     *
     * @param inputSchema the schema to expand on
     * @return the expanded formula.
     */
    public Formula expand(StructType inputSchema) {
        Set<String> columns = new HashSet<>();
        if (response != null) columns.addAll(response.variables());
        Arrays.stream(predictors)
                .filter(term -> term instanceof FactorCrossing || term instanceof Variable)
                .forEach(term -> columns.addAll(term.variables()));

        List<Variable> rest = Arrays.stream(inputSchema.fields())
                .filter(field -> !columns.contains(field.name))
                .map(field -> new Variable(field.name))
                .collect(Collectors.toList());

        List<Term> expanded = new ArrayList<>();
        for (Term predictor : predictors) {
            if (predictor instanceof Dot) {
                expanded.addAll(rest);
            } else if (!(predictor instanceof Delete)) {
                expanded.addAll(predictor.expand());
            }
        }

        Set<String> deletes = Arrays.stream(predictors)
                .filter(predictor -> predictor instanceof Delete)
                .flatMap(predictor -> predictor.expand().stream())
                .map(term -> term.toString().substring(2)) // Delete starts with "- "
                .collect(Collectors.toSet());

        expanded.removeIf(term -> deletes.contains(term.toString()));
        return new Formula(response, expanded.toArray(new Term[0]));
    }

    /**
     * Binds the formula to a schema and returns the schema of predictors.
     *
     * @param inputSchema the schema to bind with
     * @return the data structure of output data frame.
     */
    public StructType bind(StructType inputSchema) {
        if (binding != null && binding.get().inputSchema == inputSchema) {
            return binding.get().xschema;
        }

        Formula formula = expand(inputSchema);

        Binding binding = new Binding();
        binding.inputSchema = inputSchema;

        List<Feature> features = Arrays.stream(formula.predictors)
                .filter(predictor -> !(predictor instanceof Delete) && !(predictor instanceof Intercept))
                .flatMap(predictor -> predictor.bind(inputSchema).stream())
                .collect(Collectors.toList());

        binding.x = features.toArray(new Feature[0]);
        binding.xschema = DataTypes.struct(
                features.stream()
                        .map(Feature::field)
                        .toArray(StructField[]::new)
        );

        if (response != null) {
            try {
                features.addAll(0, response.bind(inputSchema));
                binding.yx = features.toArray(new Feature[0]);
                binding.yxschema = DataTypes.struct(
                        features.stream()
                                .map(Feature::field)
                                .toArray(StructField[]::new)
                );
            } catch (NullPointerException ignored) {

            }
        }

        this.binding = new ThreadLocal<Binding>() {
            protected synchronized Binding initialValue() {
                return binding;
            }
        };
        return binding.xschema;
    }

    /**
     * Apply the formula on a tuple to generate the model data.
     *
     * @param tuple the input tuple.
     * @return the output tuple.
     */
    public Tuple apply(Tuple tuple) {
        bind(tuple.schema());

        Binding binding = this.binding.get();
        return new com.anguel.dissertation.ml.smile.data.AbstractTuple() {
            @Override
            public StructType schema() {
                return binding.yxschema;
            }

            @Override
            public Object get(int i) {
                return binding.yx[i].apply(tuple);
            }

            @Override
            public int getInt(int i) {
                return binding.yx[i].applyAsInt(tuple);
            }

            @Override
            public long getLong(int i) {
                return binding.yx[i].applyAsLong(tuple);
            }

            @Override
            public float getFloat(int i) {
                return binding.yx[i].applyAsFloat(tuple);
            }

            @Override
            public double getDouble(int i) {
                return binding.yx[i].applyAsDouble(tuple);
            }

            @Override
            public String toString() {
                return binding.yxschema.toString(this);
            }
        };
    }

    /**
     * Apply the formula on a tuple to generate the predictors data.
     *
     * @param tuple the input tuple.
     * @return the output tuple.
     */
    public Tuple x(Tuple tuple) {
        bind(tuple.schema());

        Binding binding = this.binding.get();
        return new com.anguel.dissertation.ml.smile.data.AbstractTuple() {
            @Override
            public StructType schema() {
                return binding.xschema;
            }

            @Override
            public Object get(int i) {
                return binding.x[i].apply(tuple);
            }

            @Override
            public int getInt(int i) {
                return binding.x[i].applyAsInt(tuple);
            }

            @Override
            public long getLong(int i) {
                return binding.x[i].applyAsLong(tuple);
            }

            @Override
            public float getFloat(int i) {
                return binding.x[i].applyAsFloat(tuple);
            }

            @Override
            public double getDouble(int i) {
                return binding.x[i].applyAsDouble(tuple);
            }

            @Override
            public String toString() {
                return binding.xschema.toString(this);
            }
        };
    }

    /**
     * Returns a data frame of predictors and optionally response variable
     * (if input data frame has the related variable(s)).
     *
     * @param data The input data frame.
     * @return the output data frame.
     */
    public DataFrame frame(DataFrame data) {
        bind(data.schema());

        Binding binding = this.binding.get();
        BaseVector[] vectors = Arrays.stream(binding.yx != null ? binding.yx : binding.x)
                .map(term -> term.apply(data)).toArray(BaseVector[]::new);
        return DataFrame.of(vectors);
    }

    /**
     * Returns a data frame of predictors.
     *
     * @param data The input data frame.
     * @return the data frame of predictors.
     */
    public DataFrame x(DataFrame data) {
        bind(data.schema());
        Binding binding = this.binding.get();
        BaseVector[] vectors = Arrays.stream(binding.x)
                .map(term -> term.apply(data)).toArray(BaseVector[]::new);
        return DataFrame.of(vectors);
    }

    /**
     * Returns the response vector.
     *
     * @param data The input data frame.
     * @return the response vector.
     */
    public BaseVector y(DataFrame data) {
        if (response == null) {
            throw new UnsupportedOperationException("The formula has no response variable.");
        }

        bind(data.schema());

        Binding binding = this.binding.get();
        if (binding.yx == null) {
            throw new UnsupportedOperationException("The data has no response variable.");
        }

        return binding.yx[0].apply(data);
    }

    /**
     * Returns the real-valued response value.
     *
     * @param tuple the input tuple.
     * @return the response variable.
     */
    public double y(Tuple tuple) {
        if (response == null) {
            throw new UnsupportedOperationException("The formula has no response variable.");
        }

        bind(tuple.schema());

        Binding binding = this.binding.get();
        if (binding.yx == null) {
            throw new UnsupportedOperationException("The data has no response variable.");
        }

        return binding.yx[0].applyAsDouble(tuple);
    }

}
