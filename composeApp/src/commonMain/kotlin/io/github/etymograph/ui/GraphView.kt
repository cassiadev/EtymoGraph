package io.github.etymograph.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etymograph.explorer.ExplorerState
import io.github.etymograph.explorer.hiddenNeighborCount
import io.github.etymograph.explorer.visibleRelations
import io.github.etymograph.graph.GraphLayout
import io.github.etymograph.graph.Point
import io.github.etymograph.model.Era
import io.github.etymograph.model.Language
import io.github.etymograph.model.LanguageFamily
import io.github.etymograph.model.Lexicon
import io.github.etymograph.model.RelationType
import io.github.etymograph.model.WordNode
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

// GraphLayout.MIN_DX는 이 폭에 여백을 더한 값이다. 노드는 상자 없이 글자만 그리지만, 누르는 영역과 선이 닿는 자리는 이 크기다.
internal val NodeWidth = 136.dp
internal val NodeHeight = 62.dp

/** 어파 칸이 노드 둘레로 띄우는 여백과 세로 범위(월드 좌표). 칸은 어파 조어 층부터 아래로 둔다. */
private const val LANE_PAD = 14f
private val LANE_TOP = GraphLayout.BAND_EDGES[1] + 14f
private val LANE_BOTTOM = GraphLayout.BAND_EDGES[Era.entries.size] - 12f

private class EdgeStyle(val width: Dp, val dash: List<Dp>?, val arrow: Boolean)

private fun styleOf(type: RelationType): EdgeStyle = when (type) {
    RelationType.INHERITED_FROM -> EdgeStyle(1.4.dp, null, arrow = true)
    RelationType.BORROWED_FROM -> EdgeStyle(1.4.dp, listOf(6.dp, 4.dp), arrow = true)
    RelationType.DERIVED_FROM -> EdgeStyle(1.7.dp, listOf(1.5.dp, 4.5.dp), arrow = true)
    RelationType.COGNATE_OF -> EdgeStyle(1.6.dp, listOf(1.5.dp, 4.dp), arrow = false)
    RelationType.SIMILAR_FORM -> EdgeStyle(1.6.dp, listOf(8.dp, 3.dp, 1.5.dp, 3.dp), arrow = false)
    RelationType.TRANSLATION_EQUIVALENT -> EdgeStyle(1.4.dp, listOf(10.dp, 6.dp), arrow = false)
}

/** 곡선 하나를 관계 모양(실선·파선·점선, 화살촉)대로 그린다. 화살촉은 끝점(후대 형태·받아들인 말)에 단다. */
private fun DrawScope.drawRelationCurve(curve: Curve, type: RelationType, color: Color, width: Dp, scale: Float, alpha: Float) {
    val style = styleOf(type)
    val tint = color.copy(alpha = color.alpha * alpha)
    val effect = style.dash?.let { d -> PathEffect.dashPathEffect(d.map { it.toPx() * scale }.toFloatArray()) }
    val path = Path().apply {
        moveTo(curve.start.x, curve.start.y)
        cubicTo(curve.c1.x, curve.c1.y, curve.c2.x, curve.c2.y, curve.end.x, curve.end.y)
    }
    drawPath(path, tint, style = Stroke(width = (width.toPx() * scale).coerceAtLeast(1f), cap = StrokeCap.Round, pathEffect = effect))
    if (style.arrow) {
        val dir = curve.endDirection()
        val length = 8.dp.toPx() * scale
        val normal = Offset(-dir.y, dir.x) * (length * 0.5f)
        val base = curve.end - dir * length
        val head = Path().apply {
            moveTo(curve.end.x, curve.end.y)
            lineTo(base.x + normal.x, base.y + normal.y)
            lineTo(base.x - normal.x, base.y - normal.y)
            close()
        }
        drawPath(head, tint)
    }
}

