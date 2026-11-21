package com.mykeyboard

import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class MyKeyboardService : InputMethodService() {

    private var isShift = false
    private val letterKeys = mutableListOf<Pair<Button, Char>>()

    private val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")

    override fun onCreateInputView(): View {
        val root = LayoutInflater.from(this)
            .inflate(R.layout.keyboard_view, null) as LinearLayout
        letterKeys.clear()

        for ((index, row) in rows.withIndex()) {
            val rowLayout = newRow()
            if (index == 2) {
                rowLayout.addView(makeKey("⇧", 1.5f) { toggleShift() })
            }
            for (ch in row) {
                val key = makeKey(ch.toString(), 1f) { typeLetter(ch) }
                letterKeys.add(Pair(key, ch))
                rowLayout.addView(key)
            }
            if (index == 2) {
                rowLayout.addView(makeKey("⌫", 1.5f) { deleteChar() })
            }
            root.addView(rowLayout)
        }

        val bottom = newRow()
        bottom.addView(makeKey(",", 1f) { commit(",") })
        bottom.addView(makeKey("space", 5f) { commit(" ") })
        bottom.addView(makeKey(".", 1f) { commit(".") })
        bottom.addView(makeKey("⏎", 1.5f) { enter() })
        root.addView(bottom)

        return root
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun newRow(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        return row
    }

    private fun makeKey(label: String, weight: Float, onClick: () -> Unit): Button {
        val button = Button(this)
        button.text = label
        button.isAllCaps = false
        button.textSize = 18f
        button.setTextColor(0xFFFFFFFF.toInt())
        button.setBackgroundColor(0xFF424242.toInt())
        val params = LinearLayout.LayoutParams(0, dp(48), weight)
        params.setMargins(dp(2), dp(2), dp(2), dp(2))
        button.layoutParams = params
        button.setOnClickListener { onClick() }
        return button
    }

    private fun typeLetter(ch: Char) {
        val text = if (isShift) ch.uppercaseChar().toString() else ch.toString()
        commit(text)
        if (isShift) {
            isShift = false
            refreshLabels()
        }
    }

    private fun commit(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun enter() {
        currentInputConnection?.sendDefaultEditorAction(true)
    }

    private fun toggleShift() {
        isShift = !isShift
        refreshLabels()
    }

    private fun refreshLabels() {
        for ((button, ch) in letterKeys) {
            button.text = if (isShift) ch.uppercaseChar().toString() else ch.toString()
        }
    }
}
