package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import androidx.core.view.ViewCompat
import com.google.android.material.shape.MaterialShapeDrawable

/**
 * Call this function when the activity is shown as a dialog and the background is black and not gray.
 */
fun Activity.applyMaterialBackground() {
    val context = this
    val decorView = window?.decorView

    if (decorView != null) {
        val backgroundInsets = MaterialDialog.getDialogBackgroundInsets(context, MaterialDialog.DEF_STYLE_ATTR, MaterialDialog.DEF_STYLE_RES)

        val surfaceColor = MaterialDialog.getColor(context, com.google.android.material.R.attr.colorSurface, "colorSurface not found")
        val materialShapeDrawable = MaterialShapeDrawable(context, null, MaterialDialog.DEF_STYLE_ATTR, MaterialDialog.DEF_STYLE_RES)
        materialShapeDrawable.initializeElevationOverlay(context)
        materialShapeDrawable.fillColor = ColorStateList.valueOf(surfaceColor)

        val theme = context.theme

        // dialogCornerRadius first appeared in Android Pie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val dialogCornerRadiusValue = TypedValue()
            theme.resolveAttribute(android.R.attr.dialogCornerRadius, dialogCornerRadiusValue, true)
            val dialogCornerRadius = dialogCornerRadiusValue.getDimension(context.resources.displayMetrics)
            if (dialogCornerRadiusValue.type == TypedValue.TYPE_DIMENSION && dialogCornerRadius >= 0) {
                materialShapeDrawable.setCornerSize(dialogCornerRadius)
            }
        }

        materialShapeDrawable.elevation = ViewCompat.getElevation(decorView)

        val insetDrawable: Drawable = MaterialDialog.insetDrawable(materialShapeDrawable, backgroundInsets)
        window?.setBackgroundDrawable(insetDrawable)
    }
}
