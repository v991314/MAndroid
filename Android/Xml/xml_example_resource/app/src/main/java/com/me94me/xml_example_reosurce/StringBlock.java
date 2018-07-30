
package com.me94me.xml_example_reosurce;

import android.graphics.Color;
import android.text.*;
import android.text.style.*;
import android.util.Log;
import android.util.SparseArray;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Arrays;

/**
 * Conveniences for retrieving data out of a compiled string resource.
 *
 * {@hide}
 */
final class StringBlock {
    private static final String TAG = "AssetManager";
    private static final boolean localLOGV = false;

    private final long mNative;
    private final boolean mUseSparse;
    private final boolean mOwnsNative;
    private CharSequence[] mStrings;
    private SparseArray<CharSequence> mSparseStrings;
    StyleIDs mStyleIDs = null;

    public StringBlock(byte[] data, boolean useSparse) {
        mNative = nativeCreate(data, 0, data.length);
        mUseSparse = useSparse;
        mOwnsNative = true;
        if (localLOGV) Log.v(TAG, "Created string block " + this
                + ": " + nativeGetSize(mNative));
    }

    public StringBlock(byte[] data, int offset, int size, boolean useSparse) {
        mNative = nativeCreate(data, offset, size);
        mUseSparse = useSparse;
        mOwnsNative = true;
        if (localLOGV) Log.v(TAG, "Created string block " + this
                + ": " + nativeGetSize(mNative));
    }

