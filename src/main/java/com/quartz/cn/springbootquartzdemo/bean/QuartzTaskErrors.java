package com.quartz.cn.springbootquartzdemo.bean;
/**
 * 定时任务出错现场信息表
 * @author wgz
 * @date 创建时间：2020年6月12日 下午2:53:59
 */
public class QuartzTaskErrors {
    private Long id;
/**
 * 任务执行记录Id
 */
    private String taskexecuterecordid;
    /**
     *信息关键字
     */
    private String errorkey;
    /**
     * 信息内容
     */
    private String errorvalue;
    /**
     *创建时间
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

    public String getTaskexecuterecordid() {
        return taskexecuterecordid;
    }

    public void setTaskexecuterecordid(String taskexecuterecordid) {
        this.taskexecuterecordid = taskexecuterecordid == null ? null : taskexecuterecordid.trim();
    }

    public String getErrorkey() {
        return errorkey;
    }

    public void setErrorkey(String errorkey) {
        this.errorkey = errorkey == null ? null : errorkey.trim();
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

    public String getErrorvalue() {
        return errorvalue;
    }

    public void setErrorvalue(String errorvalue) {
        this.errorvalue = errorvalue == null ? null : errorvalue.trim();
    }
}
