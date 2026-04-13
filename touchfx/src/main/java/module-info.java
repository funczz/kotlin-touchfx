module com.github.funczz.touchfx {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires kotlin.stdlib;

    exports com.github.funczz.touchfx;
    exports com.github.funczz.touchfx.controls;
    exports com.github.funczz.touchfx.skin;
    exports com.github.funczz.touchfx.behavior;
    exports com.github.funczz.touchfx.i18n;

    opens com.github.funczz.touchfx to javafx.fxml;
}