/** 범례와 상세 패널의 견본 선. 지도의 선과 같은 모양으로 그린다. */
internal fun DrawScope.drawRelationLine(start: Offset, end: Offset, type: RelationType, scale: Float, alpha: Float) =
    drawRelationCurve(Curve(start, start, end, end), type, relationColor(type), styleOf(type).width, scale, alpha)

/** 시대 층 배경. 층을 번갈아 칠하고 층 경계에 옅은 괘선을 긋는다. [screenY]는 월드 y를 화면 y로 바꾼다. */
private fun DrawScope.drawEraBands(screenY: (Float) -> Float) {
    for (i in Era.entries.indices) {
        val top = screenY(GraphLayout.BAND_EDGES[i])
        val bottom = screenY(GraphLayout.BAND_EDGES[i + 1])
        if (i % 2 == 1) drawRect(Palette.Band, Offset(0f, top), Size(size.width, bottom - top))
        drawLine(Palette.Rule.copy(alpha = 0.7f), Offset(0f, top), Offset(size.width, top), 1.dp.toPx())
    }
}

/** 어파마다 화면에 있는 단어 중심의 가로 범위(월드 좌표). 어파 칸은 이 범위를 감싼다. */
private fun familyLanesOf(lexicon: Lexicon, positions: Map<String, Point>): Map<LanguageFamily, ClosedFloatingPointRange<Float>> =
    positions.entries
        .mapNotNull { (id, p) -> lexicon.word(id).language.family?.let { it to p.x } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, xs) -> xs.min()..xs.max() }

