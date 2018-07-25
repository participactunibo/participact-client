/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by danielecampogiani on 27/01/15.
 */
public class ViewUtils {

    private static final int FAB_ANIMATION_DURATION = 300;
    private static final int FAB_Y_TRANSLATION = 20;
    private static final int ERROR_ANIMATION_DURATION = 300;
    private static final int TOOLBAR_ANIMATION_DURATION = 100;

    public static void toggleFab(final View fab, boolean show) {

        if (fab == null)
            throw new NullPointerException("fab can't be null");

        if (show) {
            fab.setScaleX(0);
            fab.setScaleY(0);
            fab.setY(fab.getY() + FAB_Y_TRANSLATION);
            fab.setVisibility(View.VISIBLE);
            ViewPropertyAnimator.animate(fab).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(FAB_ANIMATION_DURATION).translationY(-FAB_Y_TRANSLATION).scaleX(1).scaleY(1).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fab.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else if (fab.getVisibility() == View.VISIBLE) {
            ViewPropertyAnimator.animate(fab).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(FAB_ANIMATION_DURATION).translationY(FAB_Y_TRANSLATION).scaleX(0).scaleY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fab.setY(fab.getY() - FAB_Y_TRANSLATION);
                    fab.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    public static void toggleError(final TextView textView, String error, boolean show) {

        if (textView == null)
            throw new NullPointerException("textView can't be null");

        if (error == null && show)
            throw new NullPointerException("error can't be null");
        if ("".equals(error) && show)
            throw new IllegalArgumentException("text can't be empty");

        if (show) {
            textView.setAlpha(0);
            textView.setText(error);
            textView.setVisibility(View.VISIBLE);
            ViewPropertyAnimator.animate(textView).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ERROR_ANIMATION_DURATION).alpha(1).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    textView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        } else if (textView.getVisibility() == View.VISIBLE) {
            ViewPropertyAnimator.animate(textView).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ERROR_ANIMATION_DURATION).alpha(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    textView.setVisibility(View.INVISIBLE);
                    textView.setText("");
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

    }

    public static void toggleAlpha(final View view, boolean show) {

        if (view == null)
            throw new NullPointerException("view can't be null");

        if (show) {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
            ViewPropertyAnimator.animate(view).setInterpolator(new AccelerateDecelerateInterpolator()).alpha(1).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        } else if (view.getVisibility() == View.VISIBLE) {
            ViewPropertyAnimator.animate(view).setInterpolator(new AccelerateDecelerateInterpolator()).alpha(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

    }
}
