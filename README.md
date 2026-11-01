# EtymoGraph

단어 하나에서 시작해 언어와 시대를 넘나드는 어원 지도를 펼쳐 보는 학습 앱입니다. 지금은 출처를 확인한 표본 데이터(단어 28개, 관계 32개)로 동작합니다.
Kotlin Multiplatform(KMP)과 Compose Multiplatform(CMP)으로 만들고, 브라우저에서는 Kotlin/Wasm(WebAssembly)으로 실행합니다.

## 실행 방법

필요한 것: JDK(Java Development Kit) 17 이상. 이 저장소는 Android Studio에 내장된 JBR(JetBrains Runtime)로 검증했습니다.
첫 빌드는 Gradle·Kotlin·Node.js·npm 의존성을 내려받으므로 수 분이 걸립니다.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

개발 서버 실행 (http://localhost:8080):

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

배포용 빌드 (결과물: `composeApp/build/dist/wasmJs/productionExecutable/`):

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
```

배포용 빌드 확인용 정적 서버 (http://localhost:8081):

- `.wasm`에는 `application/wasm` MIME 타입이 필요합니다.
- `Cache-Control: no-cache`를 보냅니다. 다시 빌드해도 `composeApp.js`의 파일 이름은 그대로라, 이 헤더가 없으면 브라우저가 캐시에 남은 이전 판을 실행할 수 있습니다.

```bash
python3 -c "
import http.server as h, functools
class NoCache(h.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Cache-Control', 'no-cache')
        super().end_headers()
NoCache.extensions_map['.wasm'] = 'application/wasm'
h.test(HandlerClass=functools.partial(NoCache, directory='composeApp/build/dist/wasmJs/productionExecutable'), port=8081, bind='127.0.0.1')
"
```

단위 테스트 (설치된 Chrome을 Headless 모드로 사용):

```bash
./gradlew :composeApp:wasmJsTest
```

한글 문구나 데이터를 바꾼 뒤에는 앱에 넣은 한글 글꼴(쓰는 글자만 담은 부분집합)을 다시 만듭니다. 소스에 쓰인 한글 목록을 Google Fonts API에 보내 그 글자만 담은 Noto Sans KR을 받습니다.

```bash
python3 -I scripts/update_korean_font.py
```

브라우저 요구사항: WasmGC(WebAssembly Garbage Collection)를 지원하는 최신 브라우저. 앱이 쓰는 한글과 발음 기호는 번들한 글꼴로 표시하므로 외부 글꼴 서버가 필요 없습니다. 번들 글꼴에 없는 글자만 `fonts.gstatic.com`에서 늦게 받아 옵니다(아래 "제약 1").

## 구현한 기능

- **검색**: 표제어 검색(대소문자 무시, 정확히 일치 → 접두 일치 → 부분 일치, 현대어 우선). Enter 또는 결과 버튼으로 시작합니다.
- **어원 그래프**: 단어를 시대 층과 어파 칸에 맞춰 놓습니다.
  - 세로 위치는 시대입니다. 위에서부터 인도유럽 조어 → 어파 조어 → 고대·고전어 → 현대어 순서라, 조상은 위에, 후손은 아래에 놓입니다.
  - 가로 위치는 어파입니다. 게르만어파는 왼쪽, 이탈리아어파는 오른쪽, 어느 어파에도 속하지 않는 인도유럽 조어는 가운데입니다.
- **동적 확장**: 노드의 `+n` 배지 또는 상세 패널 버튼으로 연결된 단어를 펼칩니다. 이미 있는 노드는 다시 만들지 않고, 기존 노드의 위치는 바뀌지 않습니다. 상세 패널의 연결 단어를 누르면 필요하면 펼친 뒤 선택합니다.
- **지도 조작**: 드래그 이동, 마우스 휠·두 손가락 확대·축소(포인터 위치 기준), `+`/`−` 버튼, 펼친 단어가 모두 보이게 맞추는 `전체` 버튼(1배보다 크게 확대하지는 않음).
- **자동 화면 이동**: 선택한 노드나 새로 펼친 노드가 화면 밖에 있으면 0.3초 동안 지도를 옮겨 보여 줍니다.
  - 이미 보이는 노드는 움직이지 않고, 넘친 만큼만 옮깁니다.
  - 새로 나타난 노드 묶음이 화면보다 크면 다 보일 만큼만 축소합니다. 새 지도 열기와 이전 지도 복원도 같은 방식입니다.
  - 자동으로 확대하지는 않습니다. 선택만 바뀐 경우에는 배율을 바꾸지 않고 이동만 합니다.
  - 최소 배율(0.3배)로도 넘치면 묶음의 가운데를 맞추되, 선택한 노드는 항상 화면 안에 둡니다.
  - 화면 움직임(자동 이동, `전체`)은 한 번에 하나만 돕니다. 새 움직임이 시작되면 진행 중인 것을 취소하고 그 시점의 화면 위치에서 다시 계산합니다. 그래서 `전체` 버튼을 더블클릭하거나 화면이 움직이는 0.3초 사이에 한 번 더 눌러도, 한 번 누른 것과 같은 곳에 멈춥니다(`ui/ViewportAnimator.kt`).
- **새 출발점과 되돌아가기**: `여기서 새 지도`로 선택한 단어(예: courier)에서 새 지도를 열고, `← 이전 지도`로 펼쳐 두었던 지도로 돌아갑니다.
- **상세 정보**: 표제어, 언어·시대, 품사, 한국어 의미, 국제음성기호(IPA, International Phonetic Alphabet), 어원 설명, 어원 계보(조상 사슬), 연결된 단어와 관계 설명, 출처 링크.
- **학습 상태**: 미학습/학습 완료 토글. 노드에 ✓ 배지로 표시하고, 브라우저 `localStorage`에 저장해 새로고침 후에도 유지합니다.
- **시각 규칙과 범례**: 색만으로 구분하지 않도록 언어는 색 견본과 언어 이름 텍스트를 함께, 관계는 선 모양(실선·파선·점선, 화살표 유무)과 텍스트 라벨을 함께 씁니다.
  - 선택한 단어는 붉은 밑줄과 약한 확대로, 그 단어의 계보(조상까지 이어지는 관계)는 붉고 굵은 선으로 표시합니다.
  - 선택한 단어의 동계어는 파란 점선 밑줄과 "동계어" 꼬리표로 표시합니다.
  - 불확실한 연결은 흐린 선과 "(불확실)" 라벨로 표시합니다.
  - 범례는 지도 오른쪽 위에 둡니다. 지도 영역(시대 이름 칸 포함)이 1100×700dp 이상일 때만 처음부터 펼쳐 두고, 그보다 작으면 접어 둡니다. 1223px 폭 창에서 펼친 범례가 가운데 위의 인도유럽 조어를 가렸기 때문입니다.
- **반응형**:
  - 너비 840dp 이상은 오른쪽 상세 패널, 그보다 좁으면 하단 패널.
  - 지도 영역이 600dp보다 좁으면 시대 이름을 짧게 쓰고(예: "고대 · 고전어" → "고대") 설명 줄을 숨깁니다.
  - 범례는 자리가 모자라면 열을 다음 줄로 넘기고, 지도보다 길면 범례 안에서 스크롤합니다.

### 화면 디자인 기준

어원 사전의 계통도를 펼쳐 놓은 느낌을 목표로 했습니다. 후보 시안 세 가지를 그림으로 비교했습니다(A 시대 층, B 어파 지도, C 어근 나무). 그중 **A를 바탕으로 B의 어파 묶음을 옅게 겹친 안**을 적용했습니다. 시안 이미지는 로컬 `design/` 폴더에만 두고 저장소에는 넣지 않았습니다. 색과 글꼴은 `ui/Theme.kt`에 모아 두었습니다.

- **시대 층**: 지도를 가로 띠 네 개로 나누고 띠마다 바탕색을 번갈아 칠했습니다. 층 이름과 설명은 지도 왼쪽의 고정 칸에 둡니다. 그래서 지도를 움직여도 단어와 겹치지 않고, 이름이 층의 위 끝에 붙어 따라옵니다.
- **어파 칸**: 같은 어파의 단어를 옅은 색 둥근 칸과 파선 테두리로 둘러쌉니다.
  - 칸은 어파 조어 층부터 현대어 층까지 이어집니다.
  - 칸 이름은 가운데에서 먼 쪽 위 모서리에 두어, 가운데로 내려오는 공통 조상의 선을 피합니다. 단어처럼 지도 배율을 따릅니다.
- **단어(노드)**: 상자 없이 표제어와 언어 줄만 씁니다. 누르는 영역은 136×62dp이고, 마우스를 올리면 옅은 상자가 나타납니다.
- **연결선**:
  - 층이 다른 단어는 위 단어의 아랫면과 아래 단어의 윗면을 S자 곡선으로 잇습니다.
  - 같은 층의 단어는 두 단어 위로 넘어가는 호로 잇습니다. 멀리 떨어질수록 높이 띄워, 사이에 있는 단어를 가로지르거나 그 단어와 이어진 것처럼 보이지 않게 했습니다. 예를 들어 court 지도의 cort → cortoisie는 cohors 위로 넘어갑니다.
  - 라벨은 곡선 가운데에 둡니다. 계승은 기본 관계라 라벨을 생략하고(불확실하면 표시), 동계어는 선택한 단어와 이어질 때만 라벨을 붙입니다. 배율이 0.55보다 작으면 글자가 읽히지 않아 라벨을 모두 숨깁니다.
- **색**:
  - 종이색 바탕·잉크색 글자·괘선색 테두리를 기본으로 둡니다.
  - 붉은 강조색은 선택한 단어와 그 계보에만 씁니다. 동계어는 파랑, 학습 완료는 초록으로 따로 둡니다.
- **언어 색**: 언어마다 임의로 고르지 않고 규칙으로 정했습니다.
  - 어족이 색상을 정합니다. 게르만어파는 푸른 계열, 이탈리아어파는 붉은 흙빛 계열입니다. 어파 칸도 같은 색상을 아주 옅게 씁니다.
  - 시대가 명도를 정합니다. 현대어는 진하게, 옛 단계는 옅게, 조어는 회색에 가깝게 씁니다.
- **글꼴**:
  - 표제어(지도의 단어, 상세 패널 제목, 계보, 검색 결과)에는 세리프 Noto Serif를 씁니다. 나머지는 기본 글꼴이고, 한글은 앱에 쓰는 글자만 담은 Noto Sans KR 부분집합(70KB)으로 채웁니다.
  - 시작할 때 이 한글 글꼴과 Noto Serif를 미리 불러와 빠진 글자의 대체 글꼴로 등록한 뒤 화면을 그립니다. 그동안(첫 방문 때 약 1초)은 빈 종이색 화면입니다. 발음 기호(ɑ, ð, ə 등)는 Noto Serif에서 채워집니다.
  - 두 글꼴에 모두 없는 ✓는 글자 대신 초록 원 안의 체크로 그립니다.
  - Noto Serif를 고른 이유는 표제어의 옛 문자(ḗ, ḱ, æ, þ, ā)를 모두 갖춘 세리프가 이것뿐이었기 때문입니다. Source Serif 4는 ḗ·ḱ, Literata는 ḱ가 없었습니다.
  - 재구형은 기울임 대신 `*` 표기와 옅은 잉크로 구분해, 기울임꼴 파일(약 2.4MB)을 넣지 않았습니다.
- **상세 패널**: 머리줄(언어·품사와 학습 토글), 큰 표제어, 번호 붙인 뜻, 어원, 계보, 연결된 단어, 출처 순서입니다. 계보는 세로 단계로 보여 주고 불확실한 단계는 점선으로 잇습니다.
- **참고**: 사용자 지침에 있는 [Anti Slop UI 규칙](https://github.com/miqdadbadjuber/anti-slop/blob/main/skills/antislop-ui/SKILL.md)으로 기존 화면의 '기본값 같은 밋밋함', 장식용 색 띠, 모든 요소의 그림자를 점검했습니다.

### 관계 유형과 표현

| 유형 | 의미 | 그래프 표현 |
|---|---|---|
| `INHERITED_FROM` | 조상 언어 형태에서 계승 | 잉크색 실선 + 화살표(조상 → 후대) |
| `BORROWED_FROM` | 다른 언어에서 차용 | 잉크색 파선 + 화살표(출처 → 받아들인 말) |
| `DERIVED_FROM` | 같은 언어 안에서 접사로 파생 (사양서에 없어 추가) | 잉크색 점선 + 화살표 |
| `COGNATE_OF` | 공통 조상을 가진 동계어 | 파란 점선, 화살표 없음 |
| `SIMILAR_FORM` | 철자만 비슷함. 어원이 같다는 주장도 다르다는 주장도 아님 | 보라 일점쇄선(긴 선과 점의 반복). 사양의 점선을 동계어·파생의 점선과 구분하려고 섞었고, 라벨을 늘 붙임 |
| `TRANSLATION_EQUIVALENT` | 번역·의미 대응 (이번 데이터에는 없음) | 옅은 회색 긴 파선 |

선택한 단어의 계보에 속한 관계는 유형과 관계없이 붉고 굵게 덧그립니다. 선 모양(실선·파선·점선)은 그대로 둡니다.

`DERIVED_FROM`을 추가한 이유: `pater → paternālis`, `correre → corriere`처럼 같은 언어 안의 접사 파생을 "계승"으로 표시하면 사실과 다르기 때문입니다.
`SIMILAR_FORM` 관계에서 "어원이 다르다"는 설명은 두 단어의 서로 다른 라틴어 근원을 각 출처에서 확인한 경우에만 적었습니다.

## 프로젝트 구조

KMP 공식 생성기(kmp.jetbrains.com)가 만드는 `composeApp` 모듈 구조를 따랐습니다. 생성기는 브라우저 안에서만 프로젝트를 만들 수 있어, 같은 구조와 설정을 직접 작성했습니다.

```
composeApp/src/
├── commonMain/kotlin/io/github/etymograph/
│   ├── model/        Model.kt (WordNode, RelationEdge, RelationType, Language, Era, LanguageFamily), Lexicon.kt (조회·검색·계보·무결성 검사)
│   ├── data/         SampleData.kt (검증한 28개 단어와 32개 관계, 출처 URL)
│   ├── graph/        GraphLayout.kt (시대 층·어파 칸에 맞춘 결정적 증분 배치)
│   ├── explorer/     ExplorerState.kt (펼친 지도 상태와 순수 상태 전환 함수)
│   ├── ui/           Viewport.kt (이동·확대 상태, 화면 맞춤 계산), FocusTracker.kt (화면에 보여야 할 노드 선택),
│   │                 ViewportAnimator.kt (화면 움직임 애니메이션을 한 번에 하나만 실행),
│   │                 GraphView.kt (시대 층·어파 칸·연결선 Canvas + 시대 이름 칸 + 노드 컴포저블 + 제스처 + 범례),
│   │                 EdgeGeometry.kt (연결선 곡선 계산), DetailPanel.kt, DisplayText.kt (웹 글꼴에 없는 글자 대체 표시),
│   │                 Theme.kt (색·글꼴)
│   └── App.kt        루트 상태와 화면 배치
├── commonMain/composeResources/font/noto_serif.ttf     표제어 글꼴 (OFL, licenses/NotoSerif-OFL.txt)
├── commonMain/composeResources/font/noto_sans_kr.ttf   앱에 쓰는 한글만 담은 글꼴 (OFL, licenses/NotoSansKR-OFL.txt)
├── commonTest/       Lexicon·GraphLayout·EdgeGeometry·ExplorerState·Viewport·FocusTracker·DisplayText·계보 단위 테스트
└── wasmJsMain/       main.kt (ComposeViewport 진입점, 글꼴 미리 불러오기, localStorage 연동), index.html, styles.css
```

그 밖에 `scripts/update_korean_font.py`(한글 글꼴 부분집합 갱신)와 `licenses/`(글꼴 라이선스)가 있습니다.

설계 요점:

- **책임 분리**: `model`·`data`·`graph`·`explorer`는 Compose에 의존하지 않는 순수 Kotlin입니다. Android 등 다른 타깃을 추가해도 그대로 쓸 수 있습니다.
- **단방향 상태 흐름**: `App`이 `ExplorerState`(지도), `Viewport`(이동·확대), 학습 집합(`Set<String>`)을 소유하고, 하위 화면은 값과 이벤트 콜백만 받습니다. 세 상태는 서로 독립적입니다.
- **불변 상태**: `ExplorerState`의 모든 변경은 `withNewRoot`, `withExpanded`, `withSelection`, `withRevealed`, `withPreviousMap`이 새 객체를 돌려주는 방식입니다.
- **좌표 분리**: 노드 위치는 월드 좌표(dp 단위)로만 저장합니다. 화면 좌표는 `Viewport`(화면 = 중심 + pan + 월드 × scale)로 그릴 때 계산합니다. 연결선과 노드가 같은 변환을 쓰므로 이동·확대 후에도 어긋나지 않습니다.
- **배치 알고리즘**(`GraphLayout.placeInBands`): 세로 위치는 언어의 시대가 정하는 층의 가운데로 고정하고, 가로 위치만 찾습니다.
  - 새 단어가 펼친 단어와 같은 어파면 펼친 단어와 같은 가로 위치를, 다른 어파면 그 어파의 기준 위치(게르만어파 −260dp, 이탈리아어파 +260dp, 인도유럽 조어 0)를 먼저 시도합니다.
  - 같은 층에 이미 단어가 있으면 156dp 간격으로 바깥쪽부터 번갈아 빈자리를 찾습니다. 바깥쪽을 먼저 쓰는 이유는 가운데의 공통 조상 자리와 다른 어파 칸 쪽을 비워 두기 위해서입니다.
  - 어파 칸에 속한 단어는 가운데에서 90dp 안쪽으로 들어가지 않아, 두 칸이 겹치지 않습니다.
  - 같은 입력에는 항상 같은 결과가 나오고 기존 노드는 움직이지 않습니다. 물리 기반(force-directed) 레이아웃은 쓰지 않았습니다.
- **그리기 방식**: 시대 층 바탕·어파 칸·연결선·화살표·라벨은 하나의 `Canvas`에, 노드와 어파 칸 이름은 컴포저블로 그립니다. 시대 이름 칸(`EraGutter`)은 지도와 같은 세로 변환으로 따로 그립니다. 노드를 컴포저블로 만든 이유는 클릭 판정, 텍스트 렌더링, 접근성 의미(semantics)를 Compose가 처리하게 하기 위해서입니다. 노드를 전체 그래프의 `graphicsLayer` 안에 두는 대신 각자 화면 좌표에 배치해, 부모 영역 밖에 놓인 자식의 클릭 판정 문제를 피했습니다.
- **학습 기록 저장**: `App`은 `initialLearned`·`onLearnedChange`만 받고, 저장 방식은 플랫폼 진입점(`wasmJsMain/main.kt`)이 정합니다. 웹은 `js()` 상호운용으로 `localStorage`를 읽고 쓰며, 사생활 보호 모드 등에서 예외가 나면 기록 없이 동작합니다.

## 라이브러리와 버전

| 항목 | 버전 | 선택 이유 |
|---|---|---|
| Kotlin (Multiplatform, Compose Compiler 플러그인) | 2.4.21 | 개발 시점(2026-10-25)의 최신 안정판 |
| Compose Multiplatform Gradle 플러그인, runtime·foundation·ui | 1.12.2 | 최신 안정판. 공식 문서상 최신 Kotlin과 호환 |
| Compose Material (Material 2) | 1.12.2 | CMP 1.12.2와 짝을 이루는 Material3는 `1.12.0-alpha03` 알파 버전뿐이라, 안정판인 Material 2를 사용 |
| Compose Resources (`components-resources`) | 1.12.2 | 번들한 표제어 글꼴을 웹에서 불러오기 위해 사용 |
| Noto Serif (글꼴 파일, 가변 글꼴 1.9MB) | Google Fonts 저장소 판 | 표제어용 세리프. SIL Open Font License 1.1, `licenses/NotoSerif-OFL.txt` |
| Noto Sans KR (글꼴 부분집합 70KB) | Google Fonts API, 2026-11-02 | 앱에 쓰는 한글 454자와 화살표 2개만 담음. SIL Open Font License 1.1, `licenses/NotoSansKR-OFL.txt` |
| kotlin-test | 2.4.21 | 공통 단위 테스트 |
| kotlinx-coroutines-test (테스트 전용) | 1.9.0 | 화면 움직임 애니메이션을 가상 프레임으로 돌려 `전체` 더블클릭을 재현. Compose가 이미 쓰는 코루틴 버전과 맞춤 |
| Gradle Wrapper | 9.8.2 | 최신 안정판 |

외부 JavaScript 그래프 라이브러리, 직렬화, 코루틴, ViewModel 라이브러리는 쓰지 않았습니다. 데이터는 Kotlin 코드로 된 정적 목록이고 상태는 Compose `mutableStateOf`로 충분했습니다.

## 데이터와 출처

- 단어 28개(father 계열 11개, court 계열 9개, courier 계열 8개)와 관계 32개. 모든 단어와 관계에 출처 URL이 있으며, 단위 테스트가 이를 확인합니다.
- 내용은 2026-10-25에 확인한 영어 Wiktionary 항목을 한국어로 요약·가공한 것입니다. 원문을 그대로 옮기지 않았고, 앱 상세 패널과 이 문서에 출처를 표시합니다.
- 중간 단계 형태(중세 영어 fader, 서게르만 조어 *fader 등)는 노드로 만들지 않고 관계 설명에 "~를 거쳐"로 적었습니다.
- **courtier는 차용과 파생을 나눠 연결했습니다**: courtier ←(차용) 앵글로노르만어 corteour ←(파생) 고대 프랑스어 cort.
  - [Dictionary.com](https://www.dictionary.com/browse/courtier)(Random House)이 Wiktionary와 같은 경로를 따로 제시합니다. 앵글로프랑스어 courte(i)our는 고대 프랑스어 cortoyer(궁정에 출입하다, court에서 파생)에 -our를 붙인 말입니다. 두 출처가 일치해 중간 노드를 넣었습니다.
  - [앵글로노르만어 사전(AND, Anglo-Norman Dictionary)](https://anglo-norman.net/entry/corteour)의 corteour는 '중개인'을 뜻하는 같은 철자의 다른 말(currere 계열)입니다. 이 사실은 노드 설명에 적었습니다.
  - Etymonline은 이 형태를 재구형 *corteour로 적는다는 리뷰 지적이 있습니다. 하지만 Cloudflare 보안 확인 때문에 직접 확인하지 못해 `*`는 붙이지 않았습니다.
  - 이전 판에서는 중간 형태를 확인하지 못해 courtier ← cort를 '불확실한 차용' 하나로 표시했습니다.
- 데이터를 만들며 확인한 흥미로운 사례:
  - 고대 프랑스어 cortoisie는 원래 '기사도, 용맹'을 뜻했습니다.
  - courier는 고대 프랑스어 coreor 계열과 이탈리아어 corriere 계열이 합쳐진 말입니다.
  - 영어 horse가 라틴어 currere와 같은 어근 *ḱers-(달리다)에서 왔다는 견해는 Wiktionary가 "most likely"로 표기해 불확실한 연결로 표시했습니다.
- 한계: 다음 사전은 접근이 막혀 자동 교차 검증을 하지 못했습니다. 그래서 관계는 대부분 Wiktionary 한 곳에만 근거합니다.
  - Etymonline, Merriam-Webster, Collins: HTTP 403 또는 Cloudflare 보안 확인
  - CNRTL: 빈 페이지
  - MED(Middle English Dictionary): Cloudflare 확인 페이지

  추가로 인용한 출처는 그림의 법칙(Grimm's law) 설명의 Wikipedia, courtier 관계의 Dictionary.com과 AND입니다.

### 데이터 라이선스

- 어원 데이터(`SampleData.kt`의 한국어 설명)는 Wiktionary 항목을 요약·가공한 것입니다. 원 자료의 라이선스에 맞춰 [CC BY-SA 4.0(저작자표시-동일조건변경허락)](https://creativecommons.org/licenses/by-sa/4.0/deed.ko)으로 배포합니다.
- 저작자는 각 항목의 Wiktionary 기여자입니다. 출처는 단어·관계마다 연결한 페이지이고, 변경 사항은 '한국어로 요약·가공'입니다. 앱 상세 패널에도 같은 고지와 라이선스 링크를 표시합니다.
- 데이터를 수정해 다시 배포할 때도 같은 라이선스와 출처 표시를 유지해야 합니다.
- 코드는 Apache License 2.0입니다(`LICENSE`). 저작권 표시와 예외(어원 데이터, 글꼴)는 `NOTICE`에 적었습니다.
- 글꼴(Noto Serif, Noto Sans KR 부분집합)은 SIL Open Font License 1.1입니다(`licenses/`).

## 수행한 테스트와 결과

기준: 이 README와 같은 작업 트리(커밋 없음), macOS 26 (Darwin 25.6), JBR 25.0.3.

| 항목 | 방법 | 결과 |
|---|---|---|
| 단위 테스트 42개 | `./gradlew :composeApp:wasmJsTest`, Karma + Chrome Headless 156 | 42개 통과, 실패 0. 두 손가락 제스처 회귀 테스트와 같은 단어 재검색 회귀 테스트는 수정 전 코드로 되돌리면 실패하는 것도 확인 |
| 배포용 Wasm 빌드 | `./gradlew :composeApp:wasmJsBrowserDistribution` | 성공. 앱 Wasm 2.9MB + skiko.wasm 8.6MB + JS 0.3MB. webpack이 경고 3건을 보고했지만 Gradle 출력에 내용이 나오지 않아 확인하지 못함 |
| 개발용 웹 빌드 실행 | `wasmJsBrowserDevelopmentRun` | 성공 (http://localhost:8080) |
| 시나리오 A: 검색과 탐색 | Claude 데스크톱 앱 내장 브라우저(Chromium 152), 배포 빌드, 시대 층 디자인 적용 후 다시 확인 | 통과. father 검색 → 4개 노드가 층·칸에 맞춰 표시 → fæder·*fadēr·pater·paternālis를 차례로 펼침(기존 노드 위치 유지, 새 노드가 보이도록 화면 이동) → 선택한 단어의 계보가 붉게, 상세 패널 계보가 단계로 표시 |
| 시나리오 B: 지도 조작 | 같은 환경 | 통과. 드래그 이동, 휠 확대(포인터 아래 지점 고정), `+` 버튼. 연결선·어파 칸·시대 이름이 노드와 함께 움직임. `전체`는 두 번 확대하고 옆으로 밀어 둔 지도에서 펼친 단어 5개를 모두 화면 가운데로 되돌림(0.93배로 축소) |
| 시나리오 C: 학습 기록 | 같은 환경 | 통과. 상세 패널 머리줄의 토글로 father를 학습 완료로 표시 → 지도의 father에 ✓ → 새로고침 후에도 유지(`localStorage`의 `etymograph.learned`). 확인 후 기록을 지움 |
| 시나리오 D: 다른 계보 | 같은 환경 | 통과. court 검색 → cort(차용)·courier(유사 철자) 표시 → courier 선택 → `여기서 새 지도`(courier ← 프랑스어 courrier 차용이 붉은 계보로 표시) → `← 이전 지도` 두 번으로 court, father 지도 순서로 복원 |
| 좁은 화면 | 375×812 모바일 에뮬레이션 | 통과. 하단 상세 패널, 상세 목록에서 숨은 단어(currere) 펼치며 선택(이전 디자인에서 확인). 시대 층 디자인에서는 짧은 시대 이름, 접힌 범례, 펼친 범례가 열을 줄바꿈해 지도 안에 모두 들어가는 것을 확인 |
| 배포 빌드 동작 | 위 정적 서버(8081) | 통과. 콘솔에 새 오류 없음(아래 "제약 8"의 사용 중단 메시지와 Skiko의 WebGL 확장 조회 경고만 있음) |
| 시대 층·어파 칸 디자인 | 배포 빌드, 1280×800·1600×1000·375×812 | 통과. father 지도를 끝까지 펼친 화면이 적용한 시안과 같은 배치. 어파를 넘는 차용(paternālis → paternal) 표시. 같은 층 연결이 단어 위로 넘어감(Vater–father, pater → paternālis, cohors 위를 넘는 cort → cortoisie). 1600×1000에서는 범례가 펼쳐진 채 *ph₂tḗr를 가리지 않고, 1280×800에서는 접혀 있음 |
| 첫 화면 글꼴 | 배포 빌드, 1280×800·375×812 | 통과. 새로고침 직후 약 1초 빈 종이색 화면 뒤 한글·화살표가 빈 상자 없이 한 번에 표시됨. father 지도, 발음 기호, 범례, 학습 표시까지 열어도 외부(Google Fonts) 요청 0건. 바꾸기 전에는 Compose가 페이지를 연 지 약 11초 뒤에야 한글 글꼴을 요청했음(이미 캐시에 있었는데도) |
| `전체` 더블클릭 | 단위 테스트(`ViewportAnimatorTest`, 가상 프레임으로 첫 맞춤 도중 두 번째 입력) | 고치기 전 코드로는 실패(한 번 누르면 배율 0.46인데 연달아 누르면 0.49로 더 확대됨). 진행 중인 움직임을 취소하도록 고친 뒤 통과 |
| 입력 직후 Enter | 내장 브라우저 자동 입력(글자 입력 바로 뒤 Enter) | 수정 전: 4번 중 2번은 추천 목록만 뜨고 지도가 열리지 않음. 수정 후: courtier·father·court 3번 모두 입력한 단어의 지도가 열림(courtier가 court로 열리지 않음) |
| 자동 화면 이동 | 배포 빌드, 내장 브라우저(데스크톱·375px 모바일) | 통과. 화면 밖으로 끌어낸 뒤 상세 목록에서 선택한 노드가 화면 안으로 들어옴. 모바일에서 가장자리에 잘린 pater를 선택하면 가장자리 안쪽까지만 이동. 펼친 새 조상 노드가 위쪽 밖에 생기면 아래로 이동. 이미 보이는 노드를 누르거나 직접 드래그할 때는 움직이지 않음. 모바일에서 father 지도를 열거나 pater를 펼치면 새 노드가 모두 보이도록 축소됨. 콘솔 오류 없음. 여기까지는 이전 디자인에서 확인했고, 시대 층 디자인에서는 pater 펼치기 후 새 노드 쪽으로 이동하는 것과 court 새 지도가 courier까지 보이도록 축소되는 것을 다시 확인 |
| 2차 리뷰 반영 확인 | 배포 빌드, 375px 모바일 | 통과. 화면 맞춤 후 같은 단어(father)를 다시 검색해도 축소된 맞춤 유지, courtier → corteour(차용) → cort(파생) 표시, 검색 버튼과 출처 링크에 `*ph₂tḗr`의 ₂가 아래 첨자로 표시, 콘솔 오류 없음 |
| 1차 리뷰 반영 확인 | 내장 브라우저 | 통과. `*ḱers-` 그대로 검색, horse 계보의 `←?`, 출처·라이선스 링크가 올바른 URL로 `window.open` 호출(실제 창은 열지 않음), 320px 폭에서 상단 버튼이 통째로 다음 줄로 내려가고 검색창 안내문이 한 줄로 줄어듦 |

검증하지 않은 항목:

- **Google Chrome 앱에서의 수동 조작**: 시나리오는 Chromium 기반 내장 브라우저에서 확인했습니다. 설치된 Chrome은 단위 테스트(Headless)에만 썼습니다.
- **Safari**: macOS Safari 27.2에서는 흰 화면이 나옵니다. WebKit 버그일 가능성이 높지만 확정하지 못했습니다(아래 "제약 9"). 그 밖의 Safari 버전은 확인하지 않았습니다.
- **실제 터치 기기의 드래그·핀치**: 모바일 에뮬레이션에서 마우스 입력으로만 확인했습니다. 두 손가락 이동·확대는 Compose 소스(`detectTransformGestures`가 이전 프레임의 중심점을 넘김)와 단위 테스트로만 확인했고, 실제 기기에서 실행해 보지 않았습니다.
- **스크린 리더**: 노드에 역할·선택 상태·학습 상태 semantics를 지정했지만, 검증 중 접근성 DOM(Document Object Model) 노드가 생성되지 않아 스크린 리더 노출은 확인하지 못했습니다.
- **휠 확대 속도**: 마우스 한 칸의 `scrollDelta` 크기를 측정하지 않았습니다. 다른 장치에서는 너무 빠르거나 느릴 수 있습니다(`WHEEL_ZOOM_RATE`).

## Kotlin/Wasm·Compose Web에서 발견한 제약

1. **글꼴**: CMP 1.12부터 웹에서 없는 글자를 Noto 글꼴로 자동으로 받아 옵니다. 다만 받아 오기 시작이 늦습니다.
   - 이 앱에서 재 보니, 글꼴 조각이 이미 브라우저 캐시에 있어도 Compose가 페이지를 연 지 약 11초 뒤에야 한글 글꼴을 요청했습니다. 그동안 한글이 빈 상자로 보였습니다.
   - 그래서 앱에 쓰는 한글만 담은 Noto Sans KR 부분집합을 번들했습니다. 시작할 때 Noto Serif와 함께 `FontFamily.Resolver.preload`로 미리 불러오면, 미리 불러온 글꼴이 빠진 글자의 대체 글꼴로 쓰입니다([JetBrains/compose-multiplatform#3967](https://github.com/JetBrains/compose-multiplatform/issues/3967)에서 이어진 변경). 확인한 흐름에서는 외부 글꼴 요청이 없습니다.
   - 한글 문구를 바꾸고 `scripts/update_korean_font.py`를 다시 실행하지 않으면, 새로 쓴 글자만 자동 대체 글꼴로 늦게 표시됩니다.
   - 자동 대체 글꼴에 없던 아래 첨자 숫자(₂, U+2082)와 결합용 아래 고리(r̥의 U+0325)는 데이터는 그대로 두고 화면에서만 아래 첨자 스타일 숫자와 `˳`(U+02F3)로 바꿔 표시합니다(`DisplayText.kt`).

   자동 대체 글꼴은 글자 범위별 조각으로 나뉘어 필요할 때 도착하므로, 처음 쓰는 글자는 그려진 뒤에 폭이 바뀔 수 있습니다. 범례 너비를 `IntrinsicSize.Max`로 맞췄을 때, 처음 펼친 범례에서 "어파 칸"이 두 줄로 꺾여 아래 줄과 겹쳤습니다(다시 펼치면 정상). 그래서 범례 너비는 내용대로 두었습니다.
2. **다운로드 크기**: 그래픽 엔진 skiko.wasm만 8.6MB이고, 번들한 Noto Serif 1.9MB와 한글 부분집합 70KB가 더해집니다(배포 폴더 전체 15MB, 소스맵 포함). 첫 로드 비용이 큽니다. Noto Serif도 쓰는 글자만 받으면 크게 줄일 수 있지만, 가변 굵기(보통·세미볼드)가 유지되는지 확인하지 않아 그대로 두었습니다.
3. **Canvas 기반 렌더링**: 화면 전체가 하나의 `<canvas>`라 브라우저의 텍스트 선택·페이지 내 검색·기본 접근성 트리를 쓸 수 없습니다.
4. **개발 서버**: 이 환경에서는 `wasmJsBrowserDevelopmentRun --continuous`가 파일 변경을 감지하지 않아, 코드를 바꿀 때마다 서버를 다시 시작했습니다(원인 미조사). 또 개발 서버는 시작할 때마다 기본 브라우저에 새 창을 엽니다. Kotlin Gradle 플러그인의 webpack 개발 서버 기본값으로 추정하며, 설정 파일에서 직접 확인하지는 않았습니다.
5. **Material3**: CMP 1.10부터 Material3가 별도 버전으로 분리되었습니다. 1.12.2와 짝인 버전은 알파뿐입니다.
6. **JS 상호운용**: Kotlin 2.4에서 `js()` 호출에 `@OptIn(ExperimentalWasmJsInterop::class)`가 필요합니다.
7. **백그라운드 탭과 프레임**: 브라우저 탭이 화면에 보이지 않으면 프레임 갱신이 늦어집니다. 입력은 처리되지만 화면 반영이 지연됩니다. 자동화 검증 중 스크린샷이 늦게 갱신되는 것으로 관찰했습니다.
   - Compose는 프레임마다 화면을 다시 구성하므로, 다음 프레임 전에 처리되는 입력 이벤트는 이전 구성 때의 값을 볼 수 있습니다.
   - 검색창의 Enter가 이 때문에 직전 입력값의 검색 결과를 써서, 지도가 열리지 않거나 입력 도중의 단어가 열릴 수 있었습니다. 지금은 Enter를 누른 시점의 입력값으로 다시 검색합니다(`App.kt`의 `submit`). 보이는 탭에서도 자동 입력처럼 빠르면 재현됐습니다.
8. **콘솔 경고**: 번들 글꼴을 넣은 뒤부터 페이지를 열 때마다 콘솔에 "Accessing `memory` via `wasmExports` is deprecated" 메시지가 한 번 error 수준으로 찍힙니다.
   - Kotlin/Wasm 로더의 호환용 래퍼가 내는 사용 중단 경고이고, 앱 동작에는 영향이 없습니다.
   - Safari 콘솔의 호출 스택에서 출처를 확인했습니다. Compose Resources가 글꼴 같은 리소스를 Wasm 메모리로 옮기는 `copyArrayBufferToWasmMemory`가 옛 방식으로 `memory`에 접근합니다.
   - 없애려면 라이브러리 업데이트(지금은 1.13 베타뿐)나 다른 글꼴 로딩 방식이 필요합니다.
9. **Safari 27.2 흰 화면**: macOS Safari 27.2에서 앱이 흰 화면에 머뭅니다.
   - 진단 결과를 정리하면 이렇습니다.
     - Compose 캔버스는 창 크기에 맞게 만들어졌습니다(3584×438, 화면 배율 2배).
     - WebGL2 연결은 살아 있고, 캔버스를 덮는 요소도 없습니다.
     - 그런데 캔버스 픽셀이 투명했습니다.
     - 글꼴 파일은 정상으로 받았고, 콘솔에 앱이 멈췄다는 오류도 없습니다. Skia가 그래픽 카드 정보를 묻는 `WEBGL_debug_renderer_info` 경고만 반복되는데, 이 경고는 정상 동작하는 Chrome에서도 나옵니다.
   - **원인은 WebKit 회귀 버그 CMP-10986일 가능성이 높지만, 확정하지는 못했습니다.**
     - 같은 증상(캔버스는 정상인데 아무것도 그려지지 않는 흰 화면)이 Compose 이슈 트래커에 CMP-10986으로 보고돼 있습니다. 다른 프로젝트의 기록에 따르면 Compose 1.12.1, 1.12.2, 1.13.0-beta01에서 모두 재현되고, Safari Technology Preview 255에서 고쳐졌습니다([OnlyShots #33](https://github.com/parez-dev/OnlyShots/pull/33), [OnlyShots #34](https://github.com/parez-dev/OnlyShots/pull/34)).
     - 확정하지 못한 이유는 셋입니다. YouTrack 원문을 읽지 못했습니다. 그 기록은 "HTTPS 페이지에서 생기고 일반 HTTP는 그려진다"고 하는데, 이 앱은 HTTP 주소(`127.0.0.1`)에서 재현됐습니다(브라우저가 `127.0.0.1`을 HTTPS처럼 안전한 출처로 취급해서일 것으로 추정). 버그가 고쳐졌다는 Safari Technology Preview에서 그려지는지도 확인하지 않았습니다.
     - 같은 Safari 27.2 흰 화면을 WebAssembly 실행(JIT) 오류로 본 기록도 있습니다([pharmacy-app #449](https://github.com/app-devper/pharmacy-app-kmp/pull/449)). 다만 그 경우엔 콘솔에 예외가 찍혔고, 이 앱은 오류가 없었습니다.
   - 앱 코드로 피하는 방법은 알려지지 않았습니다. 그동안 Safari 27.2에서는 Chrome이나 Firefox를 쓰면 됩니다.

## 다음 단계

- 펼친 지도 자체를 저장해 새로고침 후에도 이어 보기. 지금은 학습 상태만 저장하고 지도는 세션 안에서만 유지합니다.
- Safari 27.2 수정판이 나오면 Safari 다시 확인. Google Chrome 앱, 실제 터치 기기, 스크린 리더 검증
- 어원 데이터 교차 검증(사전 2곳 이상)과 데이터 파일(JSON 등) 분리, 출처별 라이선스 표기 정리
- 한 층에 단어가 많아지면 지도가 가로로 길어집니다. 층 안 두 줄 배치나 접기 기능 검토
- 라틴어와 고대 프랑스어가 같은 "고대 · 고전어" 층이라, 같은 층 안의 계승(cohors → cort)이 생깁니다. 층을 고전어와 중세어로 나누는 방안 검토
- 창 크기가 바뀌면 지도를 자동으로 다시 맞출지 검토(지금은 `전체` 버튼으로 맞춤)
- Android 타깃 추가 시 `model`·`graph`·`explorer`는 그대로 쓰고 저장소만 `SharedPreferences`/DataStore로 구현
