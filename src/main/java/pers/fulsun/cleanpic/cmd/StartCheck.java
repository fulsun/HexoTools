package pers.fulsun.cleanpic.cmd;

import pers.fulsun.cleanpic.cmd.common.Constant;
import pers.fulsun.cleanpic.cmd.utils.MarkdownImageChecker;
import pers.fulsun.cleanpic.cmd.utils.MarkdownImageFix;
import pers.fulsun.cleanpic.cmd.utils.Md5Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class StartCheck {
    public static void main(String[] args) {

        // 接受一个_post目录
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入_post目录: ");
        String input = scanner.nextLine();
        String mdDir = input == null || input.trim().isEmpty() ? "C:\\Users\\fulsun\\Documents\\Github\\hexo\\source\\_posts" : input;
        System.out.println("您输入的_post目录为: " + mdDir);

        // 检查目录是否存在
        File directory = new File(mdDir);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("错误：指定的目录不存在或不是一个有效的目录！");
            return;
        }

        // 打印菜单
        while (true) {
            showMenu(scanner, mdDir);
        }
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

    private static void renameMdImageName(String mdDir) {
        Map<String, List<String>> renameMap = new HashMap<>();
        System.out.println(">>>>> 开始进行图片重命名");
        MarkdownImageChecker markdownImageChecker = new MarkdownImageChecker();
        Set<File> allMarkdownFiles = markdownImageChecker.getAllMarkdownFiles(Path.of(mdDir));
        Optional.of(allMarkdownFiles).ifPresent(files -> {
            files.stream().forEach(file -> {
                try {
                    Set<String> strings = markdownImageChecker.extractImagePathsFromMarkdown(file);
                    strings.stream().forEach(imageurl -> {
                        // 获取图片
                        Path imagePath = Path.of(file.getParent()).resolve(imageurl);
                        if (Files.exists(imagePath)) {
                            // 获取MD5
                            try {
                                String md5 = Md5Utils.calculateMD5(imagePath);
                                String oldName = imagePath.getFileName().toString().substring(0, imagePath.getFileName().toString().lastIndexOf("."));
                                if (!oldName.equals(md5)) {
                                    String newName = imageurl.replace(oldName, md5);
                                    renameMap.computeIfAbsent(file.toString(), k -> new ArrayList<>()).add(imageurl + "=>" + newName);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            System.out.println("图片不存在  >>>> " + imagePath);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        });


        if (renameMap.isEmpty()) {
            return;
        }
        // 处理图片名称
        renameMap.keySet().stream().forEach(file -> {
            // 打印修复结果
            System.out.println("重命名文档：" + file);
            try {
                // 读取文件
                String content = Files.readString(Path.of(file), StandardCharsets.UTF_8);
                List<String> fixStringList = renameMap.get(file);
                // 遍历所有需要替换的路径
                for (String fixString : fixStringList) {
                    if (fixString != null && !fixString.isBlank()) {
                        String[] split = fixString.split("=>");
                        if (split.length == 2) {
                            String oldUrl = split[0];
                            String newUrl = split[1];
                            // 重命名图片
                            Path oldpath = Path.of(file).getParent().resolve(oldUrl);
                            Path newpath = Path.of(file).getParent().resolve(newUrl);
                            Files.move(oldpath, newpath, StandardCopyOption.REPLACE_EXISTING);
                            content = content.replace(oldUrl, newUrl); // 更新 content
                            System.out.println("\t图片名称：" + oldUrl + " => " + newUrl);
                        }
                    }
                }
                // 写出文件
                Files.write(Path.of(file), content.getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                System.err.println("更新文件 " + file + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        });

    }

    private static void handleCheckAndFix(String mdDir) {
        Map<String, String> fixMap = new HashMap();
        // 输入图库路径
        System.out.print("请输入图库路径：");
        String grallyDir = new Scanner(System.in).nextLine();
        // 指定默认
        grallyDir = grallyDir.isEmpty() ? "C:\\Users\\fulsun\\Pictures\\unused-images" : grallyDir;
        if (!Path.of(grallyDir).toFile().exists()) {
            System.out.println("图库路径不存在！");
            return;
        }
        System.out.println("》》》》》 正在检测图片并修复本地文件...");
        // 实现具体逻辑
        Map<String, Map<String, List<String>>> checkResult = new MarkdownImageChecker().check(Path.of(mdDir));
        if (checkResult == null || checkResult.isEmpty()) {
            return;
        } else {
            new MarkdownImageFix().fix(checkResult, grallyDir);
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

    private static void handleCheck(String mdDir) {
        System.out.println("开始检测图片...");
        Map<String, Map<String, List<String>>> check = new MarkdownImageChecker().check(Path.of(mdDir));
        MarkdownImageChecker.printCheckResult(check.get(Constant.INVALID_IMAGES), check.get(Constant.INVALID_REMOTE_IMAGES), check.get(Constant.USED_IMAGES));
        System.out.println("图片检测完成。");
        waitForUserInput();
    }


    private static void waitForUserInput() {
        System.out.println("按回车键继续...");
        new Scanner(System.in).nextLine(); // 等待用户按下回车
    }

}
