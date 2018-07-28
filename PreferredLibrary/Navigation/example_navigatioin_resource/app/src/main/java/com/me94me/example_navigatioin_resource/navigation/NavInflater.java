package com.me94me.example_navigatioin_resource.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.annotation.NavigationRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Class which translates a navigation XML file into a {@link NavGraph}
 */
public class NavInflater {
    /**
     * Metadata key for defining an app's default navigation graph.
     *
     * <p>Applications may declare a graph resource in their manifest instead of declaring
     * or passing this data to each host or controller:</p>
     *
     * <pre class="prettyprint">
     *     <meta-data android:name="android.nav.graph" android:resource="@xml/my_nav_graph" />
     * </pre>
     *
     * <p>A graph resource declared in this manner can be inflated into a controller by calling
     * {@link NavController#setMetadataGraph()} or directly via {@link #inflateMetadataGraph()}.
     * Navigation host implementations should do this automatically
     * if no navigation resource is otherwise supplied during host configuration.</p>
     */
    public static final String METADATA_KEY_GRAPH = "android.nav.graph";

    private static final String TAG_ARGUMENT = "argument";
    private static final String TAG_DEEP_LINK = "deepLink";
    private static final String TAG_ACTION = "action";
    private static final String TAG_INCLUDE = "include";
    private static final String APPLICATION_ID_PLACEHOLDER = "${applicationId}";

    private static final ThreadLocal<TypedValue> sTmpValue = new ThreadLocal<>();

    private Context mContext;
    private NavigatorProvider mNavigatorProvider;

    public NavInflater(@NonNull Context c, @NonNull NavigatorProvider navigatorProvider) {
        mContext = c;
        mNavigatorProvider = navigatorProvider;
    }

