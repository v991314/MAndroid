/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.me94me.example_retrofit.retrofit2;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;


import com.me94me.example_retrofit.retrofit2.calladapterfactory.DefaultCallAdapterFactory;
import com.me94me.example_retrofit.retrofit2.calladapterfactory.ExecutorCallAdapterFactory;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


class Platform {

  private static final Platform PLATFORM = findPlatform();

  static Platform get() {
    return PLATFORM;
  }

  private static Platform findPlatform() {
    try {
      Class.forName("android.os.Build");
      if (Build.VERSION.SDK_INT != 0) {
        return new Android();
      }
    } catch (ClassNotFoundException ignored) {
    }
    try {
      Class.forName("java.util.Optional");
      return new Java8();
    } catch (ClassNotFoundException ignored) {
    }
    return new Platform();
  }

  @Nullable
  Executor defaultCallbackExecutor() {
    return null;
  }

  /**
   * 获取默认的callAdapterFactory
   */
  CallAdapter.Factory defaultCallAdapterFactory(@Nullable Executor callbackExecutor) {
    if (callbackExecutor != null) {
      //回调执行器不为null
      return new ExecutorCallAdapterFactory(callbackExecutor);
    }
    return DefaultCallAdapterFactory.INSTANCE;
  }


  boolean isDefaultMethod(Method method) {
    return false;
  }

  @Nullable Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object,
      @Nullable Object... args) throws Throwable {
    throw new UnsupportedOperationException();
  }


  /**
   * Java8
   */
  //@IgnoreJRERequirement // Only classloaded and used on Java 8.
  static class Java8 extends Platform {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override boolean isDefaultMethod(Method method) {
      return method.isDefault();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object,
                                         @Nullable Object... args) throws Throwable {
      // Because the Service interface might not be public, we need to use a MethodHandle lookup
      // that ignores the visibility of the declaringClass.
      Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);
      constructor.setAccessible(true);
      return constructor.newInstance(declaringClass, -1 /* trusted */)
          .unreflectSpecial(method, declaringClass)
          .bindTo(object)
          .invokeWithArguments(args);
    }
  }




  /**
   * Android平台
   */

  static class Android extends Platform {
    /**
     * 默认回调器
     */
    @Override
    public Executor defaultCallbackExecutor() {
      return new MainThreadExecutor();
    }
    /**
     * 默认CallAdapter
     */
    @Override CallAdapter.Factory defaultCallAdapterFactory(@Nullable Executor callbackExecutor) {
      if (callbackExecutor == null) throw new AssertionError();
      return new ExecutorCallAdapterFactory(callbackExecutor);
    }

    static class MainThreadExecutor implements Executor {
      /**
       * 主线程handler
       */
      private final Handler handler = new Handler(Looper.getMainLooper());
      /**
       *  发送消息
       */
      @Override public void execute(Runnable r) {
        //在UI线程对请求返回的数据进行处理
        handler.post(r);
      }
    }
  }
}
