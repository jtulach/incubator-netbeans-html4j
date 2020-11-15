/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.java.html.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.html.json.spi.Technology;

/** Can be used in classes annotated with {@link Model} annotation to
 * define a derived property. Value of derived property is based on values
 * of regular {@link Property properties} as specified by {@link Model#properties()}.
 * The name of the computed/derived property is the name of the annotated method.
 * <p>
 * <b>Classic Example</b>
 * <p>
 * Imagine one wants to represent a formula with two variables and certain
 * computations performed on top of them. One can represent such computations
 * with {@code @ComputedProperty}:
 *
 * {@codesnippet net.java.html.json.SquaresTest}
 *
 * There are two variables {@code a} and {@code b} with appropriate getters and
 * setters. In addition there are derived properties {@code plus}, {@code minus}
 * and {@code aPlusBTimesAMinusB} which is based on the previous two derived
 * properties. The usage then follows classical bean patterns:
 * <p>
 * 
 * {@codesnippet net.java.html.json.SquaresTest#threeAndTwo}
 * {@codesnippet net.java.html.json.SquaresTest#fiveAndTwo}
 *
 * <p>
 * <b>Shorthand Syntax</b>
 * <p>
 * Sometimes using the getters to compute a property may lead to <em>unnecessary
 * verbosity</em>. As such there is a shorthand syntax for defining the derived
 * properties. Rather than writing:
 * <p>
 * {@codesnippet net.java.html.json.SquaresTest#aSquareMinusBSquareClassic}
 * <p>
 * one can choose <em>decomposition - pattern matching</em> and name the
 * properties one wants to access in the signature of the method:
 * <p>
 * {@codesnippet net.java.html.json.SquaresTest#aSquareMinusBSquare}
 * <p>
 * The arguments
 * of the method must match names and types of some of the properties 
 * from {@link Model#properties()} list. 
 * <p>
 * As soon as one of the properties the derived property method is accessing
 * changes, the method is called again to recompute its new value and the
 * change is notified to the underlying {@linkplain Technology (rendering) technology}.
 * <p>
 * Method's return type defines the type of the derived property. It may be
 * any primitive type, {@link String}, {@link Enum enum type} or a 
 * type generated by {@link Model @Model} annotation. One may 
 * also return a list of such (boxed) type
 * (for example {@link java.util.List List}&lt;{@link String}&gt; or {@link java.util.List List}&lt;{@link Integer}&gt;).
 * <p>
 * An example testing <a target="_blank" href="http://dew.apidesign.org/dew/#7545568">
 * whether a number is a prime</a> using a {@link ComputedProperty} is available
 * on-line.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ComputedProperty {
    /** Defines mutable computed property.
     * The value of this attribute references another method in the same class
     * by its name. The method is then called to handle changes to this computed property
     * made by its generated setter.
     * <p>
     * By default the computed properties are read-only (e.g. they only have a getter),
     * however one can make them mutable by defining a static method that takes
     * two parameters:
     * <ol>
     * <li>model class - provides access to <em>"this"</em> and
     *    allows you to call appropriate setters in response of changing
     *    the computed property value
     * </li>
     * <li>value - either exactly of the same type as is the return type of the annotated method
     *    or a superclass (like {@link Object}) of that type
     * </li>
     * </ol>
     * <p>
     * <b>Power of a Value Example</b>
     * <p>
     * Imagine you want to compute power of a number. You can define simple
     * model class and a computed property {@code pow} inside it:
     * <p>
     * {@codesnippet net.java.html.json.PowerTest}
     * <p>
     * Such code allows you to find out that the power of three is nine:
     * <p>
     * {@codesnippet net.java.html.json.PowerTest#computesPower}
     * <p>
     * However you can change the above example to also compute a square of the
     * value by making the {@code power} property writable:
     * <p>
     * {@codesnippet net.java.html.json.PowerTest#sqrt}
     * <p>
     * Then it is possible to use the same model to find out that square of four
     * is two:
     * <p>
     * {@codesnippet net.java.html.json.PowerTest#canSetComputedProperty}
     * <p>
     * <em>Implementation note</em>:
     * There cannot be two properties of the same name and different
     * behavior and as such the example is using two properties {@code pow}
     * and {@code power}. One of them is read-only and the second is writable.
     * The body of both methods is the same however.
     * 
     * @return the name of a method to handle changes to the computed
     *   property
     * @since 1.2
     */
    public String write() default "";
}