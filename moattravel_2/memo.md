# 2周目研修メモ
研修中、どうしても気になった事項とかまとめておきたいメモを一覧にしておく場所（余り時間は取らないように…！）

## ディレクトリの構成と役割　（MVCに沿った考え方）

### Model(= データとロジック全体)
Entity / Repository / Service / DTO(formとか)

### View(= 画面表示)
HTML / Thymeleaf / CSS / JS

### Controller(=リクエストを受けて処理を振り分け)
Controller



##　機能のまとめ

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