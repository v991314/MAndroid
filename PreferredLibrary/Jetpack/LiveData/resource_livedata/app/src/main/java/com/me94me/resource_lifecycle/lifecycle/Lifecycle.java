package com.me94me.resource_lifecycle.lifecycle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * Defines an object that has an Android Lifecycle. {@link androidx.fragment.app.Fragment Fragment}
 * and {@link androidx.fragment.app.FragmentActivity FragmentActivity} classes implement
 * {@link LifecycleOwner} interface which has the {@link LifecycleOwner#getLifecycle()
 * getLifecycle} method to access the Lifecycle. You can also implement {@link LifecycleOwner}
 * in your own classes.
 * <p>
 * {@link Event#ON_CREATE}, {@link Event#ON_START}, {@link Event#ON_RESUME} events in this class
 * are dispatched <b>after</b> the {@link LifecycleOwner}'s related method returns.
 * {@link Event#ON_PAUSE}, {@link Event#ON_STOP}, {@link Event#ON_DESTROY} events in this class
 * are dispatched <b>before</b> the {@link LifecycleOwner}'s related method is called.
 * For instance, {@link Event#ON_START} will be dispatched after
 * {@link android.app.Activity#onStart onStart} returns, {@link Event#ON_STOP} will be dispatched
 * before {@link android.app.Activity#onStop onStop} is called.
 * This gives you certain guarantees on which state the owner is in.
 * <p>
 * If you use <b>Java 8 Language</b>, then observe events with {@link DefaultLifecycleObserver}.
 * To include it you should add {@code "androidx.lifecycle:common-java8:<version>"} to your
 * build.gradle file.
 * <pre>
 * class TestObserver implements DefaultLifecycleObserver {
 *     {@literal @}Override
 *     public void onCreate(LifecycleOwner owner) {
 *         // your code
 *     }
 * }
 * </pre>
 * If you use <b>Java 7 Language</b>, Lifecycle events are observed using annotations.
 * Once Java 8 Language becomes mainstream on Android, annotations will be deprecated, so between
 * {@link DefaultLifecycleObserver} and annotations,
 * you must always prefer {@code DefaultLifecycleObserver}.
 * <pre>
 * class TestObserver implements LifecycleObserver {
 *   {@literal @}OnLifecycleEvent(ON_STOP)
 *   void onStopped() {}
 * }
 * </pre>
 * <p>
 * Observer methods can receive zero or one argument.
 * If used, the first argument must be of type {@link LifecycleOwner}.
 * Methods annotated with {@link Event#ON_ANY} can receive the second argument, which must be
 * of type {@link Event}.
 * <pre>
 * class TestObserver implements LifecycleObserver {
 *   {@literal @}OnLifecycleEvent(ON_CREATE)
 *   void onCreated(LifecycleOwner source) {}
 *   {@literal @}OnLifecycleEvent(ON_ANY)
 *   void onAny(LifecycleOwner source, Event event) {}
 * }
 * </pre>
 * These additional parameters are provided to allow you to conveniently observe multiple providers
 * and events without tracking them manually.
 */
public abstract class Lifecycle {
    /**
     * Adds a LifecycleObserver that will be notified when the LifecycleOwner changes
     * state.
     * <p>
     * The given observer will be brought to the current state of the LifecycleOwner.
     * For example, if the LifecycleOwner is in {@link State#STARTED} state, the given observer
     * will receive {@link Event#ON_CREATE}, {@link Event#ON_START} events.
     *
     * @param observer The observer to notify.
     */
    @MainThread
    public abstract void addObserver(@NonNull LifecycleObserver observer);

    /**
     * Removes the given observer from the observers list.
     * <p>
     * If this method is called while a state change is being dispatched,
     * <ul>
     * <li>If the given observer has not yet received that event, it will not receive it.
     * <li>If the given observer has more than 1 method that observes the currently dispatched
     * event and at least one of them received the event, all of them will receive the event and
     * the removal will happen afterwards.
     * </ul>
     *
     * @param observer The observer to be removed.
     */
    @MainThread
    public abstract void removeObserver(@NonNull LifecycleObserver observer);

    /**
     * Returns the current state of the Lifecycle.
     *
     * @return The current state of the Lifecycle.
     */
    @MainThread
    @NonNull
    public abstract State getCurrentState();



    public enum Event {
        /**
         * Constant for onCreate event of the {@link LifecycleOwner}.
         */
        ON_CREATE,
        /**
         * Constant for onStart event of the {@link LifecycleOwner}.
         */
        ON_START,
        /**
         * Constant for onResume event of the {@link LifecycleOwner}.
         */
        ON_RESUME,
        /**
         * Constant for onPause event of the {@link LifecycleOwner}.
         */
        ON_PAUSE,
        /**
         * Constant for onStop event of the {@link LifecycleOwner}.
         */
        ON_STOP,
        /**
         * Constant for onDestroy event of the {@link LifecycleOwner}.
         */
        ON_DESTROY,
        /**
         * An {@link Event Event} constant that can be used to match all events.
         */
        ON_ANY
    }

    /**
     * 生命周期状态
     *
     * 可以将这些状态比作图上的结点，{@link Event}作为这些结点的边
     */
    public enum State {
        /**
         * 生命周期持有者的Destroyed状态
         * 该事件之后，该生命周期不会分发任何事件
         * 对于Activity来说
         * 在{@link android.app.Activity#onDestroy() onDestroy}之前调用
         */
        DESTROYED,

        /**
         * 生命周期持有者的Initialized状态
         * 对于Activity来说
         * 当状态已经构造但还没有接受到{@link android.app.Activity#onCreate(android.os.Bundle) onCreate}
         */
        INITIALIZED,

        /**
         * 生命周期持有者的Created状态
         * 对于Activity来说，该状态处于两种情况之间
         * <ul>
         *     <li>after {@link android.app.Activity#onCreate(android.os.Bundle) onCreate} call;
         *     <li><b>right before</b> {@link android.app.Activity#onStop() onStop} call.
         * </ul>
         */
        CREATED,

        /**
         * 生命周期持有者的Started状态
         * 对于Activity，状态处于两者情况之间
         * <ul>
         *     <li>after {@link android.app.Activity#onStart() onStart} call;
         *     <li><b>right before</b> {@link android.app.Activity#onPause() onPause} call.
         * </ul>
         */
        STARTED,

        /**
         * 生命周期持有者的Resumed状态
         * 对于Activity来说，在{@link android.app.Activity#onResume() onResume}后调用
         */
        RESUMED;


        /**
         * 比较当前状态是否大于或等于给定的{@code state}
         */
        public boolean isAtLeast(@NonNull State state) {
            return compareTo(state) >= 0;
        }
    }
}
