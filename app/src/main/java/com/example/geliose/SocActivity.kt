package com.example.geliose

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_menu.*

class SocActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soc)

        button.setOnClickListener(){
            val intent = Intent(this,SocActivity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener(){
            val intent = Intent(this,ValuesActivity::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener(){
            val intent = Intent(this,CellStatusActivity::class.java)
            startActivity(intent)
        }

        button4.setOnClickListener(){
            val intent = Intent(this,InfoActivity::class.java)
            startActivity(intent)
        }
    }
}