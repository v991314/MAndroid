
package com.me94me.example_reflection_reource.reflection;

import java.lang.annotation.Annotation;

/**
 * AccessibleObject类是Field，Method和Constructor对象的基类
 * 它提供了将反射对象标记为在使用时禁止默认Java语言访问控制检查的功能。
 *
 * 当使用Fields，Methods或Constructors设置或获取字段，调用方法或创建和初始化类的新实例时
 * 将执行访问检查 - 对于公共，默认（包）访问，受保护和私有成员。
 *
 * 在反射对象中设置{@code accessible}标志允许具有足够权限的复杂应用程序
 * （例如Java对象序列化或其他持久性机制）以通常被禁止的方式操作对象。
 *
 * 默认情况下，反射对象不可访问。
 *
 * @see Field
 * @see Method
 * @see Constructor
 * @see ReflectPermission
 *
 * @since 1.2
 */
public class AccessibleObject implements AnnotatedElement {

    /**
     * 通过单个安全检查为效率设置对象数组的{@code accessible}标志的便捷方法（为了提高效率）。
     *
     * <p>首先，如果有安全管理器，则使用{@code ReflectPermission（“suppressAccessChecks”）}权限调用其{@code checkPermission}方法。
     */
    public static void setAccessible(AccessibleObject[] array, boolean flag)
        throws SecurityException {
        for (int i = 0; i < array.length; i++) {
            setAccessible0(array[i], flag);
        }
    }

    /**
     * 将此对象的{@code accessible}标志设置为指示的布尔值。
     * 值{@code true}表示反射对象在使用时应禁止Java语言访问检查。
     * 值{@code false}表示反射对象应强制执行Java语言访问检查。
     *
     * @throws SecurityException if the request is denied.
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public void setAccessible(boolean flag) throws SecurityException {
        setAccessible0(this, flag);
    }

    /* Check that you aren't exposing java.lang.Class.<init> or sensitive
       fields in java.lang.Class. */
    private static void setAccessible0(AccessibleObject obj, boolean flag)
        throws SecurityException
    {
        if (obj instanceof Constructor && flag == true) {
            Constructor<?> c = (Constructor<?>)obj;
            // Android-changed: Added additional checks below.
            Class<?> clazz = c.getDeclaringClass();
            if (c.getDeclaringClass() == Class.class) {
                throw new SecurityException("Can not make a java.lang.Class" +
                                            " constructor accessible");
            } else if (clazz == Method.class) {
                throw new SecurityException("Can not make a java.lang.reflect.Method" +
                                            " constructor accessible");
            } else if (clazz == Field.class) {
                throw new SecurityException("Can not make a java.lang.reflect.Field" +
                                            " constructor accessible");
            }
        }
        obj.override = flag;
    }


    /**
     * Get the value of the {@code accessible} flag for this object.
     *
     * @return the value of the object's {@code accessible} flag
     */
    public boolean isAccessible() {
        return override;
    }

    /**
     * Constructor: 仅被Java虚拟机只用
     */
    protected AccessibleObject() {}

    // Indicates whether language-level access checks are overridden
    // by this object. Initializes to "false". This field is used by
    // Field, Method, and Constructor.
    //
    // NOTE: for security purposes, this field must not be visible
    // outside this package.
    boolean override;

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return AnnotatedElement.super.isAnnotationPresent(annotationClass);
    }

   /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotation is the same as
        // getAnnotation.
        return getAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotationsByType is the same as
        // getAnnotationsByType.
        return getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        throw new AssertionError("All subclasses should override this method");
    }
}
