# TouchFX 使用方法

TouchFX は、JavaFX アプリケーションにモダンなタッチ操作機能を提供するための Kotlin ライブラリです。

## 1. ライブラリの導入

本ライブラリをプロジェクトに導入するには、Gradle の依存関係に追加してください。

```kotlin
dependencies {
    implementation("com.github.funczz:touchfx:0.1.0")
}
```

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

タッチ操作に最適化されたコントロール群です。全てのコントロールで `useDefaultStyle: Boolean` 引数によりデフォルトスタイルの適用を制御できます。詳細はデモアプリの「Controls」タブを参照してください。

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

#### TouchDialog
タッチ操作に最適化されたダイアログボックスです。JavaFX 標準の `Dialog` と互換性を持ち、大きなボタン、広い余白、Ripple Effect を備えています。

```kotlin
// 情報ダイアログの表示
val dialog = TouchDialog.createAlert(
    Alert.AlertType.INFORMATION,
    "Operation Successful",
    "The task has been completed."
)
dialog.showAndWait()

// 確認ダイアログの表示と結果の取得
val confirm = TouchDialog.createAlert(
    Alert.AlertType.CONFIRMATION,
    "Confirm Action",
    "Are you sure you want to proceed?"
)
val result = confirm.showAndWait()
if (result.isPresent && result.get() == ButtonType.OK) {
    // 実行...
}
```

### 2.4 AdaptiveLayouts (レスポンシブコンテナ)

画面幅に応じて動的にレイアウトを調整するコンテナです。

#### AdaptivePane
幅の閾値（breakpoint）に基づき、子要素を水平（HBox風）または垂直（VBox風）に自動配置します。

```kotlin
val adaptivePane = AdaptivePane(breakpoint = 500.0)
adaptivePane.children.addAll(node1, node2, node3)
```

#### FluidGridPane
利用可能な幅に合わせて、1行あたりの列数を自動計算してグリッド配置します。

```kotlin
val fluidGrid = FluidGridPane(columnWidth = 200.0)
fluidGrid.children.addAll(items)
```

#### ResponsiveLayout
ナビゲーションとコンテンツの位置関係を画面幅に応じて柔軟に変更します。Android の Bottom Navigation と Navigation Rail の切り替えなどに最適です。

```kotlin
val layout = ResponsiveLayout(breakpoint = 600.0)
layout.navigation = myNavBar
layout.content = myMainContent

// 狭い画面での位置 (デフォルト: BOTTOM)
layout.narrowPosition = ResponsiveLayout.Side.BOTTOM
// 広い画面での位置 (デフォルト: LEFT)
layout.widePosition = ResponsiveLayout.Side.LEFT
```

## 3. 高度な機能

TouchFX には、タッチ操作をより快適にするための高度な機能が備わっています。

### 3.1 方向ロック (Direction Lock)

ドラッグ開始時の移動方向に基づいて、スクロール軸を水平または垂直に固定します。
垂直リスト内のスワイプアクション等との干渉を防ぐため、デフォルトで **有効 (true)** に設定されています。

```kotlin
// 無効化する場合
inertialScrollPane.isDirectionLockEnabled = false
```

### 3.2 スクロールバーの動的表示 (Dynamic Visibility)

スクロール中のみスクロールバーを表示し、静止時には自動的に隠します。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isDynamicScrollBarVisible = true
```

### 3.3 境界での跳ね返り (Bounce Effect)

スクロール上限/下限に達した際に境界を超えてスクロールし、滑らかに戻る視覚効果です。
移動範囲の制限や、復元速度の個別設定が可能です。

```kotlin
// 有効化 (デフォルト: false)
inertialScrollPane.isBounceEnabled = true

// --- 詳細設定 ---

// 最大移動距離の制限 (ピクセル単位。デフォルト: 無制限)
inertialScrollPane.bounceMaxRangeY = 100.0

// 復元速度の調整 (0.01〜1.0、値が大きいほど速く戻る)
// デフォルトは 0.45 (瞬時に戻るキビキビとした設定)
inertialScrollPane.bounceRestorationY = 0.15 // 約0.5秒かけて戻る設定
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

### 3.6 タッチフィードバック (Ripple Effect)

タップした位置から波紋が広がる視覚効果を付与します。

### 3.7 スワイプアクション (Swipe Actions)

リストアイテムを左右にスワイプして、アクションボタン（編集、削除など）を表示します。

```kotlin
inertialListView.swipeLeftFactory = { item, container ->
    Button("Edit").apply { setOnAction { container.reset() } }
}
```

### 3.8 スティッキーヘッダー (Sticky Headers)

リスト内の見出し項目を上端に固定して表示します。

```kotlin
inertialListView.isHeader = { it.startsWith("HEADER:") }
inertialListView.stickyHeaderEnabled = true
```

### 3.9 オンデマンド・データロード (On-Demand Data Loading)

大量のデータを扱う際、画面に表示されている範囲のデータのみを動的にロードします。

```kotlin
val listView = InertialListView<MyData>()

// 1. 仮想的なアイテム枠を確保
listView.setVirtualItems(10_000, placeholder = null)

// 2. 可視範囲が変化した際のデータ取得ロジックを実装
listView.onVisibleRangeChanged = { first, last ->
    // 指定範囲 (first..last) のデータを非同期で取得して items を更新
}
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

- `sensitivity`: スクロールの感度。デフォルトの **1.0** は指の移動量とリストのスクロール量が 1:1 で同期するようにスケーリングされています。
- `inertia`: 慣性の強さ。指を離した後の「滑り」の長さを決定します。
- `friction`: 摩擦係数（デフォルト: `0.92`）。
- `bounceMaxRangeX/Y`: Bounce 時の最大移動距離。
- `bounceRestorationX/Y`: Bounce から戻る際の速度（0.01〜1.0）。

## 6. スタイリング

デフォルトでタッチ操作に最適化された CSS が適用されます。独自のスタイルを適用するには `useDefaultStyle = false` を指定してください。

```kotlin
val button = TouchButton("Custom", useDefaultStyle = false)
button.stylesheets.add(getClass().getResource("my-style.css").toExternalForm())
```

## 7. デモアプリケーションの実行

```bash
./gradlew :touchfx-demo:run
```
