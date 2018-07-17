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

import java.io.IOException;

import okhttp3.Request;

/**
 * 可使用{@link #clone}可复制这个请求生成多个相同参数的请求
 * @param <T> 请求成功返回的数据类型
 */
public interface Call<T> extends Cloneable {

  /**
   * 同步请求
   * @throws IOException 正在写入请求或者读取response时可能产生
   * @throws RuntimeException 创建请求或解码的响应时的意外错误
   */
  Response<T> execute() throws IOException;

  /**
   * 异步请求通过回调返回response数据
   */
  void enqueue(Callback<T> callback);

  /**
   * 是否同步请求
   */
  boolean isExecuted();

  /**
   * 取消请求
   * 请求还没有执行不能取消
   */
  void cancel();

  /**
   * 是否已经取消了
   */
  boolean isCanceled();

  /**
   * 复制一个请求
   */
  Call<T> clone();

  /**
   * 原始Http请求
   */
  Request request();
}
