package com.example.geliose

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity: AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        btn2.setOnClickListener(){

            val intent = Intent(this,MenuActivity::class.java)

            // intent.putExtra(LoginActivity.NAME_EXTRA,name)

            startActivity(intent)

        }
    }
}