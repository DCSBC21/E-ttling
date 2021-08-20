# Eöttling

|page|화면|상태|비고|
|:-|:-|:-:|:-:|
|2|로그인|완료||
|4|홈|완료||
|6|계좌목록|완료||
|6|계좌목록 > pending 계좌 item|완료||
|6|계좌목록 > Receive 버튼 > 주소팝업|완료||
|8|계좌상세|완료||
|10|계좌생성 > 코인종류 선택|생략|다이얼로그로 대체|
|11|계좌생성 > ERC 20 생성 알림 팝업|완료||
|11|계좌 생성 > SSS|완료||
|11|계좌 생성 > T-ECDSA|완료||
|12|계좌 생성 > SSS|중복||
|13|계좌 생성 > T-ECDSA > 날짜 선택 팝업|완료||
|13|계좌 생성 > T-ECDSA > 인증유효시간 선택 팝업|완료||
|14|계좌 생성 > T-ECDSA > 인증을 위한 키 생성 시작|완료||
|14|계좌 생성 > T-ECDSA > 인증 유효 시간 만료 팝업|완료||
|14|계좌 생성 > T-ECDSA > 계좌 생성 확인 팝업|완료||
|16|트랜잭션 목록|완료||
|18|트랜잭션 상세|완료||
|20|트랜잭션 생성 > SSS|완료||
|21|트랜잭션 생성 > SSS > 인증|완료||
|21|트랜잭션 생성 > SSS > 인증 > 성공 팝업|완료||
|22|트랜잭션 생성 > T-ECDSA|완료||
|22|트랜잭션 생성 > T-ECDSA > 예정 시간 알림 팝업|완료||
|23|트랜잭션 생성 > T-ECDSA > 인증|완료||
|23|트랜잭션 생성 > T-ECDSA > 인증 > 트랜잭션 생성 확인 팝업|완료||
|25|마이페이지|완료||


### Volley API 테스트 

```kotlin
			// call GET (with query params)
			VolleySingleton.Instance(this).SendGET(
				"http://192.168.0.191:22227/users/ranks",
				"period_type=TOTAL",
				Response.Listener<String> { response ->
					var strResp = response.toString()
					Log.d("API", strResp)
				},
				Response.ErrorListener { Log.d("API", "that didn't work") }
			)

			// call GET (with JSON Object)
			VolleySingleton.Instance(this).SendGET(
				"http://192.168.0.191:13309/api/user/1",
				"",
				Response.Listener<String> { response ->
					var strResp = response.toString()
					Log.d("API", strResp)
				},
				Response.ErrorListener { Log.d("API", "that didn't work") }
			)

			// call POST
			val json = JSONObject()
			json.put("loginId", "u2@email.com")
			json.put("passrd", "1111")
			json.put("name", "홍길동")
			VolleySingleton.Instance(this).SendPOST(
				"http://192.168.0.191:13309/api/user",
				json,
				Response.Listener { response ->
					// response
					// Get your json response and convert it to whatever you want.
					Log.d("API", response.toString())
				},
				Response.ErrorListener { error ->
					Log.d("API", "error => $error")
				}
			)
```
