package com.quartz.cn.springbootquartzdemo.confg.esText;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

/**
 * highlight 高亮
 * 有些字段高亮不行
 * @author wgz
 * @date 创建时间：2020年6月20日 下午2:38:27
 */
public class HighlightDemo {
    
    private static Logger logger = LogManager.getRootLogger();  
 
    public static void main(String[] args) {
        try (RestHighLevelClient client = InitDemo.getClient();) {
            
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest("mess"); 
            
            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
            
            //构造QueryBuilder
            QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("message", "out");
            sourceBuilder.query(matchQueryBuilder);
            //分页设置
            /*sourceBuilder.from(0); 
            sourceBuilder.size(5); ;*/ 
            
                    
            // 高亮设置
            HighlightBuilder highlightBuilder = new HighlightBuilder(); 
//            highlightBuilder.requireFieldMatch(false).field("name").field("parentId")
//                .preTags("<strong>").postTags("</strong>");
            //不同字段可有不同设置，如不同标签
  //          /*
            HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("message"); 
            highlightTitle.preTags("<strong>").postTags("</strong>");
            highlightBuilder.field(highlightTitle);  
           HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("user");
            highlightContent.preTags("<b>").postTags("</b>");   
            highlightBuilder.field(highlightContent).requireFieldMatch(false);
            
      //      */
            
            sourceBuilder.highlighter(highlightBuilder);
            
            searchRequest.source(sourceBuilder);
            
            //3、发送请求        
            SearchResponse searchResponse = client.search(searchRequest , RequestOptions.DEFAULT);
            
            
            //4、处理响应
            if(RestStatus.OK.equals(searchResponse.status())) {
                //处理搜索命中文档结果
                SearchHits hits = searchResponse.getHits();
                long totalHits = (long) hits.getMaxScore();
                
                SearchHit[] searchHits = hits.getHits();
                for (SearchHit hit : searchHits) {        
                    String index = hit.getIndex();
                    String type = hit.getType();
                    String id = hit.getId();
                    float score = hit.getScore();
                    
                    //取_source字段值
                    //String sourceAsString = hit.getSourceAsString(); //取成json串
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap(); // 取成map对象
                    //从map中取字段值
                    /*String title = (String) sourceAsMap.get("title"); 
                    String content  = (String) sourceAsMap.get("content"); */
                    System.err.println("index:" + index + "  type:" + type + "  id:" + id);
                    System.err.println("sourceMap : " +  sourceAsMap);
                    //取高亮结果
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    HighlightField highlight = highlightFields.get("message"); 
                    if(highlight != null) {
                        Text[] fragments = highlight.fragments();  //多值的字段会有多个值
                        if(fragments != null) {
                            String fragmentString = fragments[0].string();
                            System.err.println("title highlight : " +  fragmentString);
                            //可用高亮字符串替换上面sourceAsMap中的对应字段返回到上一级调用
                            //sourceAsMap.put("title", fragmentString);
                        }
                    }
                    
                    highlight = highlightFields.get("user"); 
                    if(highlight != null) {
                        Text[] fragments = highlight.fragments();  //多值的字段会有多个值
                        if(fragments != null) {
                            String fragmentString = fragments[0].string();
                            System.err.println("content highlight : " +  fragmentString);
                            //可用高亮字符串替换上面sourceAsMap中的对应字段返回到上一级调用
                            //sourceAsMap.put("content", fragmentString);
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
 