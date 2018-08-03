/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.util.Objects;

// Android更改：删除了一些不适用的字节码规范的引用，并添加了关于注释排序的注释。

/**
 * 表示当前在此VM中运行的程序的带注解的元素
 * 该接口允许以反射方式读取注解
 * 此接口中的方法返回的所有注解都是不可变的和可序列化的
 * 通过此接口的方法返回的数组可以由调用者修改，而不会影响返回给其他调用者的数组
 *
 * <p>Android note: 返回多个不同类型注释的方法，如{@link #getAnnotations（）}和{@link #getDeclaredAnnotations（）}
 * 可能会受到DEX格式指定的注解类型的显式字符代码排序的影响
 * 不保证按单个元素在源中声明的顺序返回单个元素上的不同类型的注释
 * <p>
 * {@link #getAnnotationsByType（Class）}和{@link #getDeclaredAnnotationsByType（Class）}方法支持元素上相同类型的多个注解
 * 如果任一方法的参数是可重复的注释类型（JLS 9.6），那么该方法将“查看”容器注释（JLS 9.7），如果存在，并返回容器内的任何注释
 * 可以在编译时生成容器注解以包装参数类型的多个注解
 *
 * <p>Finally, attempting to read a member whose definition has evolved
 * incompatibly will result in a {@link
 * java.lang.annotation.AnnotationTypeMismatchException} or an
 * {@link java.lang.annotation.IncompleteAnnotationException}.
 *
 * @author Josh Bloch
 * @see java.lang.EnumConstantNotPresentException
 * @see java.lang.TypeNotPresentException
 * @see java.lang.annotation.AnnotationTypeMismatchException
 * @see java.lang.annotation.IncompleteAnnotationException
 * @since 1.5
 */
public interface AnnotatedElement {
    /**
     * 如果指定类型的注解在此元素上<em> present </ em>，则返回true，否则返回false
     * 此方法主要用于方便地访问标记注解
     *
     * <p>该方法返回true等同于:@code getAnnotation(annotationClass) != null}
     *
     * @param annotationClass 与注释类型对应的Class对象
     * @throws NullPointerException 如果给定的注解Class为null
     * @since 1.5
     */
    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    /**
     * 指定类型元素的注解存在则返回，不存在返回null
     *
     * @throws NullPointerException if the given annotation class is null
     * @since 1.5
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * 返回该元素上所有注解，如果存在
     * 如果不存在注解，返回一个长度为0 的数组
     * 此方法的调用者可以自由修改返回的数组; 它对返回给其他调用者的数组没有影响。
     *
     * @return annotations present on this element
     * @since 1.5
     */
    Annotation[] getAnnotations();

    /**
     * 返回该元素关联的注解
     * 如果该元素没有关联的注解，则返回长度为0的数组
     * 此方法的调用者可以自由修改返回的数组; 它对返回给其他调用者的数组没有影响。
     *
     */
    default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        //使用{@code Method}, {@code Field}, {@code Package}，该方法不会处理继承的注解
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }


    //******************************** 忽略继承的注解 ***************************************


    /**
     * 如果注解存在，返回该元素的指定类型的注解，不存在返回null
     * <p>
     * 该方法忽略继承的注解
     * @since 1.8
     */
    default <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        // 遍历所有直接存在的注释，寻找匹配的注释
        for (Annotation annotation : getDeclaredAnnotations()) {
            if (annotationClass.equals(annotation.annotationType())) {
                // 更强大，可以在运行时进行动态转换，而不仅仅是编译时
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }

    /**
     * 如果注解直接或者间接存在，返回该元素的指定类型的注解
     * 该方法忽略继承的注解
     * 如果没有给定类型的注解，返回长度为0的数组
     */
    default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }

    /**
     * 返回该元素的注解，若没有返回长度为0的数组
     * 该方法忽略继承的注解
     */
    Annotation[] getDeclaredAnnotations();
}
