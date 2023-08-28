package pers.fulsun.cleanpicfxml.common;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class TextAreaAppender extends AppenderBase<ILoggingEvent> {
    private TextArea textArea;

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (textArea != null) {
            Platform.runLater(() -> textArea.appendText(event.getMessage() + "\n"));
        }
    }
}

