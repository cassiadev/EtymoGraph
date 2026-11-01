package io.github.etymograph.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em

/**
 * Compose Web이 자동으로 받아 오는 Noto 폴백 글꼴(Google Fonts 하위 집합)에는 아래 첨자 숫자(U+2080–2089, *ph₂tḗr의 ₂)와
 * 결합용 아래 고리(U+0325, *ḱr̥séti의 r̥)가 없어 빈 상자로 보인다. 데이터는 그대로 두고 화면에서만 대체 표기로 바꾼다.
 */
private val SUBSCRIPT_SHIFT = BaselineShift(-0.25f)

internal fun displayTextOf(text: String): AnnotatedString = buildAnnotatedString {
    for (c in text) {
        when (c) {
            // 기본 BaselineShift.Subscript(-0.5)는 숫자를 글꼴의 아래쪽 여백 밖까지 내려 줄 높이를 늘리고, 그 아래 줄을 밀어낸다.
            in '₀'..'₉' ->
                withStyle(SpanStyle(baselineShift = SUBSCRIPT_SHIFT, fontSize = 0.75.em)) { append('0' + (c - '₀')) }
            '̥' -> append('˳') // MODIFIER LETTER LOW RING
            else -> append(c)
        }
    }
}
