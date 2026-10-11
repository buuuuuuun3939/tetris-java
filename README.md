# tetris-java

Java (Swing) で実装したシンプルなテトリスです。

## 特徴

- 7種類のテトリミノ（I, O, T, S, Z, J, L）
- 落下・左右移動・回転・ソフトドロップ
- ライン消去とスコア加算
- Next（次のミノ）表示
- BGM再生（一時停止・ゲームオーバー時に停止）
- ミノ固定時・ライン消去時・ゲームオーバー時・回転時・左右移動時・ハードドロップ時の効果音再生
- 一時停止 / ゲームオーバー後のリスタート

## 動作環境

- Java 17以上（`javax.swing` / `javax.sound.sampled` を使用）

## ビルドとテスト

Mavenをインストールした環境で、プロジェクトのルートから次を実行します。

```bash
mvn test
```

JUnit 5のテストを実行します。テストを含めてビルドする場合は `mvn package` を実行します。

テストの方針と機能別のケースは [`docs/test-design/`](docs/test-design/TEST_PLAN.md) にまとめています。

## 起動方法

Mavenでビルドした後、プロジェクトのルートで起動します。

```bash
mvn package
java -cp target/classes Main
```

従来どおり、Javaだけで直接コンパイルして起動することもできます。

```bash
javac *.java
java Main
```

## 操作方法

| キー | 動作 |
| --- | --- |
| ← | 左に移動 |
| → | 右に移動 |
| ↓ | 下に移動（ソフトドロップ） |
| ↑ | 回転 |
| P | 一時停止 / 再開 |
| R | リスタート（ゲームオーバー時のみ） |

## BGMについて

BGMファイル（`bgm/korobeiniki_piano.wav`）は著作権フリー音源で、リポジトリに含まれています。
BGMはカレントディレクトリからの相対パス（`./bgm/`）で読み込むため、リポジトリのルートで起動してください。音源が見つからない場合、BGMは再生されませんがゲーム自体は動作します。

効果音は `bgm/lock.wav`（ミノ固定時）、`bgm/clear.wav`（ライン消去時）、`bgm/gameover.wav`（ゲームオーバー時）、`bgm/rotate.wav`（回転時）、`bgm/move.wav`（左右移動時）、`bgm/drop.wav`（ハードドロップ時）から読み込みます。音源が見つからない場合もゲームは動作します。

## ファイル構成

既存のJavaソースはルート直下に残し、テストは `src/test/java` に配置します。

| ファイル | 役割 |
| --- | --- |
| `Main.java` | エントリーポイント。ウィンドウを生成しゲームを起動 |
| `GamePanel.java` | ゲーム全体の描画・入力処理・ゲームループ |
| `Board.java` | ボード（盤面）の状態管理、ライン消去処理 |
| `Tetromino.java` | テトリミノの形状・色・回転処理 |
| `TetrominoType.java` | テトリミノの種類（enum） |
| `BgmPlayer.java` | BGMの再生・停止 |
| `SoundEffectPlayer.java` | 効果音の読み込み・再生 |
