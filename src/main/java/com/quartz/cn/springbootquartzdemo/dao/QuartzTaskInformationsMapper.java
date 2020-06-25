package com.quartz.cn.springbootquartzdemo.dao;


import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskInformations;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuartzTaskInformationsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(QuartzTaskInformations record);

    int insertSelective(QuartzTaskInformations record);

    QuartzTaskInformations selectByPrimaryKey(Long id);
/**
 *启动 、暂停   更新定时任务表
 *update quartz_task_informations  
 *  set()
        where id = #{id,jdbcType=BIGINT} and version = #{version}
 * @param record
 * @return
 */
    int updateByPrimaryKeySelective(QuartzTaskInformations record);

    int updateByPrimaryKey(QuartzTaskInformations record);

    List<QuartzTaskInformations> selectList(Map<String, Object> map);
/**
 * 统计数量   
 *  select count(*) from quartz_task_informations where taskNo=#{taskNo}
 * @param taskNo 任务编号
 * @return
 */
    Integer selectByTaskNo(String taskNo);
//    select * from quartz_task_informations where taskNo = #{taskNo}
    QuartzTaskInformations getTaskByTaskNo(String taskNo);

    List<QuartzTaskInformations> getUnfrozenTasks(String status);
}
