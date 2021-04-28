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

import com.anguel.dissertation.ml.smile.data.Tuple;
import com.anguel.dissertation.ml.smile.data.type.DataTypes;
import com.anguel.dissertation.ml.smile.data.type.StructField;
import com.anguel.dissertation.ml.smile.data.type.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

/**
 * Predefined terms.
 *
 * @author Haifeng Li
 */
public interface Terms {
    /**
     * Creates a variable.
     *
     * @param x the variable.
     * @return the term.
     */
    static Term $(String x) {
        switch (x) {
            case ".":
                return new Dot();
            case "0":
                return new Intercept(false);
            case "1":
                return new Intercept(true);
            default:
                return new Variable(x);
        }
    }

    /**
     * Factor crossing of two or more factors.
     *
     * @param factors the factors.
     * @return the crossing term.
     */
    static FactorCrossing cross(String... factors) {
        return new FactorCrossing(factors);
    }

    /**
     * Factor crossing of two or more factors.
     *
     * @param order   the order of factor interactions.
     * @param factors the factors.
     * @return the crossing term.
     */
    static FactorCrossing cross(int order, String... factors) {
        return new FactorCrossing(order, factors);
    }

    /**
     * Adds two terms.
     *
     * @param a the term.
     * @param b the term.
     * @return the {@code a + b} term.
     */
    static Term add(Term a, Term b) {
        return new Add(a, b);
    }

    /**
     * Adds two terms.
     *
     * @param a the term.
     * @param b the term.
     * @return the {@code a + b} term.
     */
    static Term add(String a, String b) {
        return new Add($(a), $(b));
    }

    /**
     * Adds two terms.
     *
     * @param a the term.
     * @param b the term.
     * @return the {@code a + b} term.
     */
    static Term add(Term a, String b) {
        return new Add(a, $(b));
    }

    /**
     * Adds two terms.
     *
     * @param a the term.
     * @param b the term.
     * @return the {@code a + b} term.
     */
    static Term add(String a, Term b) {
        return new Add($(a), b);
    }

    /**
     * The {@code log(x)} term.
     *
     * @param x the term.
     * @return the {@code log(x)} term.
     */
    static DoubleFunction log(String x) {
        return log($(x));
    }

    /**
     * The {@code log(x)} term.
     *
     * @param x the term.
     * @return the {@code log(x)} term.
     */
    static DoubleFunction log(Term x) {
        return new DoubleFunction("log", x, Math::log);
    }

    /**
     * The {@code sign(x)} term.
     *
     * @param x the term.
     * @return the {@code sign(x)} term.
     */
    static IntFunction sign(String x) {
        return sign($(x));
    }

    /**
     * The {@code sign(x)} term.
     *
     * @param x the term.
     * @return the {@code sign(x)} term.
     */
    static IntFunction sign(Term x) {
        return new IntFunction("sign", x, Integer::signum);
    }


    /**
     * Returns a term that applies a lambda on given variable.
     *
     * @param name the function name.
     * @param x    the variable name.
     * @param f    the lambda to apply on the variable.
     * @param <T>  the data type of input term.
     * @return the term.
     */
    static <T> Term of(final String name, final String x, ToIntFunction<T> f) {
        return of(name, $(x), f);
    }

    /**
     * Returns a term that applies a lambda on given term.
     *
     * @param name the function name.
     * @param x    the term.
     * @param f    the lambda to apply on the term.
     * @param <T>  the data type of input term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T> Term of(final String name, final Term x, ToIntFunction<T> f) {
        return new AbstractFunction(name, x) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();

                for (Feature feature : x.bind(schema)) {
                    features.add(new Feature() {
                        private final StructField field = new StructField(String.format("%s(%s)", name, feature), DataTypes.IntegerType, null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public int applyAsInt(Tuple o) {
                            return f.applyAsInt((T) feature.apply(o));
                        }

                        @Override
                        public long applyAsLong(Tuple o) {
                            return f.applyAsInt((T) feature.apply(o));
                        }

                        @Override
                        public float applyAsFloat(Tuple o) {
                            return f.applyAsInt((T) feature.apply(o));
                        }

                        @Override
                        public double applyAsDouble(Tuple o) {
                            return f.applyAsInt((T) feature.apply(o));
                        }

                        @Override
                        public Object apply(Tuple o) {
                            return f.applyAsInt((T) feature.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variable.
     *
     * @param name the function name.
     * @param x    the variable name.
     * @param f    the lambda to apply on the variable.
     * @param <T>  the data type of input term.
     * @return the term.
     */
    static <T> Term of(final String name, final String x, ToLongFunction<T> f) {
        return of(name, $(x), f);
    }