@Composable
fun GraphView(
    lexicon: Lexicon,
    state: ExplorerState,
    learned: Set<String>,
    viewport: Viewport,
    onViewportChange: ((Viewport) -> Viewport) -> Unit,
    onSelect: (String) -> Unit,
    onExpand: (String) -> Unit,
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.positions.isEmpty()) {
        Box(modifier.background(Palette.Paper)) { EmptyHint(onStart, Modifier.align(Alignment.Center)) }
        return
    }
    BoxWithConstraints(modifier.background(Palette.Paper)) {
        // 시대 층 이름은 지도 왼쪽의 고정 칸에 둔다. 지도를 움직여도 노드와 겹치지 않고, 화면 맞춤도 지도 영역만 계산한다.
        val wide = maxWidth >= 600.dp
        // 범례는 지도 위에 떠 있어 오른쪽 위의 단어(가운데 놓이는 인도유럽 조어 등)를 가릴 수 있다. 자리가 넉넉할 때만 펼쳐 둔다.
        val legendOpen = maxWidth >= 1100.dp && maxHeight >= 700.dp
        Row(Modifier.fillMaxSize()) {
            EraGutter(viewport, showNotes = wide, modifier = Modifier.width(if (wide) 116.dp else 76.dp).fillMaxHeight())
            MapArea(
                lexicon = lexicon,
                state = state,
                learned = learned,
                viewport = viewport,
                onViewportChange = onViewportChange,
                onSelect = onSelect,
                onExpand = onExpand,
                legendOpen = legendOpen,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun EraGutter(viewport: Viewport, showNotes: Boolean, modifier: Modifier) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier.clipToBounds()) {
        val height = constraints.maxHeight.toFloat()
        val unitPx = density.density * viewport.scale
        // 지도 영역과 높이가 같으므로 같은 세로 변환을 쓴다.
        val screenY = { y: Float -> height / 2 + viewport.pan.y + y * unitPx }
        Canvas(Modifier.fillMaxSize()) { drawEraBands(screenY) }
        val labelWidth = maxWidth - 18.dp
        Era.entries.forEachIndexed { i, era ->
            val top = screenY(GraphLayout.BAND_EDGES[i])
            val bottom = screenY(GraphLayout.BAND_EDGES[i + 1])
            if (bottom <= 0f || top >= height) return@forEachIndexed
            // 층의 윗부분이 화면 위로 지나가도 이름은 그 층 안, 화면 위쪽에 붙어 있게 한다.
            val y = minOf(maxOf(top, 0f) + 10 * density.density, bottom - 44 * density.density)
            Column(Modifier.offset { IntOffset(12.dp.roundToPx(), y.roundToInt()) }.widthIn(max = labelWidth)) {
                Text(if (showNotes) era.labelKo else era.shortKo, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                if (showNotes) Text(era.noteKo, fontSize = 11.sp, color = Palette.InkMuted)
            }
        }
    }
}

@Composable
private fun MapArea(
    lexicon: Lexicon,
    state: ExplorerState,
    learned: Set<String>,
    viewport: Viewport,
    onViewportChange: ((Viewport) -> Viewport) -> Unit,
    onSelect: (String) -> Unit,
    onExpand: (String) -> Unit,
    legendOpen: Boolean,
    modifier: Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current.density
    val relations = remember(state.positions) { state.visibleRelations(lexicon) }
    val lanes = remember(state.positions) { familyLanesOf(lexicon, state.positions) }
    val selected = state.selectedId
    // 선택한 단어의 계보(조상까지 이어지는 관계)는 붉게, 동계어는 파란 점선 밑줄로 표시한다.
    val ancestry = remember(selected) { selected?.let { lexicon.ancestryOf(it).toSet() }.orEmpty() }
    val cognates = remember(selected) {
        selected?.let { id -> lexicon.relationsOf(id).filter { it.type == RelationType.COGNATE_OF }.map { it.otherEnd(id) }.toSet() }.orEmpty()
    }

    BoxWithConstraints(
        modifier
            .clipToBounds()
            // 한 손가락·마우스 드래그는 이동, 두 손가락은 확대·축소.
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val focus = centroid - Offset(size.width / 2f, size.height / 2f)
                    onViewportChange { it.withTransformGesture(focus, pan, zoom) }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type != PointerEventType.Scroll) continue
                        val change = event.changes.first()
                        val delta = change.scrollDelta.y.coerceIn(-3f, 3f)
                        if (delta == 0f) continue
                        val focus = change.position - Offset(size.width / 2f, size.height / 2f)
                        onViewportChange { it.withZoom(exp(-delta * WHEEL_ZOOM_RATE), focus) }
                        change.consume()
                    }
                }
            },
    ) {
        val center = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
        val unitPx = density * viewport.scale
        fun screenOf(p: Point) = center + viewport.pan + Offset(p.x * unitPx, p.y * unitPx)
        val screenX = { x: Float -> center.x + viewport.pan.x + x * unitPx }
        val screenY = { y: Float -> center.y + viewport.pan.y + y * unitPx }
        val half = Offset(NodeWidth.value, NodeHeight.value) * (unitPx / 2)
        fun rectOf(id: String): Rect = screenOf(state.positions.getValue(id)).let { Rect(it - half, it + half) }

        // 화면 맞춤은 노드 상자들을 월드 px 좌표로 묶은 범위로 계산한다.
        val viewSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val margin = 16f * density
        val halfNode = Offset(NodeWidth.value, NodeHeight.value) * (density / 2)
        fun boundsOf(ids: Collection<String>): Rect = ids
            .map { id ->
                val p = state.positions.getValue(id)
                val c = Offset(p.x * density, p.y * density)
                Rect(c - halfNode, c + halfNode)
            }
            .reduce { a, b -> Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom)) }
        // 자동 화면 이동과 "전체"는 같은 ViewportAnimator를 거쳐, 새 움직임이 진행 중인 움직임(지도 전환 전의 것 포함)을 취소한다.
        val currentViewport by rememberUpdatedState(viewport)
        val currentOnViewportChange by rememberUpdatedState(onViewportChange)
        val animator = remember { ViewportAnimator(current = { currentViewport }, update = { currentOnViewportChange(it) }) }

        // 선택하거나 새로 펼친 노드가 화면 밖에 있으면 지도를 옮겨(필요하면 축소해) 보여 준다. 이미 보이면 움직이지 않는다.
        val focusTracker = remember { FocusTracker() }
        LaunchedEffect(state.mapNumber, state.selectedId, state.positions) {
            val focus = focusTracker.track(state) ?: return@LaunchedEffect
            // 새로 펼친 묶음이 화면보다 크면 다 보일 만큼만 축소한다. 선택만 바뀐 경우에는 이동만 한다.
            animator.move { v ->
                v.movementToShow(
                    group = boundsOf(focus.ids),
                    keepVisible = state.selectedId?.let { boundsOf(listOf(it)) },
                    viewSize = viewSize,
                    margin = margin,
                    allowZoomOut = focus.allowZoomOut,
                )
            }
        }
        val scope = rememberCoroutineScope()

        Canvas(Modifier.fillMaxSize()) {
            drawEraBands(screenY)
            for ((family, xs) in lanes) {
                val colors = familyColors(family)
                val topLeft = Offset(screenX(xs.start - NodeWidth.value / 2 - LANE_PAD), screenY(LANE_TOP))
                val laneSize = Size(screenX(xs.endInclusive + NodeWidth.value / 2 + LANE_PAD) - topLeft.x, screenY(LANE_BOTTOM) - topLeft.y)
                val corner = CornerRadius(18.dp.toPx() * viewport.scale)
                drawRoundRect(colors.fill, topLeft, laneSize, corner)
                val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx() * viewport.scale, 6.dp.toPx() * viewport.scale))
                drawRoundRect(colors.line, topLeft, laneSize, corner, style = Stroke(width = 1.3.dp.toPx(), pathEffect = dash))
            }

            val labelStyle = TextStyle(fontSize = 11.sp * viewport.scale, color = Palette.InkMuted)
            // 강조하지 않는 선을 먼저 그리고, 선택한 단어의 계보를 맨 위에 그린다.
            for (r in relations.sortedBy { it in ancestry }) {
                val hot = r in ancestry
                val touchesSelected = selected == r.fromId || selected == r.toId
                // 계승·차용·파생은 조상·출처(toId)에서 후대(fromId)로, 방향 없는 관계는 위쪽 단어에서 아래쪽 단어로 긋는다.
                val (a, b) = if (r.type.directed) {
                    rectOf(r.toId) to rectOf(r.fromId)
                } else {
                    val f = rectOf(r.fromId)
                    val t = rectOf(r.toId)
                    if (f.center.y <= t.center.y) f to t else t to f
                }
                val curve = curveBetween(a, b)
                val alpha = (if (hot) 1f else if (selected == null || touchesSelected) 0.8f else 0.45f) * (if (r.uncertain) 0.6f else 1f)
                drawRelationCurve(
                    curve,
                    r.type,
                    color = if (hot) Palette.Accent else relationColor(r.type),
                    width = if (hot) 2.4.dp else styleOf(r.type).width,
                    scale = viewport.scale,
                    alpha = alpha,
                )

                // 계승은 기본 관계라 라벨을 생략하고, 동계어는 선택한 단어와 이어질 때만 이름을 붙인다.
                val showLabel = when (r.type) {
                    RelationType.INHERITED_FROM -> r.uncertain
                    RelationType.COGNATE_OF -> touchesSelected
                    else -> true
                }
                if (showLabel && viewport.scale >= 0.55f) {
                    val label = textMeasurer.measure(r.type.labelKo + if (r.uncertain) " (불확실)" else "", labelStyle)
                    val mid = curve.pointAt(0.5f)
                    val topLeft = mid - Offset(label.size.width / 2f, label.size.height / 2f)
                    val pad = 3.dp.toPx() * viewport.scale
                    // 라벨 뒤를 그 자리의 층 색으로 덮어 선 위에서도 읽히게 한다.
                    val worldY = (mid.y - center.y - viewport.pan.y) / unitPx
                    val band = GraphLayout.BAND_EDGES.indexOfLast { it <= worldY }
                    drawRoundRect(
                        (if (band % 2 == 1 && band < Era.entries.size) Palette.Band else Palette.Paper).copy(alpha = 0.92f),
                        topLeft - Offset(pad, pad / 2),
                        Size(label.size.width + pad * 2, label.size.height + pad),
                        CornerRadius(pad * 2),
                    )
                    drawText(label, topLeft = topLeft, alpha = alpha)
                }
            }
        }

        for ((family, xs) in lanes) {
            val colors = familyColors(family)
            val onLeft = (xs.start + xs.endInclusive) / 2 < 0
            val left = screenX(xs.start - NodeWidth.value / 2 - LANE_PAD)
            val right = screenX(xs.endInclusive + NodeWidth.value / 2 + LANE_PAD)
            val top = screenY(LANE_TOP)
            // 칸 이름은 가운데 기준선에서 먼 쪽 위 모서리에 둬서, 가운데로 내려오는 공통 조상의 선을 피한다.
            // 지도의 일부라 노드처럼 배율을 따르고, 그 모서리를 기준으로 줄어든다.
            Column(
                Modifier
                    .offset {
                        val inset = 14.dp.toPx() * viewport.scale
                        val x = if (onLeft) left + inset else right - inset - 170.dp.toPx()
                        IntOffset(x.roundToInt(), (top + 10.dp.toPx() * viewport.scale).roundToInt())
                    }
                    .width(170.dp)
                    .graphicsLayer {
                        scaleX = viewport.scale
                        scaleY = viewport.scale
                        transformOrigin = TransformOrigin(if (onLeft) 0f else 1f, 0f)
                    },
                horizontalAlignment = if (onLeft) Alignment.Start else Alignment.End,
            ) {
                Text(family.labelKo, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = colors.label)
                Text(family.noteKo, fontSize = 11.sp, color = colors.label.copy(alpha = 0.75f))
            }
        }

        state.positions.forEach { (id, p) ->
            key(id) {
                val s = screenOf(p)
                WordNodeLabel(
                    word = lexicon.word(id),
                    selected = id == selected,
                    cognate = id in cognates,
                    learned = id in learned,
                    hiddenCount = state.hiddenNeighborCount(lexicon, id),
                    scale = viewport.scale,
                    onClick = { onSelect(id) },
                    onExpand = { onExpand(id) },
                    modifier = Modifier.offset {
                        IntOffset((s.x - NodeWidth.toPx() / 2).roundToInt(), (s.y - NodeHeight.toPx() / 2).roundToInt())
                    },
                )
            }
        }

        Row(Modifier.align(Alignment.TopEnd).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 확대 버튼 자리를 먼저 잡고, 범례는 남은 너비 안에서 펼친다.
            Legend(initiallyOpen = legendOpen, modifier = Modifier.weight(1f, fill = false))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ZoomButton("+", "확대") { onViewportChange { it.withZoom(1.25f, Offset.Zero) } }
                ZoomButton("−", "축소") { onViewportChange { it.withZoom(0.8f, Offset.Zero) } }
                ZoomButton("전체", "전체 보기", fontSize = 11.5.sp) {
                    scope.launch { animator.move { v -> v.movementToFit(boundsOf(state.positions.keys), viewSize, margin) } }
                }
            }
        }
    }
}

