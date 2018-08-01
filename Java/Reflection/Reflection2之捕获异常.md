# Reflection之捕获异常

在程序运行过程中发生错误时，就会“抛出异常”。抛出异常比终止程序要灵活得多，这是因为可以提供一个“捕获”异常的处理器（handler）对异常情况进行处理。

如果没有提供处理器，程序就会终止，并在控制台上打印出一条信息，其中给出了异常的类型。

```java
tyr{
    String name = ...;//get class name
    Class cl = Class.forName(name);//might throw exception
    ...//do something with cl
}catch(Exception e){
    e.printStackTrace();
}
```
如果类名不存在，则将跳过try块中的剩余代码，程序直接进入catch子句（这里，利用Throwable类的pintStackTrace方法打印出栈的轨迹。Throwable是Exception类的超类）。如果try块中没有抛出任何异常，那么会跳过catch子句的处理器代码。

