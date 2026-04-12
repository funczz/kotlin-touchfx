package com.github.funczz.touchfx.controls

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Window
import javafx.util.StringConverter
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.PosixFileAttributes
import java.nio.file.attribute.PosixFilePermissions
import java.text.SimpleDateFormat
import java.util.*

/**
 * タッチ操作に最適化したファイル選択ダイアログ。
 */
class TouchFileChooser {

    class ExtensionFilter(val description: String, vararg val extensions: String)

    var title: String? = null
    var initialDirectory: File? = null
    val extensionFilters: ObservableList<ExtensionFilter> = FXCollections.observableArrayList()
    var selectedExtensionFilter: ExtensionFilter? = null

    var prefWidth: Double = 900.0 // 検索ボックス追加に合わせて幅を調整
    var prefHeight: Double = 600.0

    fun showOpenDialog(owner: Window?): File? {
        val dialog = createDialog(isMultiSelect = false, isDirOnly = false)
        owner?.let { dialog.initOwner(it) }
        val result = dialog.showAndWait()
        return result.map { it.firstOrNull() }.orElse(null)
    }

    fun showOpenMultipleDialog(owner: Window?): List<File>? {
        val dialog = createDialog(isMultiSelect = true, isDirOnly = false)
        owner?.let { dialog.initOwner(it) }
        val result = dialog.showAndWait()
        return result.orElse(null)
    }

    fun showDirectoryDialog(owner: Window?): File? {
        val dialog = createDialog(isMultiSelect = false, isDirOnly = true)
        owner?.let { dialog.initOwner(it) }
        val result = dialog.showAndWait()
        return result.map { it.firstOrNull() }.orElse(null)
    }

    private fun createDialog(isMultiSelect: Boolean, isDirOnly: Boolean): TouchDialog<List<File>> {
        val dialog = TouchDialog<List<File>>()
        dialog.title = title ?: if (isDirOnly) "Select Directory" else "Open File"
        
        val browser = TouchFileBrowser(isMultiSelect = isMultiSelect, isDirOnly = isDirOnly)
        browser.initialDirectory = initialDirectory ?: File(System.getProperty("user.home"))
        browser.extensionFilters.addAll(extensionFilters)
        browser.root.setPrefSize(prefWidth, prefHeight)
        
        dialog.dialogPane.content = browser.root
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        
        val okButton = dialog.dialogPane.lookupButton(ButtonType.OK)
        okButton.disableProperty().bind(Bindings.isEmpty(browser.selectedFiles))
        
        dialog.setResultConverter { buttonType ->
            if (buttonType == ButtonType.OK) browser.selectedFiles.toList() else null
        }
        
        return dialog
    }
}

internal class TouchFileBrowser(val isMultiSelect: Boolean = false, val isDirOnly: Boolean = false) {

    enum class SortType(val label: String) { 
        NAME("Name"), SIZE("Size"), DATE("Date");
        override fun toString(): String = label
    }

    var initialDirectory: File = File(System.getProperty("user.home"))
    val extensionFilters: ObservableList<TouchFileChooser.ExtensionFilter> = FXCollections.observableArrayList()
    val selectedFiles: ObservableList<File> = FXCollections.observableArrayList()
    
    private val fileList = InertialListView<File>()
    private var currentDirFile: File = File(System.getProperty("user.home"))
    private var allFilesOfCurrentDir: List<File> = emptyList()
    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm")

    private var sortBy = SortType.NAME
    private var sortAscending = true
    private var filterText = ""

    val root: VBox = VBox()

    init {
        root.styleClass.add("touch-file-browser")
        
        // パス表示
        val pathBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(5.0, 10.0, 5.0, 10.0)
            styleClass.add("path-box")
        }
        
        val scrollPath = InertialScrollPane().apply {
            content = pathBox
            scrollPane.minHeight = 66.0
            scrollPane.maxHeight = 66.0
            isBounceEnabledX = true
        }

        // ツールバー
        val toolbar = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("toolbar")
            minHeight = 60.0
            
            // 一括選択・解除
            val selectButtons = HBox(10.0).apply {
                alignment = Pos.CENTER_LEFT
                visibleProperty().set(isMultiSelect)
                managedProperty().bind(visibleProperty())
                
                children.addAll(
                    TouchButton("Select All").apply { 
                        styleClass.add("toolbar-button")
                        minHeight = 44.0; maxHeight = 44.0
                        setOnAction { selectAll() } 
                    },
                    TouchButton("Deselect All").apply { 
                        styleClass.add("toolbar-button")
                        minHeight = 44.0; maxHeight = 44.0
                        setOnAction { deselectAll() } 
                    }
                )
            }
            
