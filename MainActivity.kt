package com.example.traneirtester

import android.app.Activity
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import kotlin.math.min

class MainActivity : Activity() {
    private lateinit var ir: ConsumerIrManager
    private val handler = Handler(Looper.getMainLooper())
    private var index = 0
    private var scanning = false

    // Initial candidate timings. These are TEST patterns, not confirmed Trane codes.
    private val candidates = listOf(
        Pair(38000, intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,1690,560,560,560,1690)),
        Pair(38000, intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,1690,560,1690,560,560)),
        Pair(40000, intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,1690,560,560,560,1690)),
        Pair(38000, intArrayOf(4500,4500,560,560,560,1690,560,560,560,1690,560,560,560,1690)),
        Pair(36000, intArrayOf(9000,4500,500,500,500,1600,500,500,500,1600,500,500,500,1600)),
        Pair(40000, intArrayOf(9000,4500,500,500,500,1600,500,500,500,1600,500,500,500,1600))
    )

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ir = getSystemService(CONSUMER_IR_SERVICE) as ConsumerIrManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        status = TextView(this).apply {
            textSize = 18f
            text = "Checking IR..."
        }
        layout.addView(status)

        val power = Button(this).apply {
            text = "POWER — Try Current Code"
            setOnClickListener { sendCurrent() }
        }
        layout.addView(power)

        val next = Button(this).apply {
            text = "NEXT CODE"
            setOnClickListener {
                index = (index + 1) % candidates.size
                sendCurrent()
            }
        }
        layout.addView(next)

        val auto = Button(this).apply {
            text = "AUTO SCAN"
            setOnClickListener {
                if (!scanning) {
                    scanning = true
                    auto.text = "SCANNING..."
                    scanStep()
                }
            }
        }
        layout.addView(auto)

        val stop = Button(this).apply {
            text = "STOP"
            setOnClickListener {
                scanning = false
                auto.text = "AUTO SCAN"
            }
        }
        layout.addView(stop)

        val note = TextView(this).apply {
            text = "\nPoint the TOP of the Redmi Note 10S directly at the AC indoor unit.\n\nEvery press sends a different TEST IR pattern. These are not confirmed Trane codes."
            textSize = 15f
        }
        layout.addView(note)

        setContentView(layout)
        updateStatus()
    }

    private fun updateStatus() {
        val (freq, _) = candidates[index]
        status.text = "Candidate ${index + 1}/${candidates.size}\nFrequency: ${freq} Hz\nIR available: ${ir.hasIrEmitter()}"
    }

    private fun sendCurrent() {
        if (!ir.hasIrEmitter()) {
            status.text = "This phone reports no IR emitter."
            return
        }
        val (freq, pattern) = candidates[index]
        ir.transmit(freq, pattern)
        updateStatus()
    }

    private fun scanStep() {
        if (!scanning) return
        sendCurrent()
        index = (index + 1) % candidates.size
        handler.postDelayed({ scanStep() }, 1800)
    }

    override fun onDestroy() {
        scanning = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
