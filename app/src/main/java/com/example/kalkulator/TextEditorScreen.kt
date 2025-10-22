package com.example.kalkulator

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Note(
    val id: Int,
    var title: String,
    var content: String,
    var textSize: Int = 16,
)

class NoteRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notepad_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val NOTES_KEY = "notes_list"

    fun saveNotes(notes: List<Note>) {
        val json = gson.toJson(notes)
        sharedPreferences.edit().putString(NOTES_KEY, json).apply()
    }

    fun loadNotes(): List<Note> {
        val json = sharedPreferences.getString(NOTES_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<Note>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun deleteNote(noteId: Int) {
        val currentNotes = loadNotes().toMutableList()
        currentNotes.removeAll { it.id == noteId }
        saveNotes(currentNotes)
    }
}

val NoteListSaver: Saver<SnapshotStateList<Note>, Any> = listSaver(
    save = { list ->
        list.flatMap { listOf(it.id, it.title, it.content, it.textSize) }
    },
    restore = { list ->
        mutableStateListOf<Note>().apply {
            list.chunked(4) {
                add(
                    Note(
                        id = it[0] as Int,
                        title = it[1] as String,
                        content = it[2] as String,
                        textSize = it[3] as Int,
                    )
                )
            }
        }
    }
)

enum class NotepadScreen {
    NoteList,
    NoteEdit
}

fun htmlToAnnotatedString(html: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var i = 0

    while (i < html.length) {
        when {
            html.startsWith("<b>", i) -> {
                val endTag = html.indexOf("</b>", i)
                if (endTag != -1) {
                    val start = builder.length
                    val content = html.substring(i + 3, endTag)
                    builder.append(content)
                    builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, builder.length)
                    i = endTag + 4
                } else {
                    builder.append(html[i])
                    i++
                }
            }
            html.startsWith("<i>", i) -> {
                val endTag = html.indexOf("</i>", i)
                if (endTag != -1) {
                    val start = builder.length
                    val content = html.substring(i + 3, endTag)
                    builder.append(content)
                    builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, builder.length)
                    i = endTag + 4
                } else {
                    builder.append(html[i])
                    i++
                }
            }
            else -> {
                builder.append(html[i])
                i++
            }
        }
    }
    return builder.toAnnotatedString()
}

fun annotatedStringToHtml(annotatedString: AnnotatedString): String {
    if (annotatedString.spanStyles.isEmpty()) {
        return annotatedString.text
    }

    val sortedSpans = annotatedString.spanStyles.sortedBy { it.start }
    val result = StringBuilder()
    var lastIndex = 0

    for (span in sortedSpans) {
        result.append(annotatedString.text.substring(lastIndex, span.start))

        val styleText = annotatedString.text.substring(span.start, span.end)
        val isBold = span.item.fontWeight == FontWeight.Bold
        val isItalic = span.item.fontStyle == FontStyle.Italic

        when {
            isBold && isItalic -> result.append("<b><i>$styleText</i></b>")
            isBold -> result.append("<b>$styleText</b>")
            isItalic -> result.append("<i>$styleText</i>")
        }
        lastIndex = span.end
    }

    result.append(annotatedString.text.substring(lastIndex))
    return result.toString()
}

fun AnnotatedString.applySpanStyle(
    style: SpanStyle,
    selection: TextRange,
): AnnotatedString {
    val start = selection.min
    val end = selection.max

    if (start == end) return this

    val builder = AnnotatedString.Builder(text = this.text)

    this.spanStyles.forEach { span ->
        builder.addStyle(span.item, span.start, span.end)
    }

    builder.addStyle(style, start, end)

    return builder.toAnnotatedString()
}

val PinkMist = Color(0xFFF7DAE7)
val CardBackground = Color(0xFFE2B4C1)
val TopBarColor = Color(0xFFD38C9D)
val RubyPetals = Color(0xFFA55166)
val DarkRuby = Color(0xFF7A384A)

private val LightColorScheme = lightColorScheme(
    primary = RubyPetals,
    onPrimary = Color.White,
    primaryContainer = TopBarColor,
    onPrimaryContainer = Color.White,
    secondary = CardBackground,
    onSecondary = DarkRuby,
    background = PinkMist,
    onBackground = Color.Black,
    surface = PinkMist,
    onSurface = Color.Black,
    surfaceContainerHigh = CardBackground,
    onSurfaceVariant = Color.DarkGray
)

