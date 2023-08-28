package pers.fulsun.cleanpicfxml.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.fulsun.cleanpicfxml.controller.MainPageController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ImageUtil {
    private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    public static String imageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        FileInputStream fileInputStream = new FileInputStream(imageFile);
        byte[] imageData = new byte[(int) imageFile.length()];
        fileInputStream.read(imageData);
        fileInputStream.close();
        return Base64.getEncoder().encodeToString(imageData);
    }

    public static String base64ToImage(String base64ImageData, String imagePath) throws NoSuchAlgorithmException {
        try {
            String[] parts = base64ImageData.split(",");
            if (parts.length != 2) {
                 logger.info("Invalid base64ImageData format");
                return null;
            }
            String mimeType = parts[0].split(":")[1];
            String[] mimeTypeParts = mimeType.split("/");
            String base64Data = parts[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            String extension = mimeTypeParts[1];
            int semicolonIndex = extension.indexOf(";");
            if (semicolonIndex != -1) {
                extension = extension.substring(0, semicolonIndex);
            }
            String md5 = MD5Util.calculateMD5(imageBytes);
            // Change the file name and extension as needed
            File outputFile = new File(imagePath, md5 + "." + extension);
            ImageIO.write(bufferedImage, extension, outputFile);
             logger.info("Image saved successfully.");
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileExtension(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] magicNumber = new byte[4];
            fis.read(magicNumber);

            if (isJPEG(magicNumber)) {
                return ".jpg";
            } else if (isPNG(magicNumber)) {
                return ".png";
            } else if (isGIF(magicNumber)) {
                return ".gif";
            }
            // Add more checks for other file types

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ".jpg"; // Default extension if the file type is unknown
    }

    /**
     * 通过网络请求下载图片到指定目录
     *
     * @param imageUrl   图片链接
     * @param targetFile 输出目录
     * @param fileName   文件名称
     * @return 图片路径
     * @throws IOException
     */
    public static String downloadImageByURL(String imageUrl, File targetFile, String fileName) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            String extension = ImageUtil.getExtensionFromUrl(imageUrl);
            String newFileName = MD5Util.calculateMD5(in) + "." + extension;
            String downloadAbsolutePath = new File(targetFile, newFileName).getAbsolutePath();
            Files.copy(in, Paths.get(downloadAbsolutePath), StandardCopyOption.REPLACE_EXISTING);
            return downloadAbsolutePath;
        } catch (Exception e) {
            e.printStackTrace();
             logger.info("网络请求下载图片失败：" + imageUrl);
        }
        return null;
    }

    public static String getExtensionFromUrl(String url) {
        String[] parts = url.split("\\.");
        if (parts.length > 1) {
            String ext = parts[parts.length - 1];
            if (ext.length() <= 4) {
                return ext;
            }
        }
        return "jpg"; // Default extension if unable to determine
    }

    private static boolean isJPEG(byte[] magicNumber) {
        // Check if the magic number matches JPEG file format
        // Example: return magicNumber[0] == (byte) 0xFF && magicNumber[1] == (byte) 0xD8;
        return magicNumber[0] == (byte) 0xFF && magicNumber[1] == (byte) 0xD8;
    }

    private static boolean isPNG(byte[] magicNumber) {
        // Check if the magic number matches PNG file format
        // Example: return magicNumber[0] == (byte) 0x89 && magicNumber[1] == (byte) 0x50 && magicNumber[2] == (byte) 0x4E && magicNumber[3] == (byte) 0x47;
        return magicNumber[0] == (byte) 0x89 && magicNumber[1] == (byte) 0x50 && magicNumber[2] == (byte) 0x4E && magicNumber[3] == (byte) 0x47;
    }

    private static boolean isGIF(byte[] magicNumber) {
        // Check if the magic number matches GIF file format
        // Example: return magicNumber[0] == (byte) 0x47 && magicNumber[1] == (byte) 0x49 && magicNumber[2] == (byte) 0x46 && magicNumber[3] == (byte) 0x38;
        return magicNumber[0] == (byte) 0x47 && magicNumber[1] == (byte) 0x49 && magicNumber[2] == (byte) 0x46 && magicNumber[3] == (byte) 0x38;
    }

    /**
     * 解码Base64数据，并将其保存为实际的图片文件
     * <p>数据URI的一般格式如下：
     * data:[<MIME-type>][;charset=<encoding>][;base64],<data>
     *
     * @param dataUri         数据Base64数据
     * @param outputDirectory 数据文件目录
     * @return 图片路径
     */
    public static String coverImageToFile(String dataUri, File outputDirectory) throws IOException {
        int commaIndex = dataUri.indexOf(",");
        if (commaIndex != -1) {
            String imageData = dataUri.substring(commaIndex + 1);
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            String extension = getExtensionFromDataUri(dataUri);
            String fileName = MD5Util.calculateMD5(imageBytes) + "." + extension;
            outputDirectory = new File(outputDirectory, fileName);
            try (FileOutputStream stream = new FileOutputStream(outputDirectory)) {
                stream.write(imageBytes);
            }
             logger.info("Image saved to: " + outputDirectory);
            return outputDirectory.getAbsolutePath();
        } else {
             logger.info("Invalid data URI format 【" + dataUri + "】");
        }
        return null;
    }

    public static String getExtensionFromDataUri(String dataUri) {
        int mimeTypeStart = dataUri.indexOf(":") + 1;
        int mimeTypeEnd = dataUri.indexOf(";");
        if (mimeTypeEnd == -1) {
            mimeTypeEnd = dataUri.indexOf(",");
        }

        if (mimeTypeStart != -1 && mimeTypeEnd != -1) {
            String mimeType = dataUri.substring(mimeTypeStart, mimeTypeEnd);
            return getExtensionForMimeType(mimeType);
        }

        return null;
    }

    public static String getExtensionForMimeType(String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            // Add more cases for other image types if needed
            default:
                return "jpg";
        }
    }

    /**
     * @param url
     * @return 0:本地链接 1:http 2:base64图片
     */
    public static int isImageUrl(String url) {
        try {
            URL imageUrl = new URL(null, url);
            String protocol = imageUrl.getProtocol().toLowerCase();

            // 判断是否是http或https协议的链接
            if (protocol.equals("http") || protocol.equals("https")) {
                return 1;
            }

            // 判断是否是data协议的base64图片
            if (protocol.equals("data")) {
                return 2;
            }

            // 其他类型的URL，例如本地文件路径等
            return 0;
        } catch (MalformedURLException e) {
            // URL格式不正确
            return 0;
        }
    }
}
