# Reflection之方法指针

从表面上看，Java没有提供方法指针，即将一个方法的存储地址传给另外一个方法，以便第二个方法能够随后调用它。

事实上，Java的设计者曾说过：方法指针是很危险的，并且常常会带来隐患。他们认为Java提供的额接口（inteface）是一种更好的解决方法。然而，在Java1.1中方法指针已经作为反射包的副产品出现了。

为了能够看到方法指针的工作过程，先回忆一下利用Field类的get方法查看对象域的过程。与之类似，在Method类中有一个invoke方法，它允许调用包装在当前Method对象中的方法。

invoke方法的签名是：

```Java
Object invoke（Object obj,Object... args）
```

第一个参数是隐式参数，其余的对象提供了显式参数（在Java SE 5.0以前的版本中，必须传递一个对象数组，如果没有显式参数就传递一个null）

对于静态方法，第一个参数可以被忽略，即可以将它设置为null。

例如，假设ml代表Employee类的getName方法，下面这条语句显示了如何调用这个方法：

```Java
String n = (String)ml.invoke(harry);
```

如果参数或范湖类型不是类而是基本类型，那么在调用Field类的get和set方法时会存在一些问题。需要依靠自动打包功能将其打包。相反地，如果返回类型是一种基本类型，则invoke方法将返回包装器类型

```Java
double s = (Double)m2.invoke(harry);
```

如何得到Method对象呢？当然，可以通过调用getDeclareMethods方法，然后对返回的Method对象数组进行查找，直到发现想要的方法为止。也可以通过调用Class类中的getMethod方法得到想要的方法。它与getField方法类似。然而可能存在若干个相同名字的方法，因此要格外地小心，以确保能够准确地得到想要的那个方法。有鉴于此，还必须提供想要的方法的参数类型。

getMethod的签名是：

```Java
Method getMethod（String name,Class... parameterTypes）
```


```Java
Method m1 = Employee.class.getMethod("getName");
Method m2 = Employee.class.getMethod("raiseSalary",double.class);
```

可以使用method对象实现C语言中函数指针的所有操作。同C一样，这种程序设计风格并不太简便，出错的可能也比较大。如果在调用方法的时候提供一个错误的参数，那么invoke方法将会抛出一个异常。

另外，invoke的参数和返回值必须是Object类型的。这就以为着必须进行多次的类型转换。这样做将会使编译器错过检查代码的机会。不仅如此，使用反射获得方法指针的代码要比仅仅直接调用方法明显慢一些。

有鉴于此，建议仅在必要的时候才使用Method对象，而最好使用接口和内部类。特别重申：建议Java开发者不要使用Method对象的回调功能。使用接口进行回调会使得代码的执行速度更快，更易于维护。