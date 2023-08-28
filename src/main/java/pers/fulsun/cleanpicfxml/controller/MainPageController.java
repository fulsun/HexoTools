package pers.fulsun.cleanpicfxml.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.fulsun.cleanpicfxml.common.TextAreaAppender;
import pers.fulsun.cleanpicfxml.handle.MarkdownHandle;

public class MainPageController {
    private static final Logger logger = LoggerFactory.getLogger(MainPageController.class);
    @FXML
    private Button selectpath;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Button savelog;

    @FXML
    private Button startclean;

    @FXML
    private TextField logpath;

    @FXML
    private TextArea logTextArea;

    @FXML
    private TextField markdownpath;

    @FXML
    private RadioButton downloadflag;

    @FXML
    private RadioButton cleanflag;

    @FXML
    void initialize() {
        // 默认清理失效图片
        cleanflag.setSelected(true);
        // 设置 TextAreaAppender
        TextAreaAppender appender = new TextAreaAppender();
        appender.setTextArea(logTextArea);
        appender.start();
        logTextArea.setEditable(false); // 设置为不可编辑，只用于展示日志
        // 将 appender 添加到日志记录器中
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }


    @FXML
    void savelog(ActionEvent event) {
    }

    @FXML
    void selectpath(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择MarkDown所在文件夹");

        // 获取当前的 Stage（舞台）
        Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();

        // 显示文件选择对话框
        java.io.File selectedFile = directoryChooser.showDialog(stage);
        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            logger.info("选择的文件路径：" + filePath);
            markdownpath.setText(filePath); // 设置路径值给 markdownpath 文本字段
        }
    }

    private static void fileSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("所有文件", "*.*"), new FileChooser.ExtensionFilter("文本文件", "*.txt"), new FileChooser.ExtensionFilter("图像文件", "*.png", "*.jpg", "*.gif"));
    }

    @FXML
    void startcleanPicture(ActionEvent event) {
        if (markdownpath.getText() == null || markdownpath.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText(null);
            alert.setContentText("请选择要扫描的路径");

            // 获取当前的 Stage（舞台）
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();

            // 显示警告对话框
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }
        if (!cleanflag.isSelected() && !downloadflag.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText(null);
            alert.setContentText("请选择要执行的操作");

            // 获取当前的 Stage（舞台）
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();

            // 显示警告对话框
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }

        String scanMarkdownPath = markdownpath.getText();
        try {
            logger.info("开始检测目录 >>> " + scanMarkdownPath + " <<< 的无效图片----");
            MarkdownHandle.handle(scanMarkdownPath, cleanflag.isSelected(), downloadflag.isSelected());
            logger.info("扫描结束，感谢你的使用。");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("扫描出现异常，请联系开发者解决。");
        }
    }


}


