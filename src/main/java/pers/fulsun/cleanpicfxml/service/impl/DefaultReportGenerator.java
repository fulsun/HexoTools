package pers.fulsun.cleanpicfxml.service.impl;


import pers.fulsun.cleanpicfxml.bean.ImageReport;
import pers.fulsun.cleanpicfxml.service.ReportGenerator;

import java.util.List;
// 默认报告生成器实现
public class DefaultReportGenerator implements ReportGenerator {
    @Override
    public void generate(List<ImageReport> reports) {
        // 实现原printSummaryReport方法的逻辑
        System.out.println("图片检查摘要报告:");
        System.out.println("总检查图片数: " + reports.size());
        // 实现报告生成逻辑
    }
}
