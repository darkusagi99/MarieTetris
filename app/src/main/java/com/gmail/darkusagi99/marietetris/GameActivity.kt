package com.gmail.darkusagi99.marietetris

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameActivity : AppCompatActivity() {

    var NUM_ROWS = 21
    var NUM_COLUMNS = 16
    val BOARD_HEIGHT = 800
    val BOARD_WIDTH = 720

    lateinit var bitmap: Bitmap
    lateinit var canvas: Canvas
    lateinit var paint: Paint
    lateinit var linearLayout: LinearLayout

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

        val scoreField = findViewById<TextView>(R.id.scoreText)
        scoreField.text = "0"

        bitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        paint = Paint()
        linearLayout = findViewById<View>(R.id.game_board) as LinearLayout

        mVisible = true


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

    fun DrawScreen() { // Paint the game board background
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, BOARD_WIDTH.toFloat(), BOARD_HEIGHT.toFloat(), paint)

        val colWidth = (BOARD_WIDTH / (NUM_COLUMNS)).toFloat()
        val rowHeight = (BOARD_HEIGHT / (NUM_ROWS)).toFloat()

        paint.color = Color.LTGRAY
        canvas.drawRect(0f, 0f, colWidth, BOARD_HEIGHT.toFloat(), paint)
        canvas.drawRect(colWidth*11, 0f, colWidth*12, BOARD_HEIGHT.toFloat(), paint)
        canvas.drawRect(0f, rowHeight*20, colWidth*12, BOARD_HEIGHT.toFloat(), paint)

        // Paint the grid on the game board
        paint.color = Color.WHITE
        for (i in 0..NUM_ROWS) {
            canvas.drawLine(
                0f, i * (BOARD_HEIGHT / (NUM_ROWS)).toFloat(), BOARD_WIDTH.toFloat(),
                i * (BOARD_HEIGHT / (NUM_ROWS)).toFloat(), paint
            )
        }
        for (i in 1..NUM_COLUMNS - 5) {
            canvas.drawLine(
                i * colWidth, 0f,
                i * colWidth, BOARD_HEIGHT.toFloat(), paint
            )
        }
        // Paint the tetris blocks
        /*for (i in 3 until NUM_ROWS - 3) {
            for (j in 3 until NUM_COLUMNS - 3) {
                if (gameMatrix.get(i).get(j).getState() == 1) {
                    paint.color = gameMatrix.get(i).get(j).getColor()
                    canvas.drawRect(
                        (j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        paint
                    )
                }
            }
        }
        // Paint borders to the tetris blocks
        for (i in 3 until NUM_ROWS - 3) {
            for (j in 3 until NUM_COLUMNS - 3) {
                if (gameMatrix.get(i).get(j).getState() == 1) {
                    paint.color = Color.BLACK
                    canvas.drawLine(
                        (j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        (j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        paint
                    )
                    canvas.drawLine(
                        (j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        paint
                    )
                    canvas.drawLine(
                        (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        paint
                    )
                    canvas.drawLine(
                        (j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)).toFloat(),
                        (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)).toFloat(),
                        paint
                    )
                }
            }
        }*/
        /*if (!gameInProgress) {
            val textView =
                findViewById<View>(R.id.game_over_textview) as TextView
            textView.visibility = View.VISIBLE
            val textView2 =
                findViewById<View>(R.id.game_over_textview2) as TextView
            textView2.visibility = View.VISIBLE
        } else if (gamePaused) {
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 60f
            canvas.drawText(
                "GAME PAUSED",
                (BOARD_WIDTH / 2.0).toFloat(),
                (BOARD_HEIGHT / 2.0).toFloat(),
                paint
            )
        }*/
        // Display the current painting
        linearLayout.setBackgroundDrawable(BitmapDrawable(bitmap))
        // Update the score textview
        /*val textView =
            findViewById<View>(R.id.game_score_textview) as TextView
        textView.text = "Score: $score"*/
    }

}
