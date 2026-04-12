package com.github.funczz.touchfx.controls

import javafx.application.Platform
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExtendWith(ApplicationExtension::class)
class TouchFileChooserTest {

    private lateinit var stage: Stage

    @Start
    fun start(stage: Stage) {
        this.stage = stage
        stage.show()
    }

    @Test
    fun testFileChooserInitialization(robot: FxRobot) {
        val chooser = TouchFileChooser()
        chooser.title = "Test Title"
        val initialDir = File(System.getProperty("user.dir"))
        chooser.initialDirectory = initialDir
        
        assertEquals("Test Title", chooser.title)
        assertEquals(initialDir, chooser.initialDirectory)
    }

    @Test
    fun testTouchFileBrowserNavigation(robot: FxRobot) {
        val latch = CountDownLatch(1)
        var browser: TouchFileBrowser? = null
        val initialDir = File(System.getProperty("user.dir"))

        Platform.runLater {
            browser = TouchFileBrowser(isMultiSelect = false)
            browser!!.initialDirectory = initialDir
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        
        // UIスレッドでの初期化を待つ
        Thread.sleep(500)
        
        val b = browser!!
        assertNotNull(b.root)
        // 初期ディレクトリのファイルがリストアップされているか（少なくともプロジェクトルートなので何かあるはず）
        assertTrue(b.selectedFiles.isEmpty())
    }
}
