package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.ConvexPolygon
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.SquareMatrix
import com.nectar.doodle.utils.matrixOf
import com.nectar.doodle.utils.squareMatrixOf
import com.nectar.doodle.utils.times
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.radians
import kotlin.math.cos
import kotlin.math.sin

@Suppress("ReplaceSingleLineLet")
class AffineTransform private constructor(private val matrix: SquareMatrix<Double>) {

    /**
     * Creates a transform with the given properties
     *
     * @param scaleX     how much to scale the x direction
     * @param shearX     how much to shear the x direction
     * @param translateX how much to translate in the x direction
     * @param scaleY     how much to scale the y direction
     * @param shearY     how much to shear the y direction
     * @param translateY how much to translate in the y direction
     */
    constructor(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0):
            this(listOf(
                listOf(scaleX, shearX, translateX),
                listOf(shearY, scaleY, translateY),
                listOf(   0.0,    0.0,        1.0)
            ).let { squareMatrixOf(3) { col, row -> it[row][col] } })

    val isIdentity       = matrix.isIdentity
    val translateX get() = matrix[0, 2]
    val translateY get() = matrix[1, 2]
    val scaleX     get() = matrix[0, 0]
    val scaleY     get() = matrix[1, 1]
    val shearX     get() = matrix[0, 1]
    val shearY     get() = matrix[1, 0]

    operator fun times(other: AffineTransform) = AffineTransform(matrix * other.matrix)

    fun scale(x: Double = 1.0, y: Double = x) = AffineTransform(
            matrix * listOf(this[  x, 0.0, 0.0],
                            this[0.0,   y, 0.0],
                            this[0.0, 0.0, 1.0]).let { squareMatrixOf(3) { col, row -> it[row][col] } })

    fun translate(by: Point) = translate(by.x, by.y)

    fun translate(x: Double = 0.0, y: Double = 0.0) = AffineTransform(
            matrix * listOf(this[1.0, 0.0,   x],
                            this[0.0, 1.0,   y],
                            this[0.0, 0.0, 1.0]).let { squareMatrixOf(3) { col, row -> it[row][col] } })

    fun skew(x: Double, y: Double) = AffineTransform(
            matrix * listOf(this[1.0,   x, 0.0],
                            this[  y, 1.0, 0.0],
                            this[0.0, 0.0, 1.0]).let { squareMatrixOf(3) { col, row -> it[row][col] } })

    fun rotate(angle: Measure<Angle>): AffineTransform {
        val radians = angle `in` radians
        val sin     = sin(radians)
        val cos     = cos(radians)

        return AffineTransform(
                matrix * listOf(this[cos, -sin, 0.0],
                                this[sin,  cos, 0.0],
                                this[0.0,  0.0, 1.0]).let { squareMatrixOf(3) { col, row -> it[row][col] } })
    }

    fun flipVertically  () = scale (1.0, -1.0)
    fun flipHorizontally() = scale(-1.0,  1.0)

    val inverse: AffineTransform? by lazy {
        when {
            isIdentity -> this
            else       -> matrix.inverse?.let { AffineTransform(it) }
        }
    }

    operator fun invoke(point: Point) = this(listOf(point)).first()

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     */
    operator fun invoke(points: List<Point>) = points.map {
        val point   = listOf(listOf(it.x), listOf(it.y), listOf(1.0)).let { matrixOf(3, 1) { col, row -> it[row][col] } }
        val product = matrix * point

        Point(product[0, 0], product[1, 0])
    }

    operator fun invoke(rectangle: Rectangle): ConvexPolygon = when {
        isIdentity -> rectangle
        else       -> this(rectangle.points).let { ConvexPolygon(it[0], it[1], it[2], it[3]) }
    }

    override fun toString() = matrix.toString()

    override fun hashCode(): Int = matrix.hashCode()

    private operator fun get(vararg values: Double) = values.toList()
    override fun equals(other: Any?): Boolean {
        if (this === other           ) return true
        if (other !is AffineTransform) return false

        if (matrix != other.matrix) return false

        return true
    }

    companion object {
        val Identity = AffineTransform()
    }
}
