
package com.me94me.example_navigatioin_resource.navigation;

import android.annotation.SuppressLint;

import java.util.HashMap;

import androidx.annotation.RestrictTo;

/**
 * Simple implementation of a {@link NavigatorProvider} that stores instances of
 * {@link Navigator navigators} by name, using the {@link Navigator.Name} when given a class name.
 *
 * @hide
 */
@SuppressLint("TypeParameterUnusedInFormals")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class SimpleNavigatorProvider implements NavigatorProvider {

    private static final HashMap<Class, String> sAnnotationNames = new HashMap<>();

    private final HashMap<String, Navigator<? extends NavDestination>> mNavigators = new HashMap<>();

    private String getNameForNavigator(Class<? extends Navigator> navigatorClass) {
        String name = sAnnotationNames.get(navigatorClass);
        if (name == null) {
            Navigator.Name annotation = navigatorClass.getAnnotation(Navigator.Name.class);
            name = annotation != null ? annotation.value() : null;
            if (!validateName(name)) {
                throw new IllegalArgumentException("No @Navigator.Name annotation found for "
                        + navigatorClass.getSimpleName());
            }
            sAnnotationNames.put(navigatorClass, name);
        }
        return name;
    }


    @Override
    public <D extends NavDestination, T extends Navigator<? extends D>> T getNavigator(Class<T> navigatorClass) {
        String name = getNameForNavigator(navigatorClass);
        return getNavigator(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends NavDestination, T extends Navigator<? extends D>> T getNavigator(String name) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("navigator name cannot be an empty string");
        }

        Navigator<? extends NavDestination> navigator = mNavigators.get(name);
        if (navigator == null) {
            throw new IllegalStateException("Could not find Navigator with name \"" + name
                    + "\". You must call NavController.addNavigator() for each navigation type.");
        }
        return (T) navigator;
    }


    /**
     * 添加Navigator,默认为FragmentNavigator
     * 并以name和navigator存入hashMap中
     */
    @Override
    public Navigator<? extends NavDestination> addNavigator( Navigator<? extends NavDestination> navigator) {
        String name = getNameForNavigator(navigator.getClass());
        return addNavigator(name, navigator);
    }


    @Override
    public Navigator<? extends NavDestination> addNavigator(String name, Navigator<? extends NavDestination> navigator) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("navigator name cannot be an empty string");
        }
        return mNavigators.put(name, navigator);
    }

    private boolean validateName(String name) {
        return name != null && !name.isEmpty();
    }
}
