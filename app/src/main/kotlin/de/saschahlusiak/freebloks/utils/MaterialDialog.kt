package de.saschahlusiak.freebloks.utils

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import com.google.android.material.R
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.theme.overlay.MaterialThemeOverlay

fun Dialog.applyMaterialBackground() {
    val context = context
    val decorView = window?.decorView

    if (decorView != null) {
        val backgroundInsets = MaterialDialog.getDialogBackgroundInsets(context, MaterialDialog.DEF_STYLE_ATTR, MaterialDialog.DEF_STYLE_RES)

        val surfaceColor = MaterialDialog.getColor(context, R.attr.colorSurface, "colorSurface not found")
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
        decorView.setOnTouchListener(InsetDialogOnTouchListener(this, backgroundInsets))
    }
}

internal class InsetDialogOnTouchListener(private val dialog: Dialog, insets: Rect) : View.OnTouchListener {
    private val leftInset: Int
    private val topInset: Int
    private val prePieSlop: Int
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val insetView = view.findViewById<View>(android.R.id.content)
        val insetLeft = leftInset + insetView.left
        val insetRight = insetLeft + insetView.width
        val insetTop = topInset + insetView.top
        val insetBottom = insetTop + insetView.height
        val dialogWindow = RectF(insetLeft.toFloat(), insetTop.toFloat(), insetRight.toFloat(), insetBottom.toFloat())
        if (dialogWindow.contains(event.x, event.y)) {
            return false
        }
        val outsideEvent = MotionEvent.obtain(event)
        outsideEvent.action = MotionEvent.ACTION_OUTSIDE
        // Window.shouldCloseOnTouch does not respect MotionEvent.ACTION_OUTSIDE until Pie, so we fix
// the coordinates outside the view and use MotionEvent.ACTION_DOWN
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            outsideEvent.action = MotionEvent.ACTION_DOWN
            outsideEvent.setLocation(-prePieSlop - 1.toFloat(), -prePieSlop - 1.toFloat())
        }
        view.performClick()
        return dialog.onTouchEvent(outsideEvent)
    }

    init {
        leftInset = insets.left
        topInset = insets.top
        prePieSlop = ViewConfiguration.get(dialog.context).scaledWindowTouchSlop
    }
}

open class MaterialDialog : AppCompatDialog {

    constructor(context: Context, overrideThemeResId: Int) : super(createMaterialThemedContext(context), overrideThemeResId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyMaterialBackground()
    }

    companion object {

        @AttrRes
        val DEF_STYLE_ATTR = R.attr.alertDialogStyle
        @StyleRes
        val DEF_STYLE_RES = R.style.MaterialAlertDialog_MaterialComponents
        @AttrRes
        val MATERIAL_ALERT_DIALOG_THEME_OVERLAY = R.attr.materialAlertDialogTheme

        private fun getMaterialAlertDialogThemeOverlay(context: Context): Int {
            val materialAlertDialogThemeOverlay = resolve(context, MATERIAL_ALERT_DIALOG_THEME_OVERLAY)
                ?: return 0
            return materialAlertDialogThemeOverlay.data
        }

        private fun createMaterialThemedContext(context: Context): Context {
            val themeOverlayId = getMaterialAlertDialogThemeOverlay(context)
            val themedContext = MaterialThemeOverlay.wrap(context, null, DEF_STYLE_ATTR, DEF_STYLE_RES)
            return if (themeOverlayId == 0) {
                themedContext
            } else ContextThemeWrapper(themedContext, themeOverlayId)
        }

        /**
         * Returns the color int for the provided theme color attribute.
         *
         * @throws IllegalArgumentException if the attribute is not set in the current theme.
         */
        @ColorInt
        fun getColor(context: Context, @AttrRes colorAttributeResId: Int, errorMessageComponent: String): Int {
            return resolveOrThrow(context, colorAttributeResId, errorMessageComponent)
        }

        fun insetDrawable(drawable: Drawable?, backgroundInsets: Rect): InsetDrawable {
            return InsetDrawable(
                drawable,
                backgroundInsets.left,
                backgroundInsets.top,
                backgroundInsets.right,
                backgroundInsets.bottom)
        }

        /**
         * Returns the [TypedValue] for the provided `attributeResId` or null if the attribute
         * is not present in the current theme.
         */
        fun resolve(context: Context, @AttrRes attributeResId: Int): TypedValue? {
            val typedValue = TypedValue()
            return if (context.theme.resolveAttribute(attributeResId, typedValue, true)) {
                typedValue
            } else null
        }

        /**
         * Returns the [TypedValue] for the provided `attributeResId`.
         *
         * @throws IllegalArgumentException if the attribute is not present in the current theme.
         */
        fun resolveOrThrow(
            context: Context,
            @AttrRes attributeResId: Int,
            errorMessageComponent: String): Int {
            val typedValue = resolve(context, attributeResId)
            if (typedValue == null) {
                val errorMessage = ("%1\$s requires a value for the %2\$s attribute to be set in your app theme. "
                    + "You can either set the attribute in your theme or "
                    + "update your theme to inherit from Theme.MaterialComponents (or a descendant).")
                throw IllegalArgumentException(String.format(
                    errorMessage,
                    errorMessageComponent,
                    context.resources.getResourceName(attributeResId)))
            }
            return typedValue.data
        }

        /**
         * Returns the [TypedValue] for the provided `attributeResId`, using the context of
         * the provided `componentView`.
         *
         * @throws IllegalArgumentException if the attribute is not present in the current theme.
         */
        fun resolveOrThrow(componentView: View, @AttrRes attributeResId: Int): Int {
            return resolveOrThrow(
                componentView.context, attributeResId, componentView.javaClass.canonicalName!!)
        }

        fun getDialogBackgroundInsets(
            context: Context, @AttrRes defaultStyleAttribute: Int, defaultStyleResource: Int): Rect {
            val attributes = context.obtainStyledAttributes(
                null,
                R.styleable.MaterialAlertDialog,
                defaultStyleAttribute,
                defaultStyleResource)
            val backgroundInsetStart = attributes.getDimensionPixelSize(
                R.styleable.MaterialAlertDialog_backgroundInsetStart,
                context
                    .resources
                    .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_start))
            val backgroundInsetTop = attributes.getDimensionPixelSize(
                R.styleable.MaterialAlertDialog_backgroundInsetTop,
                context
                    .resources
                    .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_top))
            val backgroundInsetEnd = attributes.getDimensionPixelSize(
                R.styleable.MaterialAlertDialog_backgroundInsetEnd,
                context
                    .resources
                    .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_end))
            val backgroundInsetBottom = attributes.getDimensionPixelSize(
                R.styleable.MaterialAlertDialog_backgroundInsetBottom,
                context
                    .resources
                    .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_bottom))
            attributes.recycle()
            var backgroundInsetLeft = backgroundInsetStart
            var backgroundInsetRight = backgroundInsetEnd
            val layoutDirection = context.resources.configuration.layoutDirection
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                backgroundInsetLeft = backgroundInsetEnd
                backgroundInsetRight = backgroundInsetStart
            }
            return Rect(
                backgroundInsetLeft, backgroundInsetTop, backgroundInsetRight, backgroundInsetBottom)
        }
    }
}