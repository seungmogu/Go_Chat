# Go_Chat

# 블루투스 통신을 이용한 채팅 어플리케이션

# 어플에 사용한 것
  Firebase(로그인 정보, 채팅정보) 저장
  Bluetooth Classic

# Permissions
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
  <uses-permission android:name="android.permission.BLUETOOTH"/>
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    
# Java Class 설명

  ChatController: 블루투스를 연결, 메세지 송수신, 연결 해제, 데이터베이스에 채팅 기록 값 저장  -  (Thread를 이용)
  Gochat: ChatController에서 받아온 값들을 업데이트, 검색된 디바이스를 Dialog에 저장, 표시
  Logo_splash: 시작 화면에 뜨는 스플래시 띄움
  MainActivity: 데이터베이스에 저장된 회원정보를 불러와 사용자가 로그인 한 값이 맞는지 확인  
  signinActivity: 회원가입을 위한 조건(인터넷, 비밀번호 조건 등)을 확인, 데이터베이스에 회원정보 저장
  User: 데이터베이스에 회원정보를 정형화 하여 return
  
