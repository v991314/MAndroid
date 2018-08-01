# Reflection之Class类

能够分析类能力的程序被成为反射

* 在运行中分析类的能力
* 在运行中查看对象，例如，编写一个toString方法供所有类使用
* 实现数组的操作代码
* 利用Method对象，这个对象很像C++中的函数指针

## Class类

在程序运行期间，Java运行时系统始终为所有的对象维护一个被成为运行时的类型标识。这个信息保存着每个对象所属的类足迹。虚拟机利用运行时信息选择相应的方法执行。

然而，可以通过专门的Java类访问这些信息。保存这些信息的类被称为Class。

如果类在一个包里，包的名字也作为类名的一部分

```Java
Date d = new Date();
Class c1 = d.getClass();
String name = c1.getName();//name is set to "java.util.Date"
```

还可以调用静态方法forName获得类名对应的Class对象

```Java
String className = "java.util.Date";
Class c1 = Class.forName(className);
```

如果类名保存在字符串中，并可在运行中改变，就可以使用这个方法。

这个方法只有在className是类名或接口名时才能够执行。否则，forName方法将抛出一个checkedexception。无论何时使用这个方法，都应该提供一个异常处理器（exception handle）。

> 提示：在启动时，包含main方法的类被加载。它会家在所有需要的类。这些被加载的类又要加载它们需要的类，以此类推。对于一个大型的应用程序来说，这将会消耗很多时间，用户会因此感到不耐烦。可以使用下面的这个技巧给用户一种启动速度比较快的幻觉。不过要确保main方法的类没有显式地引用其他的类。首先显式一个启动画面；然后，通过调用Class.forName手动地加载其他的类。

然后获得Class类对象的第三种方法非常简单。如果T是任意的Java类型，T.class将代表匹配的类对象。例如

```Java
Class cl1 = Date.class;
Class cl2 = int.class;
```

请注意，一个Class对象实际上表示的是一个类型，而这个类型未必一定是一种类。例如，int不是类，但int.class是一个Class类型的对象。

虚拟机为每个类型管理一个Class对象。因此可以立即 == 运算符实现两个类对象比较的操作。例如

```Java
if(e.Class() == Employee.class){}
```

还有一个很有用的方法newInstance().可以用来快速地创建一个类的实例。例如，

	e.getClass().newInstance();

创建了一个与e具有相同类型的实例。newInstance方法调用默认的构造器（没有参数的构造器）初始化新创建的对象。如果这个类没有默认的构造器，就抛出一个异常。

将forName与newInstance配合起来使用，可以根据存储在字符串中的类名创建一个对象。

```Java
String s = "java.util.Date";
Object m = Class.forName(s).newInstance();
```

> 注释：如果需要以这种方式向希望按名称创建的类的构造器提供参数，就不要使用上面那条语句，而必须使用Constructor类中的newInstance方法

