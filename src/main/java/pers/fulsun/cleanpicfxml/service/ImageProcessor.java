package pers.fulsun.cleanpicfxml.service;


import pers.fulsun.cleanpicfxml.bean.ImageReport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
// 图片处理器接口
public interface ImageProcessor {
    List<ImageReport> process(Path mdFile)throws IOException;
}
