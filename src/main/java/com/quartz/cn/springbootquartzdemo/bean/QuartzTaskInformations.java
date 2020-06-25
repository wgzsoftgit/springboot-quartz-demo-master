package com.quartz.cn.springbootquartzdemo.bean;

/**
 * 定时任务信息表
 * @author wgz
 * @date 创建时间：2020年6月11日 下午11:53:55
 */
public class QuartzTaskInformations {
    private Long id;
    /**
     *版本号：需要乐观锁控制
     */
    private Integer version; 
/**
 * 任务编号
 */
    private String taskno;
    /**
     * 任务名称
     */
    private String taskname;
    /**
     * 定时规则表达式
     */
    private String schedulerrule;
    /**
     * 冻结状态
     */
    private String frozenstatus;
    /**
     * 执行方
     */
    private String executorno;
    /**
     * 冻结时间
     */
    private Long frozentime;
    /**
     * 解冻时间
     */
    private Long unfrozentime;
    /**
     * 创建时间
     */
    private Long createtime;
    /**
     * 最近修改时间
     */
    private Long lastmodifytime;
    /**
     * 发送方式
     */
    private String sendtype;
    /**
     * 请求地址
     */
    private String url;
    /**
     * 执行参数
     */
    private String executeparamter;
    /**
     * 任务编号
     */
    private String timekey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTaskno() {
        return taskno;
    }

    public void setTaskno(String taskno) {
        this.taskno = taskno == null ? null : taskno.trim();
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname == null ? null : taskname.trim();
    }

    public String getSchedulerrule() {
        return schedulerrule;
    }

    public void setSchedulerrule(String schedulerrule) {
        this.schedulerrule = schedulerrule == null ? null : schedulerrule.trim();
    }

    public String getFrozenstatus() {
        return frozenstatus;
    }

    public void setFrozenstatus(String frozenstatus) {
        this.frozenstatus = frozenstatus == null ? null : frozenstatus.trim();
    }

    public String getExecutorno() {
        return executorno;
    }

    public void setExecutorno(String executorno) {
        this.executorno = executorno == null ? null : executorno.trim();
    }

    public Long getFrozentime() {
        return frozentime;
    }

    public void setFrozentime(Long frozentime) {
        this.frozentime = frozentime;
    }

    public Long getUnfrozentime() {
        return unfrozentime;
    }

    public void setUnfrozentime(Long unfrozentime) {
        this.unfrozentime = unfrozentime;
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

    public String getSendtype() {
        return sendtype;
    }

    public void setSendtype(String sendtype) {
        this.sendtype = sendtype == null ? null : sendtype.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getExecuteparamter() {
        return executeparamter;
    }

    public void setExecuteparamter(String executeparamter) {
        this.executeparamter = executeparamter == null ? null : executeparamter.trim();
    }

    public String getTimekey() {
        return timekey;
    }

    public void setTimekey(String timekey) {
        this.timekey = timekey == null ? null : timekey.trim();
    }

	@Override
	public String toString() {
		return "QuartzTaskInformations [id=" + id + ", version=" + version + ", taskno=" + taskno + ", taskname="
				+ taskname + ", schedulerrule=" + schedulerrule + ", frozenstatus=" + frozenstatus + ", executorno="
				+ executorno + ", frozentime=" + frozentime + ", unfrozentime=" + unfrozentime + ", createtime="
				+ createtime + ", lastmodifytime=" + lastmodifytime + ", sendtype=" + sendtype + ", url=" + url
				+ ", executeparamter=" + executeparamter + ", timekey=" + timekey + "]";
	}
    
}
