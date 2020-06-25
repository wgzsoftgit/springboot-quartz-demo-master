package com.quartz.cn.springbootquartzdemo.service.quartz;

import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskRecords;

import java.util.List;

public interface QuartzTaskRecordsService {
/**
 * 添加记录  定时任务执行情况记录表
 * @param quartzTaskRecords
 * @return
 */
    long addTaskRecords(QuartzTaskRecords quartzTaskRecords);

    Integer updateTaskRecords(QuartzTaskRecords quartzTaskRecords);

    List<QuartzTaskRecords> listTaskRecordsByTaskNo(String taskNo);

}
