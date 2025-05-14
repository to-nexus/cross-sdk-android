package io.crosstoken.sample.modal.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.crosstoken.sample.modal.R
import io.crosstoken.appkit.ui.AppKitView

class ViewActivity: AppCompatActivity(R.layout.activity_view) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = findViewById<AppKitView>(R.id.web3Modal)

        view.setOnCloseModal { finish() }
    }

}