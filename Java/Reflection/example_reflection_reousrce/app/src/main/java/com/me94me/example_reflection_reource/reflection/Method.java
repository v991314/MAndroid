/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.me94me.example_reflection_reource.reflection;

import dalvik.annotation.optimization.FastNative;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import libcore.reflect.Types;
import libcore.util.EmptyArray;

/**
 * {@code Method}提供有关类或接口上单个方法的信息和访问权限。
 * 反射的方法可以是类方法或实例方法（包括抽象方法）。
 *
 * {@code 方法}允许在将实际参数与基础方法的形式参数进行匹配时进行扩展转换，但如果发生缩小转换，则会抛出{@code IllegalArgumentException}。
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getMethod(String, Class[])
 * @see java.lang.Class#getDeclaredMethods()
 * @see java.lang.Class#getDeclaredMethod(String, Class[])
 *
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public final class Method extends Executable  {
    /**
     * Orders methods by their name, parameters and return type.
     *
     * @hide
     */
    public static final Comparator<Method> ORDER_BY_SIGNATURE = new Comparator<Method>() {
        @Override public int compare(Method a, Method b) {
            if (a == b) {
                return 0;
            }
            int comparison = a.getName().compareTo(b.getName());
            if (comparison == 0) {
                comparison = a.compareMethodParametersInternal(b);
                if (comparison == 0) {
                    // This is necessary for methods that have covariant return types.
                    Class<?> aReturnType = a.getReturnType();
                    Class<?> bReturnType = b.getReturnType();
                    if (aReturnType == bReturnType) {
                        comparison = 0;
                    } else {
                        comparison = aReturnType.getName().compareTo(bReturnType.getName());
                    }
                }
            }
            return comparison;
        }
    };

    private Method() {
    }

    @Override
    boolean hasGenericInformation() {
        // Android-changed: Signature retrieval is handled in Executable.
        return super.hasGenericInformationInternal();
    }

    /**
     * 获取该方法声明的类
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass() {
        // Android-changed: This is handled by Executable.
        return super.getDeclaringClassInternal();
    }

    /**
     * Returns the name of the method represented by this {@code Method}
     * object, as a {@code String}.
     */
    @Override
    public String getName() {
        // Android-changed: This is handled by Executable.
        return getMethodNameInternal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getModifiers() {
        // Android-changed: This is handled by Executable.
        return super.getModifiersInternal();
    }

    /**
     * {@inheritDoc}
     * @throws GenericSignatureFormatError {@inheritDoc}
     * @since 1.5
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public TypeVariable<Method>[] getTypeParameters() {
        // Android-changed: This is mostly handled by Executable.
        GenericInfo info = getMethodOrConstructorGenericInfoInternal();
        return (TypeVariable<Method>[]) info.formalTypeParameters.clone();
    }

    /**
     * Returns a {@code Class} object that represents the formal return type
     * of the method represented by this {@code Method} object.
     *
     * @return the return type for the method this object represents
     */
    public Class<?> getReturnType() {
        return getMethodReturnTypeInternal();
    }

    /**
     * Returns a {@code Type} object that represents the formal return
     * type of the method represented by this {@code Method} object.
     *
     * <p>If the return type is a parameterized type,
     * the {@code Type} object returned must accurately reflect
     * the actual type parameters used in the source code.
     *
     * <p>If the return type is a type variable or a parameterized type, it
     * is created. Otherwise, it is resolved.
     *
     * @return  a {@code Type} object that represents the formal return
     *     type of the underlying  method
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if the underlying method's
     *     return type refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if the
     *     underlying method's return typed refers to a parameterized
     *     type that cannot be instantiated for any reason
     * @since 1.5
     */
    public Type getGenericReturnType() {
        // Android-changed: Modified implementation to use Executable.
      return Types.getType(getMethodOrConstructorGenericInfoInternal().genericReturnType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getParameterTypes() {
        // Android-changed: This is handled by Executable.
        Class<?>[] paramTypes = super.getParameterTypesInternal();
        if (paramTypes == null) {
            return EmptyArray.CLASS;
        }

        return paramTypes;
    }

    /**
     * {@inheritDoc}
     * @since 1.8
     */
    public int getParameterCount() {
        // Android-changed: This is handled by Executable.
        return super.getParameterCountInternal();
    }

    /**
     * {@inheritDoc}
     * @throws GenericSignatureFormatError {@inheritDoc}
     * @throws TypeNotPresentException {@inheritDoc}
     * @throws MalformedParameterizedTypeException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public Type[] getGenericParameterTypes() {
        return super.getGenericParameterTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @FastNative
    public native Class<?>[] getExceptionTypes();

    /**
     * {@inheritDoc}
     * @throws GenericSignatureFormatError {@inheritDoc}
     * @throws TypeNotPresentException {@inheritDoc}
     * @throws MalformedParameterizedTypeException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public Type[] getGenericExceptionTypes() {
        return super.getGenericExceptionTypes();
    }

    /**
     * Compares this {@code Method} against the specified object.  Returns
     * true if the objects are the same.  Two {@code Methods} are the same if
     * they were declared by the same class and have the same name
     * and formal parameter types and return type.
     */
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Method) {
            Method other = (Method)obj;
            if ((getDeclaringClass() == other.getDeclaringClass())
                && (getName() == other.getName())) {
                if (!getReturnType().equals(other.getReturnType()))
                    return false;
                // Android-changed: Use getParameterTypes.
                return equalParamTypes(getParameterTypes(), other.getParameterTypes());
            }
        }
        return false;
    }

    /**
     * Returns a hashcode for this {@code Method}.  The hashcode is computed
     * as the exclusive-or of the hashcodes for the underlying
     * method's declaring class name and the method's name.
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    /**
     * Returns a string describing this {@code Method}.  The string is
     * formatted as the method access modifiers, if any, followed by
     * the method return type, followed by a space, followed by the
     * class declaring the method, followed by a period, followed by
     * the method name, followed by a parenthesized, comma-separated
     * list of the method's formal parameter types. If the method
     * throws checked exceptions, the parameter list is followed by a
     * space, followed by the word throws followed by a
     * comma-separated list of the thrown exception types.
     * For example:
     * <pre>
     *    public boolean java.lang.Object.equals(java.lang.Object)
     * </pre>
     *
     * <p>The access modifiers are placed in canonical order as
     * specified by "The Java Language Specification".  This is
     * {@code public}, {@code protected} or {@code private} first,
     * and then other modifiers in the following order:
     * {@code abstract}, {@code default}, {@code static}, {@code final},
     * {@code synchronized}, {@code native}, {@code strictfp}.
     *
     * @return a string describing this {@code Method}
     *
     * @jls 8.4.3 Method Modifiers
     */
    public String toString() {
        // Android-changed: Use getParameterTypes.
        return sharedToString(Modifier.methodModifiers(),
                              isDefault(),
                              getParameterTypes(),
                              getExceptionTypes());
    }

    @Override
    void specificToStringHeader(StringBuilder sb) {
        sb.append(getReturnType().getTypeName()).append(' ');
        sb.append(getDeclaringClass().getTypeName()).append('.');
        sb.append(getName());
    }

    /**
     * Returns a string describing this {@code Method}, including
     * type parameters.  The string is formatted as the method access
     * modifiers, if any, followed by an angle-bracketed
     * comma-separated list of the method's type parameters, if any,
     * followed by the method's generic return type, followed by a
     * space, followed by the class declaring the method, followed by
     * a period, followed by the method name, followed by a
     * parenthesized, comma-separated list of the method's generic
     * formal parameter types.
     *
     * If this method was declared to take a variable number of
     * arguments, instead of denoting the last parameter as
     * "<tt><i>Type</i>[]</tt>", it is denoted as
     * "<tt><i>Type</i>...</tt>".
     *
     * A space is used to separate access modifiers from one another
     * and from the type parameters or return type.  If there are no
     * type parameters, the type parameter list is elided; if the type
     * parameter list is present, a space separates the list from the
     * class name.  If the method is declared to throw exceptions, the
     * parameter list is followed by a space, followed by the word
     * throws followed by a comma-separated list of the generic thrown
     * exception types.
     *
     * <p>The access modifiers are placed in canonical order as
     * specified by "The Java Language Specification".  This is
     * {@code public}, {@code protected} or {@code private} first,
     * and then other modifiers in the following order:
     * {@code abstract}, {@code default}, {@code static}, {@code final},
     * {@code synchronized}, {@code native}, {@code strictfp}.
     *
     * @return a string describing this {@code Method},
     * include type parameters
     *
     * @since 1.5
     *
     * @jls 8.4.3 Method Modifiers
     */
    @Override
    public String toGenericString() {
        return sharedToGenericString(Modifier.methodModifiers(), isDefault());
    }

    @Override
    void specificToGenericStringHeader(StringBuilder sb) {
        Type genRetType = getGenericReturnType();
        sb.append(genRetType.getTypeName()).append(' ');
        sb.append(getDeclaringClass().getTypeName()).append('.');
        sb.append(getName());
    }

    /**
     * 在具有指定参数的指定对象上调用此{@code Method}对象表示的基础方法。
     * 单独的参数自动解包以匹配原始形式参数，并且原始参数和引用参数都根据需要进行方法调用转换。
     *
     * 如果底层方法是静态的，则忽略指定的{@code obj}参数。 它可能为空。
     *
     * 如果基础方法所需的形式参数的数量为0，则提供的{@code args}数组的长度可以为0或null。
     *
     * 如果底层方法是静态的，则声明该方法的类如果尚未初始化则初始化。
     *
     * 如果方法正常完成，则返回的值返回给invoke的调用者;
     * 如果值具有基本类型，则首先将其适当地包装在对象中。
     * 但是，如果值具有基本类型数组的类型，则数组的元素不包装在对象中;
     * 换句话说，返回一个原始类型的数组。 如果底层方法返回类型为void，则调用返回null。
     *
     * @param obj  调用该方法的对象
     * @param args method调用所需要的参数
     * @return 调用该方法的返回值
     *
     * @exception IllegalAccessException    if this {@code Method} object
     *              is enforcing Java language access control and the underlying
     *              method is inaccessible.
     * @exception IllegalArgumentException  if the method is an
     *              instance method and the specified object argument
     *              is not an instance of the class or interface
     *              declaring the underlying method (or of a subclass
     *              or implementor thereof); if the number of actual
     *              and formal parameters differ; if an unwrapping
     *              conversion for primitive arguments fails; or if,
     *              after possible unwrapping, a parameter value
     *              cannot be converted to the corresponding formal
     *              parameter type by a method invocation conversion.
     * @exception InvocationTargetException if the underlying method
     *              throws an exception.
     * @exception NullPointerException      if the specified object is null
     *              and the method is an instance method.
     * @exception ExceptionInInitializerError if the initialization
     * provoked by this method fails.
     */
    @FastNative
    public native Object invoke(Object obj, Object... args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    /**
     * Returns {@code true} if this method is a bridge
     * method; returns {@code false} otherwise.
     *
     * @return true if and only if this method is a bridge
     * method as defined by the Java Language Specification.
     * @since 1.5
     */
    public boolean isBridge() {
        // Android-changed: This is handled by Executable.
        return super.isBridgeMethodInternal();
    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isVarArgs() {
        return super.isVarArgs();
    }

    /**
     * {@inheritDoc}
     * @jls 13.1 The Form of a Binary
     * @since 1.5
     */
    @Override
    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    /**
     * Returns {@code true} if this method is a default
     * method; returns {@code false} otherwise.
     *
     * A default method is a public non-abstract instance method, that
     * is, a non-static method with a body, declared in an interface
     * type.
     *
     * @return true if and only if this method is a default
     * method as defined by the Java Language Specification.
     * @since 1.8
     */
    public boolean isDefault() {
        // Android-changed: This is handled by Executable.
        return super.isDefaultMethodInternal();
    }

    /**
     * Returns the default value for the annotation member represented by
     * this {@code Method} instance.  If the member is of a primitive type,
     * an instance of the corresponding wrapper type is returned. Returns
     * null if no default is associated with the member, or if the method
     * instance does not represent a declared member of an annotation type.
     *
     * @return the default value for the annotation member represented
     *     by this {@code Method} instance.
     * @throws TypeNotPresentException if the annotation is of type
     *     {@link Class} and no definition can be found for the
     *     default class value.
     * @since  1.5
     */
    @FastNative
    public native Object getDefaultValue();

    /**
     * {@inheritDoc}
     * @throws NullPointerException  {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return super.getAnnotation(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return super.getDeclaredAnnotations();
    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    @Override
    public Annotation[][] getParameterAnnotations() {
        // Android-changed: This is handled by Executable.
        return super.getParameterAnnotationsInternal();
    }

    /**
     * Returns true if this and {@code method} have the same name and the same
     * parameters in the same order. Such methods can share implementation if
     * one method's return types is assignable to the other.
     *
     * @hide needed by Proxy
     */
    boolean equalNameAndParameters(Method m) {
        return equalNameAndParametersInternal(m);
    }
}
