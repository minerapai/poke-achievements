# Poke Achievements (업적)

코블몬 플레이에 맞춘 업적 173개를 추가하는 모드입니다. 업적을 달성하면 포켓몬 육성 아이템, CobbleDollars, 채팅 칭호를 받습니다.

- 버전: 1.7.1
- Minecraft 1.21.1 / Fabric
- 필요한 모드: Cobblemon, Radical Cobblemon Trainers (rctmod), Fabric API (다른 내 모드들과 함께 쓰면 그 모드의 업적도 생깁니다)
- 설치: 서버에만 넣으면 됩니다. 접속하는 사람(클라이언트)은 이 모드가 없어도 됩니다.

## 다운로드

**[⬇ poke-achievements-1.7.1.jar 다운로드](https://github.com/minerapai/poke-achievements/releases/download/v1.7.1/poke-achievements-1.7.1.jar)**

받은 jar 파일을 마인크래프트(또는 서버) 폴더의 `mods` 폴더에 넣으면 됩니다. 다른 버전은 [Releases](https://github.com/minerapai/poke-achievements/releases) 에 있습니다.

## 필요한 데이터팩

| 데이터팩 | 없으면 |
|---|---|
| **COBBLEVERSE-RCT-DP** (COBBLEVERSE 모드팩에 들어 있음) | 관동~신오 체육관, 리그 업적이 숨겨집니다. |
| **[Unova-Paldea Leaders](https://github.com/minerapai/unova-paldea-leaders)** (따로 설치) | 하나~팔데아 관련 업적(지방별 챔피언, 9지방 정복, 내 트레이너의 하나~팔데아 단계)이 숨겨집니다. |

## 없는 모드의 업적은 자동으로 숨겨집니다

업적 중 일부는 다른 모드나 데이터팩의 기능에 대한 업적입니다. 서버에 그 모드가 없으면 그 업적은 업적 화면과 목록에서 **자동으로 빠집니다** (달성할 수 없는 업적이 화면을 채우지 않도록). 서버 로그에 `N of 173 achievements available on this server` 로 몇 개가 쓰이는지 나옵니다.

| 업적 | 필요한 것 |
|---|---|
| 배틀 타워 | [Battle Tower](https://github.com/minerapai/battle-tower) |
| 서버 이벤트 (전설, 대량발생, 이로치) | [Poke Events](https://github.com/minerapai/poke-events) |
| 퀘스트와 라이벌 | [Poke Quest](https://github.com/minerapai/poke-quest) |
| 내 트레이너, 친선전, 내 체육관 | [Poke Trainer](https://github.com/minerapai/poke-trainer) |
| 레이드 | Cobblemon Raid Dens |
| 지방별 챔피언 | 그 지방의 RCT 시리즈 (관동~신오: COBBLEVERSE-RCT-DP, 하나~팔데아: Unova-Paldea Leaders) |

## 기능

- 도감, 체육관/리그, 포획, 이로치, 알, 배틀, 레이드 업적
- 배틀 타워, 퀘스트, 이벤트, 트레이너 로드(117단계), 내 체육관, 지방별 챔피언 업적
- 달성한 업적의 칭호를 채팅 이름 앞에 붙이기

## 사용법

| 명령어 | 설명 |
|---|---|
| `/achv (/업적)` | 업적 목록 화면 (마우스를 올리면 보상이 보입니다) |
| `/achv title <업적>` | 칭호로 설정 |
| `/achv title off` | 칭호 끄기 |
| `/achv reload` | (OP) 설정 다시 읽기 |

(OP) 표시가 있는 명령어는 서버 관리자(OP)만 쓸 수 있습니다.
괄호 안의 한글 명령어는 [Korean Commands](https://github.com/minerapai/korean-commands) 모드가 있을 때만 동작합니다. 없으면 영어 명령어를 쓰세요.

## 빌드

```bash
./gradlew build
```

`build/libs/` 에 만들어진 jar 파일을 서버의 `mods` 폴더에 넣습니다.

## 라이선스

MIT License. 출처(minerapai)만 밝히면 누구나 자유롭게 쓰고 고치고 배포할 수 있습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 보세요.
