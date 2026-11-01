package io.github.etymograph.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.etymograph.model.Language
import io.github.etymograph.model.LanguageFamily
import io.github.etymograph.model.RelationType
import io.github.etymograph.resources.Res
import io.github.etymograph.resources.noto_serif
import org.jetbrains.compose.resources.Font

/**
 * 어원 사전 지면을 목표로 한다. 종이(바탕)·잉크(글자와 선)·괘선(테두리)을 기본으로 두고,
 * 붉은 강조색은 "지금 보고 있는 단어와 그 계보"에만 쓴다. 버튼은 잉크색이다.
 */
internal object Palette {
    val Paper = Color(0xFFFBF8F2)
    /** 시대 층을 번갈아 칠하는 색. */
    val Band = Color(0xFFF5EFE4)
    val Surface = Color(0xFFFFFDF9)
    val Ink = Color(0xFF1F1B16)
    val InkMuted = Color(0xFF7B7165)
    val Faint = Color(0xFFB3A998)
    val Rule = Color(0xFFE4DBCB)
    val Accent = Color(0xFFB8402A)
    val Learned = Color(0xFF4F7B52)
    val Cognate = Color(0xFF5E7FA0)
}

/**
 * 언어 색은 규칙으로 정한다. 어파가 색상(게르만어파는 푸른 계열, 이탈리아어파는 붉은 흙빛 계열)을,
 * 시대가 명도(현대어는 진하게, 옛 단계는 옅게, 조어는 회색에 가깝게)를 정한다.
 */
internal fun languageColor(language: Language): Color = when (language) {
    Language.ENGLISH -> Color(0xFF2F5E8E)
    Language.GERMAN -> Color(0xFF2B7474)
    Language.OLD_ENGLISH -> Color(0xFF7FA2C6)
    Language.OLD_HIGH_GERMAN -> Color(0xFF7DB0AC)
    Language.PROTO_GERMANIC -> Color(0xFF8E9AA6)
    Language.LATIN -> Color(0xFF8A3A2E)
    Language.FRENCH -> Color(0xFFB4612C)
    Language.OLD_FRENCH -> Color(0xFFD49B6C)
    Language.ANGLO_NORMAN -> Color(0xFFC7A57F)
    Language.SPANISH -> Color(0xFFA8812A)
    Language.ITALIAN -> Color(0xFF6E8538)
    Language.PROTO_INDO_EUROPEAN -> Color(0xFF8A8276)
}

/** 어파 칸의 색. 칸은 시대 층 위에 겹치므로 아주 옅게 칠하고, 점선 테두리와 이름으로 구분한다. */
internal class FamilyColors(val fill: Color, val line: Color, val label: Color)

internal fun familyColors(family: LanguageFamily): FamilyColors = when (family) {
    LanguageFamily.GERMANIC -> FamilyColors(Color(0x0E467DBE), Color(0x613C6EA5), Color(0xD9345C87))
    LanguageFamily.ITALIC -> FamilyColors(Color(0x0EC86E32), Color(0x61B0673A), Color(0xD996542D))
}

/**
 * 관계 선의 색. 어원 관계(계승·차용·파생)는 잉크색 하나로 두고 실선·파선·점선으로 구분한다.
 * 어원 근거가 아닌 관계는 다른 색과 모양을 쓴다. 어느 것도 색만으로 구분하지 않도록 범례와 라벨을 함께 둔다.
 */
internal fun relationColor(type: RelationType): Color = when (type) {
    RelationType.INHERITED_FROM, RelationType.BORROWED_FROM, RelationType.DERIVED_FROM -> Palette.Ink
    RelationType.COGNATE_OF -> Palette.Cognate
    RelationType.SIMILAR_FORM -> Color(0xFF8A6A8E)
    RelationType.TRANSLATION_EQUIVALENT -> Palette.Faint
}

/** 표제어 글꼴. 옛 문자(ḗ, ḱ, æ, þ)까지 갖춘 세리프가 필요해 Noto Serif를 번들했다(OFL, licenses/ 참고). */
internal val LocalHeadwordFont = staticCompositionLocalOf<FontFamily> { FontFamily.Serif }

@Composable
internal fun EtymoTheme(content: @Composable () -> Unit) {
    val headword = FontFamily(
        Font(Res.font.noto_serif, FontWeight.Normal),
        Font(Res.font.noto_serif, FontWeight.SemiBold),
    )
    MaterialTheme(
        colors = lightColors(
            primary = Palette.Ink,
            primaryVariant = Palette.Ink,
            secondary = Palette.Accent,
            background = Palette.Paper,
            surface = Palette.Surface,
            onBackground = Palette.Ink,
            onSurface = Palette.Ink,
        ),
        shapes = Shapes(small = RoundedCornerShape(6.dp), medium = RoundedCornerShape(8.dp), large = RoundedCornerShape(10.dp)),
    ) {
        CompositionLocalProvider(LocalHeadwordFont provides headword, LocalContentColor provides Palette.Ink, content = content)
    }
}
