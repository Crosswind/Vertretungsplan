package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

// this class lets the layout move up when snackbar is shown
public class FloatingActionBehavior extends CoordinatorLayout.Behavior<LinearLayout> {
    public FloatingActionBehavior() {
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull LinearLayout child, @NonNull View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;


    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, LinearLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}
