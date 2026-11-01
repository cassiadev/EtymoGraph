package io.github.etymograph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import io.github.etymograph.resources.Res
import io.github.etymograph.resources.noto_sans_kr
import io.github.etymograph.resources.noto_serif
import io.github.etymograph.ui.Palette
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

private const val LEARNED_KEY = "etymograph.learned"

// 사생활 보호 모드 등에서 localStorage 접근이 예외를 던지면 기록 없이 계속 동작한다.
@OptIn(ExperimentalWasmJsInterop::class)
private fun readStorage(key: String): String? =
    js("{ try { return localStorage.getItem(key); } catch (e) { return null; } }")

@OptIn(ExperimentalWasmJsInterop::class)
private fun writeStorage(key: String, value: String): Unit =
    js("{ try { localStorage.setItem(key, value); } catch (e) {} }")

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    val learned = readStorage(LEARNED_KEY)?.split(',')?.filter { it.isNotBlank() }?.toSet().orEmpty()
    ComposeViewport {
        // 한글(앱에 쓰는 글자만 담은 Noto Sans KR)과 발음 기호(Noto Serif) 글꼴을 미리 불러와 빠진 글자의 대체 글꼴로 등록한 뒤 그린다.
        // 그러지 않으면 Compose가 대체 글꼴을 몇 초 늦게 받아 와, 그동안 한글과 발음 기호가 빈 상자로 보인다.
        val korean by preloadFont(Res.font.noto_sans_kr)
        val serif by preloadFont(Res.font.noto_serif)
        val resolver = LocalFontFamilyResolver.current
        var fontsReady by remember { mutableStateOf(false) }
        LaunchedEffect(korean, serif) {
            val k = korean ?: return@LaunchedEffect
            val s = serif ?: return@LaunchedEffect
            runCatching {
                resolver.preload(FontFamily(k))
                resolver.preload(FontFamily(s))
            }.onFailure { println("EtymoGraph: 글꼴을 미리 불러오지 못했습니다: $it") }
            fontsReady = true
        }
        // 글꼴을 받지 못하는 환경에서도 빈 화면에 멈추지 않게, 3초가 지나면 기다리지 않고 그린다.
        // 그때 빠진 글자는 Compose의 자동 대체 글꼴이 늦게 채운다.
        LaunchedEffect(Unit) {
            delay(3_000)
            if (!fontsReady) {
                println("EtymoGraph: 글꼴을 3초 안에 불러오지 못해 기다리지 않고 그립니다.")
                fontsReady = true
            }
        }
        if (fontsReady) {
            App(initialLearned = learned, onLearnedChange = { writeStorage(LEARNED_KEY, it.joinToString(",")) })
        } else {
            Box(Modifier.fillMaxSize().background(Palette.Surface))
        }
    }
}