    /**
     * Inflates {@link NavGraph navigation graph} as specified in the application manifest.
     *
     * <p>Applications may declare a graph resource in their manifest instead of declaring
     * or passing this data to each host or controller:</p>
     *
     * <pre class="prettyprint">
     *     <meta-data android:name="android.nav.graph" android:resource="@xml/my_nav_graph" />
     * </pre>
     *
     * @see #METADATA_KEY_GRAPH
     */
    @Nullable
    public NavGraph inflateMetadataGraph() {
        final Bundle metaData = mContext.getApplicationInfo().metaData;
        if (metaData != null) {
            final int resid = metaData.getInt(METADATA_KEY_GRAPH);
            if (resid != 0) {
                return inflate(resid);
            }
        }
        return null;
    }
    /**
     * Inflate a NavGraph from the given XML resource id.
     *
     * @param graphResId
     * @return
     */
    @SuppressLint("ResourceType")
    public NavGraph inflate(@NavigationRes int graphResId) {
        Resources res = mContext.getResources();
        XmlResourceParser parser = res.getXml(graphResId);
        final AttributeSet attrs = Xml.asAttributeSet(parser);
        try {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                // Empty loop
            }
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            String rootElement = parser.getName();
            NavDestination destination = inflate(res, parser, attrs);
            if (!(destination instanceof NavGraph)) {
                throw new IllegalArgumentException("Root element <" + rootElement + ">"
                        + " did not inflate into a NavGraph");
            }
            return (NavGraph) destination;
        } catch (Exception e) {
            throw new RuntimeException("Exception inflating "
                    + res.getResourceName(graphResId) + " line "
                    + parser.getLineNumber(), e);
        } finally {
            parser.close();
        }
    }

    private NavDestination inflate(Resources res, XmlResourceParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        Navigator navigator = mNavigatorProvider.getNavigator(parser.getName());
        final NavDestination dest = navigator.createDestination();

        dest.onInflate(mContext, attrs);

        final int innerDepth = parser.getDepth() + 1;
        int type;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth
                || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth) {
                continue;
            }

            final String name = parser.getName();
            if (TAG_ARGUMENT.equals(name)) {
                inflateArgument(res, dest, attrs);
            } else if (TAG_DEEP_LINK.equals(name)) {
                inflateDeepLink(res, dest, attrs);
            } else if (TAG_ACTION.equals(name)) {
                inflateAction(res, dest, attrs);
            } else if (TAG_INCLUDE.equals(name) && dest instanceof NavGraph) {
                final TypedArray a = res.obtainAttributes(attrs, R.styleable.NavInclude);
                final int id = a.getResourceId(R.styleable.NavInclude_graph, 0);
                ((NavGraph) dest).addDestination(inflate(id));
                a.recycle();
            } else if (dest instanceof NavGraph) {
                ((NavGraph) dest).addDestination(inflate(res, parser, attrs));
            }
        }

        return dest;
    }

    private void inflateArgument(Resources res, NavDestination dest, AttributeSet attrs)
            throws XmlPullParserException {
        final TypedArray a = res.obtainAttributes(attrs, R.styleable.NavArgument);
        String name = a.getString(R.styleable.NavArgument_android_name);

        TypedValue value = sTmpValue.get();
        if (value == null) {
            value = new TypedValue();
            sTmpValue.set(value);
        }
        if (a.getValue(R.styleable.NavArgument_android_defaultValue, value)) {
            switch (value.type) {
                case TypedValue.TYPE_STRING:
                    dest.getDefaultArguments().putString(name, value.string.toString());
                    break;
                case TypedValue.TYPE_DIMENSION:
                    dest.getDefaultArguments().putInt(name,
                            (int) value.getDimension(res.getDisplayMetrics()));
                    break;
                case TypedValue.TYPE_FLOAT:
                    dest.getDefaultArguments().putFloat(name, value.getFloat());
                    break;
                case TypedValue.TYPE_REFERENCE:
                    dest.getDefaultArguments().putInt(name, value.data);
                    break;
                default:
                    if (value.type >= TypedValue.TYPE_FIRST_INT
                            && value.type <= TypedValue.TYPE_LAST_INT) {
                        dest.getDefaultArguments().putInt(name, value.data);
                    } else {
                        throw new XmlPullParserException("unsupported argument type " + value.type);
                    }
            }
        }
        a.recycle();
    }

    private void inflateDeepLink(Resources res, NavDestination dest, AttributeSet attrs) {
        final TypedArray a = res.obtainAttributes(attrs, R.styleable.NavDeepLink);
        String uri = a.getString(R.styleable.NavDeepLink_uri);
        if (TextUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("Every <" + TAG_DEEP_LINK
                    + "> must include an app:uri");
        }
        uri = uri.replace(APPLICATION_ID_PLACEHOLDER, mContext.getPackageName());
        dest.addDeepLink(uri);
        a.recycle();
    }

    private void inflateAction(Resources res, NavDestination dest, AttributeSet attrs) {
        final TypedArray a = res.obtainAttributes(attrs, R.styleable.NavAction);
        final int id = a.getResourceId(R.styleable.NavAction_android_id, 0);
        final int destId = a.getResourceId(R.styleable.NavAction_destination, 0);
        NavAction action = new NavAction(destId);

        NavOptions.Builder builder = new NavOptions.Builder();
        builder.setLaunchSingleTop(a.getBoolean(R.styleable.NavAction_launchSingleTop, false));
        builder.setLaunchDocument(a.getBoolean(R.styleable.NavAction_launchDocument, false));
        builder.setClearTask(a.getBoolean(R.styleable.NavAction_clearTask, false));
        builder.setPopUpTo(a.getResourceId(R.styleable.NavAction_popUpTo, 0),
                a.getBoolean(R.styleable.NavAction_popUpToInclusive, false));
        builder.setEnterAnim(a.getResourceId(R.styleable.NavAction_enterAnim, -1));
        builder.setExitAnim(a.getResourceId(R.styleable.NavAction_exitAnim, -1));
        builder.setPopEnterAnim(a.getResourceId(R.styleable.NavAction_popEnterAnim, -1));
        builder.setPopExitAnim(a.getResourceId(R.styleable.NavAction_popExitAnim, -1));
        action.setNavOptions(builder.build());

        dest.putAction(id, action);
        a.recycle();
    }
}