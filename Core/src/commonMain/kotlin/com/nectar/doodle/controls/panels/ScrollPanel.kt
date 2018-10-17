package com.nectar.doodle.controls.panels

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer
import kotlin.math.max
import kotlin.math.min


interface ScrollPanelRenderer: Renderer<ScrollPanel> {
    var onScroll: ((Point) -> Unit)?

    fun scrollTo(point: Point)
}

open class ScrollPanel(content: Gizmo? = null): Gizmo() {

    var content = null as Gizmo?
        set(new) {
            if (new == this) {
                throw IllegalArgumentException("ScrollPanel cannot be added to its self")
            }

            field?.let {
                children -= it
            }

            if (field != new) {
                field = new

                field?.let {
                    children += it
                }
            }
        }

    var scroll = Origin
        private set

    var renderer: ScrollPanelRenderer? = null
        set(new) {
            field?.onScroll = null

            field = new?.also { it.onScroll = {
                scrollTo(it, force = true)
            } }
        }

    init {
        this.content = content
        layout       = ViewLayout()
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)

    /**
     * Scrolls the viewport so the top-left is at point, or as close as possible.
     *
     * @param point
     */
    fun scrollTo(point: Point) {
        renderer?.scrollTo(point)
    }

    /**
     * Scrolls the viewport by point.x and and point.y in the x and y directions respectively.
     *
     * @param delta
     */
    fun scrollBy(delta: Point) = scrollTo(scroll + delta)

    /**
     * Scrolls the viewport so the given point is visible.
     *
     * @param point
     */
    fun scrollToVisible(point: Point) = moveToVisible(point)

    /**
     * Scrolls the viewport so the given rectangle is visible.
     *
     * @param rect
     */
    fun scrollToVisible(rect: Rectangle) = moveToVisible(rect)

    protected fun moveToVisible(point: Point) {
        var farSide = scroll.x + width

        val x = when {
            point.x > farSide -> point.x - farSide
            else              -> point.x - scroll.x
        }

        farSide = scroll.y + height

        val y = when {
            point.y > farSide -> point.y - farSide
            else              -> point.y - scroll.y
        }

        scrollBy(Point(x, y))
    }

    protected fun moveToVisible(rect: Rectangle) {
        moveToVisible(rect.position)
        moveToVisible(Point(rect.x + rect.width, rect.y + rect.height))
    }

    private fun scrollTo(point: Point, force: Boolean = false) {
        content?.let {
            if (scroll != point) {
                val newScroll = when (force) {
                    true  -> point
                    false -> Point(max(0.0, min(point.x, it.width - width)), max(0.0, min(point.y, it.height - height)))
                }

                scroll      =  newScroll
                it.position = -newScroll
            }
        }
    }

    private inner class ViewLayout: Layout() {
        override fun layout(positionable: Positionable) {
            positionable.children.forEach  {
                var width  = it.width
                var height = it.height

                it.idealSize?.let {
                    width  = it.width
                    height = it.height
                }

                it.bounds = Rectangle(-scroll.x, -scroll.y, width, height)
            }
        }

        override val usesChildIdealSize = true
    }
}