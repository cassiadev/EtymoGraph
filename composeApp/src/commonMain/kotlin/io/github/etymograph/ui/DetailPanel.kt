package io.github.etymograph.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etymograph.explorer.ExplorerState
import io.github.etymograph.explorer.hiddenNeighborCount
import io.github.etymograph.model.Lexicon
import io.github.etymograph.model.RelationEdge
import io.github.etymograph.model.RelationType
import io.github.etymograph.model.WordNode

@Composable
fun DetailPanel(
    lexicon: Lexicon,
    state: ExplorerState,
    learned: Set<String>,
    onToggleLearned: (String) -> Unit,
    onReveal: (String) -> Unit,
    onExpand: (String) -> Unit,
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val id = state.selectedId
    // 다른 단어를 고르면 패널을 맨 위부터 보여 준다.
    val scroll = remember(id) { ScrollState(0) }
    Column(modifier.background(Palette.Surface).verticalScroll(scroll).padding(horizontal = 20.dp, vertical = 18.dp)) {
        if (id == null) {
            Text("지도에서 단어를 고르면 뜻과 어원이 여기에 나옵니다.", color = Palette.InkMuted, fontSize = 14.sp)
            return@Column
        }
        val word = lexicon.word(id)

        Row(verticalAlignment = Alignment.CenterVertically) {
            LanguageSwatch(word.language, size = 9.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                listOfNotNull(word.language.nameKo, word.period, word.partOfSpeech).joinToString(" · "),
                color = Palette.InkMuted,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            LearnedToggle(id in learned) { onToggleLearned(id) }
        }
        Text(
            displayTextOf(word.word),
            fontFamily = LocalHeadwordFont.current,
            fontSize = 52.sp,
            lineHeight = 58.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (word.reconstructed) Palette.InkMuted else Palette.Ink,
            modifier = Modifier.padding(top = 10.dp),
        )
        word.ipa?.let { Text(displayTextOf(it), color = Palette.InkMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp)) }
        if (word.reconstructed) {
            Text("* 재구형: 문헌에 남지 않아 비교언어학으로 복원한 형태", color = Palette.InkMuted, fontSize = 12.sp)
        }
        // 사전의 뜻풀이 번호처럼 붉은 숫자를 붙인다. 지금 데이터는 뜻이 하나라 1만 쓴다.
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.Top) {
            Text("1", fontFamily = LocalHeadwordFont.current, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Palette.Accent, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.width(12.dp))
            Text(word.meaningKo, fontSize = 20.sp, lineHeight = 28.sp)
        }

        FlowRow(
            Modifier.padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val hidden = state.hiddenNeighborCount(lexicon, id)
            Button(onClick = { onExpand(id) }, enabled = hidden > 0, elevation = null) {
                Text(if (hidden > 0) "관련 단어 ${hidden}개 펼치기" else "모두 펼침")
            }
            if (state.rootId != id) {
                OutlinedButton(onClick = { onStart(id) }) { Text("여기서 새 지도", color = Palette.Ink) }
            }
        }

        Section("어원") {
            Text(displayTextOf(word.etymologyNote), fontSize = 15.sp, lineHeight = 23.sp)
        }
        lexicon.ancestryOf(id).takeIf { it.isNotEmpty() }?.let { chain ->
            Section("계보") {
                Lineage(lexicon, word, chain)
                if (chain.any { it.uncertain }) {
                    Text(
                        "점선 단계는 출처가 단정하지 않은 연결입니다.",
                        color = Palette.InkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        Section("연결된 단어 ${lexicon.relationsOf(id).size}") {
            lexicon.relationsOf(id).forEachIndexed { i, edge ->
                if (i > 0) Divider(color = Palette.Rule.copy(alpha = 0.6f))
                RelationRow(lexicon, edge, id, onReveal)
            }
        }
        Section("출처") {
            word.sourceUrls.forEach { SourceLink(it) }
            Text(
                "어원 정보는 Wiktionary 항목을 한국어로 요약·가공한 것으로, CC BY-SA 4.0(저작자표시-동일조건변경허락)에 따라 제공합니다.",
                color = Palette.InkMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
            SourceLink(CC_BY_SA_URL, fontSize = 11, label = "라이선스 전문: CC BY-SA 4.0")
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Divider(Modifier.padding(top = 22.dp, bottom = 12.dp), color = Palette.Rule)
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Palette.InkMuted)
    Spacer(Modifier.height(8.dp))
    Column(content = content)
}

@Composable
private fun LearnedToggle(learned: Boolean, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .clip(shape)
            .background(if (learned) Palette.Learned.copy(alpha = 0.10f) else Palette.Surface)
            .border(1.dp, if (learned) Palette.Learned.copy(alpha = 0.5f) else Palette.Rule, shape)
            .toggleable(value = learned, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = learned, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Palette.Learned))
        Text(if (learned) "학습 완료" else "학습 완료로 표시", fontSize = 13.sp, color = if (learned) Palette.Learned else Palette.Ink)
    }
}

/**
 * 조상 사슬을 지금 단어부터 가장 오래된 조상까지 세로 단계로 보여 준다. 오른쪽에 시대 층 이름을 붙이고,
 * 출처가 단정하지 않은 연결은 점선으로 잇는다. 화면 낭독기는 한 줄짜리 계보 문장을 읽는다.
 */
@Composable
private fun Lineage(lexicon: Lexicon, word: WordNode, chain: List<RelationEdge>) {
    // 각 단계와, 그 단계로 올라오는 연결이 불확실한지
    val steps = listOf(word to false) + chain.map { lexicon.word(it.toId) to it.uncertain }
    Column(Modifier.semantics(mergeDescendants = true) { contentDescription = lineageTextOf(lexicon, word.id).orEmpty() }) {
        steps.forEachIndexed { i, (step, uncertainAbove) ->
            Row(Modifier.height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.width(14.dp).fillMaxHeight().drawBehind {
                        val c = Offset(size.width / 2, size.height / 2)
                        val r = 5.dp.toPx()
                        val line = Palette.Accent.copy(alpha = 0.45f)
                        val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                        if (i > 0) drawLine(line, Offset(c.x, 0f), Offset(c.x, c.y - r), 1.5.dp.toPx(), pathEffect = if (uncertainAbove) dash else null)
                        if (i < steps.lastIndex) {
                            drawLine(line, Offset(c.x, c.y + r), Offset(c.x, size.height), 1.5.dp.toPx(), pathEffect = if (steps[i + 1].second) dash else null)
                        }
                        if (i == 0) {
                            drawCircle(Palette.Accent, r, c)
                        } else {
                            drawCircle(Palette.Surface, r, c)
                            drawCircle(Palette.Accent, r, c, style = Stroke(2.dp.toPx()))
                        }
                    },
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    displayTextOf(step.word),
                    fontFamily = LocalHeadwordFont.current,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (step.reconstructed) Palette.InkMuted else Palette.Ink,
                )
                Spacer(Modifier.width(8.dp))
                Text(step.language.nameKo, fontSize = 12.sp, color = Palette.InkMuted, modifier = Modifier.weight(1f))
                Text(step.language.era.labelKo, fontSize = 11.5.sp, color = Palette.Faint)
            }
        }
    }
}

/** "horse(영어) ←? *ḱers-(인도유럽 조어)"처럼 조상 사슬을 한 줄로 만든다. 조상이 없으면 null. */
internal fun lineageTextOf(lexicon: Lexicon, id: String): String? {
    val chain = lexicon.ancestryOf(id)
    if (chain.isEmpty()) return null
    fun label(w: WordNode) = "${w.word}(${w.language.nameKo})"
    return label(lexicon.word(id)) + chain.joinToString("") {
        (if (it.uncertain) " ←? " else " ← ") + label(lexicon.word(it.toId))
    }
}

/** 선택한 단어 쪽에서 본 관계 이름. */
private fun roleOf(edge: RelationEdge, viewpointId: String): String {
    val outgoing = edge.fromId == viewpointId
    return when (edge.type) {
        RelationType.INHERITED_FROM -> if (outgoing) "계승 · 조상 형태" else "계승 · 후대 형태"
        RelationType.BORROWED_FROM -> if (outgoing) "차용 · 출처" else "차용 · 이 말을 받아들인 단어"
        RelationType.DERIVED_FROM -> if (outgoing) "파생 · 바탕 단어" else "파생 · 파생어"
        RelationType.COGNATE_OF -> "동계어 · 공통 조상"
        RelationType.SIMILAR_FORM -> "유사 철자 · 어원 근거 아님"
        RelationType.TRANSLATION_EQUIVALENT -> "번역 대응 · 어원 근거 아님"
    }
}

@Composable
private fun RelationRow(lexicon: Lexicon, edge: RelationEdge, viewpointId: String, onReveal: (String) -> Unit) {
    val other: WordNode = lexicon.word(edge.otherEnd(viewpointId))
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(role = Role.Button, onClickLabel = "${other.word} 보기") { onReveal(other.id) }
            .padding(vertical = 10.dp, horizontal = 2.dp),
    ) {
        // 지도의 선과 같은 견본을 앞에 두어, 목록에서도 관계 종류를 선 모양으로 알아볼 수 있게 한다.
        Canvas(Modifier.padding(top = 7.dp).size(26.dp, 10.dp)) {
            drawRelationLine(Offset(0f, size.height / 2), Offset(size.width, size.height / 2), edge.type, 1f, if (edge.uncertain) 0.6f else 1f)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(roleOf(edge, viewpointId) + if (edge.uncertain) " · 불확실" else "", fontSize = 12.sp, color = Palette.InkMuted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    displayTextOf(other.word),
                    fontFamily = LocalHeadwordFont.current,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (other.reconstructed) Palette.InkMuted else Palette.Ink,
                )
                Spacer(Modifier.width(8.dp))
                LanguageSwatch(other.language, size = 6.dp)
                Spacer(Modifier.width(3.dp))
                Text(other.language.nameKo, fontSize = 12.sp, color = Palette.InkMuted)
            }
            Text(displayTextOf(edge.description), fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 2.dp))
            edge.sourceUrls.forEach { SourceLink(it, fontSize = 11) }
        }
    }
}

