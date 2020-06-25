package com.quartz.cn.springbootquartzdemo.bean;
/**
 * 定时任务执行情况记录表
 * @author wgz
 * @date 创建时间：2020年6月11日 下午11:52:16
 */
public class QuartzTaskRecords {
    private Long id; 
/**
 * 任务编号
 */
    private String taskno;
    /**
     * 执行时间格式值
     */
    private String timekeyvalue;
    /**
     *执行时间
     */
    private Long executetime;
    /**
     * 任务状态
     */
    private String taskstatus;
    /**
     * 失败统计数
     */
    private Integer failcount;
    /**
     * 失败错误描述
     */
    private String failreason;
    /**
     * 创建时间
     */
    private Long createtime;
    /**
     * 最近修改时间
     */
    private Long lastmodifytime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskno() {
        return taskno;
    }

    public void setTaskno(String taskno) {
        this.taskno = taskno == null ? null : taskno.trim();
    }

    public String getTimekeyvalue() {
        return timekeyvalue;
    }

    public void setTimekeyvalue(String timekeyvalue) {
        this.timekeyvalue = timekeyvalue == null ? null : timekeyvalue.trim();
    }

    public Long getExecutetime() {
        return executetime;
    }

    public void setExecutetime(Long executetime) {
        this.executetime = executetime;
    }

    public String getTaskstatus() {
        return taskstatus;
    }

    public void setTaskstatus(String taskstatus) {
        this.taskstatus = taskstatus == null ? null : taskstatus.trim();
    }

    public Integer getFailcount() {
        return failcount;
    }

    public void setFailcount(Integer failcount) {
        this.failcount = failcount;
    }

    public String getFailreason() {
        return failreason;
    }

    public void setFailreason(String failreason) {
        this.failreason = failreason == null ? null : failreason.trim();
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

    public Long getLastmodifytime() {
        return lastmodifytime;
    }

    public void setLastmodifytime(Long lastmodifytime) {
        this.lastmodifytime = lastmodifytime;
    }
}
