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
package com.me94me.example_retrofit.retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.Nullable;

/**
 * CallAdapter用于对原始Call进行再次封装，
 * 适配请求返回的Response类型,如Call<R>到Observable<R>
 * 从{@linkplain Retrofit.Builder#addCallAdapterFactory(Factory) installed}的Factory中创建的CallAdapter
 */
public interface CallAdapter<R, T> {
  /**
   * 当数据转换器将responseBody转换成Java对象,返回这个Java对象
   * 用于适配的对象
   * 注意：CallAdapterFactory提供的类型通常与返回值不是同一类型，因为转换了嘛
   */
  Type responseType();

  /**
   * 返回T类型
   */
  T adapt(Call<R> call);




  /**
   * 创建CallAdapter
   */
  abstract class Factory {
    /**
     * 为接口方法返回一个callAdapter
     * 或者null如果不能比factory处理
     */
    public abstract @Nullable
    CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
                          Retrofit retrofit);

    /**
     * 提取多参数数组中index序列号的参数
     * 例如 {@code Map<String, ? extends Runnable>} index为1的就{@code Runnable}
     */
    protected static Type getParameterUpperBound(int index, ParameterizedType type) {
      return Utils.getParameterUpperBound(index, type);
    }

    /**
     * 提取type的class类型
     * 例如 这个type表示为{@code List<? extends Runnable>}返回的就是{@code List.class}
     */
    protected static Class<?> getRawType(Type type) {
      return Utils.getRawType(type);
    }
  }
}
