package com.nectar.doodle.controls.text

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.text.invoke
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.HorizontalAlignment.Right
import com.nectar.doodle.utils.VerticalAlignment
import com.nectar.doodle.utils.VerticalAlignment.Bottom
import com.nectar.doodle.utils.VerticalAlignment.Middle
import com.nectar.doodle.utils.VerticalAlignment.Top
import com.nectar.doodle.utils.observable


interface LabelFactory {
    operator fun invoke(
            styledText         : StyledText          = StyledText(""),
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center): Label

    operator fun invoke(
            text               : String,
            verticalAlignment  : VerticalAlignment   = Middle,
            horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center) = this(StyledText(text), verticalAlignment, horizontalAlignment)
}

class LabelFactoryImpl(private val textMetrics: TextMetrics): LabelFactory {
    override operator fun invoke(
            styledText         : StyledText,
            verticalAlignment  : VerticalAlignment,
            horizontalAlignment: HorizontalAlignment) = Label(textMetrics, styledText, verticalAlignment, horizontalAlignment)
}

open class Label internal constructor(
        private val textMetrics        : TextMetrics,
                    styledText         : StyledText          = StyledText(""),
                    verticalAlignment  : VerticalAlignment   = Middle,
                    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center): Gizmo() {

    var fitText = true

    var text: String get() = styledText.text
        set(new) {
            styledText = StyledText(new, font, foregroundColor)
        }

    var styledText = font?.invoke { foregroundColor?.invoke { styledText } ?: styledText } ?: styledText
        set(new) {
            if (new == field) { return }

            field = font?.invoke { foregroundColor?.invoke { new } ?: new } ?: new
            measureText()

            rerender()
        }

    var wrapsWords = false
        set(new) {
            if (field != new) {
                field = new
                measureText()
            }
        }

    var verticalAlignment   by observable(verticalAlignment  ) { _,_,_ -> measureText(); rerender() }
    var horizontalAlignment by observable(horizontalAlignment) { _,_,_ -> measureText(); rerender() }

    private fun measureText(): Size {
        val height = when {
            fitText || verticalAlignment != Top -> if (wrapsWords) textMetrics.height(styledText, width) else textMetrics.height(styledText)
            else                                -> 0.0
        }

        val width = when {
            fitText || horizontalAlignment != Left -> if (wrapsWords) textMetrics.width(styledText, width) else textMetrics.width(styledText)
            else                                   -> 0.0
        }

        return Size(width, height).also { textSize = it }
    }

    private var textSize = Size.Empty
        set(new) {
            field             = new
            if (fitText) size = new
        }

    init {
        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                measureText()
            }
        }

        styleChanged += {
            text = text

            rerender()
        }

        size = textSize
    }

    override var focusable = false

    override fun addedToDisplay() {
        if (textSize.empty) {
            measureText()
        }
    }

    override fun render(canvas: Canvas) {
        val y = when (verticalAlignment) {
            Top    -> 0.0
            Middle -> (height - textSize.height) / 2
            Bottom ->  height - textSize.height
        }

        val x = when (horizontalAlignment) {
            Left                       -> 0.0
            HorizontalAlignment.Center -> (width - textSize.width) / 2
            Right                      ->  width - textSize.width
        }

        backgroundColor?.let {
            canvas.rect(bounds.atOrigin, ColorBrush(it))
        }

        if (wrapsWords) {
            canvas.wrapped(styledText, Point(x, y), 0.0, width)
        } else {
            canvas.text(styledText, Point(x, y))
        }
    }
}