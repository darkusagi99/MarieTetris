package com.gmail.darkusagi99.marietetris

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import kotlin.random.Random


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    var NUM_ROWS = 22
    var NUM_COLUMNS = 16

    var GAME_ROWS = 20
    var GAME_COLUMNS = 10
    var START_COL_DELTA = 8
    var NEW_PIECE_COL = 13
    val BOARD_HEIGHT = 1280
    val BOARD_WIDTH = 720
    var SPEED_DOWN : Long = 500

    lateinit var bitmap: Bitmap
    lateinit var canvas: Canvas
    lateinit var paint: Paint
    lateinit var linearLayout: LinearLayout
    var gameMatrix = Array(GAME_ROWS) {IntArray(GAME_COLUMNS)}
    var a = Array(4) { Point() }
    var b = Array(4) { Point() }
    var c = Array(4) { Point() }
    var gameInProgress = false
    var gameInPause = false
    var currentPiece = 0
    var next = 0
    var colorNext : Int = 0
    var currentColor : Int = 0
    var scoreVal = 0

    private var gestureDetector: GestureDetectorCompat? = null
    val handler = Handler()

    val figures = arrayOf(
    arrayOf(1, 3, 5, 7), // I
    arrayOf(2, 4, 5, 7), // Z
    arrayOf(3, 5, 4, 6), // S
    arrayOf(3, 5, 4, 7), // T
    arrayOf(2, 3, 5, 7), // L
    arrayOf(3, 5, 7, 6), // J
    arrayOf(2, 3, 4, 5) // O
    )

    val scoreArray = arrayOf(40, 100, 300, 1200)

    private val colorList = arrayOf(
        Color.BLACK,
        Color.rgb(224, 255, 255),
        Color.rgb(220, 20, 60),
        Color.rgb(0, 250, 154),
        Color.rgb(186, 85, 211),
        Color.rgb(255, 165, 0),
        Color.rgb(65, 105, 225),
        Color.rgb(255, 255, 0))

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {

    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        paint = Paint()
        linearLayout = findViewById<View>(R.id.game_board) as LinearLayout

        mVisible = true

        this.gestureDetector = GestureDetectorCompat(this, this)
        gestureDetector?.setOnDoubleTapListener(this)

        // Init game
        GameInit()

        // Paint the initial matrix (frontend)
        DrawScreen()

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

    fun check(): Boolean {
        for (i in 0..3) {
            if (a[i].y < 0) {
                return false
            }
            if (a[i].x < 1 || a[i].x >= (GAME_COLUMNS + 1) || a[i].y >= GAME_ROWS) {
                return false
            }
            else if (gameMatrix[a[i].y][a[i].x-1] > 0) {
                return false
            }
        }
        return true
    }

    fun checkLines() {

        var k = GAME_ROWS - 1
        var nbLignes = 0
        for (i in GAME_ROWS - 1 downTo 1) {
            var count = 0
            for (j in 0 until GAME_COLUMNS) {
                if (gameMatrix[i][j] > 0f) {
                    count++
                }
                gameMatrix[k][j] = gameMatrix[i][j]
            }
            if (count < GAME_COLUMNS) {
                k--
            } else {
                nbLignes++
            }
        }

        if (nbLignes > 0) {
            scoreVal += scoreArray.get(nbLignes - 1)
        }

    }

    fun initNewPiece() {

        // Init new piece
        next = Random.nextInt(0, 7)
        colorNext = next + 1
        for (i in 0..3) {
            c[i].x = figures[next][i] % 2 + NEW_PIECE_COL
            c[i].y = figures[next][i] / 2
        }
    }

    fun getNextPiece() {

        currentPiece=next
        currentColor=colorNext
        for (i in 0..3)
        {
            a[i].x = c[i].x - START_COL_DELTA
            a[i].y = c[i].y

        }
        if (check() == false) {
            gameInProgress = false
        }

        initNewPiece()

    }

    fun GameInit() {

        // Create the game board (backend)
        for (i in 0 until GAME_ROWS) {
            for (j in 0 until GAME_COLUMNS) {
                gameMatrix[i][j] = 0
            }
        }

        // RAZ du score
        scoreVal = 0

        // Initialisation du premier bloc
        initNewPiece()
        getNextPiece()

        // Start the game
        gameInProgress = true
        gameInPause = false
        // Paint the initial matrix (frontend)
        DrawScreen()
        // Set a timer
        handler.postDelayed(downRunnable, SPEED_DOWN)
    }

    val downRunnable = object: Runnable {
        override fun run() {
            if (gameInProgress == true) {
                moveDown()
                DrawScreen()
                handler.postDelayed(this, SPEED_DOWN)
            }
        }
    }


    fun DrawScreen() { // Paint the game board background
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, BOARD_WIDTH.toFloat(), BOARD_HEIGHT.toFloat(), paint)

        val colWidth = (BOARD_WIDTH / (NUM_COLUMNS)).toFloat()
        val rowHeight = (BOARD_HEIGHT / (NUM_ROWS)).toFloat()

        paint.color = Color.LTGRAY
        canvas.drawRect(0f, 0f, colWidth, rowHeight*21, paint)
        canvas.drawRect(colWidth*11, 0f, colWidth*12, rowHeight*21, paint)
        canvas.drawRect(0f, rowHeight*20, colWidth*12, rowHeight*21, paint)

        // Paint the grid on the game board
        paint.color = Color.WHITE
        for (i in 0..NUM_ROWS -2) {
            canvas.drawLine(
                colWidth, i * rowHeight, colWidth*11,
                i * rowHeight, paint
            )
        }
        for (i in 1..NUM_COLUMNS - 5) {
            canvas.drawLine(
                i * colWidth, 0f,
                i * colWidth, rowHeight*20, paint
            )
        }

        // Paint the block already placed
        // Paint borders to the tetris blocks
        for (i in 0 until GAME_ROWS) {
            for (j in 0 until GAME_COLUMNS) {
                if (gameMatrix.get(i).get(j) != 0) {
                    paint.color = colorList[gameMatrix.get(i).get(j)]

                    canvas.drawRect((j+1)*colWidth, i*rowHeight, (j+2) * colWidth, (i+1)*rowHeight, paint)
                }
            }
        }


        // Paint current piece - not displayed in pause
        paint.color = colorList[currentColor]
        if ( !gameInPause) {
            for (i in 0..3) {
                canvas.drawRect(a[i].x*colWidth, a[i].y*rowHeight, (a[i].x+1)*colWidth, (a[i].y+1)*rowHeight, paint)

            }
        }

        // Paint next piece
        paint.color = colorList[colorNext]
        for (i in 0..3) {
            canvas.drawRect(c[i].x*colWidth, c[i].y*rowHeight, (c[i].x+1)*colWidth, (c[i].y+1)*rowHeight, paint)

        }

        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText(getString(R.string.score_text, scoreVal), 30f, rowHeight*22, paint)

        // Display the current painting
        linearLayout.setBackgroundDrawable(BitmapDrawable(bitmap))
    }

    override fun onShowPress(e: MotionEvent?) {
        // Ne rien faire
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {

        if (!gameInProgress) {
            return true
        }
        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getSize(size)
        val width = size.x.toFloat()
        val x = e!!.x
        if (x <= width / 2.0) { // rotate left
            rotatePiece()
            DrawScreen()
        } else { // rotate right
            rotatePiece()
            DrawScreen()
        }
        return true

    }

    fun rotatePiece() {

        // Save previous position
        for (i in 0..3) {
            b[i].x = a[i].x
            b[i].y = a[i].y
        }

        val p = a[1] //center of rotation

        // Rotation
        for (i in 0..3) {
            val x = a[i].y - p.y
            val y = a[i].x - p.x
            a[i].x = p.x - x
            a[i].y = p.y + y
        }

        checkRevert()

    }



    fun movePiece(dx : Int) {

        // Save position and move (Left - Right)
        for (i in 0..3) {
            b[i].x = a[i].x
            b[i].y = a[i].y
            a[i].x += dx
        }

        checkRevert()
    }

    fun moveDown() {

        if (gameInPause) { return }

        for (i in 0..3) {
            b[i].x = a[i].x
            b[i].y = a[i].y
            a[i].y += 1
        }

        if (check() == false) {
            for (i in 0..3) {
                gameMatrix[b[i].y][b[i].x-1] = currentColor
            }

            checkLines()

            getNextPiece()

            if (check() == false) {
                gameInProgress = false
            }
        }
    }



    fun checkRevert() {
        // Check OK ou retour arrière
        if (check() == false) {
            for (i in 0..3) {
                a[i].x = b[i].x
                a[i].y = b[i].y
            }
        }
    }


    override fun onDown(e: MotionEvent?): Boolean {
        // ne rien faire
        return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        if (!gameInProgress) return false

        try {
            val x1 = e1!!.x
            val y1 = e1.y
            val x2 = e2!!.x
            val y2 = e2.y
            val angle: Double = getAngle(x1, y1, x2, y2)
            if (inRange(angle, 45f, 135f)) { // UP
                // Ne rien faire pour l'instant
            } else if (inRange(angle, 0f, 45f) || inRange(angle, 315f, 360f)) { // RIGHT
                // Vers la droite
                movePiece(1)
                DrawScreen()
            } else if (inRange(angle, 225f, 315f)) { // DOWN
                // Vers le bas
                moveDown()
                DrawScreen()
            } else { // LEFT
                // Vers la gauche
                movePiece(-1)
                DrawScreen()
            }
        } catch (e: Exception) { // nothing
        }
        return true

    }

    fun getAngle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Double {
        val rad =
            Math.atan2(y1 - y2.toDouble(), x2 - x1.toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // ne rien faire
        return false

    }

    private fun inRange(
        angle: Double,
        init: Float,
        end: Float
    ): Boolean {
        return angle >= init && angle < end
    }

    override fun onLongPress(e: MotionEvent?) {
        gameInPause = ! gameInPause

    }



    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        if (gameInProgress == false) {

            GameInit()
        }

        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.gestureDetector?.onTouchEvent(event)
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event)
    }

}

