# Poke Achievements (업적)

코블몬 플레이에 맞춘 업적 173개를 추가하는 모드입니다. 업적을 달성하면 포켓몬 육성 아이템, CobbleDollars, 채팅 칭호를 받습니다.

- 버전: 1.7.0
- Minecraft 1.21.1 / Fabric
- 필요한 모드: Cobblemon, Radical Cobblemon Trainers (rctmod), Fabric API (다른 내 모드들과 함께 쓰면 그 모드의 업적도 생깁니다)
- 설치: 서버에만 넣으면 됩니다. 접속하는 사람(클라이언트)은 이 모드가 없어도 됩니다.

## 다운로드

**[⬇ poke-achievements-1.7.0.jar 다운로드](https://github.com/minerapai/poke-achievements/releases/download/v1.7.0/poke-achievements-1.7.0.jar)**

받은 jar 파일을 마인크래프트(또는 서버) 폴더의 `mods` 폴더에 넣으면 됩니다. 다른 버전은 [Releases](https://github.com/minerapai/poke-achievements/releases) 에 있습니다.

## 필요한 데이터팩

| 데이터팩 | 없으면 |
|---|---|
| **COBBLEVERSE-RCT-DP** (COBBLEVERSE 모드팩에 들어 있음) | 관동~신오 체육관, 리그 업적을 달성할 수 없습니다. |
| **[Unova-Paldea Leaders](https://github.com/minerapai/unova-paldea-leaders)** (따로 설치) | 하나~팔데아 관련 업적(지방별 챔피언, 9지방 정복, 내 트레이너의 하나~팔데아 단계)을 달성할 수 없습니다. 나머지 업적은 그대로 쓸 수 있습니다. |

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

## 빌드

```bash
./gradlew build
```

`build/libs/` 에 만들어진 jar 파일을 서버의 `mods` 폴더에 넣습니다.

## 라이선스

MIT License. 출처(minerapai)만 밝히면 누구나 자유롭게 쓰고 고치고 배포할 수 있습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 보세요.