// shortcut: 휠 한 칸의 scrollDelta 크기를 Chrome·Safari에서만 맞췄다. 다른 입력 장치에서 너무 빠르거나 느리면 조정한다.
private const val WHEEL_ZOOM_RATE = 0.12f

/**
 * 지도의 단어. 상자 없이 표제어와 언어만 쓴다. 선택한 단어는 붉은 밑줄, 선택한 단어의 동계어는 파란 점선 밑줄로 표시하고,
 * 누르는 영역은 [NodeWidth]×[NodeHeight]로 둔다.
 */
@Composable
private fun WordNodeLabel(
    word: WordNode,
    selected: Boolean,
    cognate: Boolean,
    learned: Boolean,
    hiddenCount: Int,
    scale: Float,
    onClick: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emphasis by animateFloatAsState(if (selected) 1f else 0f)
    Box(
        modifier
            .size(NodeWidth, NodeHeight)
            .graphicsLayer {
                scaleX = scale * (1f + 0.04f * emphasis)
                scaleY = scale * (1f + 0.04f * emphasis)
            }
            .clip(RoundedCornerShape(8.dp))
            .semantics(mergeDescendants = true) {
                this.selected = selected
                stateDescription = if (learned) "학습 완료" else "미학습"
            }
            .clickable(role = Role.Button, onClickLabel = "선택", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                displayTextOf(word.word),
                fontFamily = LocalHeadwordFont.current,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                // 재구형은 * 표기에 더해 옅은 잉크로 문헌에 없는 형태임을 드러낸다.
                color = if (word.reconstructed) Palette.InkMuted else Palette.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.drawBehind {
                    val y = size.height - 1.dp.toPx()
                    when {
                        selected -> drawLine(Palette.Accent, Offset(0f, y), Offset(size.width, y), 2.5.dp.toPx())
                        cognate -> drawLine(
                            Palette.Cognate, Offset(0f, y), Offset(size.width, y), 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.5.dp.toPx(), 4.dp.toPx())),
                        )
                    }
                },
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (learned) LearnedMark()
                LanguageSwatch(word.language)
                Text(
                    word.language.nameKo,
                    fontSize = 11.sp,
                    color = if (selected) Palette.Accent else Palette.InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (cognate) {
                    Text(
                        "동계어",
                        fontSize = 10.sp,
                        color = Palette.Cognate,
                        modifier = Modifier.border(1.dp, Palette.Cognate.copy(alpha = 0.35f), RoundedCornerShape(4.dp)).padding(horizontal = 3.dp),
                    )
                }
                if (hiddenCount > 0) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Palette.Surface)
                            .border(1.dp, Palette.Rule, RoundedCornerShape(6.dp))
                            .clickable(role = Role.Button, onClickLabel = "관련 단어 펼치기", onClick = onExpand)
                            .semantics { contentDescription = "관련 단어 ${hiddenCount}개 펼치기" }
                            .padding(horizontal = 4.dp),
                    ) { Text("+$hiddenCount", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Palette.InkMuted) }
                }
            }
        }
    }
}

