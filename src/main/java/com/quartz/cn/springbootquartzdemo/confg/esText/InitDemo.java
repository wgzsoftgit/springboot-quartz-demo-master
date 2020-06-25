package com.quartz.cn.springbootquartzdemo.confg.esText;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class InitDemo {
 
    public static RestHighLevelClient getClient() {
 
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
        		//设置http客户请求时长
                             
        return client;
    }
}