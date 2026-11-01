package io.github.etymograph.model

/** 지도의 시대 층. 위에서 아래로 오래된 순서다. [shortKo]는 좁은 화면에서 이름이 음절 중간에서 줄바뀜하지 않게 쓴다. */
enum class Era(val labelKo: String, val shortKo: String, val noteKo: String) {
    PROTO_INDO_EUROPEAN("인도유럽 조어", "인도유럽", "재구형 · 문헌 이전"),
    PROTO_BRANCH("어파 조어", "어파 조어", "재구형"),
    ANCIENT("고대 · 고전어", "고대", "문헌 시대"),
    MODERN("현대어", "현대", "지금 쓰는 말"),
}

/** 인도유럽어족 안의 어파. 어파 이전 단계(인도유럽 조어)는 어느 어파에도 속하지 않는다. */
enum class LanguageFamily(val labelKo: String, val noteKo: String) {
    GERMANIC("게르만어파", "영어 · 독일어와 그 조상"),
    ITALIC("이탈리아어파", "라틴어와 로망스어"),
}

/** 언어 또는 역사적 언어 단계. [code]는 Wiktionary 언어 코드를 따른다. */
enum class Language(val code: String, val nameKo: String, val era: Era, val family: LanguageFamily?) {
    ENGLISH("en", "영어", Era.MODERN, LanguageFamily.GERMANIC),
    OLD_ENGLISH("ang", "고대 영어", Era.ANCIENT, LanguageFamily.GERMANIC),
    GERMAN("de", "독일어", Era.MODERN, LanguageFamily.GERMANIC),
    OLD_HIGH_GERMAN("goh", "고대 고지 독일어", Era.ANCIENT, LanguageFamily.GERMANIC),
    PROTO_GERMANIC("gem-pro", "게르만 조어", Era.PROTO_BRANCH, LanguageFamily.GERMANIC),
    PROTO_INDO_EUROPEAN("ine-pro", "인도유럽 조어", Era.PROTO_INDO_EUROPEAN, null),
    LATIN("la", "라틴어", Era.ANCIENT, LanguageFamily.ITALIC),
    FRENCH("fr", "프랑스어", Era.MODERN, LanguageFamily.ITALIC),
    OLD_FRENCH("fro", "고대 프랑스어", Era.ANCIENT, LanguageFamily.ITALIC),
    ANGLO_NORMAN("xno", "앵글로노르만어", Era.ANCIENT, LanguageFamily.ITALIC),
    SPANISH("es", "스페인어", Era.MODERN, LanguageFamily.ITALIC),
    ITALIAN("it", "이탈리아어", Era.MODERN, LanguageFamily.ITALIC),
    ;

    val historical: Boolean get() = era != Era.MODERN
}

data class WordNode(
    val id: String,
    val language: Language,
    val word: String,
    val partOfSpeech: String,
    val meaningKo: String,
    val etymologyNote: String,
    val sourceUrls: List<String>,
    val ipa: String? = null,
    val period: String? = null,
) {
    /** 문헌 없이 비교언어학으로 복원한 형태는 관례대로 `*`로 시작한다. */
    val reconstructed: Boolean get() = word.startsWith("*")
}

/**
 * 관계 유형. [etymological]이 false인 관계는 어원적 연결이 아니다.
 * [directed]인 관계에서 [RelationEdge.fromId]는 후대 형태(또는 차용한 쪽), [RelationEdge.toId]는 조상(또는 출처)이다.
 */
enum class RelationType(val labelKo: String, val etymological: Boolean, val directed: Boolean) {
    INHERITED_FROM("계승", etymological = true, directed = true),
    BORROWED_FROM("차용", etymological = true, directed = true),

    /** 같은 언어 안에서 접사를 붙여 만든 말. 사양서의 유형 목록에 없어 추가했다(예: pater → paternālis). */
    DERIVED_FROM("파생", etymological = true, directed = true),
    COGNATE_OF("동계어", etymological = true, directed = false),

    /** 외형이 비슷할 뿐이다. 어원이 같다는 주장도, 다르다는 주장도 아니다. */
    SIMILAR_FORM("유사 철자", etymological = false, directed = false),
    TRANSLATION_EQUIVALENT("번역 대응", etymological = false, directed = false),
}

data class RelationEdge(
    val fromId: String,
    val toId: String,
    val type: RelationType,
    val description: String,
    val sourceUrls: List<String>,
    /** 출처가 "most likely"처럼 단정하지 않거나, 출처끼리 어긋나는 연결. */
    val uncertain: Boolean = false,
) {
    fun otherEnd(id: String): String = if (fromId == id) toId else fromId
}
