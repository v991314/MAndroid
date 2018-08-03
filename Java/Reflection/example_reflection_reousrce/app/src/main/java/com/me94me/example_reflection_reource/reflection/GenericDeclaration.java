/*
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

/**
 * 声明类型变量的所有实体的通用接口。
 *
 * @since 1.5
 */
public interface GenericDeclaration extends AnnotatedElement {
    /**
     * 返回{@code TypeVariable}对象的数组，这些对象表示由此{@code GenericDeclaration}对象表示的泛型声明声明的类型变量，按声明顺序
     * 如果基础泛型声明未声明类型变量，则返回长度为0的数组
     *
     * @return 一个{@code TypeVariable}对象数组，表示此泛型声明声明的类型变量
     * @throws GenericSignatureFormatError if the generic
     *     signature of this generic declaration does not conform to
     *     the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     */
    public TypeVariable<?>[] getTypeParameters();
}
