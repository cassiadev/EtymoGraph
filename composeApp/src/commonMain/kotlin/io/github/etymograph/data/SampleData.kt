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

// 어원 자료를 한국어로 요약·가공했다. 라이선스는 README의 "데이터 라이선스"를 참고한다.
// 출처와 대조에 사용한 사전은 항목마다 연결한다.
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

private val VERSE = wikt("verse", "English")
private val VERSED = wikt("versed", "English")
private val VERSATILE = wikt("versatile", "English")
private val VERTERE = wikt("verto", "Latin")
private val VERSUS = wikt("versus", "Latin")
private val VERSARE = wikt("verso", "Latin")
private val VERSARI = wikt("versor", "Latin")
private val VERSATUS = wikt("versatus", "Latin")
private val VERSATILIS = wikt("versatilis", "Latin")
private const val MW_VERSE = "https://www.merriam-webster.com/dictionary/verse"
private const val MW_VERSED = "https://www.merriam-webster.com/dictionary/versed"
private const val MW_VERSATILE = "https://www.merriam-webster.com/dictionary/versatile"

private val WEAVE = wikt("weave", "English")
private val WEB = wikt("web", "English")
private val WEFAN = wikt("wefan", "Old_English")
private val WEBB = wikt("webb", "Old_English")
private val WEBANA = wikt("Reconstruction:Proto-Germanic/webaną")
private val WABJA = wikt("Reconstruction:Proto-Germanic/wabją")
private const val AHD_WEAVE = "https://www.ahdictionary.com/word/search.html?q=weave"
private const val AHD_WEB = "https://www.ahdictionary.com/word/search.html?q=web"
private const val WEB_PROPOSAL = "https://www.w3.org/History/19921103-hypertext/hypertext/WWW/Proposal.html"

private val WEDGE = wikt("wedge", "English")
private val WECG = wikt("wecg", "Old_English")
private val POTATO_WEDGE = wikt("potato_wedge", "English")
private const val AHD_WEDGE = "https://www.ahdictionary.com/word/search.html?q=wedge"