    /**
     * Returns a term that applies a lambda on given term.
     *
     * @param name the function name.
     * @param x    the term.
     * @param f    the lambda to apply on the term.
     * @param <T>  the data type of input term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T> Term of(final String name, final Term x, ToLongFunction<T> f) {
        return new AbstractFunction(name, x) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();

                for (Feature feature : x.bind(schema)) {
                    features.add(new Feature() {
                        private final StructField field = new StructField(String.format("%s(%s)", name, feature), DataTypes.LongType, null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public long applyAsLong(Tuple o) {
                            return f.applyAsLong((T) feature.apply(o));
                        }

                        @Override
                        public float applyAsFloat(Tuple o) {
                            return f.applyAsLong((T) feature.apply(o));
                        }

                        @Override
                        public double applyAsDouble(Tuple o) {
                            return f.applyAsLong((T) feature.apply(o));
                        }

                        @Override
                        public Object apply(Tuple o) {
                            return f.applyAsLong((T) feature.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variable.
     *
     * @param name the function name.
     * @param x    the variable name.
     * @param f    the lambda to apply on the variable.
     * @param <T>  the data type of input term.
     * @return the term.
     */
    static <T> Term of(final String name, final String x, ToDoubleFunction<T> f) {
        return of(name, $(x), f);
    }

    /**
     * Returns a term that applies a lambda on given term.
     *
     * @param name the function name.
     * @param x    the term.
     * @param f    the lambda to apply on the term.
     * @param <T>  the data type of input term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T> Term of(final String name, final Term x, ToDoubleFunction<T> f) {
        return new AbstractFunction(name, x) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();

                for (Feature feature : x.bind(schema)) {
                    features.add(new Feature() {
                        private final StructField field = new StructField(String.format("%s(%s)", name, feature), DataTypes.DoubleType, null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public double applyAsDouble(Tuple o) {
                            return f.applyAsDouble((T) feature.apply(o));
                        }

                        @Override
                        public Object apply(Tuple o) {
                            return f.applyAsDouble((T) feature.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variable.
     *
     * @param name  the function name.
     * @param x     the variable name.
     * @param clazz the class of return object.
     * @param f     the lambda to apply on the variable.
     * @param <T>   the data type of input term.
     * @param <R>   the data type of output term.
     * @return the term.
     */
    static <T, R> Term of(final String name, final String x, final Class<R> clazz, Function<T, R> f) {
        return of(name, $(x), clazz, f);
    }

    /**
     * Returns a term that applies a lambda on given term.
     *
     * @param name  the function name.
     * @param x     the term.
     * @param clazz the class of return object.
     * @param f     the lambda to apply on the term.
     * @param <T>   the data type of input term.
     * @param <R>   the data type of output term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T, R> Term of(final String name, final Term x, final Class<R> clazz, Function<T, R> f) {
        return new AbstractFunction(name, x) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();

                for (Feature feature : x.bind(schema)) {
                    features.add(new Feature() {
                        private final StructField field = new StructField(String.format("%s(%s)", name, feature), DataTypes.object(clazz), null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public Object apply(Tuple o) {
                            return f.apply((T) feature.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variables.
     *
     * @param name the function name.
     * @param x    the first parameter of function.
     * @param y    the second parameter of function.
     * @param f    the lambda to apply on the variables.
     * @param <T>  the data type of first input term.
     * @param <U>  the data type of second input term.
     * @return the term.
     */
    static <T, U> Term of(final String name, final String x, final String y, ToIntBiFunction<T, U> f) {
        return of(name, $(x), $(y), f);
    }