            // ソートUI
            val sortBox = HBox(5.0).apply {
                alignment = Pos.CENTER_LEFT
                val sortCombo = TouchComboBox<SortType>().apply {
                    styleClass.add("toolbar-combo-box")
                    items.addAll(SortType.values())
                    value = sortBy
                    prefWidth = 150.0
                    minHeight = 44.0
                    maxHeight = 44.0
                    
                    val stringConverter = object : StringConverter<SortType>() {
                        override fun toString(st: SortType?): String = st?.label ?: ""
                        override fun fromString(string: String?): SortType? = SortType.values().find { it.label == string }
                    }
                    converter = stringConverter
                    
                    val cellFactory = { _: ListView<SortType>? ->
                        object : ListCell<SortType>() {
                            override fun updateItem(item: SortType?, empty: Boolean) {
                                super.updateItem(item, empty)
                                if (empty || item == null) {
                                    text = null
                                } else {
                                    text = item.label
                                    styleClass.add("toolbar-combo-box-cell")
                                }
                            }
                        }
                    }
                    setCellFactory(cellFactory)
                    setButtonCell(cellFactory(null))
                    
                    valueProperty().addListener { _, _, newValue -> 
                        if (newValue != null) {
                            sortBy = newValue
                            applyFilterSortAndRefresh()
                        }
                    }
                }
                val orderBtn = TouchButton(if (sortAscending) "↓" else "↑").apply {
                    styleClass.add("toolbar-button")
                    minWidth = 50.0
                    minHeight = 44.0; maxHeight = 44.0
                    setOnAction {
                        sortAscending = !sortAscending
                        text = if (sortAscending) "↓" else "↑"
                        applyFilterSortAndRefresh()
                    }
                }
                children.addAll(Label("Sort:"), sortCombo, orderBtn)
            }

            // 検索UI
            val searchBox = HBox(5.0).apply {
                alignment = Pos.CENTER_LEFT
                val searchField = TouchTextField().apply {
                    promptText = "Search files..."
                    prefWidth = 200.0
                    minHeight = 44.0
                    maxHeight = 44.0
                    textProperty().addListener { _, _, newValue ->
                        filterText = newValue.lowercase()
                        applyFilterSortAndRefresh()
                    }
                }
                children.addAll(Label("Search:"), searchField)
            }

            val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

            val newFolderBtn = TouchButton("New Folder").apply {
                styleClass.add("toolbar-button")
                minHeight = 44.0; maxHeight = 44.0
                setOnAction { showNewFolderDialog() }
            }
            
