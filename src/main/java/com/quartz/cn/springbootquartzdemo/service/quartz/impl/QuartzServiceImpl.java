package com.quartz.cn.springbootquartzdemo.service.quartz.impl;


import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskErrors;
import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskInformations;
import com.quartz.cn.springbootquartzdemo.bean.QuartzTaskRecords;
import com.quartz.cn.springbootquartzdemo.job.QuartzMainJobFactory;
import com.quartz.cn.springbootquartzdemo.service.quartz.QuartzService;
import com.quartz.cn.springbootquartzdemo.service.quartz.QuartzTaskErrorsService;
import com.quartz.cn.springbootquartzdemo.service.quartz.QuartzTaskInformationsService;
import com.quartz.cn.springbootquartzdemo.service.quartz.QuartzTaskRecordsService;
import com.quartz.cn.springbootquartzdemo.util.CommonUtil;
import com.quartz.cn.springbootquartzdemo.util.HttpClientUtil;
import com.quartz.cn.springbootquartzdemo.util.ResultEnum;
import com.quartz.cn.springbootquartzdemo.util.ResultUtil;
import com.quartz.cn.springbootquartzdemo.vo.QuartzTaskRecordsVo;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName QuartzServiceImpl
 * @Description quartz service逻辑相关
 * @Author simonsfan
 * @Date 2019/1/3
 * Version  1.0
 */
