package com.quartz.cn.springbootquartzdemo.confg.util;

import java.io.IOException;

import javax.annotation.Resource;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.stereotype.Component;

import com.quartz.cn.springbootquartzdemo.confg.esText.InitDemo;

@Component
public class ESUtil {

    @Resource
    RestHighLevelClient restHighLevelClient;
    /**
     * 判断索引是否存在
     */
    public boolean isIndexExists(String indexName) {
    	 GetIndexRequest request = new GetIndexRequest();
    	    request.indices(indexName);
    	    try {
    	       return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);

            } catch (IOException e) {
    	        e.printStackTrace();
    	         return false;
    	    }
    	    
    }
    
   
   //blog.csdn.net/qq_34412985/java/article/details/104364654
    /**
     * 判断文档是否存在
     *
     * @param indexName
     * @return
     */
    public boolean isDocumentExists(String indexName, String id) {
        boolean exists = false;
        try {
        	RestHighLevelClient client = InitDemo.getClient();
            GetRequest request = new GetRequest(indexName, id);
            //不获取_source内容,提升效率
            request.fetchSourceContext(new FetchSourceContext(false));
            request.storedFields("_none_");

             exists = client.exists(request, RequestOptions.DEFAULT);
        	
           
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * 删除索引
     *
     * @param indexName
     * @return
     */
    public boolean deleteIndex(String indexName) {
        boolean acknowledged = false;
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            acknowledged = delete.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return acknowledged;
    }

}