    /**
     * Returns a term that applies a lambda on given terms.
     *
     * @param name the function name.
     * @param x    the first parameter of function.
     * @param y    the second parameter of function.
     * @param f    the lambda to apply on the terms.
     * @param <T>  the data type of first input term.
     * @param <U>  the data type of second input term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T, U> Term of(final String name, final Term x, final Term y, ToIntBiFunction<T, U> f) {
        return new AbstractBiFunction(name, x, y) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();
                List<Feature> xfeatures = x.bind(schema);
                List<Feature> yfeatures = y.bind(schema);
                if (xfeatures.size() != yfeatures.size()) {
                    throw new IllegalStateException(String.format("The features of %s and %s are of different size: %d != %d", x, y, xfeatures.size(), yfeatures.size()));
                }

                for (int i = 0; i < xfeatures.size(); i++) {
                    Feature a = xfeatures.get(i);
                    StructField xfield = a.field();
                    Feature b = yfeatures.get(i);
                    StructField yfield = b.field();

                    features.add(new Feature() {
                        final StructField field = new StructField(String.format("%s(%s, %s)", name, xfield.name, yfield.name),
                                DataTypes.IntegerType,
                                null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public int applyAsInt(Tuple o) {
                            return f.applyAsInt((T) a.apply(o), (U) b.apply(o));
                        }

                        @Override
                        public Object apply(Tuple o) {
                            Object x = a.apply(o);
                            Object y = b.apply(o);
                            if (x == null || y == null) return null;
                            else return f.applyAsInt((T) a.apply(o), (U) b.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variables.
     *
     * @param name the function name.
     * @param x    the first parameter of function.
     * @param y    the second parameter of function.
     * @param f    the lambda to apply on the variables.
     * @param <T>  the data type of first input term.
     * @param <U>  the data type of second input term.
     * @return the term.
     */
    static <T, U> Term of(final String name, final String x, final String y, ToLongBiFunction<T, U> f) {
        return of(name, $(x), $(y), f);
    }

    /**
     * Returns a term that applies a lambda on given terms.
     *
     * @param name the function name.
     * @param x    the first parameter of function.
     * @param y    the second parameter of function.
     * @param f    the lambda to apply on the terms.
     * @param <T>  the data type of first input term.
     * @param <U>  the data type of second input term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T, U> Term of(final String name, final Term x, final Term y, ToLongBiFunction<T, U> f) {
        return new AbstractBiFunction(name, x, y) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();
                List<Feature> xfeatures = x.bind(schema);
                List<Feature> yfeatures = y.bind(schema);
                if (xfeatures.size() != yfeatures.size()) {
                    throw new IllegalStateException(String.format("The features of %s and %s are of different size: %d != %d", x, y, xfeatures.size(), yfeatures.size()));
                }

                for (int i = 0; i < xfeatures.size(); i++) {
                    Feature a = xfeatures.get(i);
                    StructField xfield = a.field();
                    Feature b = yfeatures.get(i);
                    StructField yfield = b.field();

                    features.add(new Feature() {
                        final StructField field = new StructField(String.format("%s(%s, %s)", name, xfield.name, yfield.name),
                                DataTypes.LongType,
                                null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public long applyAsLong(Tuple o) {
                            return f.applyAsLong((T) a.apply(o), (U) b.apply(o));
                        }

                        @Override
                        public Object apply(Tuple o) {
                            Object x = a.apply(o);
                            Object y = b.apply(o);
                            if (x == null || y == null) return null;
                            else return f.applyAsLong((T) a.apply(o), (U) b.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variables.
     *
     * @param name the function name.
     * @param x    the first parameter of function.
     * @param y    the second parameter of function.
     * @param f    the lambda to apply on the variables.
     * @param <T>  the data type of first input term.
     * @param <U>  the data type of second input term.
     * @return the term.
     */
    static <T, U> Term of(final String name, final String x, final String y, ToDoubleBiFunction<T, U> f) {
        return of(name, $(x), $(y), f);
    }

