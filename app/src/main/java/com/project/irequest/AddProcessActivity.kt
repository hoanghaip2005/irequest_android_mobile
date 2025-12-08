package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddProcessActivity : AppCompatActivity() {

    private lateinit var etProcessName: TextInputEditText
    private lateinit var btnSaveProcess: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_process)

        val toolbar: Toolbar = findViewById(R.id.toolbar_add_process)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        etProcessName = findViewById(R.id.et_process_name)
        btnSaveProcess = findViewById(R.id.btn_save_process)

        btnSaveProcess.setOnClickListener {
            val processName = etProcessName.text.toString().trim()
            if (processName.isNotEmpty()) {
                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val newProcess = Process(
                    id = UUID.randomUUID().toString(),
                    name = processName,
                    status = "Đang chờ",
                    date = currentDate
                )

                val resultIntent = Intent()
                resultIntent.putExtra("NEW_PROCESS", newProcess)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Vui lòng nhập tên quy trình", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
