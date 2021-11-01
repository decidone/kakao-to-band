# kakao-to-band

## Table of Contents

- [Description](#description)
- [How To Use](#how-to-use)
- [Permission](#permission)
- [Development Environment](#development-environment)
- [Usage](#usage)

## Description

- 카카오톡 대화방에 온 메시지를 밴드 게시글로 업로드 해주는 앱
- 안드로이드 알림에 뜬 카카오톡 메시지를 읽어서 밴드 Open API로 게시글 등록
- 카카오톡 메시지를 보낸 사람이나 메시지 내용에 따라 다른 기능 구현 가능
- 일정 시간 동안 온 메시지를 모아서 게시글로 업로드 하는 기능
- 밴드 맴버에게 푸시 알림 여부 선택 가능

## How To Use
- [밴드 개발자 센터](https://developers.band.us/develop/guide/api) BAND Open API 서비스 등록
- 일부 하드코딩 필요
```sh
app\src\main\java\com\decidone\messenger\KakaoNotificationListener.java
  - ACCESS_TOKEN  -> BAND ACCESS TOKEN
  - BAND_KEY      -> 메시지를 업로드할 밴드의 식별자
  - TALK_ROOM     -> 카카오톡 대화방 이름
  - SENDER        -> 특정 사람의 메시지만 밴드에 업로드 할 때 사용
  - 밴드 업로드 관련 함수들을 사용 목적에 맞게 수정
  - ACCESS_TOKEN은 외부에 노출되지 않도록 조심할 것
```

## Permission
- FOREGROUND_SERVICE
- INTERNET
- REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

## Development Environment
- Android Studio
- JAVA

## Usage
- Android Studio에서 프로젝트를 불러와서 앱 설치
