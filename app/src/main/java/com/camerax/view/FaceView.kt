package com.camerax.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.shapes.PathShape
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.camerax.R

/**
 * TODO: document your custom view class.
 */
class FaceView : View {
    private val path: Path = Path()
    private var mOvalStart: Float = 0f
    private var mOvalBottom: Float = 0f
    private var mOvalEnd: Float = 0f
    private var mOvalTop: Float = 0f
    private var _exampleString: String? = context.getString(R.string.default_string)
    private var _exampleColor: Int = context.getColor(R.color.colorPrimary)
    private var _exampleDimension: Float = 0f

    private lateinit var textPaint: Paint
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    /**
     * The text to draw
     */
    private var exampleString: String?
        get() = _exampleString
        set(value) {
            _exampleString = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * The font color
     */
    private var exampleColor: Int
        get() = _exampleColor
        set(value) {
            _exampleColor = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this dimension is the font size.
     */
    private var exampleDimension: Float
        get() = _exampleDimension
        set(value) {
            _exampleDimension = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.FaceView, defStyle, 0
        )

        val text = a.getString(
            R.styleable.FaceView_exampleString
        )

        _exampleString = text ?: _exampleString

        _exampleColor = a.getColor(
            R.styleable.FaceView_exampleColor,
            exampleColor
        )
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _exampleDimension = a.getDimension(
            R.styleable.FaceView_exampleDimension,
            exampleDimension
        )

        if (a.hasValue(R.styleable.FaceView_exampleDrawable)) {
            exampleDrawable = a.getDrawable(
                R.styleable.FaceView_exampleDrawable
            )
            exampleDrawable?.callback = this
        }

        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun setFaceOvalPoint() {
        mOvalStart = (width / 3.0f)
        mOvalEnd = (width / 3.0f) * 2
        mOvalTop = (height / 6.0f)
        mOvalBottom = (height / 2.0f)
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint.let {
            it.textSize = exampleDimension
            it.color = exampleColor
            textWidth = it.measureText(exampleString)
            textHeight = it.fontMetrics.bottom
        }
    }

    override fun onDraw(canvas: Canvas) {

        //super.onDraw(canvas)
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        setFaceOvalPoint()


        path.addOval(mOvalStart, mOvalTop, mOvalEnd, mOvalBottom, Path.Direction.CCW)


        exampleString?.let {
            // Draw the text.
            canvas.drawText(
                it,
                paddingLeft + (contentWidth - textWidth) / 2,
                paddingTop + (contentHeight + textHeight) / 2,
                textPaint
            )
        }

        // Draw the example drawable on top of the text.
        exampleDrawable?.let {
            it.setBounds(
                paddingLeft, paddingTop,
                paddingLeft + contentWidth, paddingTop + contentHeight
            )
            it.draw(canvas)
        }

        canvas.clipPath(path)
        canvas.drawColor(Color.GRAY)
    }
}
