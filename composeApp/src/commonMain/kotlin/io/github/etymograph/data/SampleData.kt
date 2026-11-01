package io.github.etymograph.data

import io.github.etymograph.model.Language.ANGLO_NORMAN
import io.github.etymograph.model.Language.ENGLISH
import io.github.etymograph.model.Language.FRENCH
import io.github.etymograph.model.Language.GERMAN
import io.github.etymograph.model.Language.ITALIAN
import io.github.etymograph.model.Language.LATIN
import io.github.etymograph.model.Language.OLD_ENGLISH
import io.github.etymograph.model.Language.OLD_FRENCH
import io.github.etymograph.model.Language.OLD_HIGH_GERMAN
import io.github.etymograph.model.Language.PROTO_GERMANIC
import io.github.etymograph.model.Language.PROTO_INDO_EUROPEAN
import io.github.etymograph.model.Language.SPANISH
import io.github.etymograph.model.Lexicon
import io.github.etymograph.model.RelationEdge
import io.github.etymograph.model.RelationType.BORROWED_FROM
import io.github.etymograph.model.RelationType.COGNATE_OF
import io.github.etymograph.model.RelationType.DERIVED_FROM
import io.github.etymograph.model.RelationType.INHERITED_FROM
import io.github.etymograph.model.RelationType.SIMILAR_FORM
import io.github.etymograph.model.WordNode

// 내용은 2026-10-25에 확인한 영어 Wiktionary 항목을 한국어로 요약·가공한 것이며 CC BY-SA 4.0으로 배포한다(README의 "데이터 라이선스").
// courtier의 중간 형태는 Dictionary.com(Random House)과 앵글로노르만어 사전(AND)으로 대조했다. 출처 URL은 항목마다 단다.
private fun wikt(title: String, section: String? = null) =
    "https://en.wiktionary.org/wiki/$title" + (section?.let { "#$it" } ?: "")

private const val GRIMMS_LAW = "https://en.wikipedia.org/wiki/Grimm%27s_law"
private const val AND_CORTEOUR = "https://anglo-norman.net/entry/corteour"
private const val DICTCOM_COURTIER = "https://www.dictionary.com/browse/courtier"

private val FATHER = wikt("father", "English")
private val FAEDER = wikt("fæder", "Old_English")
private val FADER_PG = wikt("Reconstruction:Proto-Germanic/fadēr")
private val PHTER_PIE = wikt("Reconstruction:Proto-Indo-European/ph₂tḗr")
private val VATER = wikt("Vater", "German")
private val FATER = wikt("fater", "Old_High_German")
private val PATER = wikt("pater", "Latin")
private val PERE = wikt("père", "French")
private val PADRE = wikt("padre", "Spanish")
private val PATERNAL = wikt("paternal", "English")
private val PATERNALIS = wikt("paternalis", "Latin")
private val PATERNUS = wikt("paternus", "Latin")

private val COURT = wikt("court", "English")
private val COUR = wikt("cour", "French")
private val CORT = wikt("cort", "Old_French")
private val COHORS = wikt("cohors", "Latin")
private val COURTIER = wikt("courtier", "English")
private val COURTESY = wikt("courtesy", "English")
private val COURTOISIE = wikt("courtoisie", "French")
private val COURTOIS = wikt("courtois", "French")
private val CORTOISIE = wikt("cortoisie", "Old_French")
private val COURIER = wikt("courier", "English")
private val COURRIER = wikt("courrier", "French")
private val CORRIERE = wikt("corriere", "Italian")
private val CORRERE = wikt("correre", "Italian")
private val CURRO = wikt("curro", "Latin")
private val COURIR = wikt("courir", "French")
private val KERS_PIE = wikt("Reconstruction:Proto-Indo-European/ḱers-")
private val HORSE = wikt("horse", "English")

