package com.quartz.cn.springbootquartzdemo.service.quartz;


import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskErrors;
import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskInformations;
import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskRecords;
import com.quartz.cn.springbootquartzdemo.vo.QuartzTaskRecordsVo;
import org.quartz.SchedulerException;

import java.util.List;

public interface QuartzService {
    String addTask(QuartzTaskInformations quartzTaskInformations);
/**
 * 查询所有任务 分页查询
 * @param taskNo   任务编号
 * @param currentPage  从第几页
 * @return
 */
    List<QuartzTaskInformations> getTaskList(String taskNo, String currentPage);
/**
 * 定时任务信息表  根据id
 * 
 * @param id
 * @return
 */
    QuartzTaskInformations getTaskById(String id);

    /**
     * 更新   定时任务信息表
     * @param quartzTaskInformations
     * @return
     */
    String updateTask(QuartzTaskInformations quartzTaskInformations);
  //  启动 or 暂停定时任务 taskNo  任务编号
    String startJob(String taskNo) throws SchedulerException;

    void initLoadOnlineTasks();
/**
 * kafka发送消息
 * @param message
 */
    void sendMessage(String message);//kafka发送消息
  /**
   *  * 保存任务的定时记录   定时任务执行情况记录表
 *  *任务编号  从  定时任务信息表  查询出 任务
 *     *更新定时任务信息表 
 *        **根据定时任务信息表 的 内容查询出任务的个数更新更新定时任务表
 *     *添加任务记录表   
   * @param taskNo 任务编号
   * @return
   */
    QuartzTaskRecords addTaskRecords(String taskNo);
/**
 * 更新记录      定时任务执行情况记录表
 * @param count   失败统计数
 * @param id  定时任务执行情况记录表id
 * @return
 */
    Integer updateRecordById(Integer count, Long id);

    Integer updateModifyTimeById(QuartzTaskInformations quartzTaskInformations);
/**
 * 添加异常信息 
 * @param id   任务执行记录Id
 * @param errorKey  信息关键字
 * @param errorValue   信息内容
 * @return
 */
    Integer addTaskErrorRecord(String id, String errorKey, String errorValue);

    List<QuartzTaskRecordsVo> taskRecords(String taskNo);//返回自定义类
    /**
     * 立即运行一次定时任务
     * @param taskNo 任务编号
     * @return
     */
    String runTaskRightNow(String taskNo);

    QuartzTaskErrors detailTaskErrors(String recordId);
}
