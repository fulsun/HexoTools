package pers.fulsun.cleanpicfxml.bean;

import lombok.Data;

@Data
public class ImageReport {

        // Markdown文件
        String mdFile;
        // 原始URL
        String originalUrl;
        // 规范化URL
        String normalizedUrl;
        // 图片类型 网络图片/本地图片
        String type = "";
        // 图片状态  "有效" : "无效"
        String status = "";
        // 是否建议更新相对路径格式
        String normalized = "否";
        // 建议更新的相对路径格式
        String suggestedUpdate = "";
        // 本地图片路径
        String localPath = "";

        public ImageReport(String mdFile, String originalUrl, String normalizedUrl) {
            this.mdFile = mdFile;
            this.originalUrl = originalUrl;
            this.normalizedUrl = normalizedUrl;
        }
}
