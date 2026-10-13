# 盤面・行消去テスト設計

## 対象

- 要件対象: `Board`
- テストクラス: `BoardTest`
- テストソース: `src/test/java/BoardTest.java`
- 状態: テストコード実装済み・今回の作業では未実行

## 要件

| 要件ID | 要件 |
|---|---|
| BRD-REQ-01 | 新しい盤面の全マスは空である。 |
| BRD-REQ-02 | 行が完成しているのは、その行の全列が埋まっている場合に限る。 |
| BRD-REQ-03 | 1行を消去すると、上の行は1段下がり、最上段は空になる。 |
| BRD-REQ-04 | 完成行をすべて消去し、消去した行数を返す。 |
| BRD-REQ-05 | `clear()` の後は全マスが空である。 |
| BRD-REQ-06 | 盤面のピクセル寸法は、列数・行数にブロックサイズを掛けた値である。 |
| BRD-REQ-07 | 指定した行・列にブロックを設定し、その位置から取得できる。 |

## テストケース

| ケースID | 前提条件 | 操作 | 期待結果 | 優先度 | 状態 |
|---|---|---|---|---|---|
| BRD-001 | 新しい `Board` | 全20行×10列を `getBlock()` で確認する | すべて `null` | P1 | 実装済み・未実行 |
| BRD-002a | 全列が空の行 | `isRowFull(row)` を呼ぶ | `false` | P1 | 実装済み・未実行 |
| BRD-002b | 1マスだけ空けた行 | `isRowFull(row)` を呼ぶ | `false` | P1 | 実装済み・未実行 |
| BRD-002c | 全列を埋めた行 | `isRowFull(row)` を呼ぶ | `true` | P1 | 実装済み・未実行 |
| BRD-003 | 異なる行に識別できる色のブロックを置いた盤面 | 中間行を `clearRow(row)` で消去する | 消去行より上の各行が1段下がり、最上段が空になる。消去行より下は変化しない | P1 | 実装済み・未実行 |
| BRD-004a | 完成行がない盤面 | `clearFullRows()` を呼ぶ | 戻り値が0で、盤面が変化しない | P1 | 実装済み・未実行 |
| BRD-004b | 完成行が1行ある盤面 | `clearFullRows()` を呼ぶ | 戻り値が1で、その行が消去される | P1 | 実装済み・未実行 |
| BRD-004c | 隣接した完成行が2行ある盤面 | `clearFullRows()` を呼ぶ | 戻り値が2で、両方が消去される | P1 | 実装済み・未実行 |
| BRD-004d | 離れた完成行が2行ある盤面 | `clearFullRows()` を呼ぶ | 戻り値が2で、両方が消去される。間の未完成行は保持される | P1 | 実装済み・未実行 |
| BRD-005 | 一部または全マスにブロックがある盤面 | `clear()` を呼ぶ | 全マスが `null` | P2 | 実装済み・未実行 |
| BRD-006 | `Board` の定数 | `getWidth()` と `getHeight()` を呼び出す | 幅が `COLS * BLOCK_SIZE`、高さが `ROWS * BLOCK_SIZE` | P2 | 実装済み・未実行 |
| BRD-007 | 新しい `Board` | 異なる行・列に色を設定し、指定位置と未設定位置を取得する | 設定した位置から該当色を取得でき、未設定位置は `null` | P1 | 実装済み・未実行 |

## テストメソッドとの対応

| ケースID | テストメソッド |
|---|---|
| BRD-001 | `newBoardStartsEmpty` |
| BRD-002a | `emptyRowIsNotFull` |
| BRD-002b | `partiallyFilledRowIsNotFull` |
| BRD-002c | `rowWithAllColumnsFilledIsFull` |
| BRD-003 | `clearRowMovesRowsDownAndLeavesRowsBelowUnchanged` |
| BRD-004a | `clearFullRowsReturnsZeroAndPreservesBoardWhenNoRowIsFull` |
| BRD-004b | `clearFullRowsRemovesSingleCompletedRow` |
| BRD-004c | `clearFullRowsRemovesAdjacentCompletedRows` |
| BRD-004d | `clearFullRowsRemovesNonAdjacentCompletedRowsAndKeepsRowsBetweenThem` |
| BRD-005 | `clearRemovesEveryBlock` |
| BRD-006 | `boardDimensionsMatchRowsColumnsAndBlockSize` |
| BRD-007 | `setBlockAndGetBlockUseRequestedCoordinates` |

## 実装時の注意

- `Board` の行・列は0始まりです。ケースでは、最上段を0行目、最下段を19行目として扱います。
- 消去後の行移動を確認するため、行ごとに異なる色などの識別可能な値を配置します。
- `setBlock()` の範囲外アクセス時の仕様は現在定義されていません。この設計では範囲外入力をテスト対象に含めません。
