package com.quartz.cn.springbootquartzdemo.service.quartz;


import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskInformations;

import java.util.List;

public interface QuartzTaskInformationsService {
    String insert(QuartzTaskInformations quartzTaskInformations);
/**
 *  select
        *
        from quartz_task_informations
        <if test="taskNo != null">
            <where>
                taskNo = #{taskNo}
            </where>
        </if>
        order by id desc limit #{startIndex},10;
 * @param taskNo
 * @param currentPage
 * @return
 */
    List<QuartzTaskInformations> selectList(String taskNo, String currentPage);
/**
 * 根据id查询   查询定时任务信息表  
 * @param id
 * @return
 */
    QuartzTaskInformations getTaskById(String id);
/**
 * 更新定时任务信息表 
 *    根据定时任务信息表 的 内容
 *         查询出任务的个数
 *        更新更新定时任务表
 * @param quartzTaskInformations
 * @return
 */
    String updateTask(QuartzTaskInformations quartzTaskInformations);
/**
 * 定时任务信息表
 * @param taskNo  任务编号
 * @return
 */
    QuartzTaskInformations getTaskByTaskNo(String taskNo);//
/**
 * *启动 、暂停   更新定时任务表    根据id
 * @param quartzTaskInformations
 * @return
 */
    Integer updateStatusById(QuartzTaskInformations quartzTaskInformations);

    List<QuartzTaskInformations> getUnnfrozenTasks(String status);

    Integer updateModifyTimeById(QuartzTaskInformations quartzTaskInformations);
}