/** 학습 완료 표시(초록 원 안의 체크). ✓(U+2713)는 번들한 글꼴에 없어 글자 대신 그린다. */
@Composable
internal fun LearnedMark(size: Dp = 13.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        drawCircle(Palette.Learned)
        val check = Path().apply {
            moveTo(w * 0.28f, w * 0.52f)
            lineTo(w * 0.44f, w * 0.68f)
            lineTo(w * 0.73f, w * 0.36f)
        }
        drawPath(check, Color.White, style = Stroke(width = w * 0.13f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** 언어 색 견본. 색만으로 구분하지 않도록 항상 언어 이름과 함께 쓴다. */
@Composable
internal fun LanguageSwatch(language: Language, size: Dp = 7.dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(2.dp)).background(languageColor(language)))
}

@Composable
private fun ZoomButton(label: String, description: String, fontSize: TextUnit = 18.sp, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.Rule, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = fontSize, color = Palette.Ink) }
}

@Composable
private fun EmptyHint(onStart: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(24.dp).widthIn(max = 440.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "어디서부터 거슬러 올라갈까요?",
            fontFamily = LocalHeadwordFont.current,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            "영어 단어 하나를 고르면 그 조상과 친척 단어가 시대 순으로 펼쳐집니다.",
            fontSize = 14.sp,
            color = Palette.InkMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("en:father" to "father", "en:court" to "court", "en:courier" to "courier").forEach { (id, label) ->
                WordButton(label) { onStart(id) }
            }
        }
    }
}

