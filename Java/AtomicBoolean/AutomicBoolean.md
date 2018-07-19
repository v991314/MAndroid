# AtomicBoolean意义何在

提供了一种在多线程中安全处理业务逻辑的方案

```Java
private var started = AtomicBoolean(false)
if (started.compareAndSet(false, true)) {
                    call.enqueue(object : Callback<R> {
                        override fun onResponse(call: Call<R>, response: Response<R>) {
                        }
                        override fun onFailure(call: Call<R>, throwable: Throwable) {
                        }
                    })
                }
```

先解释一个方法

```Java
boolean compareAndSet(boolean expect, boolean update)
```

该方法名释义:先比较后设置

* 比较的是初始化值与expect期望的值，若一致则执行里面代码，即例子中的call.enqueue()
* 设置的是将AtomicBoolean的设为update的值

该方案的比较和设置两个操作前后连续不间断，不会有其他方法和代码执行，保证了在多线程下业务逻辑安全的执行

