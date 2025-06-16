package pers.fulsun.cleanpicfxml.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.fulsun.cleanpicfxml.HexoImageChecker;
import pers.fulsun.cleanpicfxml.common.TextAreaAppender;
import pers.fulsun.cleanpicfxml.config.Configuration;
import pers.fulsun.cleanpicfxml.service.FileService;
import pers.fulsun.cleanpicfxml.service.ImageProcessor;
import pers.fulsun.cleanpicfxml.service.LogService;
import pers.fulsun.cleanpicfxml.service.ReportGenerator;
import pers.fulsun.cleanpicfxml.service.impl.DefaultFileService;
import pers.fulsun.cleanpicfxml.service.impl.DefaultImageProcessor;
import pers.fulsun.cleanpicfxml.service.impl.DefaultLogService;
import pers.fulsun.cleanpicfxml.service.impl.DefaultReportGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;


public class MainPageController {
    private static final Logger logger = LoggerFactory.getLogger(MainPageController.class);
    @FXML
    private Button selectpath;

    @FXML
    private Button galleryDir;

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
    private TextField gallerypath;

    @FXML
    private RadioButton downloadflag;

    @FXML
    private RadioButton cleanflag;
    @FXML
    private RadioButton formatDocumentsUrl;
    @FXML
    private RadioButton cleanUnusedImagesFlag;


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
    void selectmdpath(ActionEvent event) {
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

    @FXML
    void selectgallerypath(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择图库所在文件夹");
        // 获取当前的 Stage（舞台）
        Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();

        // 显示文件选择对话框
        java.io.File selectedFile = directoryChooser.showDialog(stage);
        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            logger.info("选择图库的文件路径：" + filePath);
            gallerypath.setText(filePath); // 设置路径值给 markdownpath 文本字段
        }
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
        if (!cleanflag.isSelected() && !downloadflag.isSelected() && !formatDocumentsUrl.isSelected()) {
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
            // 清理日志
            logTextArea.clear();
            logger.info("开始检测目录 >>> " + scanMarkdownPath + " <<< 的无效图片----");
            // 创建配置
            Path postsDir = Paths.get(scanMarkdownPath);
            Configuration config = new Configuration(true, true, true, true, postsDir, "error.log", Path.of(gallerypath.getText()));

            // 初始化服务
            FileService fileService = new DefaultFileService();
            LogService logService = new DefaultLogService(config.getErrorLogFile());
            ImageProcessor imageProcessor = new DefaultImageProcessor(config, fileService, logService);
            ReportGenerator reportGenerator = new DefaultReportGenerator();

            // 创建并运行检查器
            HexoImageChecker checker = new HexoImageChecker(config, imageProcessor, reportGenerator, fileService, logService);
            checker.run();
            logger.info("扫描结束，感谢你的使用。");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("扫描出现异常，请联系开发者解决。");
        }
    }


}


