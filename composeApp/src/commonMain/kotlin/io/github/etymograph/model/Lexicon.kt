package io.github.etymograph.model

/** 단어와 관계를 담은 읽기 전용 사전. 생성할 때 데이터의 참조 무결성을 확인한다. */
class Lexicon(val words: List<WordNode>, val relations: List<RelationEdge>) {
    private val byId = words.associateBy { it.id }
    private val relationsById: Map<String, List<RelationEdge>> =
        relations.flatMap { listOf(it.fromId to it, it.toId to it) }.groupBy({ it.first }, { it.second })

    init {
        require(byId.size == words.size) { "중복된 단어 ID가 있습니다." }
        relations.forEach {
            require(it.fromId in byId && it.toId in byId) { "존재하지 않는 단어를 가리키는 관계: $it" }
            require(it.fromId != it.toId) { "자기 자신을 가리키는 관계: $it" }
        }
    }

    fun word(id: String): WordNode = byId.getValue(id)

    fun relationsOf(id: String): List<RelationEdge> = relationsById[id].orEmpty()

    /**
     * 대소문자와 재구형 표시 `*`를 무시하고 표제어를 찾는다. 정확히 일치 → 접두 일치 → 부분 일치, 현대어 → 역사적 언어 순이다.
     * 검색어와 표제어를 같은 방식으로 정규화해야 화면에 보이는 "*ḱers-"를 그대로 입력해도 찾을 수 있다.
     */
    fun search(query: String): List<WordNode> {
        val q = query.trim().lowercase().removePrefix("*")
        if (q.isEmpty()) return emptyList()
        // shortcut: 악센트를 접지 않아 "pere"로는 père를 찾지 못한다. 비영어 검색이 필요해지면 정규화를 추가한다.
        return words
            .mapNotNull { w ->
                val text = w.word.lowercase().removePrefix("*")
                val rank = when {
                    text == q -> 0
                    text.startsWith(q) -> 1
                    q in text -> 2
                    else -> return@mapNotNull null
                }
                Triple(w, rank, w.language.historical)
            }
            .sortedWith(compareBy({ it.second }, { it.third }, { it.first.language != Language.ENGLISH }))
            .map { it.first }
    }

    /**
     * 계승·차용·파생 관계를 따라 거슬러 올라간 관계 사슬(가까운 순). 각 관계의 [RelationEdge.toId]가 한 단계 위 조상이다.
     * 단어 대신 관계를 돌려주는 이유는 [RelationEdge.uncertain] 같은 연결 정보를 계보 표시까지 보존하기 위해서다.
     * 출처가 여럿이면 데이터에 먼저 적힌 관계를 따른다.
     */
    fun ancestryOf(id: String): List<RelationEdge> {
        val chain = mutableListOf<RelationEdge>()
        val seen = mutableSetOf(id)
        var current = id
        while (true) {
            val parent = relationsOf(current)
                .firstOrNull { it.type.directed && it.type.etymological && it.fromId == current }
            if (parent == null || !seen.add(parent.toId)) return chain
            chain += parent
            current = parent.toId
        }
    }
}
