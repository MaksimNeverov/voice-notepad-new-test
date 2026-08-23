package com.example.emptyviewsactivity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var noteText: EditText
    private lateinit var btnRecord: Button
    private lateinit var btnSave: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EntryAdapter
    private lateinit var viewModel: EntryViewModel

    private lateinit var btnDelete: Button

    // ЕДИНСТВЕННЫЙ источник истины для выделения
    private val selectedIds = mutableSetOf<Int>()

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
        recyclerView = findViewById(R.id.recyclerView)

        // Создаём кнопку «Удалить»
        btnDelete = Button(this).apply {
            text = "🗑️ УДАЛИТЬ"
            visibility = View.GONE
            setBackgroundColor(Color.parseColor("#F44336"))
            setTextColor(Color.WHITE)
            isAllCaps = true
            setOnClickListener {
                // ИСПРАВЛЕНО: Используем .toList() вместо небезопасного 'as List<Int>'
                val ids = selectedIds.toList()

                if (ids.isNotEmpty()) {
                    viewModel.deleteEntries(ids)

                    // Сбрасываем состояние
                    adapter.clearSelection()
                    selectedIds.clear()
                    updateUi() // Перерисовываем интерфейс

                    // ИСПРАВЛЕНО: Корректная подстановка переменной в строку
                    Toast.makeText(
                        this@MainActivity,
                        "Удалено \${ids.size} заметок",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Вставляем кнопку между панелью кнопок и RecyclerView
        val parent = findViewById<LinearLayout>(R.id.rootLayout)
        parent?.addView(btnDelete, 1)

        val db = AppDatabase.getDatabase(this)
        viewModel = EntryViewModel(db.entryDao())

        adapter = EntryAdapter(
            onItemClick = { entry ->
                Toast.makeText(this, "Клик: \${entry.text}", Toast.LENGTH_SHORT).show()
            },
            onLongClick = { entry ->
                selectedIds.add(entry.id)
                updateUi()
                true
            },
            onSelectionToggle = { id, isSelected ->
                if (isSelected) {
                    selectedIds.add(id)
                } else {
                    selectedIds.remove(id)
                }
                updateUi()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.entries.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        // Настройка распознавания речи
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val listener = object : RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    noteText.setText(matches[0])
                } else {
                    Toast.makeText(this@MainActivity, "Не поняла, повторите", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: Int) {
                // ИСПРАВЛЕНО: Синтаксис строки теперь корректен
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка распознавания: \$error",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)

        btnRecord.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecognition()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        btnSave.setOnClickListener {
            val text = noteText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Сначала запиши что-нибудь", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveEntry(text)
            noteText.text.clear()

            if (selectedIds.isNotEmpty()) {
                selectedIds.clear()
                adapter.clearSelection()
                updateUi()
            }
        }
    }

    private fun updateUi() {
        adapter.setSelectedIds(selectedIds)

        if (selectedIds.isEmpty()) {
            btnDelete.visibility = View.GONE
            btnSave.text = "💾 Сохранить"
            btnSave.isEnabled = true
        } else {
            btnDelete.visibility = View.VISIBLE
            btnSave.text = "🔙 Готово"
            btnSave.isEnabled = false
        }
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Что записать?")
        speechRecognizer.startListening(intent)
    }
}