    /**
     * Returns a term that applies a lambda on given terms.
     *
     * @param name the function name.
     * @param x    the first parameter of function.
     * @param y    the second parameter of function.
     * @param f    the lambda to apply on the terms.
     * @param <T>  the data type of first input term.
     * @param <U>  the data type of second input term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T, U> Term of(final String name, final Term x, final Term y, ToDoubleBiFunction<T, U> f) {
        return new AbstractBiFunction(name, x, y) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();
                List<Feature> xfeatures = x.bind(schema);
                List<Feature> yfeatures = y.bind(schema);
                if (xfeatures.size() != yfeatures.size()) {
                    throw new IllegalStateException(String.format("The features of %s and %s are of different size: %d != %d", x, y, xfeatures.size(), yfeatures.size()));
                }

                for (int i = 0; i < xfeatures.size(); i++) {
                    Feature a = xfeatures.get(i);
                    StructField xfield = a.field();
                    Feature b = yfeatures.get(i);
                    StructField yfield = b.field();

                    features.add(new Feature() {
                        final StructField field = new StructField(String.format("%s(%s, %s)", name, xfield.name, yfield.name),
                                DataTypes.DoubleType,
                                null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public double applyAsDouble(Tuple o) {
                            return f.applyAsDouble((T) a.apply(o), (U) b.apply(o));
                        }

                        @Override
                        public Object apply(Tuple o) {
                            Object x = a.apply(o);
                            Object y = b.apply(o);
                            if (x == null || y == null) return null;
                            else return f.applyAsDouble((T) a.apply(o), (U) b.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }

    /**
     * Returns a term that applies a lambda on given variables.
     *
     * @param name  the function name.
     * @param x     the first parameter of function.
     * @param y     the second parameter of function.
     * @param clazz the class of return object.
     * @param f     the lambda to apply on the variables.
     * @param <T>   the data type of first input term.
     * @param <U>   the data type of second input term.
     * @param <R>   the data type of output term.
     * @return the term.
     */
    static <T, U, R> Term of(final String name, final String x, final String y, final Class<R> clazz, BiFunction<T, U, R> f) {
        return of(name, $(x), $(y), clazz, f);
    }

    /**
     * Returns a term that applies a lambda on given terms.
     *
     * @param name  the function name.
     * @param x     the first parameter of function.
     * @param y     the second parameter of function.
     * @param clazz the class of return object.
     * @param f     the lambda to apply on the terms.
     * @param <T>   the data type of first input term.
     * @param <U>   the data type of second input term.
     * @param <R>   the data type of output term.
     * @return the term.
     */
    @SuppressWarnings("unchecked")
    static <T, U, R> Term of(final String name, final Term x, final Term y, final Class<R> clazz, BiFunction<T, U, R> f) {
        return new AbstractBiFunction(name, x, y) {
            @Override
            public List<Feature> bind(StructType schema) {
                List<Feature> features = new ArrayList<>();
                List<Feature> xfeatures = x.bind(schema);
                List<Feature> yfeatures = y.bind(schema);
                if (xfeatures.size() != yfeatures.size()) {
                    throw new IllegalStateException(String.format("The features of %s and %s are of different size: %d != %d", x, y, xfeatures.size(), yfeatures.size()));
                }

                for (int i = 0; i < xfeatures.size(); i++) {
                    Feature a = xfeatures.get(i);
                    StructField xfield = a.field();
                    Feature b = yfeatures.get(i);
                    StructField yfield = b.field();

                    features.add(new Feature() {
                        final StructField field = new StructField(String.format("%s(%s, %s)", name, xfield.name, yfield.name),
                                DataTypes.object(clazz),
                                null);

                        @Override
                        public StructField field() {
                            return field;
                        }

                        @Override
                        public Object apply(Tuple o) {
                            Object x = a.apply(o);
                            Object y = b.apply(o);
                            if (x == null || y == null) return null;
                            else return f.apply((T) a.apply(o), (U) b.apply(o));
                        }
                    });
                }

                return features;
            }
        };
    }
}
