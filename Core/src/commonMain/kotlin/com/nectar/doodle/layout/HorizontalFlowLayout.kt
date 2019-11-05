package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.HorizontalAlignment.Right
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 3/17/19.
 */
class HorizontalFlowLayout(private val justification    : HorizontalAlignment = Left,
                           private val verticalSpacing  : Double              = 1.0,
                           private val horizontalSpacing: Double              = 1.0): Layout() {


    override fun layout(container: PositionableContainer) {
        var y            = container.insets.top
        var height       = 0.0
        val itemList     = mutableListOf<Positionable>()
        var lineWidth    = 0.0
        val maxLineWidth = container.width - container.insets.left - container.insets.right

        container.children.filter { it.visible }.forEach { child ->
            child.idealSize?.let { child.size = it }

            val temp = lineWidth + child.width + if (itemList.isNotEmpty()) horizontalSpacing else 0.0

            if (temp > maxLineWidth) {
                layoutLine(itemList, container, lineWidth, y)

                itemList.clear()

                lineWidth = child.width

                if (height > 0) {
                    y += height + verticalSpacing
                }

                height = child.height
            } else {
                lineWidth = temp
            }

            itemList.add(child)

            height = max(height, child.height)
        }

        if (itemList.isNotEmpty()) {
            layoutLine(itemList, container, lineWidth, y)
        }
    }

    private fun layoutLine(itemList: List<Positionable>, parent: PositionableContainer, lineWidth: Double, y: Double) {
        var startX = when (justification) {
            Right  ->  parent.width - lineWidth - parent.insets.right
            Center -> (parent.width - lineWidth) / 2
            Left   ->  parent.insets.left
        }

        itemList.forEach {
            it.position = Point(startX, y)

            startX += it.width + horizontalSpacing
        }
    }
}
