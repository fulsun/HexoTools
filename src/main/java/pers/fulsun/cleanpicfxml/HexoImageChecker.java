package pers.fulsun.cleanpicfxml;

import pers.fulsun.cleanpicfxml.bean.ImageReport;
import pers.fulsun.cleanpicfxml.config.Configuration;
import pers.fulsun.cleanpicfxml.service.FileService;
import pers.fulsun.cleanpicfxml.service.ImageProcessor;
import pers.fulsun.cleanpicfxml.service.LogService;
import pers.fulsun.cleanpicfxml.service.ReportGenerator;
import pers.fulsun.cleanpicfxml.service.impl.DefaultFileService;
import pers.fulsun.cleanpicfxml.service.impl.DefaultImageProcessor;
import pers.fulsun.cleanpicfxml.service.impl.DefaultLogService;
import pers.fulsun.cleanpicfxml.service.impl.DefaultReportGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class HexoImageChecker {

    private static final String ERROR_LOG_FILE = "error.log";
    private final Configuration config;
    private final ImageProcessor imageProcessor;
    private final ReportGenerator reportGenerator;
    private final FileService fileService;
    private final LogService logService;

    public HexoImageChecker(Configuration config, ImageProcessor imageProcessor,
                            ReportGenerator reportGenerator, FileService fileService, LogService logService) {
        this.config = config;
        this.imageProcessor = imageProcessor;
        this.reportGenerator = reportGenerator;
        this.fileService = fileService;
        this.logService = logService;
    }

    public static void  main(String[] args) throws IOException {
        // 解析命令行参数
        boolean autoFix = false;
        boolean renameImages = false;
        boolean downloadWebImages = false;
        boolean formatDocuments = false;
        String galleryPath = null;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-fix".equals(arg)) {
                autoFix = true;
                System.out.println("自动修复模式已启用");
            } else if ("-rename".equals(arg)) {
                renameImages = true;
            } else if ("-download".equals(arg)) {
                downloadWebImages = true;
            } else if ("-format".equals(arg)) {
                formatDocuments = true;
            } else if ("-gallery".equals(arg)) {
                if (i + 1 < args.length) {
                    galleryPath = args[i + 1];
                    i++;
                } else {
                    System.err.println("缺少图库目录路径，请使用 -gallery <path>");
                    return;
                }
            }
        }
        
        if (galleryPath == null) {
            System.err.println("请使用 -gallery 参数指定图库目录");
            return;
        }
        
        Path galleryDir = Paths.get(galleryPath);
        if (!Files.isDirectory(galleryDir)) {
            System.err.println("指定的图库目录不存在: " + galleryDir);
            return;
        }

        // 创建配置
        Path postsDir = Paths.get("C:\\Users\\fulsun\\blog2\\source\\_posts");
        Configuration config = new Configuration(autoFix, renameImages, downloadWebImages, formatDocuments, postsDir, ERROR_LOG_FILE, galleryDir);

        // 初始化服务
        FileService fileService = new DefaultFileService();
        LogService logService = new DefaultLogService(config.getErrorLogFile());
        ImageProcessor imageProcessor = new DefaultImageProcessor(config, fileService, logService);
        ReportGenerator reportGenerator = new DefaultReportGenerator();

        // 创建并运行检查器
        HexoImageChecker checker = new HexoImageChecker(config, imageProcessor, reportGenerator, fileService, logService);
        checker.run();
    }

    public void run() throws IOException {
        Path postsDir = config.getPostsDir();
        if (!Files.isDirectory(postsDir)) {
            logService.logError("错误: 指定的目录不存在: " + postsDir);
            logService.close();
            return;
        }

        List<ImageReport> allReports = new ArrayList<>();
        List<Path> mdFiles = fileService.findMarkdownFiles(postsDir);

        mdFiles.forEach(mdFile -> {
            try {
                allReports.addAll(imageProcessor.process(mdFile));
            } catch (Exception e) {
                logService.logError("处理文件 " + mdFile + " 时出错: " + e.getMessage());
            }
        });

        reportGenerator.generate(allReports);
        logService.close();
    }
}