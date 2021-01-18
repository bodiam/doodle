package io.nacular.doodle.theme.basic.list

import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListBehavior.RowGenerator
import io.nacular.doodle.controls.list.ListBehavior.RowPositioner
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.PatternFill
import io.nacular.doodle.drawing.horizontalStripedFill
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.theme.basic.ListPositioner
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.theme.basic.SelectableListKeyHandler

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

open class BasicItemGenerator<T>(private val selectionColor       : Color? = null,
                                 private val selectionBlurredColor: Color? = null): RowGenerator<T> {

    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?): View = when (current) {
        is ListRow<*> -> (current as ListRow<T>).apply { update(list, row, index) }
        else          -> ListRow(
                list                            = list,
                row                             = row,
                index                           = index,
                itemVisualizer                  = list.itemVisualizer ?: toString(TextVisualizer()),
                backgroundSelectionColor        = selectionColor,
                backgroundSelectionBlurredColor = selectionBlurredColor
        )
    }.apply {
        list.cellAlignment?.let { positioner = it }
    }
}

fun <T> basicItemGenerator(
        selectionColor       : Color? = null,
        selectionBlurredColor: Color? = null,
        configure            : ListRow<T>.() -> Unit
): RowGenerator<T> = object: BasicItemGenerator<T>(selectionColor, selectionBlurredColor) {
    override fun invoke(list: List<T, *>, row: T, index: Int, current: View?) = super.invoke(list, row, index, current).apply {
        if (current != this) configure(this as ListRow<T>)
    }
}

private class BasicListPositioner<T>(height: Double, spacing: Double = 0.0): ListPositioner(height, spacing), RowPositioner<T> {
    override fun row(of: List<T, *>, atY: Double) = super.rowFor(of.insets, atY)

    override fun totalRowHeight(of: List<T, *>) = super.totalHeight(of.numRows, of.insets)

    override fun rowBounds(of: List<T, *>, row: T, index: Int, view: View?) = super.rowBounds(of.width, of.insets, index, view)
}

open class BasicListBehavior<T>(private  val focusManager: FocusManager? = null,
                                override val generator   : RowGenerator<T>,
                                private  val patternFill : PatternFill? = null,
                                             rowHeight   : Double): ListBehavior<T>, KeyListener, PointerListener, SelectableListKeyHandler {
    override val positioner: RowPositioner<T> = BasicListPositioner(rowHeight)

    override fun install(view: List<T, *>) {
        view.keyChanged    += this
        view.pointerFilter += this

        view.rerender()
    }

    override fun uninstall(view: List<T, *>) {
        view.keyChanged    -= this
        view.pointerFilter -= this
    }

    override fun render(view: List<T, *>, canvas: Canvas) {
        patternFill?.let { canvas.rect(view.bounds.atOrigin, it) }
    }

    override fun pressed(event: KeyEvent) {
        super<SelectableListKeyHandler>.pressed(event)
    }

    override fun pressed(event: PointerEvent) {
        focusManager?.requestFocus(event.source)
    }

    companion object {
        operator fun <T> invoke(
                focusManager: FocusManager?,
                generator   : RowGenerator<T>,
                evenRowColor: Color?,
                oddRowColor : Color?,
                rowHeight   : Double) = BasicListBehavior(
                    focusManager,
                    generator,
                    when {
                        evenRowColor != null || oddRowColor != null -> horizontalStripedFill(rowHeight, evenRowColor, oddRowColor)
                        else                                        -> null
                    },
                    rowHeight)

        operator fun <T> invoke(
                focusManager         : FocusManager? = null,
                evenRowColor         : Color?        = null,
                oddRowColor          : Color?        = null,
                selectionColor       : Color?        = null,
                selectionBlurredColor: Color?        = null,
                rowHeight            : Double) = BasicListBehavior<T>(focusManager, BasicItemGenerator(selectionColor, selectionBlurredColor), evenRowColor, oddRowColor, rowHeight)
    }
}