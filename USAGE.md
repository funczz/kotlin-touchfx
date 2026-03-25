# TouchFX 使用方法

TouchFX は、JavaFX アプリケーションにモダンな慣性スクロール機能を提供するための Kotlin ライブラリです。

## 1. ライブラリの導入

本ライブラリをプロジェクトに導入するには、Gradle の依存関係に追加してください。

```kotlin
dependencies {
    implementation("com.github.funczz:touchfx:0.1.0")
}
```

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

## 3. パラメータの調整

スクロールの挙動は、以下のプロパティを通じて動的に調整可能です。

- `sensitivity`: スクロールの感度。値が大きいほど、ドラッグ量に対して大きくスクロールします（デフォルト: `0.005`）。
- `inertia`: 慣性の強さ。値が大きいほど、指を離した後のスクロール距離が長くなります（デフォルト: `0.0005`）。
- `friction`: 摩擦係数（減速率）。`0.0` から `1.0` の間で指定し、小さいほど早く停止します（デフォルト: `0.92`）。

```kotlin
inertialListView.sensitivity = 0.01
inertialListView.inertia = 0.001
inertialListView.friction = 0.95
```

## 4. スタイリング

デフォルトでタッチ操作に最適化された CSS（細いスクロールバーなど）が適用されます。
これを無効化するには、コンストラクタで `useDefaultStyle = false` を指定してください。

```kotlin
val listView = InertialListView<String>(useDefaultStyle = false)
```

## 5. デモアプリケーションの実行

ライブラリの機能を体験できるデモアプリケーションを、以下のコマンドで実行できます。

```bash
./gradlew :touchfx-demo:run
```

※ 実行には GUI 環境が必要です。