private val words = listOf(
    // 그룹 A: father 계열
    WordNode(
        id = "en:father", language = ENGLISH, word = "father", partOfSpeech = "명사",
        meaningKo = "아버지", ipa = "/ˈfɑː.ðə(ɹ)/ (영국), /ˈfɑ.ðɚ/ (미국)",
        etymologyNote = "중세 영어 fader, 고대 영어 fæder를 거쳐 게르만 조어 *fadēr, 인도유럽 조어 *ph₂tḗr로 거슬러 올라가는 " +
            "고유어다. 독일어 Vater와는 게르만어 안에서, 라틴어 pater와는 인도유럽 조어 단계에서 뿌리가 같다.",
        sourceUrls = listOf(FATHER),
    ),
    WordNode(
        id = "ang:faeder", language = OLD_ENGLISH, word = "fæder", partOfSpeech = "명사",
        meaningKo = "아버지", ipa = "/ˈfæ.der/",
        etymologyNote = "서게르만 조어 *fader를 거쳐 게르만 조어 *fadēr에서 이어졌다. 고대 고지 독일어 fater, 고대 노르드어 faðir와 동계어다.",
        sourceUrls = listOf(FAEDER),
    ),
    WordNode(
        id = "gem-pro:fader", language = PROTO_GERMANIC, word = "*fadēr", partOfSpeech = "명사",
        meaningKo = "아버지",
        etymologyNote = "더 이른 형태 *faþḗr를 거쳐 인도유럽 조어 *ph₂tḗr에서 왔다. 문헌에 남지 않아 비교언어학으로 복원한 재구형이다.",
        sourceUrls = listOf(FADER_PG),
    ),
    WordNode(
        id = "ine-pro:ph2ter", language = PROTO_INDO_EUROPEAN, word = "*ph₂tḗr", partOfSpeech = "명사",
        meaningKo = "아버지",
        etymologyNote = "흔히 *peh₂-(보호하다, 가축을 치다) + 행위자 접미사 *-tḗr로 분석하지만, 유아어 *pa에서 왔다는 설도 있어 " +
            "기원은 확정되지 않았다. 재구형이다.",
        sourceUrls = listOf(PHTER_PIE),
    ),
    WordNode(
        id = "de:vater", language = GERMAN, word = "Vater", partOfSpeech = "명사(남성)",
        meaningKo = "아버지", ipa = "/ˈfaːtɐ/",
        etymologyNote = "중세 고지 독일어 vater, 고대 고지 독일어 fater를 거쳐 게르만 조어 *fadēr에서 이어졌다. " +
            "Wiktionary는 영어 father, 네덜란드어 vader를 게르만어 동계어로 든다.",
        sourceUrls = listOf(VATER),
    ),
    WordNode(
        id = "goh:fater", language = OLD_HIGH_GERMAN, word = "fater", partOfSpeech = "명사",
        meaningKo = "아버지",
        etymologyNote = "서게르만 조어 *fader를 거쳐 게르만 조어 *fadēr에서 이어졌다.",
        sourceUrls = listOf(FATER),
    ),
    WordNode(
        id = "la:pater", language = LATIN, word = "pater", partOfSpeech = "명사",
        meaningKo = "아버지", ipa = "[ˈpa.tɛr] (고전 라틴어)",
        etymologyNote = "이탈리아 조어 *patēr를 거쳐 인도유럽 조어 *ph₂tḗr에서 이어졌다. 프랑스어 père와 스페인어 padre가 " +
            "이 단어(대격 patrem)에서 나왔다.",
        sourceUrls = listOf(PATER),
    ),
    WordNode(
        id = "fr:pere", language = FRENCH, word = "père", partOfSpeech = "명사(남성)",
        meaningKo = "아버지; 신부(성직자)", ipa = "/pɛʁ/",
        etymologyNote = "중세 프랑스어 pere, 고대 프랑스어 pedre·pere를 거쳐 라틴어 pater(대격 patrem)에서 이어졌다.",
        sourceUrls = listOf(PERE),
    ),
    WordNode(
        id = "es:padre", language = SPANISH, word = "padre", partOfSpeech = "명사(남성)",
        meaningKo = "아버지; 신부(성직자)", ipa = "/ˈpadɾe/",
        etymologyNote = "고대 스페인어 padre를 거쳐 라틴어 pater(대격 patrem)에서 이어졌다.",
        sourceUrls = listOf(PADRE),
    ),
    WordNode(
        id = "en:paternal", language = ENGLISH, word = "paternal", partOfSpeech = "형용사",
        meaningKo = "아버지의, 부계의; 아버지다운", ipa = "/pəˈtɜː.nəl/ (영국), /pəˈtɜɹ.nəl/ (미국)",
        etymologyNote = "고대 프랑스어 paternal(12세기)을 통해 들어온 말로, 통속 라틴어 paternālis ← 라틴어 paternus(아버지의) ← pater로 " +
            "이어진다. father와 같은 인도유럽 조어 뿌리를 공유하지만, father는 물려받은 고유어이고 paternal은 라틴계 차용어다.",
        sourceUrls = listOf(PATERNAL),
    ),
    WordNode(
        id = "la:paternalis", language = LATIN, word = "paternālis", partOfSpeech = "형용사",
        meaningKo = "아버지의", period = "통속 라틴어",
        etymologyNote = "paternus(아버지의)에 접미사 -ālis를 붙인 말이다. paternus는 pater + -nus다.",
        sourceUrls = listOf(PATERNALIS, PATERNUS),
    ),

    // 그룹 B: court 계열
    WordNode(
        id = "en:court", language = ENGLISH, word = "court", partOfSpeech = "명사",
        meaningKo = "궁정, 왕실; 법정; 안뜰; (테니스 등의) 코트", ipa = "/kɔːt/ (영국), /kɔɹt/ (미국)",
        etymologyNote = "중세 영어 court를 거쳐 고대 프랑스어 cort·curt에서 들어왔다. 이 형태는 라틴어 cohors가 줄어든 후기 라틴어 " +
            "cōrs에서 왔다. 영어 cohort와는 같은 라틴어 단어에서 갈라진 쌍형어(doublet)다.",
        sourceUrls = listOf(COURT),
    ),
    WordNode(
        id = "fr:cour", language = FRENCH, word = "cour", partOfSpeech = "명사(여성)",
        meaningKo = "안뜰, 마당; 궁정; 법원", ipa = "/kuʁ/",
        etymologyNote = "중세 프랑스어 court, 고대 프랑스어 cort를 거쳐 라틴어 cortem(cohors의 축약형)에서 이어졌다.",
        sourceUrls = listOf(COUR),
    ),
    WordNode(
        id = "fro:cort", language = OLD_FRENCH, word = "cort", partOfSpeech = "명사",
        meaningKo = "궁정(군주의)",
        etymologyNote = "라틴어 cōrtem(더 이른 형태 cohortem)에서 왔다. 현대 프랑스어 cour의 조상이며, 영어 court가 이 말을 받아들였다.",
        sourceUrls = listOf(CORT),
    ),
    WordNode(
        id = "la:cohors", language = LATIN, word = "cohors", partOfSpeech = "명사",
        meaningKo = "울타리 친 마당, 뜰; 수행원 무리; (로마 군단의) 대대", ipa = "[ˈko.(ɦ)ɔrs] (고전 라틴어)",
        etymologyNote = "이탈리아 조어 *kom + *horti-로 분석되며, 뒷부분은 *hortos(울타리)와 같은 뿌리일 가능성이 크다. " +
            "대격 cohortem이 cōrtem으로 줄어든 형태가 로망스어로 이어졌다.",
        sourceUrls = listOf(COHORS),
    ),
    WordNode(
        id = "en:courtier", language = ENGLISH, word = "courtier", partOfSpeech = "명사",
        meaningKo = "궁정 신하, 조신; 아첨꾼", ipa = "/ˈkɔːtɪə/ (영국), /ˈkɔɹtiɚ/ (미국)",
        etymologyNote = "중세 영어 courteour를 거쳐 앵글로노르만어 corteour에서 들어왔다. corteour는 고대 프랑스어 cortoier(궁정에 " +
            "출입하다)에서 만든 말이고, cortoier는 cort(궁정)에서 나왔다. court + -ier로 보는 것은 현대 형태에 따른 표면 분석이다.",
        sourceUrls = listOf(COURTIER, DICTCOM_COURTIER),
    ),
    WordNode(
        id = "xno:corteour", language = ANGLO_NORMAN, word = "corteour", partOfSpeech = "명사",
        meaningKo = "궁정에 출입하는 사람",
        etymologyNote = "고대 프랑스어 cortoier(궁정에 출입하다)에 행위자 접미사 -our를 붙인 말로, 영어 courtier의 출처다(courteour·courteiour로도 " +
            "적는다). 앵글로노르만어 사전(AND)에 실린 같은 철자의 corteour는 '중개인'을 뜻하며 currere(달리다) 계열로 분류된 다른 말이다.",
        sourceUrls = listOf(COURTIER, DICTCOM_COURTIER, AND_CORTEOUR),
    ),
    WordNode(
        id = "en:courtesy", language = ENGLISH, word = "courtesy", partOfSpeech = "명사",
        meaningKo = "예의, 공손함; 호의", ipa = "/ˈkɜːtəsi/ (영국), /ˈkɝɾəsi/ (미국)",
        etymologyNote = "중세 영어 curteisie, 앵글로노르만어 curtesie를 거쳐 고대 프랑스어 curteisie·cortoisie에서 들어왔다. " +
            "courteous + -y로 볼 수 있다.",
        sourceUrls = listOf(COURTESY),
    ),
    WordNode(
        id = "fr:courtoisie", language = FRENCH, word = "courtoisie", partOfSpeech = "명사(여성)",
        meaningKo = "예의, 정중함", ipa = "/kuʁ.twa.zi/",
        etymologyNote = "고대 프랑스어 cortoisie에서 이어졌다. cortoisie는 cortois(예의 바른) + -ie이고, cortois는 cort(현대 프랑스어 cour) + -ois다.",
        sourceUrls = listOf(COURTOISIE, COURTOIS),
    ),
    WordNode(
        id = "fro:cortoisie", language = OLD_FRENCH, word = "cortoisie", partOfSpeech = "명사",
        meaningKo = "기사도; 용맹",
        etymologyNote = "cortois(예의 바른) + -ie로 만든 말이며, cortois는 cort(궁정)에서 나왔다. 고대 프랑스어에서는 '기사도, 용맹'을 " +
            "뜻했고, 영어 courtesy와 현대 프랑스어 courtoisie로 이어졌다.",
        sourceUrls = listOf(CORTOISIE, COURTOISIE, COURTOIS),
    ),

    // 그룹 B: courier 계열
    WordNode(
        id = "en:courier", language = ENGLISH, word = "courier", partOfSpeech = "명사",
        meaningKo = "급송 배달원, 택배; 전령", ipa = "/ˈkʊ.ɹi.ə/ (영국), /ˈkʊɹ.i.ɚ/ (미국)",
        etymologyNote = "두 경로가 합쳐진 말이다. 하나는 고대 프랑스어 coreor(달리다를 뜻하는 corir의 행위자 명사)에서 온 중세 영어 corour, " +
            "다른 하나는 이탈리아어 corriere를 차용한 중세 프랑스어 courrier를 다시 받아들인 중세 영어 courier다.",
        sourceUrls = listOf(COURIER),
    ),
    WordNode(
        id = "fr:courrier", language = FRENCH, word = "courrier", partOfSpeech = "명사(남성)",
        meaningKo = "우편물, 편지; (옛) 전령", ipa = "/ku.ʁje/",
        etymologyNote = "중세 프랑스어 corier에서 왔으며, corier는 이탈리아어 corriere를 차용한 말이다.",
        sourceUrls = listOf(COURRIER),
    ),
    WordNode(
        id = "it:corriere", language = ITALIAN, word = "corriere", partOfSpeech = "명사",
        meaningKo = "전령; 배달원", ipa = "/korˈrjɛ.re/",
        etymologyNote = "correre(달리다)에 행위자 접미사 -iere를 붙인 말이다.",
        sourceUrls = listOf(CORRIERE),
    ),
    WordNode(
        id = "it:correre", language = ITALIAN, word = "correre", partOfSpeech = "동사",
        meaningKo = "달리다", ipa = "/ˈkor.re.re/",
        etymologyNote = "라틴어 currere에서 이어졌다.",
        sourceUrls = listOf(CORRERE),
    ),
    WordNode(
        id = "la:currere", language = LATIN, word = "currere", partOfSpeech = "동사",
        meaningKo = "달리다",
        etymologyNote = "사전 표제형은 1인칭 단수 currō다. 이탈리아 조어 *korsō를 거쳐 인도유럽 조어 *ḱr̥séti, 어근 *ḱers-(달리다)에서 " +
            "이어졌다. Wiktionary는 라틴어 currus(수레)와 영어 horse를 동계어로 든다.",
        sourceUrls = listOf(CURRO),
    ),
    WordNode(
        id = "fr:courir", language = FRENCH, word = "courir", partOfSpeech = "동사",
        meaningKo = "달리다", ipa = "/ku.ʁiʁ/",
        etymologyNote = "고대 프랑스어 courre·corre를 거쳐 라틴어 currere에서 이어졌다. -ir 동사의 영향으로 지금의 형태가 되었다.",
        sourceUrls = listOf(COURIR),
    ),
    WordNode(
        id = "ine-pro:kers", language = PROTO_INDO_EUROPEAN, word = "*ḱers-", partOfSpeech = "동사 어근",
        meaningKo = "달리다",
        etymologyNote = "재구된 어근이다. 라틴어 currere가 이 어근에서 나왔고, 영어 horse도 이 어근에서 나왔을 가능성이 높다고 본다.",
        sourceUrls = listOf(KERS_PIE),
    ),
    WordNode(
        id = "en:horse", language = ENGLISH, word = "horse", partOfSpeech = "명사",
        meaningKo = "말(동물)", ipa = "/hɔːs/ (영국), /hɔɹs/ (미국)",
        etymologyNote = "중세 영어 hors, 고대 영어 hors, 게르만 조어 *hrussą를 거쳐 왔다. 그 이전은 인도유럽 조어 *ḱr̥sós(탈것) ← *ḱers-(달리다)로 " +
            "보는 견해가 가장 유력하지만 확정되지는 않았다.",
        sourceUrls = listOf(HORSE),
    ),
)