@Service
public class QuartzServiceImpl implements QuartzService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(QuartzServiceImpl.class);
    public static final String QUARTZ_TOPIC = "quartztopic";

    private AtomicInteger atomicInteger;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private QuartzTaskInformationsService quartzTaskInformationsService;

    @Autowired
    private QuartzTaskErrorsService quartzTaskErrorsService;

    @Autowired
    private QuartzTaskRecordsService quartzTaskRecordsService;

    @Autowired
    private SchedulerFactoryBean schedulerBean;

    /**
     * 列表查询所有定时任务
     *
     * @return
     */
    @Override
    public List<QuartzTaskInformations> getTaskList(String taskNo, String currentPage) {
        List<QuartzTaskInformations> quartzTaskInformations = quartzTaskInformationsService.selectList(taskNo, currentPage);
        return quartzTaskInformations;
    }

    /**
     * 新增定时任务
     *
     * @param quartzTaskInformations
     * @return
     */
    @Override
    public String addTask(QuartzTaskInformations quartzTaskInformations) {
        String result = quartzTaskInformationsService.insert(quartzTaskInformations);
        return result;
    }

    @Override
    public QuartzTaskInformations getTaskById(String id) {
        return quartzTaskInformationsService.getTaskById(id);
    }

    @Override
    public String updateTask(QuartzTaskInformations quartzTaskInformations) {
        return quartzTaskInformationsService.updateTask(quartzTaskInformations);
    }

    /**
     * 启动 or 暂停定时任务
     * 先删除任务
     * 更新定时任务表
     *
     * @param taskNo  任务编号
     * @return
     * @throws SchedulerException
     */
    @Override
    @Transactional
    public String startJob(String taskNo) throws SchedulerException {
        QuartzTaskInformations quartzTaskInformation = quartzTaskInformationsService.getTaskByTaskNo(taskNo);
       // [id=1, version=2, taskno=12, taskname=测试, schedulerrule=5 * * * * ?, frozenstatus=FROZEN, executorno=12, frozentime=1591883258850, unfrozentime=1591883228489, createtime=1586238604076, lastmodifytime=1591883258850, sendtype=http, url=, executeparamter=, timekey=]
        if (quartzTaskInformation == null) {
            return ResultUtil.success(ResultEnum.NO_DATA.getCode(), ResultEnum.NO_DATA.getMessage());
        }
        String status = quartzTaskInformation.getFrozenstatus();//冻结状态
        Scheduler scheduler = schedulerBean.getScheduler();
        long currentTimeMillis = System.currentTimeMillis();
        QuartzTaskInformations task = new QuartzTaskInformations();//定时任务信息表
        task.setId(quartzTaskInformation.getId());
        task.setVersion(quartzTaskInformation.getVersion());
        //说明要暂停
        if (ResultEnum.UNFROZEN.name().equals(status)) {
            scheduler.deleteJob(new JobKey(taskNo));
            task.setFrozentime(currentTimeMillis);//冻结时间
            task.setFrozenstatus(ResultEnum.FROZEN.name());//修改冻结状态
            //说明要启动
        } else if (ResultEnum.FROZEN.name().equals(status)) {
            scheduler.deleteJob(new JobKey(taskNo));
            this.schedule(quartzTaskInformation, scheduler); //&& 同
            task.setUnfrozentime(currentTimeMillis);
            task.setFrozenstatus(ResultEnum.UNFROZEN.name());
        }
        task.setLastmodifytime(currentTimeMillis);//最近修改时间
        quartzTaskInformationsService.updateStatusById(task);
        logger.info("taskNo={},taskName={},scheduleRule={},任务{}成功", quartzTaskInformation.getTaskno(), quartzTaskInformation.getTaskname(), quartzTaskInformation.getSchedulerrule(), ResultEnum.FROZEN.name().equals(status) ? "启动" : "暂停");
        return ResultUtil.success(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage());
    }

    /**
     * 初始化加载定时任务
     *
     * @throws Exception
     */
    @Override
    public void initLoadOnlineTasks() {
        List<QuartzTaskInformations> unnfrozenTasks = quartzTaskInformationsService.getUnnfrozenTasks(ResultEnum.UNFROZEN.name());
        if (CollectionUtils.isEmpty(unnfrozenTasks)) {
            logger.info("没有需要初始化加载的定时任务");
            return;
        }
        Scheduler scheduler = schedulerBean.getScheduler();
        for (QuartzTaskInformations unnfrozenTask : unnfrozenTasks) {
            try {
                this.schedule(unnfrozenTask, scheduler);
            } catch (Exception e) {
                logger.error("系统初始化加载定时任务:taskno={},taskname={}失败原因exception={}", unnfrozenTask.getTaskno(), unnfrozenTask.getTaskname(), e);
            }
        }
    }

    /**
     * 初始化加载定时任务
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.initLoadOnlineTasks();
    }
/**
 * 保存任务的定时记录   定时任务执行情况记录表
 * 任务编号  从  定时任务信息表  查询出 任务
 * 更新定时任务信息表 
 *     根据定时任务信息表 的 内容查询出任务的个数更新更新定时任务表
 * 添加任务记录表   
 *    
 * 
 * taskNo 任务编号
 */
    @Override
    public QuartzTaskRecords addTaskRecords(String taskNo) {
        QuartzTaskRecords quartzTaskRecords = null;
        try {
            QuartzTaskInformations quartzTaskInformation = quartzTaskInformationsService.getTaskByTaskNo(taskNo);
                                                                                //冻结状态
            if (null == quartzTaskInformation || ResultEnum.FROZEN.name().equals(quartzTaskInformation.getFrozenstatus())) {
                logger.info("taskNo={} not exist or status is frozen!");
                return null;
            }
            long currentTimeMillis = System.currentTimeMillis();
            QuartzTaskInformations task = new QuartzTaskInformations();
            task.setId(quartzTaskInformation.getId());
            task.setLastmodifytime(currentTimeMillis);//最近修改时间
            quartzTaskInformationsService.updateTask(quartzTaskInformation);//更新定时任务表
            logger.info("taskNo={},taskName={}更新最后修改时间成功  ----  定时任务信息表", quartzTaskInformation.getTaskno(), quartzTaskInformation.getTaskname());

            quartzTaskRecords = new QuartzTaskRecords();
            quartzTaskRecords.setTaskno(taskNo);
                        //执行时间格式值                任务编号
            quartzTaskRecords.setTimekeyvalue(quartzTaskInformation.getTimekey());//
            quartzTaskRecords.setExecutetime(currentTimeMillis);//执行时间
            quartzTaskRecords.setTaskstatus(ResultEnum.INIT.name());//任务状态
            quartzTaskRecords.setFailcount(0);// 失败统计数
            quartzTaskRecords.setFailreason("");//失败错误描述
            quartzTaskRecords.setCreatetime(currentTimeMillis);//创建时间
            quartzTaskRecords.setLastmodifytime(currentTimeMillis);//最近修改时间
            quartzTaskRecordsService.addTaskRecords(quartzTaskRecords);  //添加任务记录表
            logger.info("taskNo={},taskName={}添加执行记录表成功", quartzTaskInformation.getTaskno(), quartzTaskInformation.getTaskname());
        } catch (Exception ex) {
            logger.error("添加执行记录表异常exceptio={}", ex);
            return null;
        }
        return quartzTaskRecords;
    }

    @Override
    public Integer updateRecordById(Integer count, Long id) {
        QuartzTaskRecords records = new QuartzTaskRecords();
        records.setId(id);//定时任务执行情况记录表id
        records.setFailcount(count);//失败统计数
        records.setLastmodifytime(System.currentTimeMillis());
        if (count > 0) {
            records.setTaskstatus(ResultEnum.FAIL.name());
        } else {
            records.setTaskstatus(ResultEnum.SUCCESS.name());
        }
        return quartzTaskRecordsService.updateTaskRecords(records);
    }

    @Override
    public Integer updateModifyTimeById(QuartzTaskInformations quartzTaskInformations) {
        return quartzTaskInformationsService.updateModifyTimeById(quartzTaskInformations);
    }