private val DarkColorScheme = darkColorScheme(
    primary = RubyPetals,
    onPrimary = Color.White,
    primaryContainer = DarkRuby,
    onPrimaryContainer = PinkMist,
    background = Color(0xFF1C1C1E),
    onBackground = PinkMist,
    surface = Color(0xFF2C2C2E),
    onSurface = PinkMist,
    surfaceContainerHigh = Color(0xFF3C3C3E),
    onSurfaceVariant = PinkMist
)

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun NotepadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun TextEditorScreen(navController: NavController = rememberNavController()) {

    NotepadTheme {
        NotepadMainScreen(navController)
    }
}

@Composable
fun NotepadMainScreen(navController: NavController) {
    var currentScreen by rememberSaveable { mutableStateOf(NotepadScreen.NoteList) }
    var selectedNote by rememberSaveable { mutableStateOf<Note?>(null) }

    val context = LocalContext.current
    val repository = NoteRepository(context)

    val notes: SnapshotStateList<Note> = rememberSaveable(saver = NoteListSaver) {
        val savedNotes = repository.loadNotes()
        mutableStateListOf<Note>().apply {
            addAll(savedNotes)
        }
    }

    when (currentScreen) {
        NotepadScreen.NoteList -> NoteListScreen(
            navController = navController,
            notes = notes,
            repository = repository,
            onNoteClick = { note ->
                selectedNote = note
                currentScreen = NotepadScreen.NoteEdit
            },
            onNewNoteClick = {
                val newId = (notes.maxOfOrNull { it.id } ?: 0) + 1
                val newNote = Note(newId, "Catatan Baru", "")
                notes.add(0, newNote)
                repository.saveNotes(notes)
                selectedNote = newNote
                currentScreen = NotepadScreen.NoteEdit
            }
        )
        NotepadScreen.NoteEdit -> selectedNote?.let { note ->
            NoteEditScreen(
                note = note,
                repository = repository,
                notes = notes,
                onSave = {
                    repository.saveNotes(notes)
                },
                onBack = {
                    selectedNote = null
                    currentScreen = NotepadScreen.NoteList
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navController: NavController,
    notes: SnapshotStateList<Note>,
    repository: NoteRepository,
    onNoteClick: (Note) -> Unit,
    onNewNoteClick: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredNotes = if (searchQuery.isNotEmpty()) {
        notes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
        }
    } else {
        notes
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Notepad")
                    },
                    actions = {
                        IconButton(onClick = onNewNoteClick) {
                            Icon(Icons.Default.Create, contentDescription = "New Note")
                        }
                        IconButton(onClick = { /* TODO: Cut */ }) {
                            Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                        }
                        IconButton(onClick = { /* TODO: Copy */ }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                        IconButton(onClick = { /* TODO: Paste */ }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                        }
                        IconButton(onClick = { /* TODO: Settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notes") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewNoteClick,

                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new note", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },

        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "Belum ada catatan\nTekan + untuk membuat catatan baru" else "Tidak ada hasil",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteItem(
                        note = note,
                        onClick = onNoteClick,
                        onDelete = {
                            notes.remove(note)
                            repository.saveNotes(notes)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: (Note) -> Unit,
    onDelete: () -> Unit
) {
    val noteTextStyle = TextStyle(fontSize = 14.sp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(note) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                val previewContent = if (note.content.isNotEmpty()) {
                    htmlToAnnotatedString(note.content)
                } else {
                    AnnotatedString("Catatan kosong")
                }

                Text(
                    text = previewContent,
                    style = noteTextStyle,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    note: Note,
    repository: NoteRepository,
    notes: SnapshotStateList<Note>,
    onSave: (Note) -> Unit,
    onBack: () -> Unit
) {
    var editedTitle by rememberSaveable { mutableStateOf(note.title) }

    var contentState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(htmlToAnnotatedString(note.content)))
    }

    var currentSize by rememberSaveable { mutableStateOf(note.textSize) }

    note.title = editedTitle
    note.textSize = currentSize

    val contentTextStyle = TextStyle(fontSize = currentSize.sp)
    val clipboardManager = LocalClipboardManager.current
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val applyBold = {
        val selection = contentState.selection
        if (!selection.collapsed) {
            val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
            val newAnnotatedString = contentState.annotatedString.applySpanStyle(
                style = boldStyle,
                selection = selection
            )
            contentState = contentState.copy(annotatedString = newAnnotatedString)
        }
    }

    val applyItalic = {
        val selection = contentState.selection
        if (!selection.collapsed) {
            val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)
            val newAnnotatedString = contentState.annotatedString.applySpanStyle(
                style = italicStyle,
                selection = selection
            )
            contentState = contentState.copy(annotatedString = newAnnotatedString)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(editedTitle.takeIf { it.isNotBlank() } ?: "Catatan Baru") },
                navigationIcon = {
                    IconButton(onClick = {
                        note.content = annotatedStringToHtml(contentState.annotatedString)
                        onSave(note)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        note.content = annotatedStringToHtml(contentState.annotatedString)
                        onSave(note)
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Simpan")
                    }

                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Lainnya")
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Salin Konten") },
                            onClick = {
                                clipboardManager.setText(contentState.annotatedString)
                                isMenuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = "Salin") }
                        )

                        DropdownMenuItem(
                            text = { Text("Tempel ke Konten") },
                            onClick = {
                                val clipboardText = clipboardManager.getText()?.text ?: ""
                                val selection = contentState.selection.start
                                val currentAnnotatedString = contentState.annotatedString
                                val newText = currentAnnotatedString.text.substring(0, selection) +
                                        clipboardText +
                                        currentAnnotatedString.text.substring(selection)

                                val builder = AnnotatedString.Builder(text = newText)
                                currentAnnotatedString.spanStyles.forEach { span ->
                                    if (span.end <= selection) {
                                        builder.addStyle(span.item, span.start, span.end)
                                    } else if (span.start >= selection) {
                                        builder.addStyle(span.item, span.start + clipboardText.length, span.end + clipboardText.length)
                                    }
                                }

                                contentState = TextFieldValue(
                                    annotatedString = builder.toAnnotatedString(),
                                    selection = TextRange(selection + clipboardText.length)
                                )
                                isMenuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = "Tempel") }
                        )

                        DropdownMenuItem(
                            text = { Text("Potong Konten") },
                            onClick = {
                                val selection = contentState.selection
                                val selectedText = contentState.text.substring(selection.min, selection.max)
                                clipboardManager.setText(AnnotatedString(selectedText))

                                val newText = contentState.annotatedString.text.removeRange(selection.min, selection.max)
                                val lengthRemoved = selection.max - selection.min

                                val builder = AnnotatedString.Builder(text = newText)
                                contentState.annotatedString.spanStyles.forEach { span ->
                                    val newStart = when {
                                        span.end <= selection.min -> span.start
                                        span.start >= selection.max -> span.start - lengthRemoved
                                        else -> selection.min + (span.end - selection.max).coerceAtLeast(0)
                                    }
                                    val newEnd = when {
                                        span.end <= selection.min -> span.end
                                        span.start >= selection.max -> span.end - lengthRemoved
                                        else -> selection.min + (span.end - selection.max).coerceAtLeast(0)
                                    }

                                    if (newStart < newEnd) {
                                        builder.addStyle(span.item, newStart, newEnd)
                                    }
                                }

                                contentState = TextFieldValue(
                                    annotatedString = builder.toAnnotatedString(),
                                    selection = TextRange(selection.min)
                                )
                                isMenuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = "Potong") }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = editedTitle,
                onValueChange = { editedTitle = it },
                label = { Text("Judul Catatan") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = applyBold,
                    modifier = Modifier.background(
                        color = if (!contentState.selection.collapsed) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                        shape = CircleShape
                    )
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = applyItalic,
                    modifier = Modifier.background(
                        color = if (!contentState.selection.collapsed) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                        shape = CircleShape
                    )
                ) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Font size buttons
                Button(
                    onClick = { if (currentSize > 8) currentSize -= 2 },
                    enabled = currentSize > 8,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Kurangi Ukuran", modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A-")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { if (currentSize < 40) currentSize += 2 },
                    enabled = currentSize < 40,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Tambah Ukuran", modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A+")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contentState,
                onValueChange = { contentState = it },
                label = { Text("Isi Catatan") },
                textStyle = contentTextStyle,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}