private const val CC_BY_SA_URL = "https://creativecommons.org/licenses/by-sa/4.0/deed.ko"

@Composable
private fun SourceLink(url: String, fontSize: Int = 13, label: String = sourceLabelOf(url)) {
    val uriHandler = LocalUriHandler.current
    Text(
        displayTextOf(label),
        color = Palette.InkMuted,
        fontSize = fontSize.sp,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable(onClickLabel = "출처 열기") { uriHandler.openUri(url) }.padding(vertical = 2.dp),
    )
}

/** "https://en.wiktionary.org/wiki/père#French" → "Wiktionary · père (French)" */
internal fun sourceLabelOf(url: String): String {
    val (site, marker) = when {
        "wiktionary.org" in url -> "Wiktionary" to "/wiki/"
        "wikipedia.org" in url -> "Wikipedia" to "/wiki/"
        "anglo-norman.net" in url -> "Anglo-Norman Dictionary" to "/entry/"
        "dictionary.com" in url -> "Dictionary.com" to "/browse/"
        else -> return url
    }
    val title = url.substringAfter(marker).replace("%27", "'").replace('_', ' ')
    val page = title.substringBefore('#')
    val section = title.substringAfter('#', "")
    return "$site · $page" + if (section.isNotEmpty()) " ($section)" else ""
}
