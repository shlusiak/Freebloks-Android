package de.saschahlusiak.freebloks.game

import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView

import android.view.Gravity.BOTTOM
import android.view.Gravity.LEFT
import de.saschahlusiak.freebloks.R

class FloatingMenuLabel(
    activity: Activity,
    private val container: ViewGroup,
    private val anchor: View,
    private val gravity: Int
) : View.OnLayoutChangeListener {

    private val view: View
    private var isShown: Boolean = false

    init {
        this.isShown = false

        view = activity.layoutInflater.inflate(R.layout.floating_menu_item, container, false)
        view.visibility = View.INVISIBLE
        view.left = 200

        anchor.addOnLayoutChangeListener(this)

        val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        container.addView(view, lp)

        view.setOnClickListener { anchor.callOnClick() }
    }

    fun setText(text: String) {
        view.findViewById<TextView>(R.id.text).apply {
            this.text = text
        }
    }

    fun show() {
        if (isShown) {
            return
        }

        isShown = true

        anchor.requestLayout()
        view.apply {
            alpha = 0.0f
            visibility = View.VISIBLE
        }
    }

    private fun dpToPx(dp: Float) = dp * view.resources.displayMetrics.density
    private fun Resources.getLong(id: Int) = getInteger(id).toLong()

    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (!isShown) {
            return
        }

        val rect = Rect()
        anchor.getDrawingRect(rect)
        container.offsetDescendantRectToMyCoords(anchor, rect)

        val resources = v.resources

        val anchorX = rect.left - container.paddingLeft
        val anchorY = rect.top - container.paddingTop
        val anchorW = anchor.width
        val anchorH = anchor.height
        val labelW = view.width
        val labelH = view.height

        when (gravity) {
            BOTTOM -> {
                val targetX = anchorX + (anchorW - labelW) / 2.0f
                view.translationX = if (targetX > 0.0f) targetX else 0.0f
                view.translationY = (anchorY + anchorH - dpToPx(12.0f))
            }
            LEFT -> {
                view.translationX = (anchorX - labelW).toFloat()
                view.translationY = anchorY + (anchorH - labelH) / 2.0f
            }

            else -> {
                view.translationX = (anchorX - labelW).toFloat()
                view.translationY = anchorY + (anchorH - labelH) / 2.0f
            }
        }

        view.postOnAnimation {
            if (isShown) {
                val dx = if (gravity == LEFT) dpToPx(-12.0f) else 0.0f
                val dy = if (gravity == BOTTOM) dpToPx(12.0f) else 0.0f

                view.apply {
                    alpha = 0.0f
                    visibility = View.VISIBLE
                    animate()
                        .setDuration(resources.getLong(android.R.integer.config_longAnimTime))
                        .setStartDelay(1000)
                        .alpha(0.85f)
                        .translationXBy(dx)
                        .translationYBy(dy)
                        .start()
                }
            }
        }
    }

    fun hide() {
        if (isShown) {
            view.visibility = View.INVISIBLE
            isShown = false
        }
    }
}
