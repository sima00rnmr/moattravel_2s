# 2周目研修メモ
研修中、どうしても気になった事項とかまとめておきたいメモを一覧にしておく場所（余り時間は取らないように…！）

## ディレクトリの構成と役割　（MVCに沿った考え方）

### Model(= データとロジック全体)
Entity / Repository / Service / DTO(formとか)

### View(= 画面表示)
HTML / Thymeleaf / CSS / JS

### Controller(=リクエストを受けて処理を振り分け)
Controller



## 機能のまとめ

### 検索
トップページ
民宿一覧
管理者民宿一覧
管理者会員一覧

### ページネーション
民宿一覧
予約一覧
管理者会員一覧
管理者民宿一覧

### フォーム
会員登録
ログイン
予約
会員編集
民宿登録
民宿編集

### GPT君が作ったプロジェクト全体マップ
なんかファイル名とかが怪しいところがあるので、時間に余裕がある時に修正

```
MoatTravel
│
├─ House（民宿）
│   ├─ Controller
│   │   ├─ HouseController
│   │   └─ AdminHouseController
│   │
│   ├─ Entity
│   │   └─ House
│   │
│   ├─ Repository
│   │   └─ HouseRepository
│   │
│   └─ View
│       ├─ houses/index.html
│       └─ houses/show.html
│
├─ User（会員）
│   ├─ Controller
│   │   ├─ AuthController
│   │   ├─ UserController
│   │   └─ AdminUserController
│   │
│   ├─ Entity
│   │   └─ User
│   │
│   ├─ Repository
│   │   └─ UserRepository
│   │
│   └─ View
│       ├─ auth/login.html
│       ├─ auth/signup.html
│       ├─ users/show.html
│       └─ users/edit.html
│
├─ Reservation（予約）
│   ├─ Controller
│   │   └─ ReservationController
│   │
│   ├─ Entity
│   │   └─ Reservation
│   │
│   ├─ Repository
│   │   └─ ReservationRepository
│   │
│   └─ View
│       ├─ reservations/confirm.html
│       └─ reservations/index.html
│
├─ Payment（決済）
│   ├─ Controller
│   │   └─ PaymentController
│   │
│   └─ View
│       └─ payment/index.html
│
└─ Common（共通）
    ├─ Security
    │   └─ SecurityConfig
    │
    ├─ Fragment
    │   ├─ header.html
    │   └─ footer.html
    │
    └─ Static
        ├─ css
        ├─ js
        └─ images
```

## 決済が反映されない問題

### 原因
```
session.getMetadata()　などが正常に実行できない状態
イベントの中身をJavaオブジェクトに変換できなかった
JSON → Sessionへの変換失敗した結果、Optional.empty（StripeがSessionオブジェクトを作れなかった）となってしまっていた。
このイベントは
・APIバージョン差
・オブジェクト構造
・フィールド展開　などを理由に
SDKの安全デシリアライズに失敗する場合があるんだそう。

安全に変換できるか保証できないから…だったらしい。
```



### 検証のプロセス
```
①エラー発見
　〇起きていたこと
	決済は出来ていたが、DBに予約が反映されない（書き込まれない）
	・Stripe決済は成功
	・しかし予約処理が動かない
　〇確認事項
　　・Spring決済画面にて決済の確認
　　・MySQL上に予約情報の追記がなされていないことを確認
	
②Webhookが届いているか確認
　〇疑ったポイント
　　Webhookが来ていないのでは？
　〇確認事項
　　・Stripe のダッシュボード
　　・Stripe CLI
　　　stripe listen
　　　と
	　stripe trigger checkout.session.completed
　〇結果
　　　Webhookは正常に送信されていた
　　…つまり
　　　Stripe → サーバー通信は正常に行われている

③ Webhook処理コードを調査
　〇疑ったポイント
	Webhookは来ている→処理段階で失敗している可能性
  〇確認事項
  　　Webhookコード（何処で処理が止まっているのか）
  〇結果
  event.getDataObjectDeserializer().getObject()
	で止まっていた。なお、
　Optional.empty　
　になっていることを発見した。
（変換に失敗して、空のオブジェクトになっている）

④Sessionが取得できていないと判明
　〇原因
　　Session sessionを取得するはずが
	Optional.empty（Sessionなし）となっていたため
	session.getMetadata()などが実行できない状態になっていた
　　
	予約情報取得不可→DB保存処理動かない
　　と判明

⑤ Stripe CLIイベントの問題を疑う
　〇疑ったポイント
	イベント構造の問題
	stripe trigger checkout.session.completed
	を使用していたので、
	Stripe CLI のテストイベントが原因の可能性を調査。
　〇結果
	→SDKの安全デシリアライズで失敗することがある
	
　〇対策
　　・deserializeUnsafe()を使って強制デシリアライズを使用

	Session session =
	(Session) event.getDataObjectDeserializer()
	.deserializeUnsafe();
	
　〇結果
　　Session取得成功

Session取得には成功したが…新たな問題発見

⑥ metadata取得の問題を発見
　　〇起きていたこと
　　　　metadataが取れない

　　〇原因
	　　payment_intent が展開されていない
	〇解決策
	expandで解決
	Session.retrieve(
 	　sessionId,
 	　SessionRetrieveParams.builder()
 	　 .addExpand("payment_intent")
	　 .build(),
 	　 .null
		);
		を追加する
	〇結果　
	session.getMetadata()の取得に成功
	
	決済→DBへの書き込み→各ユーザーが予約後に予約データの取得を確認
```
	