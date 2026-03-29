# TouchFX 使用方法

TouchFX は、JavaFX アプリケーションにモダンなタッチ操作機能を提供するための Kotlin ライブラリです。

## 1. ライブラリの導入

本ライブラリをプロジェクトに導入するには、Gradle の依存関係に追加してください。

```kotlin
dependencies {
    implementation("com.github.funczz:touchfx:0.1.0")
}
```

※ 現在開発中のため、ローカルビルドまたは Maven ローカルリポジトリ経由での利用を想定しています。

## 2. 基本的な使い方

### 2.1 InertialListView

標準の `ListView` をラップし、慣性スクロール機能を付与します。
スティッキーヘッダー等のオーバーレイ機能を使用するため、シーングラフには `listView` ではなく `root` を追加してください。

```kotlin
val inertialListView = InertialListView<String>()
inertialListView.items.addAll("Item 1", "Item 2", "Item 3")

// root (StackPane) をシーングラフに追加
root.children.add(inertialListView.root)
```

### 2.2 InertialScrollPane

標準の `ScrollPane` をラップし、慣性スクロール機能を付与します。

```kotlin
val inertialScrollPane = InertialScrollPane()
inertialScrollPane.content = myLargeContentNode

// 内部の ScrollPane インスタンスをシーングラフに追加
root.children.add(inertialScrollPane.scrollPane)
```

### 2.3 TouchFriendlyControls

タッチ操作に最適化された、大きなクリックエリアを持つコントロール群です。

#### TouchButton
自動的に Ripple Effect（波紋効果）が適用され、タップしやすいサイズ（最小高さ 66px）のボタンです。

```kotlin
val button = TouchButton("Tap Me")
button.setOnAction { println("Tapped!") }
```

#### TouchCheckBox
ラベル部分を含めてヒット判定が広く、大型のチェックボックスです。

```kotlin
val checkBox = TouchCheckBox("Enable Feature")
```

#### TouchSlider
指で掴みやすい大型のつまみ（Thumb）と太いトラックを持つスライダーです。

```kotlin
val slider = TouchSlider(0.0, 100.0, 50.0)
```

## 3. 高度な機能

TouchFX には、タッチ操作をより快適にするための高度な機能が備わっています。

### 3.1 方向ロック (Direction Lock)

ドラッグ開始時の移動方向に基づいて、スクロール軸を水平または垂直に固定します。斜め方向の誤操作を防ぐのに有効です。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isDirectionLockEnabled = true
```

### 3.2 スクロールバーの動的表示 (Dynamic Visibility)

スクロール操作中（ドラッグ中および慣性移動中）のみスクロールバーを表示し、静止時には自動的に隠します。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isDynamicScrollBarVisible = true
```

### 3.3 境界での跳ね返り (Bounce Effect)

スクロールが上限または下限に達した際に、境界を超えてスクロールする「遊び」を持たせ、指を離した際に滑らかに境界まで戻る視覚効果を提供します。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isBounceEnabled = true
```

### 3.4 スナップ機能 (Snapping)

指定したピクセル単位でスクロール位置を吸着（スナップ）させます。カード型のレイアウトや、項目ごとのページングに有効です。

```kotlin
// 有効化
inertialListView.isSnapEnabled = true
inertialListView.snapUnitY = 60.0 // 60px ごとにスナップ
```

### 3.5 Pull-to-Refresh (引っ張って更新)

リストを上限を超えて引っ張った際に、非同期の更新処理を実行します。

```kotlin
// リフレッシュ用インジケータの設定
inertialListView.refreshIndicator = Label("Refreshing...")

// 更新処理の登録 (CompletableFuture を返す)
inertialListView.onRefresh = {
    CompletableFuture.runAsync {
        // 更新ロジック...
        Thread.sleep(2000)
    }.thenRun {
        Platform.runLater {
            // UIの更新...
        }
    }
}
```

### 3.6 タッチフィードバック (Ripple Effect)

タップした位置から波紋が広がる視覚効果を付与します。

```kotlin
// 有効化 (デフォルト: false)
inertialListView.isRippleEnabled = true
```

### 3.7 スワイプアクション (Swipe Actions)

リストアイテムを左右にスワイプして、アクションボタン（編集、削除など）を表示します。ファクトリで `null` を返すとそのアイテムのスワイプを無効化できます。

```kotlin
// 右スワイプ（左側に表示されるアクション）の設定
inertialListView.swipeLeftFactory = { item, container ->
    if (isSpecialItem(item)) null else {
        Button("Edit").apply {
            setOnAction { 
                // アクション実行
                container.reset() // コンテナを閉じる
            }
        }
    }
}
```

### 3.8 スティッキーヘッダー (Sticky Headers)

リスト内の見出し項目を上端に固定して表示します。

```kotlin
// ヘッダー判定条件の設定
inertialListView.isHeader = { it.startsWith("HEADER:") }

// 有効化
inertialListView.stickyHeaderEnabled = true
```

## 4. セルのカスタマイズ

`InertialListView` では、セルの見た目を完全に自由に定義できます。

```kotlin
inertialListView.cellContentFactory = { item ->
    // アイコンとテキストを並べたカスタムレイアウトを返す
    HBox(10.0).apply {
        alignment = Pos.CENTER_LEFT
        children.addAll(ImageView(item.icon), Label(item.name))
        style = "-fx-background-color: white;" // 背景の設定を推奨
    }
}
```

## 5. ジェスチャー操作 (Gestures)

任意のノードに対して、マルチタッチによる拡大・縮小や回転、長押しなどの高次ジェスチャーを簡単に追加できます。

```kotlin
val myNode = Rectangle(100.0, 100.0, Color.BLUE)

val behavior = myNode.addGestureBehavior {
    onPinch = { factor -> myNode.scaleX *= factor; myNode.scaleY *= factor }
    onRotate = { delta -> myNode.rotate += delta }
    onLongPress = { x, y -> println("Long press at: $x, $y") }
}
```

### 5.1 マウス・キーボードシミュレーション

マルチタッチ非対応環境（デスクトップ等）向けに、以下の操作が自動的に有効になります。

- **Zoom**: Ctrl + マウススクロール
- **Rotate**: Alt + マウススクロール
- **Pinch & Rotate**: Shift + マウスドラッグ（ノード中心を支点とした疑似マルチタッチ）

## 6. パラメータの調整

スクロールの挙動は、以下のプロパティを通じて動的に調整可能です。

- `sensitivity`: スクロールの感度。値が大きいほど、ドラッグ量に対して大きくスクロールします。
- `inertia`: 慣性の強さ。値が大きいほど、指を離した後のスクロール距離が長くなります。
- `friction`: 摩擦係数（減速率）。`0.0` から `1.0` の間で指定し、小さいほど早く停止します（デフォルト: `0.92`）。

## 7. スタイリング

デフォルトでタッチ操作に最適化された CSS が適用されます。
これを無効化し、独自のスタイルのみを適用するには、コンストラクタで `useDefaultStyle = false` を指定してください。

```kotlin
// デフォルトスタイルを無効化して独自の CSS を適用する
val button = TouchButton("Custom", useDefaultStyle = false)
button.stylesheets.add(getClass().getResource("my-style.css").toExternalForm())
```

## 8. デモアプリケーションの実行

ライブラリの機能を体験できるデモアプリケーションを、以下のコマンドで実行できます。

```bash
./gradlew :touchfx-demo:run
```

※ 実行には GUI 環境が必要です。
