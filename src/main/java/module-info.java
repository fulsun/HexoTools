module pers.fulsun.cleanpicfxml {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.commons.io;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    opens pers.fulsun.cleanpicfxml to javafx.fxml;
    opens pers.fulsun.cleanpicfxml.controller to javafx.fxml;

    exports pers.fulsun.cleanpicfxml;
    exports pers.fulsun.cleanpicfxml.common to ch.qos.logback.core;
}
