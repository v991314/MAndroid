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

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import static com.me94me.example_retrofit.retrofit2.Utils.checkNotNull;
import static com.me94me.example_retrofit.retrofit2.Utils.throwIfFatal;

/**
 * OkHttpCall
 */
final class OkHttpCall<T> implements Call<T> {

  //包含所有请求参数的对象
  private final ServiceMethod<T, ?> serviceMethod;
  //网络请求接口的参数
  private final @Nullable Object[] args;

  private volatile boolean canceled;

  //实际进行访问的类
  @GuardedBy("this")
  private @Nullable okhttp3.Call rawCall;

  @GuardedBy("this") // Either a RuntimeException, non-fatal Error, or IOException.
  private @Nullable Throwable creationFailure;

  @GuardedBy("this")
  private boolean executed;

  OkHttpCall(ServiceMethod<T, ?> serviceMethod, @Nullable Object[] args) {
    this.serviceMethod = serviceMethod;
    this.args = args;
  }

  @SuppressWarnings("CloneDoesntCallSuperClone") // We are a final type & this saves clearing state.
  @Override public OkHttpCall<T> clone() {
    return new OkHttpCall<>(serviceMethod, args);
  }

  @Override public synchronized Request request() {
    okhttp3.Call call = rawCall;
    if (call != null) {
      return call.request();
    }
    if (creationFailure != null) {
      if (creationFailure instanceof IOException) {
        throw new RuntimeException("Unable to create request.", creationFailure);
      } else if (creationFailure instanceof RuntimeException) {
        throw (RuntimeException) creationFailure;
      } else {
        throw (Error) creationFailure;
      }
    }
    try {
      return (rawCall = createRawCall()).request();
    } catch (RuntimeException | Error e) {
      throwIfFatal(e); // Do not assign a fatal error to creationFailure.
      creationFailure = e;
      throw e;
    } catch (IOException e) {
      creationFailure = e;
      throw new RuntimeException("Unable to create request.", e);
    }
  }

  @Override public void enqueue(final Callback<T> callback) {
    checkNotNull(callback, "callback == null");

    okhttp3.Call call;
    Throwable failure;
    //同步锁
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already executed.");
      executed = true;

      call = rawCall;
      failure = creationFailure;
      if (call == null && failure == null) {
        try {
          //创建真正的请求的对象
          call = rawCall = createRawCall();
        } catch (Throwable t) {
          throwIfFatal(t);
          failure = creationFailure = t;
        }
      }
    }

    if (failure != null) {
      callback.onFailure(this, failure);
      return;
    }

    if (canceled) {
      call.cancel();
    }

    call.enqueue(new okhttp3.Callback() {
      @Override public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) {
        Response<T> response;
        try {
          response = parseResponse(rawResponse);
        } catch (Throwable e) {
          callFailure(e);
          return;
        }

        try {
          //回调
          callback.onResponse(OkHttpCall.this, response);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }

      @Override public void onFailure(okhttp3.Call call, IOException e) {
        callFailure(e);
      }

      private void callFailure(Throwable e) {
        try {
          callback.onFailure(OkHttpCall.this, e);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });
  }

  @Override public synchronized boolean isExecuted() {
    return executed;
  }


  /**
   * 这里同步请求
   */
  @Override public Response<T> execute() throws IOException {
    okhttp3.Call call;

    synchronized (this) {
      if (executed) throw new IllegalStateException("Already executed.");
      executed = true;

      if (creationFailure != null) {
        if (creationFailure instanceof IOException) {
          throw (IOException) creationFailure;
        } else if (creationFailure instanceof RuntimeException) {
          throw (RuntimeException) creationFailure;
        } else {
          throw (Error) creationFailure;
        }
      }

      call = rawCall;
      if (call == null) {
        try {
          call = rawCall = createRawCall();
        } catch (IOException | RuntimeException | Error e) {
          throwIfFatal(e); //  Do not assign a fatal error to creationFailure.
          creationFailure = e;
          throw e;
        }
      }
    }

    if (canceled) {
      call.cancel();
    }
    return parseResponse(call.execute());
  }

  /**
   * 创建请求对象
   */
  private okhttp3.Call createRawCall() throws IOException {
    okhttp3.Call call = serviceMethod.toCall(args);
    if (call == null) {
      throw new NullPointerException("Call.Factory returned null.");
    }
    return call;
  }

  /**
   * 解析放回值
   * @throws IOException io
   */
  Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
    ResponseBody rawBody = rawResponse.body();
    // Remove the body's source (the only stateful object) so we can pass the response along.
    rawResponse = rawResponse.newBuilder()
        .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
        .build();

    int code = rawResponse.code();
    //检查请求返回的code
    if (code < 200 || code >= 300) {
      try {
        // Buffer the entire body to avoid future I/O.
        ResponseBody bufferedBody = Utils.buffer(rawBody);
        return Response.error(bufferedBody, rawResponse);
      } finally {
        rawBody.close();
      }
    }

    if (code == 204 || code == 205) {
      rawBody.close();
      return Response.success(null, rawResponse);
    }

    ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
    try {
      //通过数据转换器解析成我们需要的类型
      T body = serviceMethod.toResponse(catchingBody);
      return Response.success(body, rawResponse);
    } catch (RuntimeException e) {
      // If the underlying source threw an exception, propagate that rather than indicating it was
      // a runtime exception.
      catchingBody.throwIfCaught();
      throw e;
    }
  }

  public void cancel() {
    canceled = true;

    okhttp3.Call call;
    synchronized (this) {
      call = rawCall;
    }
    if (call != null) {
      call.cancel();
    }
  }

  @Override public boolean isCanceled() {
    if (canceled) {
      return true;
    }
    synchronized (this) {
      return rawCall != null && rawCall.isCanceled();
    }
  }

  static final class NoContentResponseBody extends ResponseBody {
    private final MediaType contentType;
    private final long contentLength;

    NoContentResponseBody(MediaType contentType, long contentLength) {
      this.contentType = contentType;
      this.contentLength = contentLength;
    }

    @Override public MediaType contentType() {
      return contentType;
    }

    @Override public long contentLength() {
      return contentLength;
    }

    @Override public BufferedSource source() {
      throw new IllegalStateException("Cannot read raw response body of a converted body.");
    }
  }

  static final class ExceptionCatchingRequestBody extends ResponseBody {
    private final ResponseBody delegate;
    IOException thrownException;

    ExceptionCatchingRequestBody(ResponseBody delegate) {
      this.delegate = delegate;
    }

    @Override public MediaType contentType() {
      return delegate.contentType();
    }

    @Override public long contentLength() {
      return delegate.contentLength();
    }

    @Override public BufferedSource source() {
      return Okio.buffer(new ForwardingSource(delegate.source()) {
        @Override public long read(Buffer sink, long byteCount) throws IOException {
          try {
            return super.read(sink, byteCount);
          } catch (IOException e) {
            thrownException = e;
            throw e;
          }
        }
      });
    }

    @Override public void close() {
      delegate.close();
    }

    void throwIfCaught() throws IOException {
      if (thrownException != null) {
        throw thrownException;
      }
    }
  }
}
