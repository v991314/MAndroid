

# Retrofit与LiveData结合

写上题目有点不知道怎么写了的很短暂的尴尬

> 有个问题,为什么我们可以直接更改service接口方法的返回值。

先解释一波,相信我们都对Rxjava+Retrofit使用很熟悉了吧，回忆一下步骤

* 添加了Retrofit对Rxjava支持的依赖

   ```Java
   implementation 'com.squareup.retrofit2:adapter-rxjava2:2.3.0'
   ```

* 在构建Retrofit实例的时候添加了RxJavaCallAdapterFactory
  ```Java
  addCallAdapterFactory(RxJava2CallAdapterFactory.create())
  ```

* 然后好像就可以直接更改接口方法的返回值进行使用了
  ```
  @POST("test")
  fun test():LiveData<Object>
  ```

> 这个CallAdapterFactory是什么,为什么能够达到更改返回值进行使用

熟悉Retrofit源码或者了解过的人就知道CallAdapter是Retrofit的请求适配器,CallAdapterFactory用于获取CallAdapter实例,然后调用了CallAdapter的adapt方法返回你想要适配的返回值，就可以达到我们直接修改返回值进行使用的效果了

补充一句，Retrofit真的设计的美

好的开始我的表演

## LiveDataCallAdapterFactory

直接贴上代码

```Java
class LiveDataCallAdapterFactory : CallAdapter.Factory() {
    /**
     * 如果你要返回
     * LiveData<?>
     */
    override fun get(returnType: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): CallAdapter<*, *>? {
        if(returnType !is ParameterizedType){
            throw IllegalArgumentException("返回值需为参数化类型")
        }
        //获取returnType的class类型
        val returnClass = CallAdapter.Factory.getRawType(returnType)
        if(returnClass != LiveData::class.java){
            throw IllegalArgumentException("返回值不是LiveData类型")
        }
        //先解释一下getParameterUpperBound
        //官方例子
        //For example, index 1 of {@code Map<String, ? extends Runnable>} returns {@code Runnable}.
        //获取的是Map<String,? extends Runnable>参数列表中index序列号的参数类型,即0为String,1为Runnable
        //这里的0就是LiveData<?>中?的序列号,因为只有一个参数
        //其实这个就是我们请求返回的实体
        val type = CallAdapter.Factory.getParameterUpperBound(0, returnType as ParameterizedType)
        return LiveDataCallAdapter<Any>(type)
    }
    /**
     * 请求适配器
     */
    class LiveDataCallAdapter<R>(var type:Type):CallAdapter<R,LiveData<R>>{
        override fun adapt(call: Call<R>?): LiveData<R> {
            return object:LiveData<R>(){
            	//这个作用是业务在多线程中,业务处理的线程安全问题,确保单一线程作业
                val flag = AtomicBoolean(false)
                override fun onActive() {
                    super.onActive()
                    if(flag.compareAndSet(false,true)){
                        call!!.enqueue(object: Callback<R> {
                            override fun onFailure(call: Call<R>?, t: Throwable?) {
                                postValue(null)
                            }
                            override fun onResponse(call: Call<R>?, response: Response<R>?) {
                                postValue(response?.body())
                            }
                        })
                    }
                }
            }
        }
        override fun responseType(): Type {
            return type
        }
    }
}
```

注释都在代码中，相信大家都能明白的

自定义CallAdapterFactory完了就可以在构建Retrofit实例的时候添加该Factory了，就可以使用

建议大家都尝试自己自定义一个,没有看过Retrofit源码的也该看了

其实有几个小知识点

第一个,[什么是ParameterizedType](https://github.com/me94me/mandroid/blob/master/Java/Type/ParameterizedType.md)

第二个,AtomicBoolean的作用