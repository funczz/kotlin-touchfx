module com.github.funczz.touchfx.demo {
    requires com.github.funczz.touchfx;
    requires javafx.controls;
    requires javafx.graphics;
    requires kotlin.stdlib;

    opens com.github.funczz.touchfx.demo to javafx.graphics;
}
