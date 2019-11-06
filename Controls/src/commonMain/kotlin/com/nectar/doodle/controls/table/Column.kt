package com.nectar.doodle.controls.table

import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints

interface Column<T> {
    val header         : View?
    val width          : Double

    var minWidth       : Double
    var maxWidth       : Double?
    var preferredWidth : Double?
    var cellAlignment  : (Constraints.() -> Unit)?
    var headerAlignment: (Constraints.() -> Unit)?

    fun moveBy(x: Double)
    fun resetPosition()
}