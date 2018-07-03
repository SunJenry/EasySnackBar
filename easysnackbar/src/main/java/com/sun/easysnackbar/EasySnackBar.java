package com.sun.easysnackbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @author sun on 2018/6/27.
 */

/**
 * Snackbars provide lightweight feedback about an operation. They show a brief message at the
 * bottom of the screen on mobile and lower left on larger devices. Snackbars appear above all other
 * elements on screen and only one can be displayed at a time.
 * <p>
 * They automatically disappear after a timeout or after user interaction elsewhere on the screen,
 * particularly after interactions that summon a new surface or activity. Snackbars can be swiped
 * off screen.
 * <p>
 * Snackbars can contain an action which is set via
 * {@link #setAction(CharSequence, View.OnClickListener)}.
 * <p>
 * To be notified when a snackbar has been shown or dismissed, you can provide a {@link Callback}
 * via {@link BaseTransientBar#addCallback(BaseCallback)}.</p>
 */
public final class EasySnackBar extends BaseTransientBar<EasySnackBar> {

    /**
     * Show the Snackbar indefinitely. This means that the Snackbar will be displayed from the time
     * that is {@link #show() shown} until either it is dismissed, or another Snackbar is shown.
     *
     * @see #setDuration
     */
    public static final int LENGTH_INDEFINITE = BaseTransientBar.LENGTH_INDEFINITE;

    /**
     * Show the Snackbar for a short period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = BaseTransientBar.LENGTH_SHORT;

    /**
     * Show the Snackbar for a long period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = BaseTransientBar.LENGTH_LONG;

    /**
     * Callback class for {@link EasySnackBar} instances.
     * <p>
     * Note: this class is here to provide backwards-compatible way for apps written before
     * the existence of the base {@link BaseTransientBar} class.
     *
     * @see BaseTransientBar#addCallback(BaseCallback)
     */
    public static class Callback extends BaseCallback<EasySnackBar> {
        /**
         * Indicates that the Snackbar was dismissed via a swipe.
         */
        public static final int DISMISS_EVENT_SWIPE = BaseCallback.DISMISS_EVENT_SWIPE;
        /**
         * Indicates that the Snackbar was dismissed via an action click.
         */
        public static final int DISMISS_EVENT_ACTION = BaseCallback.DISMISS_EVENT_ACTION;
        /**
         * Indicates that the Snackbar was dismissed via a timeout.
         */
        public static final int DISMISS_EVENT_TIMEOUT = BaseCallback.DISMISS_EVENT_TIMEOUT;
        /**
         * Indicates that the Snackbar was dismissed via a call to {@link #dismiss()}.
         */
        public static final int DISMISS_EVENT_MANUAL = BaseCallback.DISMISS_EVENT_MANUAL;
        /**
         * Indicates that the Snackbar was dismissed from a new Snackbar being shown.
         */
        public static final int DISMISS_EVENT_CONSECUTIVE = BaseCallback.DISMISS_EVENT_CONSECUTIVE;

        @Override
        public void onShown(EasySnackBar sb) {
            // Stub implementation to make API check happy.
        }

        @Override
        public void onDismissed(EasySnackBar transientBottomBar, @DismissEvent int event) {
            // Stub implementation to make API check happy.
        }
    }

    @Nullable
    private BaseCallback<EasySnackBar> mCallback;

    private EasySnackBar(ViewGroup parent, View content, ContentViewCallback contentViewCallback, boolean top) {
        super(parent, content, contentViewCallback,top);
    }

    /**
     * Make a Snackbar to display a message
     * <p>
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param layout   The layout to show.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static EasySnackBar make(@NonNull View view, @LayoutRes int layout,
                                    @Duration int duration,boolean top) {

        View inflate = convertToContentView(view, layout);
        return make(view, inflate, duration,top);
    }

    /**
     * Make a Snackbar to display a message
     * <p>
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view        The view to find a parent from.
     * @param contentView The content view to show.
     * @param duration    How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                    #LENGTH_LONG}
     */
    @NonNull
    public static EasySnackBar make(@NonNull View view, @NonNull View contentView,
                                    @Duration int duration, boolean top) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final CustomContentLayout content =
                (CustomContentLayout) inflater.inflate(
                        R.layout.layout_custom_snack_bar, parent, false);

        content.addView(contentView);
        final EasySnackBar snackBar = new EasySnackBar(parent, content, content,top);
        snackBar.setDuration(duration);
        return snackBar;
    }

    /**
     * @param view   The view to find a parent from.
     * @param layout The layout to show.
     * @return The view to display in the easy snack bar
     */
    public static View convertToContentView(@NonNull View view, @LayoutRes int layout) {
        final ViewGroup parent = findSuitableParent(view);

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View inflate = null;
        try {
            inflate = inflater.inflate(layout, parent, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (inflate == null) {
            inflate = inflater.inflate(R.layout.layout_custom, parent, false);
        }

        return inflate;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    /**
     * Set the action to be displayed in this {@link BaseTransientBar}.
     *
     * @param resId    String resource to display for the action
     * @param listener callback to be invoked when the action is clicked
     */
    @NonNull
    public EasySnackBar setAction(@StringRes int resId, View.OnClickListener listener) {
        return setAction(getContext().getText(resId), listener);
    }

    /**
     * Set the action to be displayed in this {@link BaseTransientBar}.
     *
     * @param text     Text to display for the action
     * @param listener callback to be invoked when the action is clicked
     */
    @NonNull
    public EasySnackBar setAction(CharSequence text, final View.OnClickListener listener) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();

        if (TextUtils.isEmpty(text) || listener == null) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    // Now dismiss the Snackbar
                    dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public EasySnackBar setActionTextColor(ColorStateList colors) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(colors);
        return this;
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public EasySnackBar setActionTextColor(@ColorInt int color) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(color);
        return this;
    }

    /**
     * Set a callback to be called when this the visibility of this {@link EasySnackBar}
     * changes. Note that this method is deprecated
     * and you should use {@link #addCallback(BaseCallback)} to add a callback and
     * {@link #removeCallback(BaseCallback)} to remove a registered callback.
     *
     * @param callback Callback to notify when transient bottom bar events occur.
     * @see Callback
     * @see #addCallback(BaseCallback)
     * @see #removeCallback(BaseCallback)
     * @deprecated Use {@link #addCallback(BaseCallback)}
     */
    @Deprecated
    @NonNull
    public EasySnackBar setCallback(Callback callback) {
        // The logic in this method emulates what we had before support for multiple
        // registered callbacks.
        if (mCallback != null) {
            removeCallback(mCallback);
        }
        if (callback != null) {
            addCallback(callback);
        }
        // Update the deprecated field so that we can remove the passed callback the next
        // time we're called
        mCallback = callback;
        return this;
    }

    /**
     * @hide Note: this class is here to provide backwards-compatible way for apps written before
     * the existence of the base {@link BaseTransientBar} class.
     */
    @RestrictTo(LIBRARY_GROUP)
    public static final class SnackBarLayout extends BaseTransientBar.SnackbarBaseLayout {
        public SnackBarLayout(Context context) {
            super(context);
        }

        public SnackBarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // Work around our backwards-compatible refactoring of Snackbar and inner content
            // being inflated against snackbar's parent (instead of against the snackbar itself).
            // Every child that is width=MATCH_PARENT is remeasured again and given the full width
            // minus the paddings.
            int childCount = getChildCount();
            int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.measure(View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                                    View.MeasureSpec.EXACTLY));
                }
            }
        }
    }
}

