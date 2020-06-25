package com.quartz.cn.springbootquartzdemo.service.quartz;

import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskErrors;

public interface QuartzTaskErrorsService {
	/**
	 * 添加错误信息
	 * @param quartzTaskErrors
	 * @return
	 */
    Integer addTaskErrorRecord(QuartzTaskErrors quartzTaskErrors);

    QuartzTaskErrors detailTaskErrors(String recordId);
}
