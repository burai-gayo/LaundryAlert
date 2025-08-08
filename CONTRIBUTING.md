# Contributing to LaundryAlert

LaundryAlertプロジェクトへの貢献をお考えいただき、ありがとうございます！このドキュメントでは、プロジェクトに貢献する方法について説明します。

## 貢献の方法

### バグ報告

バグを発見した場合は、以下の情報を含めてIssueを作成してください：

- **バグの概要**: 何が起こったかの簡潔な説明
- **再現手順**: バグを再現するための詳細な手順
- **期待される動作**: 本来どのような動作をするべきか
- **実際の動作**: 実際に何が起こったか
- **環境情報**: 
  - Android バージョン
  - デバイス情報
  - アプリバージョン
- **スクリーンショット**: 可能であれば問題を示すスクリーンショット

### 機能要求

新しい機能の提案は歓迎します。以下の情報を含めてIssueを作成してください：

- **機能の概要**: 提案する機能の簡潔な説明
- **動機**: なぜこの機能が必要なのか
- **詳細な説明**: 機能の詳細な仕様
- **代替案**: 考慮した他の解決方法があれば

### プルリクエスト

コードの貢献を行う場合は、以下の手順に従ってください：

1. **フォーク**: このリポジトリをフォークします
2. **ブランチ作成**: 機能ブランチを作成します
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **開発**: 変更を行います
4. **テスト**: 変更が正しく動作することを確認します
5. **コミット**: 意味のあるコミットメッセージで変更をコミットします
6. **プッシュ**: フォークしたリポジトリにプッシュします
7. **プルリクエスト**: 元のリポジトリに対してプルリクエストを作成します

## 開発ガイドライン

### コーディング規約

- **言語**: Kotlin
- **コードスタイル**: Android Kotlin Style Guide に従う
- **命名規則**: 
  - クラス名: PascalCase
  - 関数名・変数名: camelCase
  - 定数: UPPER_SNAKE_CASE
- **コメント**: 複雑なロジックには適切なコメントを追加

### アーキテクチャ

- **MVVM パターン**: Model-View-ViewModel アーキテクチャを維持
- **Repository パターン**: データアクセスの抽象化
- **Dependency Injection**: 可能な限り依存性注入を使用

### テスト

- **Unit Tests**: 新しい機能には適切なユニットテストを追加
- **Integration Tests**: 必要に応じて統合テストを追加
- **UI Tests**: 重要なユーザーフローにはUIテストを追加

### コミットメッセージ

コミットメッセージは以下の形式に従ってください：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type**:
- `feat`: 新機能
- `fix`: バグ修正
- `docs`: ドキュメントのみの変更
- `style`: コードの意味に影響しない変更（空白、フォーマット等）
- `refactor`: バグ修正でも機能追加でもないコード変更
- `test`: テストの追加や修正
- `chore`: ビルドプロセスやツールの変更

**例**:
```
feat(notification): add snooze functionality

Add ability to snooze rain alerts for 10 minutes.
Users can now delay notifications when they need more time.

Closes #123
```

## 開発環境のセットアップ

1. **必要なツール**:
   - Android Studio Arctic Fox (2020.3.1) 以降
   - JDK 17以降
   - Git

2. **プロジェクトのクローン**:
   ```bash
   git clone https://github.com/burai-gayo/LaundryAlert.git
   cd LaundryAlert
   ```

3. **依存関係のインストール**:
   - Android Studio でプロジェクトを開く
   - Gradle Sync を実行

4. **ビルドとテスト**:
   ```bash
   ./gradlew build
   ./gradlew test
   ```

## プルリクエストのレビュープロセス

1. **自動チェック**: CI/CDパイプラインによる自動テスト
2. **コードレビュー**: メンテナーによるコードレビュー
3. **テスト**: 機能テストとリグレッションテスト
4. **マージ**: 承認後にメインブランチにマージ

## 質問やサポート

- **一般的な質問**: [Discussions](https://github.com/burai-gayo/LaundryAlert/discussions) を使用
- **バグ報告**: [Issues](https://github.com/burai-gayo/LaundryAlert/issues) を作成
- **機能要求**: [Issues](https://github.com/burai-gayo/LaundryAlert/issues) を作成

## 行動規範

このプロジェクトに参加するすべての人は、以下の行動規範に従うことが期待されます：

- **尊重**: すべての参加者を尊重し、建設的なフィードバックを提供する
- **包括性**: 多様な背景を持つ人々を歓迎する
- **協力**: 共通の目標に向かって協力する
- **プロフェッショナリズム**: プロフェッショナルで礼儀正しい態度を維持する

## ライセンス

このプロジェクトに貢献することで、あなたの貢献がMITライセンスの下でライセンスされることに同意したものとみなされます。

---

貢献していただき、ありがとうございます！あなたの参加により、LaundryAlertはより良いアプリになります。