/** 단어를 고르는 버튼. 표제어와 같은 세리프로 써서 "단어"라는 것이 보이게 한다. */
@Composable
internal fun WordButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.Rule, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) { Text(displayTextOf(text), fontFamily = LocalHeadwordFont.current, fontSize = 16.sp) }
}

// Material 기본 본문 스타일의 줄 높이(24sp)를 그대로 받으면 11sp 글자 사이가 너무 벌어진다.
private val LEGEND_LINE_HEIGHT = 16.sp

@Composable
private fun Legend(initiallyOpen: Boolean, modifier: Modifier = Modifier) {
    var open by remember(initiallyOpen) { mutableStateOf(initiallyOpen) }
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Palette.Surface.copy(alpha = 0.96f))
            .border(1.dp, Palette.Rule, RoundedCornerShape(8.dp))
            // 지도보다 길면(좁은 화면) 범례 안에서 스크롤한다. 너비를 IntrinsicSize로 맞추면 한글 글꼴 조각이 늦게 도착한
            // 첫 표시에서 줄이 꺾여 겹치는 것을 확인해, 너비는 내용대로 둔다.
            .verticalScroll(rememberScrollState()),
    ) {
        // 접었을 때도 확대 버튼과 같은 높이의 작은 머리줄만 남긴다.
        Row(
            Modifier
                .height(36.dp)
                .clickable(role = Role.Button, onClickLabel = if (open) "범례 접기" else "범례 펼치기") { open = !open }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("범례", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(if (open) "접기" else "펼치기", fontSize = 12.sp, color = Palette.InkMuted)
        }
        if (!open) return@Column
        // 방향 있는 관계, 방향 없는 관계, 지도 표시를 한 열씩 두고, 자리가 모자라면 다음 줄로 넘긴다.
        val (directed, undirected) = RelationType.entries.partition { it.directed }
        FlowRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (types in listOf(directed, undirected)) {
                Column {
                    types.forEach { type ->
                        LegendRow(type.labelKo) {
                            Canvas(Modifier.size(28.dp, 10.dp)) {
                                drawRelationLine(Offset(0f, size.height / 2), Offset(size.width, size.height / 2), type, 1f, 1f)
                            }
                        }
                    }
                }
            }
            Column {
                LegendRow("선택한 단어의 계보") {
                    Canvas(Modifier.size(28.dp, 10.dp)) {
                        drawLine(Palette.Accent, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2.4.dp.toPx())
                    }
                }
                LegendRow("선택한 단어의 동계어") {
                    Canvas(Modifier.size(28.dp, 10.dp)) {
                        drawLine(
                            Palette.Cognate, Offset(0f, size.height - 2.dp.toPx()), Offset(size.width, size.height - 2.dp.toPx()), 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.5.dp.toPx(), 4.dp.toPx())),
                        )
                    }
                }
                LegendRow("어파 칸") {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (family in LanguageFamily.entries) {
                            val colors = familyColors(family)
                            Box(
                                Modifier.size(12.dp, 10.dp)
                                    .background(colors.fill.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                    .border(1.dp, colors.line, RoundedCornerShape(3.dp)),
                            )
                        }
                    }
                }
            }
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)) {
            listOf(
                "화살표: 조상·출처 → 후대 형태",
                "유사 철자: 모양만 비교, 어원 근거 아님",
                "흐린 선·(불확실): 출처가 단정하지 않음",
            ).forEach { Text(it, fontSize = 11.sp, lineHeight = LEGEND_LINE_HEIGHT, color = Palette.InkMuted) }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LearnedMark(11.dp)
                Text("학습 완료 · +n: 펼칠 단어 수", fontSize = 11.sp, lineHeight = LEGEND_LINE_HEIGHT, color = Palette.InkMuted)
            }
        }
    }
}

@Composable
private fun LegendRow(text: String, sample: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(Modifier.width(30.dp), contentAlignment = Alignment.CenterStart) { sample() }
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 11.sp, lineHeight = LEGEND_LINE_HEIGHT)
    }
}
