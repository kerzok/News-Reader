package ru.nsmelik.newsreader.pulltorefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.nsmelik.newsreader.R;

/**
 * Created by Smelik Nick.
 */
public class HeaderTransformer {
    public static final int PROGRESS_BAR_STYLE_INSIDE = 0;
    public static final int PROGRESS_BAR_STYLE_OUTSIDE = 1;

    private View mHeaderView;
    private ViewGroup mContentLayout;
    private TextView mHeaderTextView;

    private CharSequence mPullRefreshLabel;
    private CharSequence mRefreshingLabel;

    private int mProgressDrawableColor;

    private long mAnimationDuration;
    private int mProgressBarStyle;
    private int mProgressBarHeight = RelativeLayout.LayoutParams.WRAP_CONTENT;

    public HeaderTransformer() {
        final int min = getMinimumApiLevel();
        if (Build.VERSION.SDK_INT < min) {
            throw new IllegalStateException("This HeaderTransformer is designed to run on SDK "
                    + min
                    + "+. If using ActionBarSherlock or ActionBarCompat you should use the appropriate provided extra.");
        }
    }

    public void onViewCreated(Activity activity, View headerView) {
        mHeaderView = headerView;

        // Get ProgressBar and TextView
        mHeaderTextView = (TextView) headerView.findViewById(R.id.ptr_text);
        mContentLayout = (ViewGroup) headerView.findViewById(R.id.ptr_content);

        // Default Labels to display
        mPullRefreshLabel = activity.getString(R.string.pull_to_refresh);
        mRefreshingLabel = activity.getString(R.string.refreshing);

        mAnimationDuration = activity.getResources()
                .getInteger(android.R.integer.config_shortAnimTime);

        // Setup the View styles
        setupViewsFromStyles(activity, headerView);

        applyProgressBarStyle();

        // Apply any custom ProgressBar colors and corner radius
        applyProgressBarSettings();

        // FIXME: I do not like this call here
        onReset();
    }


    public void onReset() {

        // Reset Text View
        if (mHeaderTextView != null) {
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderTextView.setText(mPullRefreshLabel);
        }

        // Reset the Content Layout
        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onRefreshStarted() {
        if (mHeaderTextView != null) {
            mHeaderTextView.setText(mRefreshingLabel);
        }
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public boolean showHeaderView() {
        final boolean changeVis = mHeaderView.getVisibility() != View.VISIBLE;

        if (changeVis) {
            mHeaderView.setVisibility(View.VISIBLE);
            AnimatorSet animSet = new AnimatorSet();
            ObjectAnimator transAnim = ObjectAnimator.ofFloat(mContentLayout, "translationY",
                    -mContentLayout.getHeight(), 0f);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mHeaderView, "alpha", 0f, 1f);
            animSet.playTogether(transAnim, alphaAnim);
            animSet.setDuration(mAnimationDuration);
            animSet.start();
        }

        return changeVis;
    }

    public boolean hideHeaderView() {
        final boolean changeVis = mHeaderView.getVisibility() != View.GONE;

        if (changeVis) {
            Animator animator;
            if (mContentLayout.getAlpha() >= 0.5f) {
                // If the content layout is showing, translate and fade out
                animator = new AnimatorSet();
                ObjectAnimator transAnim = ObjectAnimator.ofFloat(mContentLayout, "translationY",
                        0f, -mContentLayout.getHeight());
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mHeaderView, "alpha", 1f, 0f);
                ((AnimatorSet) animator).playTogether(transAnim, alphaAnim);
            } else {
                // If the content layout isn't showing (minimized), just fade out
                animator = ObjectAnimator.ofFloat(mHeaderView, "alpha", 1f, 0f);
            }
            animator.setDuration(mAnimationDuration);
            animator.addListener(new HideAnimationCallback());
            animator.start();
        }

        return changeVis;
    }

