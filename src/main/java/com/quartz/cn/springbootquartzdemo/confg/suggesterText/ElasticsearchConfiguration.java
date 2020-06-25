package com.quartz.cn.springbootquartzdemo.confg.suggesterText;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
 
public class ElasticsearchConfiguration {
 
    private static RestHighLevelClient client;
 
    private ElasticsearchConfiguration() {
    }
 

	public static RestHighLevelClient getClient() {
		if (client == null) {
			client = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
		}
		return client;
	}
    public static void close() throws IOException {
        client.close();
    }
}
//https://blog.csdn.net/wwd0501/java/article/details/80885987