private val relations = listOf(
    // 그룹 A
    RelationEdge("en:father", "ang:faeder", INHERITED_FROM, "중세 영어 fader를 거쳐 이어졌다.", listOf(FATHER)),
    RelationEdge("ang:faeder", "gem-pro:fader", INHERITED_FROM, "서게르만 조어 *fader를 거쳐 이어졌다.", listOf(FAEDER)),
    RelationEdge("gem-pro:fader", "ine-pro:ph2ter", INHERITED_FROM, "더 이른 형태 *faþḗr를 거쳐 이어졌다.", listOf(FADER_PG)),
    RelationEdge("de:vater", "goh:fater", INHERITED_FROM, "중세 고지 독일어 vater를 거쳐 이어졌다.", listOf(VATER)),
    RelationEdge("goh:fater", "gem-pro:fader", INHERITED_FROM, "서게르만 조어 *fader를 거쳐 이어졌다.", listOf(FATER)),
    RelationEdge("la:pater", "ine-pro:ph2ter", INHERITED_FROM, "이탈리아 조어 *patēr를 거쳐 이어졌다.", listOf(PATER)),
    RelationEdge("fr:pere", "la:pater", INHERITED_FROM, "고대 프랑스어 pedre·pere, 중세 프랑스어 pere를 거쳐 이어졌다.", listOf(PERE)),
    RelationEdge("es:padre", "la:pater", INHERITED_FROM, "고대 스페인어 padre를 거쳐 이어졌다.", listOf(PADRE)),
    RelationEdge(
        "en:paternal", "la:paternalis", BORROWED_FROM,
        "고대 프랑스어 paternal을 거쳐 영어에 들어왔다. Wiktionary는 이 경로를 넓은 의미의 '유래'(derived)로 표기한다.",
        listOf(PATERNAL),
    ),
    RelationEdge("la:paternalis", "la:pater", DERIVED_FROM, "paternus(pater + -nus)에 -ālis를 붙여 만들었다.", listOf(PATERNALIS, PATERNUS)),
    RelationEdge("en:father", "de:vater", COGNATE_OF, "둘 다 게르만 조어 *fadēr에서 이어진 게르만어 동계어다.", listOf(VATER)),
    RelationEdge(
        "en:father", "la:pater", COGNATE_OF,
        "둘 다 인도유럽 조어 *ph₂tḗr에서 이어졌다. 게르만어의 f가 라틴어의 p에 대응한다(그림의 법칙).",
        listOf(FADER_PG, PATER, GRIMMS_LAW),
    ),

    // 그룹 B
    RelationEdge("en:court", "fro:cort", BORROWED_FROM, "중세 영어 court를 거쳐 고대 프랑스어 cort·curt를 받아들였다.", listOf(COURT)),
    RelationEdge("fr:cour", "fro:cort", INHERITED_FROM, "중세 프랑스어 court를 거쳐 이어졌다.", listOf(COUR)),
    RelationEdge("fro:cort", "la:cohors", INHERITED_FROM, "cohortem이 줄어든 cōrtem에서 이어졌다.", listOf(CORT, COUR)),
    // 차용(영어 ← 앵글로노르만어)과 파생(cort → corteour)을 나눠 둔다. 중간 형태는 Wiktionary와 Dictionary.com이 함께 뒷받침한다.
    RelationEdge("en:courtier", "xno:corteour", BORROWED_FROM, "중세 영어 courteour를 거쳐 받아들였다.", listOf(COURTIER, DICTCOM_COURTIER)),
    RelationEdge(
        "xno:corteour", "fro:cort", DERIVED_FROM,
        "고대 프랑스어 cortoier(궁정에 출입하다, cort에서 파생)에 행위자 접미사 -our를 붙여 만들었다.",
        listOf(COURTIER, DICTCOM_COURTIER),
    ),
    RelationEdge("fro:cortoisie", "fro:cort", DERIVED_FROM, "cort + -ois → cortois(예의 바른)에 -ie를 붙여 만들었다.", listOf(COURTOISIE, COURTOIS)),
    RelationEdge("fr:courtoisie", "fro:cortoisie", INHERITED_FROM, "고대 프랑스어 cortoisie에서 이어졌다.", listOf(COURTOISIE)),
    RelationEdge(
        "en:courtesy", "fro:cortoisie", BORROWED_FROM,
        "앵글로노르만어 curtesie, 중세 영어 curteisie를 거쳐 들어왔다.",
        listOf(COURTESY, CORTOISIE),
    ),
    RelationEdge(
        "en:courier", "fr:courrier", BORROWED_FROM,
        "중세 프랑스어 단계의 courrier를 받아들였다. 고대 프랑스어 coreor에서 온 중세 영어 corour와 합쳐졌다.",
        listOf(COURIER),
    ),
    RelationEdge("fr:courrier", "it:corriere", BORROWED_FROM, "중세 프랑스어 corier 단계에서 이탈리아어 corriere를 차용했다.", listOf(COURRIER, CORRIERE)),
    RelationEdge("it:corriere", "it:correre", DERIVED_FROM, "correre에 행위자 접미사 -iere를 붙여 만들었다.", listOf(CORRIERE)),
    RelationEdge("it:correre", "la:currere", INHERITED_FROM, "라틴어 currere에서 이어졌다.", listOf(CORRERE)),
    RelationEdge("fr:courir", "la:currere", INHERITED_FROM, "고대 프랑스어 courre·corre를 거쳐 이어졌다.", listOf(COURIR)),
    RelationEdge("la:currere", "ine-pro:kers", INHERITED_FROM, "이탈리아 조어 *korsō, 인도유럽 조어 *ḱr̥séti를 거쳐 이어졌다.", listOf(CURRO, CORRERE)),
    RelationEdge(
        "en:horse", "ine-pro:kers", INHERITED_FROM,
        "게르만 조어 *hrussą를 거쳐 *ḱr̥sós(탈것) ← *ḱers-로 보는 견해가 가장 유력하다(Wiktionary: \"most likely\").",
        listOf(HORSE), uncertain = true,
    ),

    // 유사 철자 비교: 어원이 다르다는 설명은 양쪽 항목에서 서로 다른 라틴어 근원을 확인한 경우에만 적는다.
    RelationEdge(
        "en:court", "en:courier", SIMILAR_FORM,
        "철자는 비슷하지만 어원이 다르다. court는 라틴어 cohors(뜰)에서, courier는 '달리다'를 뜻하는 동사 계열" +
            "(이탈리아어 correre ← 라틴어 currere, 고대 프랑스어 corir)에서 왔다.",
        listOf(COURT, COURIER, CORRERE),
    ),
    RelationEdge(
        "en:courtier", "en:courier", SIMILAR_FORM,
        "t 한 글자 차이지만 어원이 다르다. courtier는 cort(궁정) 계열, courier는 '달리다' 동사 계열이다.",
        listOf(COURTIER, COURIER),
    ),
    RelationEdge(
        "fr:cour", "fr:courir", SIMILAR_FORM,
        "형태는 비슷하지만 어원이 다르다. cour는 라틴어 cohors(cortem)에서, courir는 라틴어 currere에서 왔다.",
        listOf(COUR, COURIR),
    ),
)

val SampleLexicon = Lexicon(words, relations)