private val PRESCIENT = wikt("prescient", "English")
private val SCIENCE = wikt("science", "English")
private val OMNISCIENT = wikt("omniscient", "English")
private val SCIRE = wikt("scio", "Latin")
private val SCIENS = wikt("sciens", "Latin")
private val SCIENTIA = wikt("scientia", "Latin")
private val PRAESCIENS = wikt("praesciens", "Latin")
private val OMNISCIENS = wikt("omnisciens", "Latin")
private const val AHD_PRESCIENT = "https://www.ahdictionary.com/word/search.html?q=prescient"
private const val AHD_SCIENCE = "https://www.ahdictionary.com/word/search.html?q=science"
private const val AHD_OMNISCIENT = "https://www.ahdictionary.com/word/search.html?q=omniscient"

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

    // verse 계열
    WordNode(
        id = "en:verse", language = ENGLISH, word = "verse", partOfSpeech = "명사",
        meaningKo = "시의 한 행, 노래의 한 절, 랩의 한 구간", ipa = "/vɜːs/ (영국), /vɝs/ (미국)",
        etymologyNote = "라틴어 명사 versus는 밭고랑과 글이나 시의 한 행을 가리켰다. 영어에서는 고대 영어 fers와 " +
            "앵글로프랑스어 vers가 중세 영어에서 합쳐졌다. 오늘날 힙합에서는 랩을 하는 한 구간을 벌스라고 한다.",
        sourceUrls = listOf(VERSE, MW_VERSE),
    ),
    WordNode(
        id = "en:versed", language = ENGLISH, word = "versed", partOfSpeech = "형용사",
        meaningKo = "어떤 분야에 정통한", ipa = "/vɜːst/ (영국), /vɝst/ (미국)",
        etymologyNote = "라틴어 versārī(어떤 일에 관여하다)의 분사 versātus에서 왔다. be versed in은 공부하거나 " +
            "경험해서 어떤 분야를 잘 안다는 뜻이다. 명사 verse와는 라틴어 vertere에서 갈라진다.",
        sourceUrls = listOf(VERSED, MW_VERSED),
    ),
    WordNode(
        id = "en:versatile", language = ENGLISH, word = "versatile", partOfSpeech = "형용사",
        meaningKo = "다재다능한, 다용도의", ipa = "/ˈvɜːsətaɪl/ (영국), /ˈvɝsətəl/ (미국)",
        etymologyNote = "라틴어 versātilis(쉽게 돌릴 수 있는)에서 왔다. 쉽게 방향을 바꾼다는 뜻에서 여러 일을 " +
            "잘하는 사람이나 여러 용도에 쓸 수 있는 도구를 가리키게 됐다.",
        sourceUrls = listOf(VERSATILE, MW_VERSATILE),
    ),
    WordNode(
        id = "la:vertere", language = LATIN, word = "vertere", partOfSpeech = "동사",
        meaningKo = "돌리다, 방향을 바꾸다",
        etymologyNote = "라틴어 vertō의 부정사다. 동작을 나타내는 명사 versus와 반복 동사 versāre가 이 동사에서 나왔다.",
        sourceUrls = listOf(VERTERE, VERSUS, VERSARE),
    ),
    WordNode(
        id = "la:versus", language = LATIN, word = "versus", partOfSpeech = "명사",
        meaningKo = "밭고랑, 줄, 시의 한 행",
        etymologyNote = "vertere(돌리다)의 어간에서 만든 명사다. 밭 끝에서 쟁기를 돌려 다음 고랑을 내는 모습을 " +
            "떠올리면 밭고랑과 시의 행이 어떻게 연결되는지 이해하기 쉽다.",
        sourceUrls = listOf(VERSUS, MW_VERSE),
    ),
    WordNode(
        id = "la:versare", language = LATIN, word = "versāre", partOfSpeech = "동사",
        meaningKo = "계속 돌리다, 이리저리 움직이다",
        etymologyNote = "라틴어 versō의 부정사다. vertere의 동작을 반복한다는 뜻을 담고 있다. " +
            "여기서 나온 versārī에는 어떤 일에 관여하거나 몰두한다는 뜻도 있다.",
        sourceUrls = listOf(VERSARE, VERSARI, MW_VERSED),
    ),
    WordNode(
        id = "la:versatus", language = LATIN, word = "versātus", partOfSpeech = "분사",
        meaningKo = "돌려진, 어떤 일에 관여한",
        etymologyNote = "versāre와 versārī에 공통으로 쓰이는 분사 형태다. 영어 versed의 뜻은 " +
            "어떤 일에 관여한다는 versārī의 용법에서 이어졌다.",
        sourceUrls = listOf(VERSATUS, VERSARI, MW_VERSED),
    ),
    WordNode(
        id = "la:versatilis", language = LATIN, word = "versātilis", partOfSpeech = "형용사",
        meaningKo = "쉽게 돌릴 수 있는, 잘 돌아가는",
        etymologyNote = "versāre의 versāt- 어간에 형용사 어미가 붙은 말이다. 영어 versatile의 출처다.",
        sourceUrls = listOf(VERSATILIS, MW_VERSATILE),
    ),

    // weave · web 계열
    WordNode(
        id = "en:weave", language = ENGLISH, word = "weave", partOfSpeech = "동사",
        meaningKo = "실을 엮어 천을 짜다", ipa = "/wiːv/",
        etymologyNote = "게르만 조어 *webaną가 고대 영어 wefan과 중세 영어 weven을 거쳐 이어졌다. " +
            "직물을 짜는 동작을 가리키며, 명사 web은 같은 뿌리에서 짜인 것을 뜻하는 갈래로 발전했다.",
        sourceUrls = listOf(WEAVE, AHD_WEAVE),
    ),
    WordNode(
        id = "en:web", language = ENGLISH, word = "web", partOfSpeech = "명사",
        meaningKo = "짜인 천, 거미줄, 연결망, 웹", ipa = "/wɛb/",
        etymologyNote = "고대 영어 webb(짜인 천)에서 이어졌다. 직물과 거미줄처럼 여러 가닥이 엮인 모양에서 " +
            "복잡한 연결망을 가리키는 뜻이 발달했다. 월드 와이드 웹(World Wide Web)의 1990년 제안서도 링크로 " +
            "이어진 정보를 하나의 그물처럼 설명했다.",
        sourceUrls = listOf(WEB, AHD_WEB, WEB_PROPOSAL),
    ),
    WordNode(
        id = "ang:wefan", language = OLD_ENGLISH, word = "wefan", partOfSpeech = "동사",
        meaningKo = "엮다, 천을 짜다",
        etymologyNote = "서게르만 조어 *weban을 거쳐 게르만 조어 *webaną에서 이어졌다. 중세 영어 weven의 조상이다.",
        sourceUrls = listOf(WEFAN, WEAVE),
    ),
    WordNode(
        id = "ang:webb", language = OLD_ENGLISH, word = "webb", partOfSpeech = "명사",
        meaningKo = "짜인 천, 직물",
        etymologyNote = "서게르만 조어 *wabi를 거쳐 게르만 조어 *wabją에서 이어졌다. 현대 영어 web의 조상이다.",
        sourceUrls = listOf(WEBB, WEB),
    ),
    WordNode(
        id = "gem-pro:webana", language = PROTO_GERMANIC, word = "*webaną", partOfSpeech = "동사",
        meaningKo = "엮다, 천을 짜다",
        etymologyNote = "문헌에 남은 게르만어 형태를 비교해 복원한 동사다. 고대 영어 wefan으로 이어졌고, 짜인 것을 " +
            "뜻하는 명사 *wabją가 여기서 파생됐다.",
        sourceUrls = listOf(WEBANA, WEAVE, WABJA),
    ),
    WordNode(
        id = "gem-pro:wabja", language = PROTO_GERMANIC, word = "*wabją", partOfSpeech = "명사",
        meaningKo = "엮거나 짠 것, 직물",
        etymologyNote = "동사 *webaną에서 파생된 것으로 보는 재구형이다. 서게르만 조어 *wabi를 거쳐 고대 영어 webb로 이어졌다.",
        sourceUrls = listOf(WABJA, WEB),
    ),

    // wedge 계열
    WordNode(
        id = "en:wedge", language = ENGLISH, word = "wedge", partOfSpeech = "명사·동사",
        meaningKo = "쐐기, 쐐기 모양의 조각, 좁은 곳에 끼워 넣다", ipa = "/wɛdʒ/",
        etymologyNote = "고대 영어 wecg가 중세 영어 wegge를 거쳐 이어졌다. potato wedges에는 쐐기의 모양이, " +
            "be wedged between에는 좁은 틈에 쐐기를 박는 동작이 남아 있다. be wedged between A and B는 " +
            "A와 B 사이에 꽉 끼어 있다는 뜻이다.",
        sourceUrls = listOf(WEDGE, AHD_WEDGE, POTATO_WEDGE),
    ),
    WordNode(
        id = "en:potato-wedges", language = ENGLISH, word = "potato wedges", partOfSpeech = "명사(복수)",
        meaningKo = "쐐기 모양으로 썬 감자, 웨지 감자",
        etymologyNote = "potato(감자)와 wedge(쐐기 모양의 조각)를 합친 이름이다. 한 조각은 potato wedge라고 한다.",
        sourceUrls = listOf(POTATO_WEDGE, AHD_WEDGE),
    ),
    WordNode(
        id = "ang:wecg", language = OLD_ENGLISH, word = "wecg", partOfSpeech = "명사",
        meaningKo = "쐐기",
        etymologyNote = "틈을 벌리거나 물건을 고정할 때 쓰는 쐐기를 가리켰다. 중세 영어 wegge를 거쳐 영어 wedge로 이어졌다.",
        sourceUrls = listOf(WECG, AHD_WEDGE),
    ),

    // prescient · science · omniscient 계열
    WordNode(
        id = "en:prescient", language = ENGLISH, word = "prescient", partOfSpeech = "형용사",
        meaningKo = "앞일을 미리 아는, 선견지명이 있는", ipa = "/ˈpɹɛsiənt/ (영국), /ˈpɹɛʃ(i)ənt/ (미국)",
        etymologyNote = "라틴어 praesciēns에서 왔다. prae-(미리)와 scīre(알다)가 합쳐져 '미리 알다'라는 뜻이 됐다. " +
            "a prescient warning처럼 뒤에 일어날 일을 정확히 내다본 판단이나 예측에 쓴다.",
        sourceUrls = listOf(PRESCIENT, AHD_PRESCIENT),
    ),
    WordNode(
        id = "en:science", language = ENGLISH, word = "science", partOfSpeech = "명사",
        meaningKo = "과학, 체계적인 지식이나 학문", ipa = "/ˈsaɪəns/",
        etymologyNote = "라틴어 scientia(지식, 앎)가 고대 프랑스어 science를 거쳐 영어에 들어왔다. 영어에서도 " +
            "처음에는 지식과 학문을 넓게 가리켰고, 체계적으로 탐구하는 과학이라는 뜻이 발달했다.",
        sourceUrls = listOf(SCIENCE, AHD_SCIENCE),
    ),
    WordNode(
        id = "en:omniscient", language = ENGLISH, word = "omniscient", partOfSpeech = "형용사",
        meaningKo = "모든 것을 아는, 전지적인", ipa = "/ɒmˈnɪsɪənt/ (영국), /ɑmˈnɪʃənt/ (미국)",
        etymologyNote = "라틴어 omnisciēns에서 왔다. omni-(모든)와 sciēns(아는)가 결합한 말이다. " +
            "an omniscient narrator는 인물들의 생각까지 아는 전지적 서술자다.",
        sourceUrls = listOf(OMNISCIENT, AHD_OMNISCIENT),
    ),
    WordNode(
        id = "la:scire", language = LATIN, word = "scīre", partOfSpeech = "동사",
        meaningKo = "알다, 이해하다",
        etymologyNote = "라틴어 sciō의 부정사다. 현재분사 sciēns와 '미리 알다'를 뜻하는 praescīre가 여기서 나왔다.",
        sourceUrls = listOf(SCIRE, SCIENS, PRAESCIENS),
    ),
    WordNode(
        id = "la:sciens", language = LATIN, word = "sciēns", partOfSpeech = "현재분사",
        meaningKo = "아는, 지식이 있는",
        etymologyNote = "scīre의 현재분사다. 어간 scient-에서 scientia(지식)가 만들어졌고, omni-와 합쳐져 omnisciēns가 됐다.",
        sourceUrls = listOf(SCIENS, SCIENTIA, AHD_OMNISCIENT),
    ),
    WordNode(
        id = "la:scientia", language = LATIN, word = "scientia", partOfSpeech = "명사",
        meaningKo = "지식, 앎",
        etymologyNote = "sciēns의 어간 scient-에 명사를 만드는 -ia가 붙었다. 고대 프랑스어를 거쳐 영어 science로 이어졌다.",
        sourceUrls = listOf(SCIENTIA, SCIENCE, AHD_SCIENCE),
    ),
    WordNode(
        id = "la:praesciens", language = LATIN, word = "praesciēns", partOfSpeech = "현재분사",
        meaningKo = "미리 아는, 예견하는",
        etymologyNote = "prae-(미리)와 scīre(알다)가 결합한 동사 praescīre의 현재분사다. 영어 prescient의 뿌리다.",
        sourceUrls = listOf(PRAESCIENS, PRESCIENT, AHD_PRESCIENT),
    ),
    WordNode(
        id = "la:omnisciens", language = LATIN, word = "omnisciēns", partOfSpeech = "형용사",
        meaningKo = "모든 것을 아는",
        etymologyNote = "omni-(모든)와 sciēns(아는)를 합친 형태다. 영어 omniscient의 출처다.",
        sourceUrls = listOf(OMNISCIENS, OMNISCIENT, AHD_OMNISCIENT),
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

    // verse 계열
    RelationEdge(
        "en:verse", "la:versus", BORROWED_FROM,
        "라틴어 versus를 받아들인 고대 영어 fers와 앵글로프랑스어 vers가 중세 영어에서 합쳐졌다.",
        listOf(VERSE, MW_VERSE),
    ),
    RelationEdge("en:versed", "la:versatus", BORROWED_FROM, "versārī의 분사 versātus에서 왔다.", listOf(MW_VERSED)),
    RelationEdge("en:versatile", "la:versatilis", BORROWED_FROM, "라틴어 versātilis를 받아들였다.", listOf(VERSATILE, MW_VERSATILE)),
    RelationEdge("la:versus", "la:vertere", DERIVED_FROM, "vertere의 동작을 나타내는 명사로 만들어졌다.", listOf(VERSUS, MW_VERSE)),
    RelationEdge("la:versare", "la:vertere", DERIVED_FROM, "vertere에서 동작의 반복을 나타내는 동사로 파생됐다.", listOf(VERSARE, MW_VERSATILE)),
    RelationEdge(
        "la:versatus", "la:versare", DERIVED_FROM,
        "versāre와 이 동사에서 나온 versārī에 공통으로 쓰이는 분사다.",
        listOf(VERSATUS, VERSARI, MW_VERSED),
    ),
    RelationEdge(
        "la:versatilis", "la:versatus", DERIVED_FROM,
        "versāt- 어간에 형용사 어미가 붙었다.",
        listOf(VERSATILIS, MW_VERSATILE),
    ),

    // weave · web 계열
    RelationEdge("en:weave", "ang:wefan", INHERITED_FROM, "중세 영어 weven을 거쳐 이어졌다.", listOf(WEAVE, AHD_WEAVE)),
    RelationEdge("en:web", "ang:webb", INHERITED_FROM, "중세 영어 web 또는 webbe를 거쳐 이어졌다.", listOf(WEB, AHD_WEB)),
    RelationEdge("ang:wefan", "gem-pro:webana", INHERITED_FROM, "서게르만 조어 *weban을 거쳐 이어졌다.", listOf(WEFAN, WEAVE)),
    RelationEdge("ang:webb", "gem-pro:wabja", INHERITED_FROM, "서게르만 조어 *wabi를 거쳐 이어졌다.", listOf(WEBB, WEB)),
    RelationEdge("gem-pro:wabja", "gem-pro:webana", DERIVED_FROM, "엮는 동작에서 엮은 것을 가리키는 명사가 파생됐다.", listOf(WABJA, WEB)),

    // wedge 계열
    RelationEdge("en:wedge", "ang:wecg", INHERITED_FROM, "중세 영어 wegge를 거쳐 이어졌다.", listOf(WECG, AHD_WEDGE)),
    RelationEdge(
        "en:potato-wedges", "en:wedge", DERIVED_FROM,
        "potato와 wedge를 합친 말이다. wedge는 감자 조각의 모양을 가리킨다.",
        listOf(POTATO_WEDGE, AHD_WEDGE),
    ),

    // prescient · science · omniscient 계열
    RelationEdge(
        "en:prescient", "la:praesciens", BORROWED_FROM,
        "라틴어 praesciēns에서 왔다. 프랑스어를 거친 경로도 제시된다.",
        listOf(PRESCIENT, AHD_PRESCIENT),
    ),
    RelationEdge(
        "en:science", "la:scientia", BORROWED_FROM,
        "고대 프랑스어 science를 거쳐 중세 영어에 들어왔다.",
        listOf(SCIENCE, AHD_SCIENCE),
    ),
    RelationEdge(
        "en:omniscient", "la:omnisciens", BORROWED_FROM,
        "모든 것을 안다는 뜻의 라틴어 omnisciēns에서 왔다.",
        listOf(OMNISCIENT, AHD_OMNISCIENT),
    ),
    RelationEdge("la:sciens", "la:scire", DERIVED_FROM, "scīre의 현재분사다.", listOf(SCIENS)),
    RelationEdge("la:scientia", "la:sciens", DERIVED_FROM, "sciēns의 어간 scient-에 -ia가 붙었다.", listOf(SCIENTIA, AHD_SCIENCE)),
    RelationEdge(
        "la:praesciens", "la:scire", DERIVED_FROM,
        "scīre에 prae-를 붙인 praescīre의 현재분사다.",
        listOf(PRAESCIENS, PRESCIENT, AHD_PRESCIENT),
    ),
    RelationEdge("la:omnisciens", "la:sciens", DERIVED_FROM, "sciēns에 omni-가 붙었다.", listOf(OMNISCIENS, AHD_OMNISCIENT)),
)

val SampleLexicon = Lexicon(words, relations)
