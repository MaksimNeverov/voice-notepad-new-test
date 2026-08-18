package com.example.emptyviewsactivity


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var noteText: EditText
    private lateinit var btnRecord: Button
    private lateinit var btnSave: Button

    // Запрос разрешения на микрофон (современный способ)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Микрофон разрешён", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Без микрофона голосовой ввод не работает", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteText = findViewById(R.id.noteText)
        btnRecord = findViewById(R.id.btnRecord)
        btnSave = findViewById(R.id.btnSave)

        // Инициализация распознавателя
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val listener = object : RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    noteText.setText(matches[0]) // самая вероятная фраза
                } else {
                    Toast.makeText(this@MainActivity, "Не поняла, попробуй ещё раз", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: Int) {
                Toast.makeText(this@MainActivity, "Ошибка распознавания: $error", Toast.LENGTH_SHORT).show()
            }

            // остальные методы можно оставить пустыми
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)

        // Кнопка записи
        btnRecord.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecognition()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        // Кнопка сохранения
        btnSave.setOnClickListener {
            val text = noteText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Сначала запиши что-нибудь", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Тут будет сохранение. Пока просто показываем сообщение.
            Toast.makeText(this, "Задача сохранена: $text", Toast.LENGTH_SHORT).show()
            // Дальше сюда можно подключить Room или файл — но сначала давай, чтобы это работало.
        }
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU") // русский язык
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Что записать?")
        speechRecognizer.startListening(intent)
    }
}
