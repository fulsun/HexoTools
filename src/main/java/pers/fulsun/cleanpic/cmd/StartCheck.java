package pers.fulsun.cleanpic.cmd;

import pers.fulsun.cleanpic.cmd.common.Constant;
import pers.fulsun.cleanpic.cmd.handle.ImageRenamer;
import pers.fulsun.cleanpic.cmd.handle.MarkdownImageChecker;
import pers.fulsun.cleanpic.cmd.handle.MarkdownImageFix;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StartCheck {
    public static void main(String[] args) {

        // 接受一个_post目录
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("请输入_post目录: ");
        String inputDir = inputScanner.nextLine();
        String postsDirectory = inputDir == null || inputDir.trim().isEmpty() ? "C:\\Users\\fulsun\\Documents\\Github\\hexo\\source\\_posts" : inputDir;
        System.out.println("您输入的_post目录为: " + postsDirectory);

        // 检查目录是否存在
        File directory = new File(postsDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("错误：指定的目录不存在或不是一个有效的目录！");
            return;
        }

        // 打印菜单
        do {
            showMenu(inputScanner, postsDirectory);
        } while (true);
    }

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                // Windows 使用 `cls`
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Linux/Mac 使用 `clear`
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (final Exception e) {
            // 如果清屏失败，至少打印 50 个空行模拟清屏
            System.out.println("\n".repeat(50));
        }
    }

    private static void showMenu(Scanner scanner, String mdDir) {
        clearConsole(); // 先清屏
        System.out.println("========请选择要执行的功能============");
        System.out.println("1. 检测图片");
        System.out.println("2. 检测图片并从图库修复失效图片");
        System.out.println("3. 检测图片并修复并下载网络图片");
        System.out.println("4. 检测图片并修复并下载网络图片并格式化文档");
        System.out.println("5. 采用MD5重命名图片");
        System.out.println("6. 退出");
        System.out.print("请输入您的选择: ");

        // 验证用户输入是否为有效整数
        while (!scanner.hasNextInt()) {
            System.out.println("无效的选择，请输入一个数字（1-5）!");
            System.out.print("请输入您的选择: ");
            scanner.next(); // 清除无效输入
        }

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                handleCheck(mdDir);
                break;
            case 2:
                handleCheckAndFix(mdDir);
                break;
            case 3:
                handleCheckAndFixAndDownload(mdDir);
                break;
            case 4:
                handleCheckAndFixAndDownloadAndFormat(mdDir);
                break;
            case 5:
                renameMdImageName(mdDir);
                break;
            case 6:
                System.out.println("退出程序");
                System.exit(0);
                break;
            default:
                System.out.println("无效的选择，请重新输入！");
        }
    }

    private static void renameMdImageName(String postsDirectory) {
        ImageRenamer imageRenamer = new ImageRenamer();
        Map<String, List<String>> imageRenameMap = imageRenamer.generateRenameMap(postsDirectory);
        if (!imageRenameMap.isEmpty()) {
            imageRenamer.applyRename(imageRenameMap);
        }
    }

    private static void handleCheckAndFix(String postsDirectory) {
        // 输入图库路径
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("请输入图库路径：");
        String galleryDirectory = inputScanner.nextLine();
        // 指定默认
        galleryDirectory = galleryDirectory.isEmpty() ? "C:\\Users\\fulsun\\Pictures\\unused-images" : galleryDirectory;
        if (!Path.of(galleryDirectory).toFile().exists()) {
            System.out.println("图库路径不存在！");
            return;
        }
        System.out.println("》》》》》 正在检测图片并修复本地文件...");
        // 实现具体逻辑
        Map<String, Map<String, List<String>>> checkResult = new MarkdownImageChecker().check(Path.of(postsDirectory));
        if (checkResult == null || checkResult.isEmpty()) {
            return;
        } else {
            new MarkdownImageFix().fix(checkResult, galleryDirectory);
        }
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void handleCheckAndFixAndDownload(String mdDir) {
        System.out.println("正在检测图片并修复，并下载网络图片...");
        // 实现具体逻辑
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void handleCheckAndFixAndDownloadAndFormat(String mdDir) {
        System.out.println("正在检测图片并修复，下载网络图片，并格式化文档...");
        // 实现具体逻辑
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void handleCheck(String postsDirectory) {
        System.out.println("开始检测图片...");
        Map<String, Map<String, List<String>>> checkResult = new MarkdownImageChecker().check(Path.of(postsDirectory));
        MarkdownImageChecker.printCheckResult(checkResult.get(Constant.INVALID_IMAGES), checkResult.get(Constant.INVALID_REMOTE_IMAGES), checkResult.get(Constant.USED_IMAGES));
        System.out.println("图片检测完成。");
        waitForUserInput();
    }


    private static void waitForUserInput() {
        System.out.println("按回车键继续...");
        new Scanner(System.in).nextLine(); // 等待用户按下回车
    }

}
