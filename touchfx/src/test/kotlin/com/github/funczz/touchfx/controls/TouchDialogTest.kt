package com.github.funczz.touchfx.controls

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExtendWith(ApplicationExtension::class)
class TouchDialogTest {

    private lateinit var stage: Stage

    @Start
    fun start(stage: Stage) {
        this.stage = stage
        stage.show()
    }

    @Test
    fun testTouchDialogInitialization(robot: FxRobot) {
        val latch = CountDownLatch(1)
        var dialog: TouchDialog<ButtonType>? = null

        Platform.runLater {
            dialog = TouchDialog.createAlert(
                Alert.AlertType.INFORMATION,
                "Test Header",
                "Test Content"
            )
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        
        val d = dialog!!
        val expectedTitle = com.github.funczz.touchfx.i18n.TouchFXI18n.getString("dialog.title.information")
        assertEquals(expectedTitle, d.title)
        assertEquals("Test Header", d.headerText)
        assertEquals("Test Content", d.contentText)
        assertTrue(d.dialogPane is TouchDialogPane)
        assertTrue(d.dialogPane.buttonTypes.contains(ButtonType.OK))
    }

    @Test
    fun testConfirmationDialogButtons(robot: FxRobot) {
        val latch = CountDownLatch(1)
        var dialog: TouchDialog<ButtonType>? = null

        Platform.runLater {
            dialog = TouchDialog.createAlert(
                Alert.AlertType.CONFIRMATION,
                "Confirm",
                "Are you sure?"
            )
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        
        val d = dialog!!
        assertTrue(d.dialogPane.buttonTypes.contains(ButtonType.OK))
        assertTrue(d.dialogPane.buttonTypes.contains(ButtonType.CANCEL))
    }
}
