package org.jetbrains.letsPlot.android.canvas

import android.graphics.*
import android.graphics.Canvas
import org.jetbrains.letsPlot.android.canvas.Utils.toAndroidColor
import org.jetbrains.letsPlot.commons.geometry.AffineTransform
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.core.canvas.*

class AndroidContext2d(
    bitmap: Bitmap,
    pixelDensity: Double,
    private val stateDelegate: ContextStateDelegate = ContextStateDelegate(failIfNotImplemented = false, logEnabled = true),
) : Context2d by stateDelegate {
    private val nativeCanvas = Canvas(bitmap).apply {
        this.scale(pixelDensity.toFloat(), pixelDensity.toFloat())
    }

    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = 0xFFFFFFFF.toInt()
    }

    override fun save() {
        stateDelegate.save()
        nativeCanvas.save()
    }

    override fun restore() {
        stateDelegate.restore()
        nativeCanvas.restore()
    }

    override fun rotate(angle: Double) {
        stateDelegate.rotate(angle)
        nativeCanvas.rotate(angle.toFloat())
    }

    override fun translate(x: Double, y: Double) {
        stateDelegate.translate(x, y)
        nativeCanvas.translate(x.toFloat(), y.toFloat())
    }

    override fun transform(sx: Double, ry: Double, rx: Double, sy: Double, tx: Double, ty: Double) {
        stateDelegate.transform(sx = sx, ry = ry, rx = rx, sy = sy, tx = tx, ty = ty)
        nativeCanvas.concat(Matrix().apply {
            setValues(floatArrayOf(
                sx.toFloat(), rx.toFloat(), tx.toFloat(),
                ry.toFloat(), sy.toFloat(), ty.toFloat(),
                0f, 0f, 1f
            ))
        })
    }

    override fun scale(x: Double, y: Double) {
        stateDelegate.scale(x, y)
        nativeCanvas.scale(x.toFloat(), y.toFloat())
    }

    override fun scale(xy: Double) {
        stateDelegate.scale(xy)
        nativeCanvas.scale(xy.toFloat(), xy.toFloat())
    }

    override fun setTransform(m00: Double, m10: Double, m01: Double, m11: Double, m02: Double, m12: Double) {
        stateDelegate.setTransform(m00, m10, m01, m11, m02, m12)
        nativeCanvas.setMatrix(Matrix().apply {
            setValues(floatArrayOf(
                m00.toFloat(), m10.toFloat(), 0f,
                m01.toFloat(), m11.toFloat(), 0f,
                m02.toFloat(), m12.toFloat(), 1f
            ))
        })
    }

    override fun clearRect(rect: DoubleRectangle) {
        nativeCanvas.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), backgroundPaint)
    }

    override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
        nativeCanvas.drawRect(x.toFloat(), y.toFloat(), (x+w).toFloat(), (y+h).toFloat(), fillPaint)
    }

    override fun strokeRect(x: Double, y: Double, w: Double, h: Double) {
        nativeCanvas.drawRect(x.toFloat(), y.toFloat(), (x+w).toFloat(), (y+h).toFloat(), strokePaint)
    }

    override fun setFont(f: Font) {
        val style = when (f.fontStyle) {
            FontStyle.NORMAL -> when (f.fontWeight) {
                FontWeight.NORMAL -> Typeface.NORMAL
                FontWeight.BOLD -> Typeface.BOLD
            }
            FontStyle.ITALIC -> when (f.fontWeight) {
                FontWeight.NORMAL -> Typeface.ITALIC
                FontWeight.BOLD -> Typeface.BOLD_ITALIC
            }
        }

        val typeface = Typeface.create(f.fontFamily, style)

        fillPaint.typeface = typeface
        strokePaint.typeface = typeface
        fillPaint.textSize = f.fontSize.toFloat()
        strokePaint.textSize = f.fontSize.toFloat()
    }

    override fun fillText(text: String, x: Double, y: Double) {
        nativeCanvas.drawText(text, x.toFloat(), y.toFloat(), fillPaint)
    }

    override fun strokeText(text: String, x: Double, y: Double) {
        nativeCanvas.drawText(text, x.toFloat(), y.toFloat(), strokePaint)
    }

    override fun measureText(str: String): TextMetrics {
        val bounds = Rect()
        fillPaint.getTextBounds(str, 0, str.length, bounds)
        return TextMetrics(
            ascent = bounds.top.toDouble(),
            descent = bounds.bottom.toDouble(),
            bbox = DoubleRectangle.LTRB(
                left = bounds.left.toDouble(),
                top = bounds.top.toDouble(),
                right = bounds.right.toDouble(),
                bottom = bounds.bottom.toDouble()
            ),
        )
    }

    override fun setLineDash(lineDash: DoubleArray) {
        stateDelegate.setLineDash(lineDash)
        setupPathEffect()
    }

    override fun setLineDashOffset(lineDashOffset: Double) {
        stateDelegate.setLineDashOffset(lineDashOffset)
        setupPathEffect()
    }

    private fun setupPathEffect() {
        val lineDash = stateDelegate.getLineDash().map(Double::toFloat).toFloatArray()
        if (lineDash.isEmpty()) {
            strokePaint.pathEffect = null
            return
        }

        val phase = stateDelegate.getLineDashOffset().toFloat()

        strokePaint.pathEffect = DashPathEffect(lineDash, phase)
    }


    override fun stroke() {
        // Make ctm identity. null for degenerate case, e.g., scale(0, 0) - skip drawing.
        val inverseCtmTransform = stateDelegate.getCTM().inverse() ?: return

        nativeCanvas.drawPath(drawPath(stateDelegate.getCurrentPath(), inverseCtmTransform), strokePaint)
    }

    override fun fill() {
        // Make ctm identity. null for degenerate case, e.g., scale(0, 0) - skip drawing.
        val inverseCtmTransform = stateDelegate.getCTM().inverse() ?: return
        nativeCanvas.drawPath(drawPath(stateDelegate.getCurrentPath(), inverseCtmTransform), fillPaint)
    }

    override fun setFillStyle(color: Color?) {
        fillPaint.color = color?.toAndroidColor() ?: 0
    }

    override fun setStrokeStyle(color: Color?) {
        strokePaint.color = color?.toAndroidColor() ?: 0
    }

    override fun setLineWidth(lineWidth: Double) {
        strokePaint.strokeWidth = lineWidth.toFloat()
    }

    override fun setLineJoin(lineJoin: LineJoin) {
        strokePaint.strokeJoin = when (lineJoin) {
            LineJoin.BEVEL -> Paint.Join.BEVEL
            LineJoin.MITER -> Paint.Join.MITER
            LineJoin.ROUND -> Paint.Join.ROUND
        }
    }

    override fun setLineCap(lineCap: LineCap) {
        strokePaint.strokeCap = when (lineCap) {
            LineCap.BUTT -> Paint.Cap.BUTT
            LineCap.ROUND -> Paint.Cap.ROUND
            LineCap.SQUARE -> Paint.Cap.SQUARE
        }
    }

    override fun setStrokeMiterLimit(miterLimit: Double) {
        strokePaint.strokeMiter = miterLimit.toFloat()
    }

    override fun measureTextWidth(str: String): Double {
        val bounds = Rect()
        fillPaint.getTextBounds(str, 0, str.length, bounds)
        return bounds.width().toDouble()
    }

    override fun clip() {
        // Make ctm identity. null for degenerate case, e.g., scale(0, 0) - skip drawing.
        val inverseCtmTransform = stateDelegate.getCTM().inverse() ?: return

        val path = drawPath(stateDelegate.getCurrentPath(), inverseCtmTransform)
        nativeCanvas.clipPath(path)
    }

    private fun drawPath(commands: List<Path2d.PathCommand>, transform: AffineTransform): Path {
        if (commands.isEmpty()) {
            return Path()
        }

        val path = Path()

        commands
            .asSequence()
            .map { cmd -> cmd.transform(transform) }
            .forEach { cmd ->
                when (cmd) {
                    is Path2d.MoveTo -> path.moveTo(cmd.x.toFloat(), cmd.y.toFloat())
                    is Path2d.LineTo -> path.lineTo(cmd.x.toFloat(), cmd.y.toFloat())
                    is Path2d.CubicCurveTo -> {
                        cmd.controlPoints.asSequence()
                            .windowed(size = 3, step = 3)
                            .forEach { (cp1, cp2, cp3) ->
                                path.cubicTo(
                                    cp1.x.toFloat(), cp1.y.toFloat(),
                                    cp2.x.toFloat(), cp2.y.toFloat(),
                                    cp3.x.toFloat(), cp3.y.toFloat()
                                )
                            }
                    }

                    is Path2d.ClosePath -> path.close()
                }
            }

        return path
    }
}