            children.addAll(selectButtons, sortBox, searchBox, spacer, newFolderBtn)
        }
        
        // ファイルリスト
        fileList.apply {
            isRippleEnabled = true
            isBounceEnabledY = true
            
            cellContentFactory = { file ->
                HBox(15.0).apply {
                    alignment = Pos.CENTER_LEFT
                    padding = Insets(10.0, 15.0, 10.0, 15.0)
                    minHeight = 70.0
                    styleClass.add("file-item")
                    if (selectedFiles.contains(file)) styleClass.add("selected")
                    
                    val showCheckBox = if (isDirOnly) file.isDirectory else !file.isDirectory
                    if (showCheckBox) {
                        children.add(TouchCheckBox("").apply {
                            isSelected = selectedFiles.contains(file)
                            setOnAction {
                                if (isMultiSelect) {
                                    if (isSelected) { if (!selectedFiles.contains(file)) selectedFiles.add(file) }
                                    else { selectedFiles.remove(file) }
                                } else {
                                    if (isSelected) { selectedFiles.setAll(listOf(file)) }
                                    else { selectedFiles.clear() }
                                }
                                fileList.refresh()
                            }
                        })
                    }

                    val icon = if (file.isDirectory) Rectangle(24.0, 20.0, Color.web("#FFA000"))
                               else Rectangle(20.0, 24.0, Color.web("#2196F3")).apply { if (isDirOnly) opacity = 0.3 }
                    
                    val infoContainer = VBox(2.0).apply {
                        alignment = Pos.CENTER_LEFT
                        val nameLabel = Label(file.name.ifEmpty { file.path }).apply {
                            styleClass.add("label"); style = "-fx-font-weight: bold; -fx-font-size: 1.1em;"
                            if (isDirOnly && !file.isDirectory) opacity = 0.3
                        }
                        val attrLabel = Label(getFileAttributes(file)).apply {
                            style = "-fx-font-size: 0.85em; -fx-text-fill: gray;"
                            if (isDirOnly && !file.isDirectory) opacity = 0.3
                        }
                        children.addAll(nameLabel, attrLabel)
                    }
                    children.addAll(icon, infoContainer)
                    maxWidth = Double.MAX_VALUE
                }
            }
            
            onItemClicked = { file ->
                if (file.isDirectory) { navigateTo(file) }
                else if (!isDirOnly) {
                    if (isMultiSelect) {
                        if (selectedFiles.contains(file)) selectedFiles.remove(file) else selectedFiles.add(file)
                    } else {
                        if (selectedFiles.contains(file)) { selectedFiles.clear() }
                        else { selectedFiles.setAll(listOf(file)) }
                    }
                    refresh()
                }
            }
        }
        
        VBox.setVgrow(fileList.root, Priority.ALWAYS)
        root.children.addAll(scrollPath.scrollPane, toolbar, fileList.root)
        
        Platform.runLater { navigateTo(initialDirectory) }
    }

    private fun getFileAttributes(file: File): String {
        return try {
            val path = file.toPath()
            val basicAttrs = Files.readAttributes(path, "basic:size,lastModifiedTime", LinkOption.NOFOLLOW_LINKS)
            val size = basicAttrs["size"] as Long
            val lastModified = basicAttrs["lastModifiedTime"] as java.nio.file.attribute.FileTime
            
            val sizeStr = if (file.isDirectory) "--" else formatSize(size)
            val dateStr = dateFormatter.format(Date(lastModified.toMillis()))
            
            var ownerInfo = "--"
            var perms = "--"
            
            try {
                val posixAttrs = Files.readAttributes(path, PosixFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                ownerInfo = "${posixAttrs.owner().name}:${posixAttrs.group().name}"
                perms = PosixFilePermissions.toString(posixAttrs.permissions())
            } catch (e: Exception) { /* 非POSIX */ }
            
            "$perms  $ownerInfo  $sizeStr  $dateStr"
        } catch (e: Exception) { "Attributes unavailable" }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    private fun applyFilterSortAndRefresh() {
        // 1. Filtering
        val filtered = if (filterText.isEmpty()) {
            allFilesOfCurrentDir
        } else {
            allFilesOfCurrentDir.filter { it.name.lowercase().contains(filterText) }
        }

        // 2. Sorting
        val comparator = when (sortBy) {
            SortType.NAME -> compareBy<File> { it.name.lowercase() }
            SortType.SIZE -> compareBy<File> { if (it.isDirectory) -1L else it.length() }
            SortType.DATE -> compareBy<File> { it.lastModified() }
        }
        val finalComparator = if (sortAscending) comparator else comparator.reversed()
        val sortedFiles = filtered.sortedWith(compareBy<File>({ !it.isDirectory }).thenComparing(finalComparator))
        
        // 3. Update UI
        fileList.items.setAll(sortedFiles)
        fileList.refresh()
    }

    private fun selectAll() {
        val files = fileList.items.filter { if (isDirOnly) it.isDirectory else !it.isDirectory }
        selectedFiles.setAll(files)
        fileList.refresh()
    }

    private fun deselectAll() {
        selectedFiles.clear()
        fileList.refresh()
    }

    private fun navigateTo(directory: File) {
        if (!directory.isDirectory) return
        val files = directory.listFiles() ?: emptyArray()
        allFilesOfCurrentDir = files.toList()
        
        applyFilterSortAndRefresh()
        
        selectedFiles.clear()
        currentDirFile = directory
        updatePathUI(directory)
    }

    private fun updatePathUI(directory: File) {
        val scrollPane = root.children[0] as? ScrollPane ?: return
        val pathBox = scrollPane.content as? HBox ?: return
        pathBox.children.clear()
        
        val parts = mutableListOf<File>()
        var curr: File? = directory
        while (curr != null) {
            parts.add(0, curr)
            curr = curr.parentFile
        }
        
        parts.forEachIndexed { index, file ->
            if (index > 0) pathBox.children.add(Label(">").apply { style = "-fx-text-fill: gray;" })
            pathBox.children.add(TouchButton(file.name.ifEmpty { file.path }).apply {
                styleClass.add("path-button"); minHeight = 40.0; setOnAction { navigateTo(file) }
            })
        }
    }

    private fun showNewFolderDialog() {
        val dialog = TouchDialog<String>()
        dialog.title = "New Folder"
        dialog.headerText = "Create a new folder in:\n${currentDirFile.path}"
        val textField = TouchTextField("New Folder").apply { promptText = "Folder name"; selectAll() }
        dialog.dialogPane.content = VBox(10.0, textField).apply { padding = Insets(20.0, 0.0, 0.0, 0.0) }
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        val okButton = dialog.dialogPane.lookupButton(ButtonType.OK)
        okButton.disableProperty().bind(textField.textProperty().isEmpty)
        dialog.setResultConverter { buttonType -> if (buttonType == ButtonType.OK) textField.text else null }
        
        dialog.showAndWait().ifPresent { name ->
            val newDir = File(currentDirFile, name)
            if (!newDir.exists()) {
                if (newDir.mkdir()) { navigateTo(currentDirFile) }
                else { TouchDialog.createAlert(Alert.AlertType.ERROR, "Error", "Failed to create folder: $name").showAndWait() }
            } else {
                TouchDialog.createAlert(Alert.AlertType.WARNING, "Warning", "Folder already exists: $name").showAndWait()
            }
        }
    }
}
