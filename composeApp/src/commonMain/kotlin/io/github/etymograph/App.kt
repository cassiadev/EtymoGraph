package io.github.etymograph

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etymograph.data.SampleLexicon
import io.github.etymograph.explorer.ExplorerState
import io.github.etymograph.explorer.withExpanded
import io.github.etymograph.explorer.withNewRoot
import io.github.etymograph.explorer.withPreviousMap
import io.github.etymograph.explorer.withRevealed
import io.github.etymograph.explorer.withSelection
import io.github.etymograph.model.Lexicon
import io.github.etymograph.model.WordNode
import io.github.etymograph.ui.DetailPanel
import io.github.etymograph.ui.EtymoTheme
import io.github.etymograph.ui.GraphView
import io.github.etymograph.ui.LanguageSwatch
import io.github.etymograph.ui.LocalHeadwordFont
import io.github.etymograph.ui.Palette
import io.github.etymograph.ui.Viewport
import io.github.etymograph.ui.displayTextOf


/**
 * 앱 루트. 상태는 여기서만 바꾸고 하위 화면은 값과 이벤트 콜백만 받는다.
 * 학습 기록의 저장 방식은 플랫폼 진입점이 [initialLearned]·[onLearnedChange]로 정한다.
 */
@Composable
fun App(initialLearned: Set<String> = emptySet(), onLearnedChange: (Set<String>) -> Unit = {}) {
    val lexicon = SampleLexicon
    var explorer by remember { mutableStateOf(ExplorerState()) }
    var viewport by remember { mutableStateOf(Viewport()) }
    var learned by remember { mutableStateOf(initialLearned) }

    fun startFrom(id: String) {
        explorer = explorer.withNewRoot(lexicon, id)
        viewport = Viewport()
    }
    val toggleLearned = { id: String ->
        learned = if (id in learned) learned - id else learned + id
        onLearnedChange(learned)
    }

    EtymoTheme {
        Column(Modifier.fillMaxSize().background(Palette.Surface)) {
            TopBar(
                lexicon = lexicon,
                canGoBack = explorer.history.isNotEmpty(),
                onSearch = ::startFrom,
                onBack = { explorer = explorer.withPreviousMap(); viewport = Viewport() },
            )
            Divider(color = Palette.Rule)
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val graph = @Composable { modifier: Modifier ->
                    GraphView(
                        lexicon = lexicon,
                        state = explorer,
                        learned = learned,
                        viewport = viewport,
                        onViewportChange = { change -> viewport = change(viewport) },
                        onSelect = { explorer = explorer.withSelection(it) },
                        onExpand = { explorer = explorer.withExpanded(lexicon, it).withSelection(it) },
                        onStart = ::startFrom,
                        modifier = modifier,
                    )
                }
                val detail = @Composable { modifier: Modifier ->
                    DetailPanel(
                        lexicon = lexicon,
                        state = explorer,
                        learned = learned,
                        onToggleLearned = toggleLearned,
                        onReveal = { explorer = explorer.withRevealed(lexicon, it) },
                        onExpand = { explorer = explorer.withExpanded(lexicon, it) },
                        onStart = ::startFrom,
                        modifier = modifier,
                    )
                }
                // 넓은 화면은 오른쪽 패널, 좁은 화면은 하단 패널.
                if (maxWidth >= 840.dp) {
                    Row(Modifier.fillMaxSize()) {
                        graph(Modifier.weight(1f).fillMaxHeight())
                        Divider(Modifier.fillMaxHeight().width(1.dp), color = Palette.Rule)
                        detail(Modifier.width(380.dp).fillMaxHeight())
                    }
                } else {
                    val panelHeight = maxHeight * 0.45f
                    Column(Modifier.fillMaxSize()) {
                        graph(Modifier.weight(1f).fillMaxWidth())
                        Divider(color = Palette.Rule)
                        detail(Modifier.fillMaxWidth().height(panelHeight))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    lexicon: Lexicon,
    canGoBack: Boolean,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) { lexicon.search(query) }
    val focusManager = LocalFocusManager.current
    fun open(id: String) {
        onSearch(id)
        query = ""
        // 검색 후에는 포커스를 풀어 모바일 키보드를 닫고 그래프 조작으로 넘어가게 한다.
        focusManager.clearFocus()
    }
    fun submit() {
        // results는 마지막으로 그린 때의 값이다. 입력 직후 화면이 갱신되기 전에 Enter가 와도 지금 입력값으로 찾는다.
        lexicon.search(query).firstOrNull()?.let { open(it.id) }
    }

    val brand = @Composable { modifier: Modifier ->
        Row(modifier, verticalAlignment = Alignment.Bottom) {
            Text("EtymoGraph", fontFamily = LocalHeadwordFont.current, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text("어원 지도", fontSize = 13.sp, color = Palette.InkMuted, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
    // 지도를 맞추는 "전체 보기"는 지도 안의 확대·축소 버튼 옆에 둔다.
    val tools = @Composable { QuietButton("← 이전 지도", enabled = canGoBack, onClick = onBack) }
    val searchButton = @Composable {
        Button(onClick = ::submit, enabled = results.isNotEmpty(), elevation = null, modifier = Modifier.height(44.dp)) { Text("검색") }
    }

    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        val wide = maxWidth >= 960.dp
        Column {
            if (wide) {
                // 넓은 화면: 이름, 검색, 보조 버튼을 한 줄에 둔다.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    brand(Modifier.width(250.dp))
                    SearchField(query = query, onQueryChange = { query = it }, onSubmit = ::submit, modifier = Modifier.width(460.dp))
                    Spacer(Modifier.width(8.dp))
                    searchButton()
                    Spacer(Modifier.weight(1f))
                    tools()
                }
            } else {
                // 좁은 화면에서는 버튼 글자가 줄바꿈되지 않고 버튼 묶음이 다음 줄로 내려간다.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    brand(Modifier.weight(1f))
                    tools()
                }
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    SearchField(query = query, onQueryChange = { query = it }, onSubmit = ::submit, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    searchButton()
                }
            }
            if (query.isNotBlank()) {
                Row(
                    Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (results.isEmpty()) {
                        Text("샘플 데이터에 '$query'(이)가 없습니다.", color = Palette.InkMuted, fontSize = 13.sp)
                    }
                    results.take(8).forEach { w -> SuggestionChip(w) { open(w.id) } }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(8.dp)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, color = Palette.Ink),
        cursorBrush = SolidColor(Palette.Accent),
        interactionSource = interaction,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        modifier = modifier
            .height(44.dp)
            .semantics { contentDescription = "영어 단어 검색" }
            .onPreviewKeyEvent {
                val enter = it.key == Key.Enter || it.key == Key.NumPadEnter
                if (enter && it.type == KeyEventType.KeyDown) onSubmit()
                enter
            },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(Palette.Paper.copy(alpha = 0.5f))
                    .border(if (focused) 2.dp else 1.dp, if (focused) Palette.Accent else Palette.Rule, shape)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text("영어 단어 검색 (예: father, court)", color = Palette.InkMuted, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                inner()
            }
        },
    )
}

/** 상단의 보조 동작. 강조색은 검색 버튼에만 쓰고, 나머지는 잉크색 테두리 버튼으로 둔다. */
@Composable
private fun QuietButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, Palette.Rule),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Palette.Surface, contentColor = Palette.Ink),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) { Text(text, softWrap = false, fontSize = 14.sp) }
}

@Composable
private fun SuggestionChip(word: WordNode, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .clip(shape)
            .background(Palette.Surface)
            .border(1.dp, Palette.Rule, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(displayTextOf(word.word), fontFamily = LocalHeadwordFont.current, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        LanguageSwatch(word.language, size = 6.dp)
        Spacer(Modifier.width(3.dp))
        Text(word.language.nameKo, fontSize = 12.sp, color = Palette.InkMuted)
    }
}
