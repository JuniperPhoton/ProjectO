package com.juniperphoton.projecto

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import com.juniperphoton.projecto.drawing.MockupView

class MainActivity : AppCompatActivity() {
    private lateinit var mockupView: MockupView
    private lateinit var mockupContainer: View
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mockupView = findViewById(R.id.mockup_view)
        mockupContainer = findViewById(R.id.mockup_container)
        fab = findViewById(R.id.output_fab)

        fab.setOnClickListener {
            output()
        }
    }

    private fun output() {

    }
}
