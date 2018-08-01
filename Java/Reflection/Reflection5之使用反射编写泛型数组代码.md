# Reflection之使用反射编写泛型数组代码

java.lang.reflect包中的Array类允许动态地创建数组。

假设有一个元素为某种类型且已经被填满数组。现在希望扩展它的长度，但不想手动地编写那些扩展、拷贝元素的代码，而是想编写一个用于扩展数组的通用方法：

```Java
Employee[] a = new Employee[100];
...
//array is full
a = (Employee[]) arrayGrow(a);
```

如何编写这样一个通用的方法呢？正好能够将Employee[]数组装换为Object[]数组，这让人感觉很有希望。下面试着编写一个通用的方法，其功能是将数组扩展到10%+10个元素

```Java
static Object[] badArrayGrow(Object[] a){//not useful
    int newLenth = a.length*11/10+10;
    Object[] newArray = new Object[newLength];
    System.arraycopy(a,0,newArray,0,a.length);
    return newArray;
}
```

然而在使用在会遇到一个问题。这段代码返回的数组类型是对象数组类型（Object[]）,一个对象数组不能转换成雇员数组（Employee[]）,如果这样做Java将会产生ClassCastException异常。

Java数组会记住每个元素的类型，即创建数组时new表达式中使用的元素类型。将一个Employee[] 的数组临时地转换成Object[] 数组，然后在把它转换回来是可以的，但一个从开始就是Object[] 数组却永远不能转换成Employee[] 数组。

为此需要java.lang.reflect包中Array类的一些方法。其中最关键的是Array类中的静态方法newInstance，它能够构造新数组。在调用它时必须提供两个参数，一个是数组的元素类型，一个是数组的长度。

```JAVA
Object newArray = Array.newInstance(componentType,newLength);
```

为了能够实际地运行，需要获得新数组的长度和元素类型。

可以通过调用Array.getLenth(a)获得数组的长度，也可以通过Array类的静态getLength方法的返回值得到任何数组的长度。

而要获得数组元素类型，就需要进行一下工作：

* 首先获得a数组的类型对象

* 确认它是一个数组

* 使用Class类（只能定义表示数组的类对象）的getComponentType方法确定数组对应的类型。

```Java
static Object goodArrayGrow(Object a){//useful
	  Class cl = a.getClass();
    if(!cl.isArray())return null;
    Class componentType = cl.getComponentType();
    int length = Array.getLenth(a);
    int newLength = length*11/10+10;
    Object newArray = Array.newInstance(componentType,newLenth);
    System.arraycopy(a,0,newArray.0,length);
    return newArray;
}
```

请注意，arrrayGrow方法可以用来扩展任意类型的数组，而不是对象数组。

如果只希望扩大数组，利用Arrays类的copyOf方法就可以

```Java
Employee[] a = new Employee[100];
...
//aaray is full
a = Arrays.copyOf(a,a.length*11/10+10);    
```