    /**
     * 通过序列号去获取string的值
     */
    public CharSequence get(int idx) {
        synchronized (this) {
            //mStrings缓存
            if (mStrings != null) {
                CharSequence res = mStrings[idx];
                if (res != null) {
                    return res;
                }
                //mSparseStrings缓存
            } else if (mSparseStrings != null) {
                CharSequence res = mSparseStrings.get(idx);
                if (res != null) {
                    return res;
                }
            } else {
                //构造num数量的容器
                final int num = nativeGetSize(mNative);
                //由构造方法判断mUseSparse并且数量要大于250才使用SparseArray
                //否则使用String数组
                if (mUseSparse && num > 250) {
                    mSparseStrings = new SparseArray<CharSequence>();
                } else {
                    mStrings = new CharSequence[num];
                }
            }
            //通过index通过native code获取想要的string
            String str = nativeGetString(mNative, idx);
            CharSequence res = str;
            //native code 返回的数据
            //style数组是<type，start，end>的连续数组
            int[] style = nativeGetStyle(mNative, idx);
            if (localLOGV) Log.v(TAG, "Got string: " + str);
            if (localLOGV) Log.v(TAG, "Got styles: " + Arrays.toString(style));
            if (style != null) {
                if (mStyleIDs == null) {
                    mStyleIDs = new StyleIDs();
                }
                //以3个<type,start,end>为一组的数据组
                //比如 type1,start1,end1,type2,start2,end2,type3...
                for (int styleIndex = 0; styleIndex < style.length; styleIndex += 3) {
                    //3个连续值中的第一个:type
                    int styleId = style[styleIndex];

                    //mStyleIDs里的值默认为-1，若不为-1并等于styleId，即已找到
                    if (styleId == mStyleIDs.boldId
                            || styleId == mStyleIDs.italicId
                            || styleId == mStyleIDs.underlineId
                            || styleId == mStyleIDs.ttId
                            || styleId == mStyleIDs.bigId
                            || styleId == mStyleIDs.smallId
                            || styleId == mStyleIDs.subId
                            || styleId == mStyleIDs.supId
                            || styleId == mStyleIDs.strikeId
                            || styleId == mStyleIDs.listItemId
                            || styleId == mStyleIDs.marqueeId) {
                        // id已找到跳到下一个style
                        continue;
                    }
                    //获取styleId的tag
                    String styleTag = nativeGetString(mNative, styleId);

                    if (styleTag.equals("b")) {
                        mStyleIDs.boldId = styleId;
                    } else if (styleTag.equals("i")) {
                        mStyleIDs.italicId = styleId;
                    } else if (styleTag.equals("u")) {
                        mStyleIDs.underlineId = styleId;
                    } else if (styleTag.equals("tt")) {
                        mStyleIDs.ttId = styleId;
                    } else if (styleTag.equals("big")) {
                        mStyleIDs.bigId = styleId;
                    } else if (styleTag.equals("small")) {
                        mStyleIDs.smallId = styleId;
                    } else if (styleTag.equals("sup")) {
                        mStyleIDs.supId = styleId;
                    } else if (styleTag.equals("sub")) {
                        mStyleIDs.subId = styleId;
                    } else if (styleTag.equals("strike")) {
                        mStyleIDs.strikeId = styleId;
                    } else if (styleTag.equals("li")) {
                        mStyleIDs.listItemId = styleId;
                    } else if (styleTag.equals("marquee")) {
                        mStyleIDs.marqueeId = styleId;
                    }
                }
                //获取字符串
                res = applyStyles(str, style, mStyleIDs);
            }

            //缓存到mStrings中
            if (mStrings != null) mStrings[idx] = res;
            //缓存到SparseArray中
            else mSparseStrings.put(idx, res);
            return res;
        }
    }

    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            if (mOwnsNative) {
                nativeDestroy(mNative);
            }
        }
    }

    static final class StyleIDs {
        private int boldId = -1;
        private int italicId = -1;
        private int underlineId = -1;
        private int ttId = -1;
        private int bigId = -1;
        private int smallId = -1;
        private int subId = -1;
        private int supId = -1;
        private int strikeId = -1;
        private int listItemId = -1;
        private int marqueeId = -1;
    }

    /**
     * t
     * @param str ypeValue的Strig
     * @param style 以3个为单位的连续数组
     * @param ids StyleIds
     * @return
     */
    private CharSequence applyStyles(String str, int[] style, StyleIDs ids) {
        if (style.length == 0)
            return str;

        SpannableString buffer = new SpannableString(str);
        int i=0;
        while (i < style.length) {
            int type = style[i];
            if (localLOGV) Log.v(TAG, "Applying style span id=" + type
                    + ", start=" + style[i+1] + ", end=" + style[i+2]);


            if (type == ids.boldId) {
                buffer.setSpan(new StyleSpan(Typeface.BOLD),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.italicId) {
                buffer.setSpan(new StyleSpan(Typeface.ITALIC),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.underlineId) {
                buffer.setSpan(new UnderlineSpan(),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.ttId) {
                buffer.setSpan(new TypefaceSpan("monospace"),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.bigId) {
                buffer.setSpan(new RelativeSizeSpan(1.25f),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.smallId) {
                buffer.setSpan(new RelativeSizeSpan(0.8f),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.subId) {
                buffer.setSpan(new SubscriptSpan(),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.supId) {
                buffer.setSpan(new SuperscriptSpan(),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.strikeId) {
                buffer.setSpan(new StrikethroughSpan(),
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ids.listItemId) {
                addParagraphSpan(buffer, new BulletSpan(10),
                                style[i+1], style[i+2]+1);
            } else if (type == ids.marqueeId) {
                buffer.setSpan(TextUtils.TruncateAt.MARQUEE,
                               style[i+1], style[i+2]+1,
                               Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                String tag = nativeGetString(mNative, type);

                if (tag.startsWith("font;")) {
                    String sub;

                    sub = subtag(tag, ";height=");
                    if (sub != null) {
                        int size = Integer.parseInt(sub);
                        addParagraphSpan(buffer, new Height(size),
                                       style[i+1], style[i+2]+1);
                    }

                    sub = subtag(tag, ";size=");
                    if (sub != null) {
                        int size = Integer.parseInt(sub);
                        buffer.setSpan(new AbsoluteSizeSpan(size, true),
                                       style[i+1], style[i+2]+1,
                                       Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    sub = subtag(tag, ";fgcolor=");
                    if (sub != null) {
                        buffer.setSpan(getColor(sub, true),
                                       style[i+1], style[i+2]+1,
                                       Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    sub = subtag(tag, ";color=");
                    if (sub != null) {
                        buffer.setSpan(getColor(sub, true),
                                style[i+1], style[i+2]+1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    sub = subtag(tag, ";bgcolor=");
                    if (sub != null) {
                        buffer.setSpan(getColor(sub, false),
                                       style[i+1], style[i+2]+1,
                                       Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    sub = subtag(tag, ";face=");
                    if (sub != null) {
                        buffer.setSpan(new TypefaceSpan(sub),
                                style[i+1], style[i+2]+1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else if (tag.startsWith("a;")) {
                    String sub;

                    sub = subtag(tag, ";href=");
                    if (sub != null) {
                        buffer.setSpan(new URLSpan(sub),
                                       style[i+1], style[i+2]+1,
                                       Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else if (tag.startsWith("annotation;")) {
                    int len = tag.length();
                    int next;

                    for (int t = tag.indexOf(';'); t < len; t = next) {
                        int eq = tag.indexOf('=', t);
                        if (eq < 0) {
                            break;
                        }

                        next = tag.indexOf(';', eq);
                        if (next < 0) {
                            next = len;
                        }

                        String key = tag.substring(t + 1, eq);
                        String value = tag.substring(eq + 1, next);

                        buffer.setSpan(new Annotation(key, value),
                                       style[i+1], style[i+2]+1,
                                       Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            i += 3;
        }
        return new SpannedString(buffer);
    }

    /**
     * Returns a span for the specified color string representation.
     * If the specified string does not represent a color (null, empty, etc.)
     * the color black is returned instead.
     *
     * @param color The color as a string. Can be a resource reference,
     *              hexadecimal, octal or a name
     * @param foreground True if the color will be used as the foreground color,
     *                   false otherwise
     *
     * @return A CharacterStyle
     *
     * @see Color#parseColor(String)
     */
    private static CharacterStyle getColor(String color, boolean foreground) {
        int c = 0xff000000;

        if (!TextUtils.isEmpty(color)) {
            if (color.startsWith("@")) {
                Resources res = Resources.getSystem();
                String name = color.substring(1);
                int colorRes = res.getIdentifier(name, "color", "android");
                if (colorRes != 0) {
                    ColorStateList colors = res.getColorStateList(colorRes, null);
                    if (foreground) {
                        return new TextAppearanceSpan(null, 0, 0, colors, null);
                    } else {
                        c = colors.getDefaultColor();
                    }
                }
            } else {
                try {
                    c = Color.parseColor(color);
                } catch (IllegalArgumentException e) {
                    c = Color.BLACK;
                }
            }
        }

        if (foreground) {
            return new ForegroundColorSpan(c);
        } else {
            return new BackgroundColorSpan(c);
        }
    }

    /**
     * If a translator has messed up the edges of paragraph-level markup,
     * fix it to actually cover the entire paragraph that it is attached to
     * instead of just whatever range they put it on.
     */
    private static void addParagraphSpan(Spannable buffer, Object what,
                                         int start, int end) {
        int len = buffer.length();

        if (start != 0 && start != len && buffer.charAt(start - 1) != '\n') {
            for (start--; start > 0; start--) {
                if (buffer.charAt(start - 1) == '\n') {
                    break;
                }
            }
        }

        if (end != 0 && end != len && buffer.charAt(end - 1) != '\n') {
            for (end++; end < len; end++) {
                if (buffer.charAt(end - 1) == '\n') {
                    break;
                }
            }
        }

        buffer.setSpan(what, start, end, Spannable.SPAN_PARAGRAPH);
    }

    private static String subtag(String full, String attribute) {
        int start = full.indexOf(attribute);
        if (start < 0) {
            return null;
        }

        start += attribute.length();
        int end = full.indexOf(';', start);

        if (end < 0) {
            return full.substring(start);
        } else {
            return full.substring(start, end);
        }
    }

    /**
     * Forces the text line to be the specified height, shrinking/stretching
     * the ascent if possible, or the descent if shrinking the ascent further
     * will make the text unreadable.
     */
    private static class Height implements LineHeightSpan.WithDensity {
        private int mSize;
        private static float sProportion = 0;

        public Height(int size) {
            mSize = size;
        }

        public void chooseHeight(CharSequence text, int start, int end,
                                 int spanstartv, int v,
                                 Paint.FontMetricsInt fm) {
            // Should not get called, at least not by StaticLayout.
            chooseHeight(text, start, end, spanstartv, v, fm, null);
        }

        public void chooseHeight(CharSequence text, int start, int end,
                                 int spanstartv, int v,
                                 Paint.FontMetricsInt fm, TextPaint paint) {
            int size = mSize;
            if (paint != null) {
                size *= paint.density;
            }

            if (fm.bottom - fm.top < size) {
                fm.top = fm.bottom - size;
                fm.ascent = fm.ascent - size;
            } else {
                if (sProportion == 0) {
                    /*
                     * Calculate what fraction of the nominal ascent
                     * the height of a capital letter actually is,
                     * so that we won't reduce the ascent to less than
                     * that unless we absolutely have to.
                     */

                    Paint p = new Paint();
                    p.setTextSize(100);
                    Rect r = new Rect();
                    p.getTextBounds("ABCDEFG", 0, 7, r);

                    sProportion = (r.top) / p.ascent();
                }

                int need = (int) Math.ceil(-fm.top * sProportion);

                if (size - fm.descent >= need) {
                    /*
                     * It is safe to shrink the ascent this much.
                     */

                    fm.top = fm.bottom - size;
                    fm.ascent = fm.descent - size;
                } else if (size >= need) {
                    /*
                     * We can't show all the descent, but we can at least
                     * show all the ascent.
                     */

                    fm.top = fm.ascent = -need;
                    fm.bottom = fm.descent = fm.top + size;
                } else {
                    /*
                     * Show as much of the ascent as we can, and no descent.
                     */

                    fm.top = fm.ascent = -size;
                    fm.bottom = fm.descent = 0;
                }
            }
        }
    }

    /**
     * Create from an existing string block native object.  This is
     * -extremely- dangerous -- only use it if you absolutely know what you
     *  are doing!  The given native object must exist for the entire lifetime
     *  of this newly creating StringBlock.
     */
    StringBlock(long obj, boolean useSparse) {
        mNative = obj;
        mUseSparse = useSparse;
        mOwnsNative = false;
        if (localLOGV) Log.v(TAG, "Created string block " + this
                + ": " + nativeGetSize(mNative));
    }

    private static native long nativeCreate(byte[] data,
                                                 int offset,
                                                 int size);
    private static native int nativeGetSize(long obj);
    private static native String nativeGetString(long obj, int idx);
    private static native int[] nativeGetStyle(long obj, int idx);
    private static native void nativeDestroy(long obj);
}
