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

タッチ操作に最適化された、大きなクリックエリアと視覚的フィードバックを持つコントロール群です。全てのコントロールで `useDefaultStyle: Boolean` 引数によりデフォルトスタイルの適用を制御できます。

#### TouchButton / TouchCheckBox / TouchRadioButton
標準より 1.5倍大きなサイズと、Ripple Effect（波紋効果）を備えています。

```kotlin
val button = TouchButton("Tap Me")
val checkBox = TouchCheckBox("Check Me")
val radio = TouchRadioButton("Select Me")
```

#### ToggleSwitch
モダンな ON/OFF 切り替えスイッチです。

```kotlin
val switch = ToggleSwitch("Wi-Fi")
switch.isSelected = true
```

#### TouchTextField / TouchComboBox / TouchDatePicker
入力および選択エリアを拡大し、タッチミスを軽減します。

```kotlin
val textField = TouchTextField()
val comboBox = TouchComboBox<String>()
val datePicker = TouchDatePicker()
```

#### TouchSpinner
左右に大型の `+` / `-` ボタンを配置した数値入力コントロールです。

```kotlin
val spinner = TouchSpinner(min = 0.0, max = 100.0, initial = 50.0, step = 5.0)
```

#### TouchTabPane
タブヘッダーの高さを拡大し、指での切り替えを容易にした TabPane です。

```kotlin
val tabPane = TouchTabPane()
tabPane.tabs.add(Tab("Home"))
```

## 3. 高度な機能

TouchFX には、タッチ操作をより快適にするための高度な機能が備わっています。

### 3.1 方向ロック (Direction Lock)

ドラッグ開始時の移動方向に基づいて、スクロール軸を水平または垂直に固定します。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isDirectionLockEnabled = true
```

### 3.2 スクロールバーの動的表示 (Dynamic Visibility)

スクロール中のみスクロールバーを表示し、静止時には自動的に隠します。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isDynamicScrollBarVisible = true
```

### 3.3 境界での跳ね返り (Bounce Effect)

スクロール上限/下限に達した際に境界を超えてスクロールし、滑らかに戻る視覚効果です。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isBounceEnabled = true
```

### 3.4 スナップ機能 (Snapping)

指定したピクセル単位でスクロール位置を吸着（スナップ）させます。

```kotlin
// 有効化
inertialListView.isSnapEnabled = true
inertialListView.snapUnitY = 60.0
```

### 3.5 Pull-to-Refresh (引っ張って更新)

リストを上限を超えて引っ張った際に、非同期の更新処理を実行します。

```kotlin
// 更新処理の登録 (CompletableFuture を返す)
inertialListView.onRefresh = {
    CompletableFuture.supplyAsync {
        // 更新ロジック...
    }
}
```

### 3.6 スワイプアクション (Swipe Actions)

リストアイテムを左右にスワイプして、アクションボタン（編集、削除など）を表示します。

```kotlin
inertialListView.swipeLeftFactory = { item, container ->
    Button("Edit").apply { setOnAction { container.reset() } }
}
```

### 3.7 スティッキーヘッダー (Sticky Headers)

リスト内の見出し項目を上端に固定して表示します。

```kotlin
inertialListView.isHeader = { it.startsWith("HEADER:") }
inertialListView.stickyHeaderEnabled = true
```

## 4. ジェスチャー操作 (Gestures)

任意のノードに対して、マルチタッチによる拡大・縮小、回転、長押しを追加できます。

```kotlin
val behavior = myNode.addGestureBehavior {
    onPinch = { factor -> myNode.scaleX *= factor; myNode.scaleY *= factor }
    onRotate = { delta -> myNode.rotate += delta }
    onLongPress = { x, y -> println("Long press at: $x, $y") }
}
```

## 5. パラメータの調整

スクロールの挙動は、以下のプロパティを通じて動的に調整可能です。

- `sensitivity`: スクロールの感度。
- `inertia`: 慣性の強さ。
- `friction`: 摩擦係数（デフォルト: `0.92`）。

## 6. スタイリング

デフォルトでタッチ操作に最適化された CSS が適用されます。
独自のスタイルのみを適用するには、コンストラクタで `useDefaultStyle = false` を指定してください。

```kotlin
val button = TouchButton("Custom", useDefaultStyle = false)
button.stylesheets.add(getClass().getResource("my-style.css").toExternalForm())
```

## 7. デモアプリケーションの実行

```bash
./gradlew :touchfx-demo:run
```
