# Reflection之在运行时使用反射分析对象

在编写程序时，如果知道想要查看的域名和类型，查看指定的域是一件很容易的事情。而利用反射机制可以查看编译时还不清楚的对象域。

查看对象域的关键方法是Field类中的get方法。如果f是一个Field类型的对象（例如，通过getDeclaredFields得到的对象），obj是某个包含f域的对象，f.get（obj）将返回一个对象，其值为obj域的当前值。

```Java
Employee harry = new Employee("Harry Hacker",35000,10,1,1999)
Class cl = harry.getClass();
Field f = cl.getDeclaredField("name")
object v = f.get(harry);//the value of the name filed of the harry object
```

但，如果name是一个私有域，get方法会抛出一个illegalAccessException。

除非拥有方法访问权限，否则Java安全机制只允许查看任意对象有哪些域，而不允许读取它们的值。

反射机制的默认行为受限于Java的访问控制。然而，如果没有收到安全管理器的控制，就可以覆盖访问控制。为了达到这个目的，需要调用Field、Method或者Contructor对象的setAccessible方法。例如

```Java
f.setAccessible(true);//now OK to call f.get(harry)
```

get方法还有一个需要解决的问题。name域是一个String，因此把它作为Object返回没有问题。但要查看double类型，而Java中数值类型不是对象。要想解决这个问题可以使用Fileld类中的getDouble方法，也可以调用get方法，此时，反射机制将会自动地将这个域值打包到相应的对象包装器中，这里将打包成Double。

当然，可以获得就可以设置。调用f。set（obj，value）可以将obj对象的f域设置成新值。