    private void setupViewsFromStyles(Activity activity, View headerView) {
        final TypedArray styleAttrs = obtainStyledAttrsFromThemeAttr(activity,
                R.attr.ptrHeaderStyle, R.styleable.PullToRefreshHeader);

        // Retrieve the Action Bar size from the app theme or the Action Bar's style
        if (mContentLayout != null) {
            mContentLayout.getLayoutParams().height = styleAttrs.getDimensionPixelSize(
                    R.styleable.PullToRefreshHeader_ptrHeaderHeight, getActionBarSize(activity));
            mContentLayout.requestLayout();
        }

        // Retrieve the Action Bar background from the app theme or the Action Bar's style (see #93)
        Drawable bg = styleAttrs.hasValue(R.styleable.PullToRefreshHeader_ptrHeaderBackground)
                ? styleAttrs.getDrawable(R.styleable.PullToRefreshHeader_ptrHeaderBackground)
                : getActionBarBackground(activity);
        if (bg != null) {
            mHeaderTextView.setBackground(bg);

            // If we have an opaque background we can remove the background from the content layout
            if (mContentLayout != null && bg.getOpacity() == PixelFormat.OPAQUE) {
                mContentLayout.setBackgroundResource(0);
            }
        }

        // Retrieve the Action Bar Title Style from the app theme or the Action Bar's style
        Context abContext = headerView.getContext();
        final int titleTextStyle = styleAttrs
                .getResourceId(R.styleable.PullToRefreshHeader_ptrHeaderTitleTextAppearance,
                        getActionBarTitleStyle(abContext));
        if (titleTextStyle != 0) {
            mHeaderTextView.setTextAppearance(abContext, titleTextStyle);
        }

        // Retrieve the Progress Bar Color the style
        if (styleAttrs.hasValue(R.styleable.PullToRefreshHeader_ptrProgressBarColor)) {
            mProgressDrawableColor = styleAttrs.getColor(
                    R.styleable.PullToRefreshHeader_ptrProgressBarColor, mProgressDrawableColor);
        }

        mProgressBarStyle = styleAttrs.getInt(
                R.styleable.PullToRefreshHeader_ptrProgressBarStyle, PROGRESS_BAR_STYLE_OUTSIDE);

        if (styleAttrs.hasValue(R.styleable.PullToRefreshHeader_ptrProgressBarHeight)) {
            mProgressBarHeight = styleAttrs.getDimensionPixelSize(
                    R.styleable.PullToRefreshHeader_ptrProgressBarHeight, mProgressBarHeight);
        }

        // Retrieve the text strings from the style (if they're set)
        if (styleAttrs.hasValue(R.styleable.PullToRefreshHeader_ptrPullText)) {
            mPullRefreshLabel = styleAttrs.getString(R.styleable.PullToRefreshHeader_ptrPullText);
        }
        if (styleAttrs.hasValue(R.styleable.PullToRefreshHeader_ptrRefreshingText)) {
            mRefreshingLabel = styleAttrs
                    .getString(R.styleable.PullToRefreshHeader_ptrRefreshingText);
        }
        if (styleAttrs.hasValue(R.styleable.PullToRefreshHeader_ptrReleaseText)) {
            CharSequence mReleaseLabel = styleAttrs.getString(R.styleable.PullToRefreshHeader_ptrReleaseText);
        }

        styleAttrs.recycle();
    }

    private void applyProgressBarStyle() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, mProgressBarHeight);

        switch (mProgressBarStyle) {
            case PROGRESS_BAR_STYLE_INSIDE:
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.ptr_content);
                break;
            case PROGRESS_BAR_STYLE_OUTSIDE:
                lp.addRule(RelativeLayout.BELOW, R.id.ptr_content);
                break;
        }

    }

    private void applyProgressBarSettings() {
    }

    protected Drawable getActionBarBackground(Context context) {
        int[] android_styleable_ActionBar = {android.R.attr.background};

        // Now get the action bar style values...
        TypedArray abStyle = obtainStyledAttrsFromThemeAttr(context, android.R.attr.actionBarStyle,
                android_styleable_ActionBar);
        try {
            // background is the first attr in the array above so it's index is 0.
            return abStyle.getDrawable(0);
        } finally {
            abStyle.recycle();
        }
    }

    protected int getActionBarSize(Context context) {
        int[] attrs = {android.R.attr.actionBarSize};
        TypedArray values = context.getTheme().obtainStyledAttributes(attrs);
        try {
            return values.getDimensionPixelSize(0, 0);
        } finally {
            values.recycle();
        }
    }

    protected int getActionBarTitleStyle(Context context) {
        int[] android_styleable_ActionBar = {android.R.attr.titleTextStyle};

        // Now get the action bar style values...
        TypedArray abStyle = obtainStyledAttrsFromThemeAttr(context, android.R.attr.actionBarStyle,
                android_styleable_ActionBar);
        try {
            // titleTextStyle is the first attr in the array above so it's index is 0.
            return abStyle.getResourceId(0, 0);
        } finally {
            abStyle.recycle();
        }
    }

    protected int getMinimumApiLevel() {
        return Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    class HideAnimationCallback extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            View headerView = getHeaderView();
            if (headerView != null) {
                headerView.setVisibility(View.GONE);
            }
            onReset();
        }
    }

    protected static TypedArray obtainStyledAttrsFromThemeAttr(Context context, int themeAttr,
                                                               int[] styleAttrs) {
        // Need to get resource id of style pointed to from the theme attr
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(themeAttr, outValue, true);
        final int styleResId = outValue.resourceId;

        // Now return the values (from styleAttrs) from the style
        return context.obtainStyledAttributes(styleResId, styleAttrs);
    }
}
