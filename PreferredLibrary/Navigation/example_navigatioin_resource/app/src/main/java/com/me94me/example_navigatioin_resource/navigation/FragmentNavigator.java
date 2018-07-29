package com.me94me.example_navigatioin_resource.navigation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;

import java.util.HashMap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 *
 * Navigator that navigates through {@link FragmentTransaction fragment transactions}. Every
 * destination using this Navigator must set a valid Fragment class name with
 * <code>android:name</code> or {@link Destination#setFragmentClass}.
 */
@Navigator.Name("fragment")
public class FragmentNavigator extends Navigator<FragmentNavigator.Destination> {
    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private int mBackStackCount;

    private final FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    int newCount = mFragmentManager.getBackStackEntryCount();
                    int backStackEffect;
                    if (newCount < mBackStackCount) {
                        backStackEffect = BACK_STACK_DESTINATION_POPPED;
                    } else if (newCount > mBackStackCount) {
                        backStackEffect = BACK_STACK_DESTINATION_ADDED;
                    } else {
                        backStackEffect = BACK_STACK_UNCHANGED;
                    }
                    mBackStackCount = newCount;

                    int destId = 0;
                    StateFragment state = getState();
                    if (state != null) {
                        destId = state.mCurrentDestId;
                    }
                    dispatchOnNavigatorNavigated(destId, backStackEffect);
                }
            };

    public FragmentNavigator(Context context, FragmentManager manager, int containerId) {
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;

        mBackStackCount = mFragmentManager.getBackStackEntryCount();
        mFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    @Override
    public boolean popBackStack() {
        return mFragmentManager.popBackStackImmediate();
    }


    @Override
    public Destination createDestination() {
        return new Destination(this);
    }


    private String getBackStackName(int destinationId) {
        // This gives us the resource name if it exists,
        // or just the destinationId if it doesn't exist
        try {
            return mContext.getResources().getResourceName(destinationId);
        } catch (Resources.NotFoundException e) {
            return Integer.toString(destinationId);
        }
    }


    @Override
    public void navigate(Destination destination,Bundle args,NavOptions navOptions) {
        //通过反射构建Fragment实例
        final Fragment frag = destination.createFragment(args);
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        //处理动画
        int enterAnim = navOptions != null ? navOptions.getEnterAnim() : -1;
        int exitAnim = navOptions != null ? navOptions.getExitAnim() : -1;
        int popEnterAnim = navOptions != null ? navOptions.getPopEnterAnim() : -1;
        int popExitAnim = navOptions != null ? navOptions.getPopExitAnim() : -1;
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = enterAnim != -1 ? enterAnim : 0;
            exitAnim = exitAnim != -1 ? exitAnim : 0;
            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
        }
        //放入容器中
        ft.replace(mContainerId, frag);
        final StateFragment oldState = getState();
        if (oldState != null) {
            ft.remove(oldState);
        }
        final int destId = destination.getId();
        final StateFragment newState = new StateFragment();
        newState.mCurrentDestId = destId;
        ft.add(newState, StateFragment.FRAGMENT_TAG);
        final boolean initialNavigation = mFragmentManager.getFragments().isEmpty();
        final boolean isClearTask = navOptions != null && navOptions.shouldClearTask();
        // TODO Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && oldState != null
                && navOptions.shouldLaunchSingleTop()
                && oldState.mCurrentDestId == destId;
        if (!initialNavigation && !isClearTask && !isSingleTopReplacement) {
            ft.addToBackStack(getBackStackName(destId));
        } else {
            ft.runOnCommit(new Runnable() {
                @Override
                public void run() {
                    dispatchOnNavigatorNavigated(destId, isSingleTopReplacement
                            ? BACK_STACK_UNCHANGED
                            : BACK_STACK_DESTINATION_ADDED);
                }
            });
        }
        ft.commit();
        mFragmentManager.executePendingTransactions();
    }

    private StateFragment getState() {
        return (StateFragment) mFragmentManager.findFragmentByTag(StateFragment.FRAGMENT_TAG);
    }

    /**
     * NavDestination specific to {@link FragmentNavigator}
     */
    public static class Destination extends NavDestination {
        private static final HashMap<String, Class<? extends Fragment>> sFragmentClasses =
                new HashMap<>();

        private Class<? extends Fragment> mFragmentClass;

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setFragmentClass(Class)}.
         *
         * @param navigatorProvider The {@link NavController} which this destination
         *                          will be associated with.
         */
        public Destination(NavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(FragmentNavigator.class));
        }

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setFragmentClass(Class)}.
         *
         * @param fragmentNavigator The {@link FragmentNavigator} which this destination
         *                          will be associated with. Generally retrieved via a
         *                          {@link NavController}'s
         *                          {@link NavigatorProvider#getNavigator(Class)} method.
         */
        public Destination( Navigator<? extends Destination> fragmentNavigator) {
            super(fragmentNavigator);
        }

        @Override
        public void onInflate(Context context, AttributeSet attrs) {
            super.onInflate(context, attrs);
            TypedArray a = context.getResources().obtainAttributes(attrs,
                    R.styleable.FragmentNavigator);
            setFragmentClass(getFragmentClassByName(context, a.getString(
                            R.styleable.FragmentNavigator_android_name)));
            a.recycle();
        }

        @SuppressWarnings("unchecked")
        private Class<? extends Fragment> getFragmentClassByName(Context context, String name) {
            if (name != null && name.charAt(0) == '.') {
                name = context.getPackageName() + name;
            }
            Class<? extends Fragment> clazz = sFragmentClasses.get(name);
            if (clazz == null) {
                try {
                    clazz = (Class<? extends Fragment>) Class.forName(name, true,
                            context.getClassLoader());
                    sFragmentClasses.put(name, clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return clazz;
        }

        /**
         * Set the Fragment associated with this destination
         * @param clazz The class name of the Fragment to show when you navigate to this
         *              destination
         * @return this {@link Destination}
         */
        public Destination setFragmentClass(Class<? extends Fragment> clazz) {
            mFragmentClass = clazz;
            return this;
        }

        /**
         * Gets the Fragment associated with this destination
         * @return
         */
        public Class<? extends Fragment> getFragmentClass() {
            return mFragmentClass;
        }

        /**
         * Create a new instance of the {@link Fragment} associated with this destination.
         * @param args optional args to set on the new Fragment
         * @return an instance of the {@link #getFragmentClass() Fragment class} associated
         * with this destination
         */
        @SuppressWarnings("ClassNewInstance")
        public Fragment createFragment( Bundle args) {
            Class<? extends Fragment> clazz = getFragmentClass();
            if (clazz == null) {
                throw new IllegalStateException("fragment class not set");
            }

            Fragment f;
            try {
                f = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (args != null) {
                f.setArguments(args);
            }
            return f;
        }
    }

    /**
     * An internal fragment used by FragmentNavigator to track additional navigation state.
     *
     * @hide
     */
    public static class StateFragment extends Fragment {
        static final String FRAGMENT_TAG = "android-support-nav:FragmentNavigator.StateFragment";

        private static final String KEY_CURRENT_DEST_ID = "currentDestId";

        int mCurrentDestId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mCurrentDestId = savedInstanceState.getInt(KEY_CURRENT_DEST_ID);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(KEY_CURRENT_DEST_ID, mCurrentDestId);
        }
    }
}
