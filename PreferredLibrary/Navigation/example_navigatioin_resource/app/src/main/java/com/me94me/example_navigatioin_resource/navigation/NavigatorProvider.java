
package com.me94me.example_navigatioin_resource.navigation;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A NavigationProvider stores a set of {@link Navigator}s that are valid ways to navigate
 * to a destination.
 */
@SuppressLint("TypeParameterUnusedInFormals")
public interface NavigatorProvider {
    /**
     * Retrieves a registered {@link Navigator} using the name provided by the
     * {@link Navigator.Name Navigator.Name annotation}.
     *
     * @param navigatorClass class of the navigator to return
     * @return the registered navigator with the given {@link Navigator.Name}
     *
     * @throws IllegalArgumentException if the Navigator does not have a
     * {@link Navigator.Name Navigator.Name annotation}
     * @throws IllegalStateException if the Navigator has not been added
     *
     * @see #addNavigator(Navigator)
     */
    @NonNull
    <D extends NavDestination, T extends Navigator<? extends D>> T getNavigator(
            @NonNull Class<T> navigatorClass);

    /**
     * Retrieves a registered {@link Navigator} by name.
     *
     * @param name name of the navigator to return
     * @return the registered navigator with the given name
     *
     * @throws IllegalStateException if the Navigator has not been added
     *
     * @see #addNavigator(String, Navigator)
     */
    @NonNull
    <D extends NavDestination, T extends Navigator<? extends D>> T getNavigator(
            @NonNull String name);

    /**
     * Register a navigator using the name provided by the
     * {@link Navigator.Name Navigator.Name annotation}. {@link NavDestination destinations} may
     * refer to any registered navigator by name for inflation. If a navigator by this name is
     * already registered, this new navigator will replace it.
     *
     * @param navigator navigator to add
     * @return the previously added Navigator for the name provided by the
     * {@link Navigator.Name Navigator.Name annotation}, if any
     */
    Navigator<? extends NavDestination> addNavigator(Navigator<? extends NavDestination> navigator);

    /**
     * Register a navigator by name. {@link NavDestination destinations} may refer to any
     * registered navigator by name for inflation. If a navigator by this name is already
     * registered, this new navigator will replace it.
     *
     * @param name name for this navigator
     * @param navigator navigator to add
     * @return the previously added Navigator for the given name, if any
     */
    @Nullable
    Navigator<? extends NavDestination> addNavigator(@NonNull String name,
            @NonNull Navigator<? extends NavDestination> navigator);
}
