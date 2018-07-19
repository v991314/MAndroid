
# ParameterizedType详解

## 参数化类型

```Java
public interface ParameterizedType extends Type {
    Type[] getActualTypeArguments();

    Type getRawType();

    Type getOwnerType();
}
```

> 何为参数化类型

列举一个实体类用代码来解释什么是参数化类型

```Java
public class ParameterizedBean {
    List<String> list1;
    List list2;
    Map<String,Long> map1;
    Map map2;
    Map.Entry<Long,Short> map3;
}
```
测试代码
```Java
Field[] fields = ParameterizedBean.class.getDeclaredFields();
for(Field f:fields){
    //是否是ParameterizedType
    System.out.print(f.getName()+":"+(f.getGenericType() instanceof ParameterizedType));
}
```
打印结果

	list1:true
	
	list2:false
	
	map1:true
	
	map2:false
	
	map3:true

从打印结果看来,具有<>符号的变量是参数化类型



>    Type[] getActualTypeArguments()

该方法返回一个Type数组

测试代码

```Java
Field[] fields = ParameterizedBean.class.getDeclaredFields();
for(Field f:fields){
	if(f.getGenericType() instanceof ParameterizedType){
        ParameterizedType pType =(ParameterizedType) f.getGenericType();
System.out.print("变量："+pType.getTypeName()+"     ");
              Type[] types =pType.getActualTypeArguments();
              for(Type t:types){
System.out.print("类型："+t.getTypeName());
              }
}
```
先把实体类放下来，免得往上翻

	public class ParameterizedBean {
		List<String> list1;
		List list2;
		Map<String,Long> map1;
		Map map2;
		Map.Entry<Long,Short> map3;
	}

打印结果

```Java
变量：list1     类型：java.lang.String
变量：map1     类型：java.lang.String类型：java.lang.Long
变量：map3     类型：java.lang.Long类型：java.lang.Short   
```

从打印结果返回来看,getActualTypeArguments()返回了一个Type数组,数组里是参数化类型的参数



>   Type getRawType()

获取变量的类型

还是用代码最有说服力

测试代码

```Java
Field[] fields =  ParameterizedBean.class.getDeclaredFields();
    for(Field f:fields){
    if(f.getGenericType() instanceof ParameterizedType){
        ParameterizedType pType = (ParameterizedType) f.getGenericType();
System.out.print("变量："+f.getName());
System.out.print("RawType："+pType.getRawType().getTypeName();
    }
}
```

先放实体类

```Java
public class ParameterizedBean {
	List<String> list1;
	List list2;
	Map<String,Long> map1;
	Map map2;
	Map.Entry<Long,Short> map3;
}
```

打印结果

```Java
变量：list1     RawType：java.util.List

变量：map1     RawType：java.util.Map

变量：map3     RawType：java.util.Map$Entry
```

从打印结果来看,其实也就是变量的类型



>  Type getOwnerType()

这个不太好理解,好不好理解代码都能解释清楚

测试代码

```Java
Field[] fields =  ParameterizedBean.class.getDeclaredFields();
    for(Field f:fields){
    if(f.getGenericType() instanceof ParameterizedType){
        ParameterizedType pType = (ParameterizedType) f.getGenericType();
System.out.print("变量："+f.getName());
		Type t = pType.getOwnerType();
        if(t == null){
System.out.print("OwnerType:Null     ");
        }else{       System.out.print("OwnerType："+t.getTypeName());
         }
    }
}
```
先放实体类

```Java
public class ParameterizedBean {
	List<String> list1;
	List list2;
	Map<String,Long> map1;
	Map map2;
	Map.Entry<Long,Short> map3;
}
```
打印结果

	变量：list1     OwnerType:Null     
	
	变量：map1     OwnerType:Null     
	
	变量：map3     OwnerType：java.util.Map

从打印结果来看,前面两个都为null,最后一个为Map类型

这里放一下官方解释

```Java
Returns a {@code Type} object representing the type that this type is a member of
```

例子

```
For example, if this type is {@code O<T>.I<S>},return a representation of {@code O<T>}
```

依据解释,我们知道

O<T>.I<S>类型的变量,调用getOwnerType()会返回O<T>

以上就是ParameterizedType的内容