/**
 * 任务执行记录Id
 * 信息关键字
 * 信息内容
 * 
 * 
 */
    @Override
    public Integer addTaskErrorRecord(String id, String errorKey, String errorValue) {
        QuartzTaskErrors taskErrors = new QuartzTaskErrors();
        taskErrors.setTaskexecuterecordid(String.valueOf(id));//任务执行记录Id
        taskErrors.setErrorkey(errorKey);//信息关键字
        taskErrors.setErrorvalue(errorValue);//信息内容
        taskErrors.setCreatetime(System.currentTimeMillis());//创建时间
        taskErrors.setLastmodifytime(System.currentTimeMillis());//最近修改时间
      
        return quartzTaskErrorsService.addTaskErrorRecord(taskErrors);
    }

    public void schedule(QuartzTaskInformations quartzTaskInfo, Scheduler scheduler) throws SchedulerException {
    	                                               //任务编号
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzTaskInfo.getTaskno(), Scheduler.DEFAULT_GROUP);
                                               //QuartzMainJobFactory && 实现job                     任务名称                                                                                                                任务编号
        JobDetail jobDetail = JobBuilder.newJob(QuartzMainJobFactory.class).withDescription(quartzTaskInfo.getTaskname()).withIdentity(quartzTaskInfo.getTaskno(), Scheduler.DEFAULT_GROUP).build();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put("id", quartzTaskInfo.getId().toString());
        jobDataMap.put("taskNo", quartzTaskInfo.getTaskno());//任务编号
        jobDataMap.put("executorNo", quartzTaskInfo.getExecutorno());////执行方
        jobDataMap.put("sendType", quartzTaskInfo.getSendtype());//发送方式
        jobDataMap.put("url", quartzTaskInfo.getUrl());//请求地址
        jobDataMap.put("executeParameter", quartzTaskInfo.getExecuteparamter());//执行参数
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(quartzTaskInfo.getSchedulerrule());// 定时规则表达式
                                                                             //任务名称
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withDescription(quartzTaskInfo.getTaskname()).withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();
        scheduler.scheduleJob(jobDetail, cronTrigger);
        logger.error("taskNo={},taskName={},scheduleRule={} load to quartz success!", quartzTaskInfo.getTaskno(), quartzTaskInfo.getTaskname(), quartzTaskInfo.getSchedulerrule());
    }

    /**
     * kafka推送消息
     *
     * @param message
     */
    @Override
    public void sendMessage(String message) {
        kafkaTemplate.send(QUARTZ_TOPIC, message);
        logger.info("给kafka推送消息message={}成功", message);
    }

    @Override
    public List<QuartzTaskRecordsVo> taskRecords(String taskNo) {
        List<QuartzTaskRecords> quartzTaskRecords = quartzTaskRecordsService.listTaskRecordsByTaskNo(taskNo);
        QuartzTaskRecordsVo recordsVo = null;
        List<QuartzTaskRecordsVo> voList = new ArrayList<>();
        for (QuartzTaskRecords quartzTaskRecord : quartzTaskRecords) {
            recordsVo = new QuartzTaskRecordsVo();
            BeanUtils.copyProperties(quartzTaskRecord, recordsVo);
            recordsVo.setTime(quartzTaskRecord.getLastmodifytime() - quartzTaskRecord.getCreatetime());
            voList.add(recordsVo);
        }
        return voList;
    }

    /**
     * 立即运行一次定时任务
     *
     * @param taskNo
     * @return
     */
    @Override
    public String runTaskRightNow(String taskNo) {
        atomicInteger = new AtomicInteger(0);
        QuartzTaskInformations quartzTaskInformations = quartzTaskInformationsService.getTaskByTaskNo(taskNo);
        if (quartzTaskInformations == null) {
            return ResultUtil.success(ResultEnum.NO_DATA.getCode(), ResultEnum.NO_DATA.getMessage());
        }
        Long id = quartzTaskInformations.getId();
        String sendType = quartzTaskInformations.getSendtype();
        String executorNo = quartzTaskInformations.getExecutorno();
        String url = quartzTaskInformations.getUrl();
        String executeParameter = quartzTaskInformations.getExecuteparamter();
        logger.info("定时任务被执行:taskNo={},executorNo={},sendType={},url={},executeParameter={}", taskNo, executorNo, sendType, url, executeParameter);
        QuartzTaskRecords records = null;
        try {
            //保存定时任务的执行记录
            records = this.addTaskRecords(taskNo);
            if (null == records || !ResultEnum.INIT.name().equals(records.getTaskstatus())) {
                logger.info("taskNo={}立即运行失--->>保存执行记录失败", taskNo);
                return ResultUtil.success(ResultEnum.RUN_NOW_FAIL.getCode(), ResultEnum.RUN_NOW_FAIL.getMessage());
            }

            if (ResultEnum.HTTP.getMessage().equals(sendType)) {
                try {
                    HttpClientUtil.doPost(url, "text/json", executeParameter);
                    logger.info("");
                } catch (Exception ex) {
                    logger.error("");
                    atomicInteger.incrementAndGet();
                    throw ex;
                }
            } else if (ResultEnum.KAFKA.getMessage().equals(sendType)) {
                try {
                    String message = new StringBuffer(taskNo).append(":").append(executeParameter).toString();
                    this.sendMessage(message);
                } catch (Exception ex) {
                    logger.error("");
                    atomicInteger.incrementAndGet();
                    throw ex;
                }
            }
        } catch (Exception ex) {
            logger.error("");
            atomicInteger.incrementAndGet();
            this.addTaskErrorRecord(records.getId().toString(), taskNo + ":" + ex.getMessage(), CommonUtil.getExceptionDetail(ex));
        }
        //更改record表的执行状态、最后修改时间、失败个数
        this.updateRecordById(atomicInteger.get(), records.getId());

        //更新taskinfo表的最后修改时间
        QuartzTaskInformations quartzTaskInformation = new QuartzTaskInformations();
        quartzTaskInformation.setId(id);
        quartzTaskInformation.setLastmodifytime(System.currentTimeMillis());
        this.updateTask(quartzTaskInformation);
        return ResultUtil.success(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage());
    }

    @Override
    public QuartzTaskErrors detailTaskErrors(String recordId) {
        return quartzTaskErrorsService.detailTaskErrors(recordId);
    }


}
