# TouchFX 使用方法

TouchFX は、JavaFX アプリケーションにモダンな慣性スクロール機能を提供するための Kotlin ライブラリです。

## 1. ライブラリの導入

本ライブラリをプロジェクトに導入するには、Gradle の依存関係に追加してください。

```kotlin
dependencies {
    implementation("com.github.funczz:touchfx:0.1.0")
}
```

※ 現在開発中のため、ローカルビルドまたは Maven ローカルリポジトリ経うでの利用を想定しています。

## 2. 基本的な使い方

### InertialListView

標準の `ListView` をラップし、慣性スクロール機能を付与します。

```kotlin
val inertialListView = InertialListView<String>()
inertialListView.items.addAll("Item 1", "Item 2", "Item 3")

// 内部の ListView インスタンスをシーングラフに追加
root.children.add(inertialListView.listView)
```

### InertialScrollPane

標準の `ScrollPane` をラップし、慣性スクロール機能を付与します。

```kotlin
val inertialScrollPane = InertialScrollPane()
inertialScrollPane.content = myLargeContentNode

// 内部の ScrollPane インスタンスをシーングラフに追加
root.children.add(inertialScrollPane.scrollPane)
```

## 3. 高度な機能

TouchFX には、タッチ操作をより快適にするための高度な機能が備わっています。

### 3.1 方向ロック (Direction Lock)

ドラッグ開始時の移動方向に基づいて、スクロール軸を水平または垂直に固定します。斜め方向の誤操作を防ぐのに有効です。

```kotlin
// 有効化 (デフォルト: true)
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

### 3.4 方向別の感度・慣性設定

水平方向 (X) と垂直方向 (Y) で個別にパラメータを設定可能です。

```kotlin
// 個別設定
inertialScrollPane.sensitivityX = 0.01
inertialScrollPane.sensitivityY = 0.005

inertialScrollPane.inertiaX = 0.001
inertialScrollPane.inertiaY = 0.0005

// 一括設定
inertialScrollPane.sensitivity = 0.008
```

## 4. パラメータの調整

スクロールの挙動は、以下のプロパティを通じて動的に調整可能です。

- `sensitivity`: スクロールの感度。値が大きいほど、ドラッグ量に対して大きくスクロールします。
- `inertia`: 慣性の強さ。値が大きいほど、指を離した後のスクロール距離が長くなります。
- `friction`: 摩擦係数（減速率）。`0.0` から `1.0` の間で指定し、小さいほど早く停止します（デフォルト: `0.92`）。

## 5. スタイリング

デフォルトでタッチ操作に最適化された CSS（細いスクロールバーなど）が適用されます。
これを無効化するには、コンストラクタで `useDefaultStyle = false` を指定してください。

```kotlin
val listView = InertialListView<String>(useDefaultStyle = false)
```

## 6. デモアプリケーションの実行

ライブラリの機能を体験できるデモアプリケーションを、以下のコマンドで実行できます。

```bash
./gradlew :touchfx-demo:run
```

※ 実行には GUI 環境が必要です。
