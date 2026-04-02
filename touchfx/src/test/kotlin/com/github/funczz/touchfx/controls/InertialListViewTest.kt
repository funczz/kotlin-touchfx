package com.github.funczz.touchfx.controls

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InertialListViewTest : ApplicationTest() {

    private lateinit var listView: InertialListView<String>

    override fun start(stage: Stage) {
        listView = InertialListView<String>().apply {
            cellContentFactory = { Label(it) }
        }
        val root = StackPane(listView.root)
        stage.scene = Scene(root, 400.0, 400.0)
        stage.show()
    }

    /**
     * setVirtualItems によって items リストが正しく初期化されることを検証。
     */
    @Test
    fun testSetVirtualItems() {
        val count = 100
        val placeholder = "LOADING"
        
        interact {
            listView.setVirtualItems(count, placeholder)
        }
        
        assertEquals(count, listView.items.size)
        assertEquals(placeholder, listView.items[0])
        assertEquals(placeholder, listView.items[count - 1])
    }

    /**
     * 可視範囲変更時のコールバックが少なくとも初期化時に呼ばれることを検証。
     */
    @Test
    fun testOnVisibleRangeChanged() {
        val count = 100
        val latch = CountDownLatch(1)
        var firstIdx = -1

        interact {
            listView.setVirtualItems(count, "Item")
            listView.onVisibleRangeChanged = { first, _ ->
                firstIdx = first
                latch.countDown()
            }
        }

        // Skin の生成とレイアウトを待機
        repeat(10) {
            if (listView.listView.skin != null) return@repeat
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(100)
        }

        interact {
            listView.update()
        }

        // 初期ロードのコールバックを待機
        val completed = latch.await(5, TimeUnit.SECONDS)
        assertTrue(completed, "Visible range change callback was not triggered. firstIdx=$firstIdx")
        assertTrue(firstIdx >= 0, "First visible index should be >= 0")
    }

    /**
     * TouchBehavior の新しい感度基準と方向ロックのデフォルト値を検証。
     */
    @Test
    fun testDefaultParameters() {
        assertEquals(0.005, listView.sensitivityY, "Default sensitivityY should be 0.005")
        // デフォルト値を 0.06 に変更
        assertEquals(0.06, listView.inertiaY, "Default inertiaY should be 0.06")
        assertTrue(listView.isDirectionLockEnabled, "Direction lock should be enabled by default")
    }
}
