# Poke Achievements (업적)

코블몬 플레이에 맞춘 업적 173개를 추가하는 모드입니다. 업적을 달성하면 포켓몬 육성 아이템, CobbleDollars, 채팅 칭호를 받습니다.

- 버전: 1.7.0
- Minecraft 1.21.1 / Fabric
- 필요한 모드: Cobblemon, Radical Cobblemon Trainers (rctmod), Fabric API (다른 내 모드들과 함께 쓰면 그 모드의 업적도 생깁니다)
- 설치: 서버에만 넣으면 됩니다. 접속하는 사람(클라이언트)은 이 모드가 없어도 됩니다.

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
