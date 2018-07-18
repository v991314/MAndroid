/*
 * Copyright (C) 2015 Square, Inc.
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
package com.me94me.example_retrofit.retrofit2.calladapterfactory;

import com.me94me.example_retrofit.retrofit2.Call;
import com.me94me.example_retrofit.retrofit2.CallAdapter;
import com.me94me.example_retrofit.retrofit2.Retrofit;
import com.me94me.example_retrofit.retrofit2.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 使用策略模式根据返回值类型寻找合适的CallAdapter然后创建CallAdapter实例
 * 创建callAdapter,使用相同的线程来进行IO操作和应用级别的回调
 */
public class DefaultCallAdapterFactory extends CallAdapter.Factory {

  public static final CallAdapter.Factory INSTANCE = new DefaultCallAdapterFactory();
  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    //返回的类型不是call则返回null
    if (getRawType(returnType) != Call.class) {
      return null;
    }

    final Type responseType = Utils.getCallResponseType(returnType);

    return new CallAdapter<Object, Call<?>>() {
      //返回Response的类型
      @Override public Type responseType() {
        return responseType;
      }
      //默认返回Call类型
      @Override public Call<Object> adapt(Call<Object> call) {
        return call;
      }
    };
  }
}
