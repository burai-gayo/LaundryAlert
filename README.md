# LaundryAlert 🌧️👕

天気情報に基づいて洗濯物の取り込みタイミングをお知らせするAndroidアプリです。

## 概要

LaundryAlertは、リアルタイムの天気情報を監視し、雨が降る前に洗濯物を取り込むよう通知してくれる実用的なアプリです。忙しい日常の中で、洗濯物を雨から守るお手伝いをします。

## 主要機能

### 🌤️ リアルタイム天気情報
- 現在の気温、湿度、降水確率を表示
- Open-Meteo APIを使用した正確な天気データ
- 位置情報に基づく地域別天気情報

### 👕 洗濯物状態管理
- 「干していない」「干し中」「取り込み済み」の3つの状態を管理
- ワンタップで状態変更が可能
- 干した時刻の自動記録

### 🔔 スマート通知システム
- 降水確率が設定した閾値を超えた際の自動アラート
- 通知から直接「取り込み完了」や「スヌーズ」が可能
- バックグラウンドでの定期監視

### ⚙️ カスタマイズ可能な設定
- 降水確率の閾値設定（デフォルト30%）
- アラートタイミングの調整
- 天気情報の更新間隔設定
- 位置情報の自動取得/手動設定

## 技術仕様

- **開発言語**: Kotlin
- **最小SDK**: API Level 24 (Android 7.0)
- **ターゲットSDK**: API Level 34 (Android 14)
- **アーキテクチャ**: MVVM (Model-View-ViewModel)
- **天気API**: Open-Meteo API (無料、APIキー不要)
- **UI**: Material Design 3準拠

## 必要な権限

- **位置情報**: 天気情報取得のため
- **通知**: 雨アラート送信のため
- **正確なアラーム**: 定時通知のため
- **ネットワーク**: 天気API通信のため

## インストール方法

### 開発環境
- Android Studio Arctic Fox (2020.3.1) 以降
- JDK 17以降
- Android SDK API Level 34

### ビルド手順
1. このリポジトリをクローン
```bash
git clone https://github.com/[username]/LaundryAlert.git
cd LaundryAlert
```

2. Android Studioでプロジェクトを開く

3. 必要なSDKコンポーネントをインストール
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Google Play Services

4. Gradle Syncを実行

5. Build → Make Project でビルド

6. Run → Run 'app' でデバイス/エミュレータにインストール

## 使用方法

### 初回セットアップ
1. アプリを起動
2. 位置情報の権限を許可
3. 通知の権限を許可（Android 13以降）
4. 設定画面で降水確率の閾値を調整

### 基本的な使い方
1. **洗濯物を干す時**: 「洗濯物を干す」ボタンをタップ
2. **取り込む時**: 「取り込む」ボタンまたは通知のアクションをタップ
3. **雨アラート受信時**: 通知を確認し、適切なアクションを選択

## プロジェクト構造

```
app/src/main/java/com/laundryalert/
├── MainActivity.kt              # メイン画面
├── SettingsActivity.kt          # 設定画面
├── HistoryActivity.kt           # 履歴画面
├── model/
│   └── LaundryStatus.kt         # 洗濯物状態enum
├── viewmodel/
│   └── MainViewModel.kt         # メイン画面ViewModel
├── repository/
│   ├── WeatherRepository.kt     # 天気データ管理
│   └── LaundryRepository.kt     # 洗濯物データ管理
├── api/
│   └── WeatherApiService.kt     # 天気API通信
├── service/
│   ├── LocationService.kt       # 位置情報サービス
│   ├── WeatherManager.kt        # 天気管理サービス
│   ├── AlarmScheduler.kt        # アラーム管理
│   └── WeatherMonitorService.kt # 天気監視サービス
├── receiver/
│   ├── AlarmReceiver.kt         # アラーム受信
│   ├── NotificationManager.kt   # 通知管理
│   ├── NotificationActionReceiver.kt # 通知アクション
│   └── BootReceiver.kt          # 端末起動時処理
└── worker/
    ├── WeatherCheckWorker.kt    # 天気チェックワーカー
    └── SnoozeWorker.kt          # スヌーズワーカー
```

## 今後の拡張予定

- [ ] 週間天気予報の表示
- [ ] 洗濯物の種類別乾燥時間予測
- [ ] 家族間での洗濯状態共有
- [ ] ダークモード対応
- [ ] ウィジェット機能

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。詳細は[LICENSE](LICENSE)ファイルをご覧ください。

## 貢献

プルリクエストやイシューの報告を歓迎します。貢献する前に、[CONTRIBUTING.md](CONTRIBUTING.md)をお読みください。

## サポート

質問やサポートが必要な場合は、[Issues](https://github.com/[username]/LaundryAlert/issues)でお気軽にお問い合わせください。

---

**開発者**: LaundryAlert Development Team  
**バージョン**: 1.0.0  
**最終更新**: 2025年8月8日

