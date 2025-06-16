package pers.fulsun.cleanpicfxml.service;



import pers.fulsun.cleanpicfxml.bean.ImageReport;

import java.util.List;

// 报告生成器接口
public interface ReportGenerator {
    void generate(List<ImageReport> reports);
}

