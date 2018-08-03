package com.me94me.example_reflection_reource.reflection;

import java.lang.annotation.Annotation;
import java.util.Objects;


/**
 * {@code Field}提供有关类或接口的单个字段的信息和动态访问
 * 反射字段可以是类（静态）字段或实例字段
 *
 * <p>{@code Field}允许在get或set访问操作期间进行扩展转换，但如果发生缩小转换，则会抛出{@code IllegalArgumentException}。
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getFields()
 * @see java.lang.Class#getField(String)
 * @see java.lang.Class#getDeclaredFields()
 * @see java.lang.Class#getDeclaredField(String)
 *
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public final class Field extends AccessibleObject implements Member {

    private int accessFlags;
    private java.lang.Class<?> declaringClass;
    private int dexFieldIndex;
    private int offset;
    private java.lang.Class<?> type;

    private Field() {
    }

    /**
     * 返回表示声明由此{@code Field}对象表示的字段的类或接口的{@code Class}对象。
     */
    public java.lang.Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * 返回此{@code Field}对象表示的字段的名称。
     */
    public String getName() {
        if (dexFieldIndex == -1) {
            // Proxy classes have 1 synthesized static field with no valid dex index.
            if (!declaringClass.isProxy()) {
                throw new AssertionError();
            }
            return "throws";
        }

        return getNameInternal();
    }

    @FastNative
    private native String getNameInternal();

    /**
     * 以整数形式返回此{@code Field}对象表示的字段的Java语言修饰符
     * {@code Modifier}类应该用于解码修饰符。
     *
     * @see Modifier
     */
    public int getModifiers() {
        return accessFlags & 0xffff;  // mask out bits not used by Java
    }

    /**
     * 如果此字段表示枚举类型的元素，则返回{@code true}; 否则返回{@code false}。
     *
     * @since 1.5
     */
    public boolean isEnumConstant() {
        return (getModifiers() & Modifier.ENUM) != 0;
    }

    /**
     * 如果此字段是合成字段，则返回{@code true}; 否则返回{@code false}。
     *
     * @since 1.5
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * 返回一个{@code Class}对象，该对象标识由此{@code Field}对象表示的字段的声明类型。
     *
     */
    public java.lang.Class<?> getType() {
        return type;
    }

    /**
     * 返回{@code Type}对象，该对象表示此{@code Field}对象表示的字段的声明类型。
     *
     * 如果{@code Type}是参数化类型，则返回的{@code Type}对象必须准确反映源代码中使用的实际类型参数。
     *
     * 如果基础字段的类型是类型变量或参数化类型，则创建它。 否则，它就解决了。
     *
     * @return a {@code Type} object that represents the declared type for
     *     the field represented by this {@code Field} object
     * @throws GenericSignatureFormatError if the generic field
     *     signature does not conform to the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if the generic type
     *     signature of the underlying field refers to a non-existent
     *     type declaration
     * @throws MalformedParameterizedTypeException if the generic
     *     signature of the underlying field refers to a parameterized type
     *     that cannot be instantiated for any reason
     * @since 1.5
     */
    public Type getGenericType() {
        String signatureAttribute = getSignatureAttribute();
        ClassLoader cl = declaringClass.getClassLoader();
        GenericSignatureParser parser = new GenericSignatureParser(cl);
        parser.parseForField(declaringClass, signatureAttribute);
        Type genericType = parser.fieldType;
        if (genericType == null) {
            genericType = getType();
        }
        return genericType;
    }

    private String getSignatureAttribute() {
        String[] annotation = getSignatureAnnotation();
        if (annotation == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String s : annotation) {
            result.append(s);
        }
        return result.toString();
    }
    @FastNative
    private native String[] getSignatureAnnotation();


    /**
     * Compares this {@code Field} against the specified object.  Returns
     * true if the objects are the same.  Two {@code Field} objects are the same if
     * they were declared by the same class and have the same name
     * and type.
     */
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Field) {
            Field other = (Field)obj;
            return (getDeclaringClass() == other.getDeclaringClass())
                && (getName() == other.getName())
                && (getType() == other.getType());
        }
        return false;
    }

    /**
     * Returns a hashcode for this {@code Field}.  This is computed as the
     * exclusive-or of the hashcodes for the underlying field's
     * declaring class name and its name.
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    /**
     * Returns a string describing this {@code Field}.  The format is
     * the access modifiers for the field, if any, followed
     * by the field type, followed by a space, followed by
     * the fully-qualified name of the class declaring the field,
     * followed by a period, followed by the name of the field.
     * For example:
     * <pre>
     *    public static final int java.lang.Thread.MIN_PRIORITY
     *    private int java.io.FileDescriptor.fd
     * </pre>
     *
     * <p>The modifiers are placed in canonical order as specified by
     * "The Java Language Specification".  This is {@code public},
     * {@code protected} or {@code private} first, and then other
     * modifiers in the following order: {@code static}, {@code final},
     * {@code transient}, {@code volatile}.
     *
     * @return a string describing this {@code Field}
     * @jls 8.3.1 Field Modifiers
     */
    public String toString() {
        int mod = getModifiers();
        return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
            + getType().getTypeName() + " "
            + getDeclaringClass().getTypeName() + "."
            + getName());
    }

    /**
     * Returns a string describing this {@code Field}, including
     * its generic type.  The format is the access modifiers for the
     * field, if any, followed by the generic field type, followed by
     * a space, followed by the fully-qualified name of the class
     * declaring the field, followed by a period, followed by the name
     * of the field.
     *
     * <p>The modifiers are placed in canonical order as specified by
     * "The Java Language Specification".  This is {@code public},
     * {@code protected} or {@code private} first, and then other
     * modifiers in the following order: {@code static}, {@code final},
     * {@code transient}, {@code volatile}.
     *
     * @return a string describing this {@code Field}, including
     * its generic type
     *
     * @since 1.5
     * @jls 8.3.1 Field Modifiers
     */
    public String toGenericString() {
        int mod = getModifiers();
        Type fieldType = getGenericType();
        return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
            + fieldType.getTypeName() + " "
            + getDeclaringClass().getTypeName() + "."
            + getName());
    }

    /**
     * Returns the value of the field represented by this {@code Field}, on
     * the specified object. The value is automatically wrapped in an
     * object if it has a primitive type.
     *
     * <p>The underlying field's value is obtained as follows:
     *
     * <p>If the underlying field is a static field, the {@code obj} argument
     * is ignored; it may be null.
     *
     * <p>Otherwise, the underlying field is an instance field.  If the
     * specified {@code obj} argument is null, the method throws a
     * {@code NullPointerException}. If the specified object is not an
     * instance of the class or interface declaring the underlying
     * field, the method throws an {@code IllegalArgumentException}.
     *
     * <p>If this {@code Field} object is enforcing Java language access control, and
     * the underlying field is inaccessible, the method throws an
     * {@code IllegalAccessException}.
     * If the underlying field is static, the class that declared the
     * field is initialized if it has not already been initialized.
     *
     * <p>Otherwise, the value is retrieved from the underlying instance
     * or static field.  If the field has a primitive type, the value
     * is wrapped in an object before being returned, otherwise it is
     * returned as is.
     *
     * <p>If the field is hidden in the type of {@code obj},
     * the field's value is obtained according to the preceding rules.
     *
     * @param obj object from which the represented field's value is
     * to be extracted
     * @return the value of the represented field in object
     * {@code obj}; primitive values are wrapped in an appropriate
     * object before being returned
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof).
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     */
    @FastNative
    public native Object get(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance {@code boolean} field.
     *
     * @param obj the object to extract the {@code boolean} value
     * from
     * @return the value of the {@code boolean} field
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code boolean} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @FastNative
    public native boolean getBoolean(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance {@code byte} field.
     *
     * @param obj the object to extract the {@code byte} value
     * from
     * @return the value of the {@code byte} field
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code byte} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @FastNative
    public native byte getByte(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance field of type
     * {@code char} or of another primitive type convertible to
     * type {@code char} via a widening conversion.
     *
     * @param obj the object to extract the {@code char} value
     * from
     * @return the value of the field converted to type {@code char}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code char} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see Field#get
     */
    @FastNative
    public native char getChar(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance field of type
     * {@code short} or of another primitive type convertible to
     * type {@code short} via a widening conversion.
     *
     * @param obj the object to extract the {@code short} value
     * from
     * @return the value of the field converted to type {@code short}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code short} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @FastNative
    public native short getShort(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance field of type
     * {@code int} or of another primitive type convertible to
     * type {@code int} via a widening conversion.
     *
     * @param obj the object to extract the {@code int} value
     * from
     * @return the value of the field converted to type {@code int}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code int} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @FastNative
    public native int getInt(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance field of type
     * {@code long} or of another primitive type convertible to
     * type {@code long} via a widening conversion.
     *
     * @param obj the object to extract the {@code long} value
     * from
     * @return the value of the field converted to type {@code long}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code long} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @FastNative
    public native long getLong(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance field of type
     * {@code float} or of another primitive type convertible to
     * type {@code float} via a widening conversion.
     *
     * @param obj the object to extract the {@code float} value
     * from
     * @return the value of the field converted to type {@code float}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code float} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see Field#get
     */
    @FastNative
    public native float getFloat(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Gets the value of a static or instance field of type
     * {@code double} or of another primitive type convertible to
     * type {@code double} via a widening conversion.
     *
     * @param obj the object to extract the {@code double} value
     * from
     * @return the value of the field converted to type {@code double}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code double} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @FastNative
    public native double getDouble(Object obj)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the field represented by this {@code Field} object on the
     * specified object argument to the specified new value. The new
     * value is automatically unwrapped if the underlying field has a
     * primitive type.
     *
     * <p>The operation proceeds as follows:
     *
     * <p>If the underlying field is static, the {@code obj} argument is
     * ignored; it may be null.
     *
     * <p>Otherwise the underlying field is an instance field.  If the
     * specified object argument is null, the method throws a
     * {@code NullPointerException}.  If the specified object argument is not
     * an instance of the class or interface declaring the underlying
     * field, the method throws an {@code IllegalArgumentException}.
     *
     * <p>If this {@code Field} object is enforcing Java language access control, and
     * the underlying field is inaccessible, the method throws an
     * {@code IllegalAccessException}.
     *
     * <p>If the underlying field is final, the method throws an
     * {@code IllegalAccessException} unless {@code setAccessible(true)}
     * has succeeded for this {@code Field} object
     * and the field is non-static. Setting a final field in this way
     * is meaningful only during deserialization or reconstruction of
     * instances of classes with blank final fields, before they are
     * made available for access by other parts of a program. Use in
     * any other context may have unpredictable effects, including cases
     * in which other parts of a program continue to use the original
     * value of this field.
     *
     * <p>If the underlying field is of a primitive type, an unwrapping
     * conversion is attempted to convert the new value to a value of
     * a primitive type.  If this attempt fails, the method throws an
     * {@code IllegalArgumentException}.
     *
     * <p>If, after possible unwrapping, the new value cannot be
     * converted to the type of the underlying field by an identity or
     * widening conversion, the method throws an
     * {@code IllegalArgumentException}.
     *
     * <p>If the underlying field is static, the class that declared the
     * field is initialized if it has not already been initialized.
     *
     * <p>The field is set to the possibly unwrapped and widened new value.
     *
     * <p>If the field is hidden in the type of {@code obj},
     * the field's value is set according to the preceding rules.
     *
     * @param obj the object whose field should be modified
     * @param value the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     */
    @FastNative
    public native void set(Object obj, Object value)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code boolean} on the specified object.
     * This method is equivalent to
     * {@code set(obj, zObj)},
     * where {@code zObj} is a {@code Boolean} object and
     * {@code zObj.booleanValue() == z}.
     *
     * @param obj the object whose field should be modified
     * @param z   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setBoolean(Object obj, boolean z)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code byte} on the specified object.
     * This method is equivalent to
     * {@code set(obj, bObj)},
     * where {@code bObj} is a {@code Byte} object and
     * {@code bObj.byteValue() == b}.
     *
     * @param obj the object whose field should be modified
     * @param b   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setByte(Object obj, byte b)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code char} on the specified object.
     * This method is equivalent to
     * {@code set(obj, cObj)},
     * where {@code cObj} is a {@code Character} object and
     * {@code cObj.charValue() == c}.
     *
     * @param obj the object whose field should be modified
     * @param c   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setChar(Object obj, char c)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code short} on the specified object.
     * This method is equivalent to
     * {@code set(obj, sObj)},
     * where {@code sObj} is a {@code Short} object and
     * {@code sObj.shortValue() == s}.
     *
     * @param obj the object whose field should be modified
     * @param s   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setShort(Object obj, short s)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as an {@code int} on the specified object.
     * This method is equivalent to
     * {@code set(obj, iObj)},
     * where {@code iObj} is a {@code Integer} object and
     * {@code iObj.intValue() == i}.
     *
     * @param obj the object whose field should be modified
     * @param i   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setInt(Object obj, int i)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code long} on the specified object.
     * This method is equivalent to
     * {@code set(obj, lObj)},
     * where {@code lObj} is a {@code Long} object and
     * {@code lObj.longValue() == l}.
     *
     * @param obj the object whose field should be modified
     * @param l   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setLong(Object obj, long l)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code float} on the specified object.
     * This method is equivalent to
     * {@code set(obj, fObj)},
     * where {@code fObj} is a {@code Float} object and
     * {@code fObj.floatValue() == f}.
     *
     * @param obj the object whose field should be modified
     * @param f   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setFloat(Object obj, float f)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of a field as a {@code double} on the specified object.
     * This method is equivalent to
     * {@code set(obj, dObj)},
     * where {@code dObj} is a {@code Double} object and
     * {@code dObj.doubleValue() == d}.
     *
     * @param obj the object whose field should be modified
     * @param d   the new value for the field of {@code obj}
     * being modified
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is either inaccessible or final.
     * @exception IllegalArgumentException  if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#set
     */
    @FastNative
    public native void setDouble(Object obj, double d)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public <T extends Annotation> T getAnnotation(java.lang.Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return getAnnotationNative(annotationClass);
    }
    @FastNative
    private native <A extends Annotation> A getAnnotationNative(java.lang.Class<A> annotationType);

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(java.lang.Class<T> annotationClass) {
        // Android-changed: Uses AnnotatedElements instead.
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }


    @Override public boolean isAnnotationPresent(java.lang.Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new NullPointerException("annotationType == null");
        }
        return isAnnotationPresentNative(annotationType);
    }
    @FastNative
    private native boolean isAnnotationPresentNative(java.lang.Class<? extends Annotation> annotationType);

    /**
     * {@inheritDoc}
     */
    @Override
    @FastNative
    public native Annotation[] getDeclaredAnnotations();

    /**
     * Returns the index of this field's ID in its dex file.
     *
     * @hide
     */
    public int getDexFieldIndex() {
        return dexFieldIndex;
    }

    /**
     * Returns the offset of the field within an instance, or for static fields, the class.
     *
     * @hide
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @hide - export for use by {@code java.lang.invoke.*}
     */
    @FastNative
    public native long getArtField();
}
