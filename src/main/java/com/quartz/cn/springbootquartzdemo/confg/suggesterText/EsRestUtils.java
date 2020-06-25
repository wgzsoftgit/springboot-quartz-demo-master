package com.quartz.cn.springbootquartzdemo.confg.suggesterText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
/**
 * select * from dept where id = '1' 根据id查询单条数据
 * select * from dept where id in ("1", "2", "3") 根据多个id查询
 * select * from dept where id in ("1", "2", "3") limt 2,10 根据条件分页查询
 * select count(1) from dept where name like '部门%' 
 *   select level, count(id) from dept where name like '部门%' group by level 
 * @author wgz
 * @date 创建时间：2020年6月21日 下午9:14:43
 */
public class EsRestUtils {

	private static RestHighLevelClient client;

	private static final String type = "_doc";

	public static RestHighLevelClient getClient() {
		if (client == null) {
			client = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
		}
		return client;
	}

//https://blog.csdn.net/winterking3/java/article/details/103178732
	/**
	 * select * from dept where id = '1' 根据id查询单条数据
	 * 
	 * @param index
	 * @param id
	 * @return
	 * @throws IOException
	 */
	protected static Map<String, Object> getById(String index, String id) throws IOException {
		getClient();
		GetRequest getRequest = new GetRequest(index, type, id);
		GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		if (getResponse.isExists()) {
			return getResponse.getSourceAsMap();
		}
		return null;
	}

	/**
	 * select * from dept where id in ("1", "2", "3") 根据多个id查询
	 * 
	 * @param index
	 * @param ids
	 * @return
	 * @throws IOException
	 */
	protected static List<Map<String, Object>> getByIds(String index, List<String> ids) throws IOException {
		getClient();
		List<Map<String, Object>> results = new ArrayList<>();
		MultiGetRequest request = new MultiGetRequest();
		ids.stream().forEach(id -> {
			request.add(new MultiGetRequest.Item(index, type, id));
		});
		MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
		GetResponse getResponse;
		for (int i = 0; i < response.getResponses().length; i++) {
			getResponse = response.getResponses()[i].getResponse();
			if (getResponse.isExists()) {
				results.add(getResponse.getSourceAsMap());
			}
		}
		return results;
	}

	/**
	 * select * from dept where id in ("1", "2", "3") limt 2,10 根据条件分页查询
	 * 
	 * @param index
	 * @param queryBuilder
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws IOException
	 */
	protected static List<Map<String, Object>> getByWhere(String index, QueryBuilder queryBuilder, int pageNo,
			int pageSize) throws IOException {
		getClient();
		List<Map<String, Object>> results = new ArrayList<>();

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.from(pageNo);
		searchSourceBuilder.size(pageSize);

		SearchRequest searchRequest = new SearchRequest(index).types(type);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			results.add(hit.getSourceAsMap());
		}
		return results;
	}

	/**
	 * select count(1) from dept where name like '部门%' count统计
	 * 
	 * @param queryBuilder
	 * @param indexs
	 * @return
	 * @throws IOException
	 */
	public static long count(QueryBuilder queryBuilder, String... indexs) throws IOException {
		getClient();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);

		CountRequest countRequest = new CountRequest(indexs);
		countRequest.source(searchSourceBuilder);

		CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
		long count = countResponse.getCount();
		return count;
	}

	/**
	 * select max(level) from dept where name like '部门%' max
	 * 
	 * @param queryBuilder
	 * @param field
	 * @param indexs
	 * @return
	 * @throws IOException
	 */
	public static Double getMax(QueryBuilder queryBuilder, String field, String... indexs) throws IOException {
		getClient();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.size(0);

		AggregationBuilder aggregationBuilder = AggregationBuilders.max("agg").field(field);
		searchSourceBuilder.aggregation(aggregationBuilder);

		SearchRequest searchRequest = new SearchRequest(indexs).types(type);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		Max agg = searchResponse.getAggregations().get("agg");
		return agg.getValue();
	}

	/**
	 * 根据1个字段group by terms聚合
	 *  select level, count(id) from dept where name like'部门%' group by level 
	 * 根据1个字段group by terms聚合
	 * 
	 * @param queryBuilder  条件
	 * @param field        索引
	 * @param Map
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Long> getTermsAgg(QueryBuilder queryBuilder, String field, String... indexs)
			throws IOException {
		Map<String, Long> groupMap = new HashMap<String, Long>();
		getClient();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.size(0);

		AggregationBuilder aggregationBuilder = AggregationBuilders.terms("agg").field(field);
		searchSourceBuilder.aggregation(aggregationBuilder);

		SearchRequest searchRequest = new SearchRequest(indexs).types(type);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		Terms terms = searchResponse.getAggregations().get("agg");
		for (Terms.Bucket entry : terms.getBuckets()) {
			groupMap.put(entry.getKey().toString(), entry.getDocCount());
		}
		return groupMap;
	}

	/**根据多个字段group by
	 * select level, status, count(id) from dept where name like '部门%' group by level, status
	 * @param queryBuilder
	 * @param field1
	 * @param field2
	 * @param indexs
	 * @return
	 * @throws IOException
	 * AggregationBuilder groupByType = AggregationBuilders.terms("分组别名").field("分组字段");
 AggregationBuilder sumDownOutputSum = AggregationBuilders.sum("聚合别名").field("聚合字段");
	 */
	public static Map<String, Map<String, Long>> getTermsAggTwoLevel(QueryBuilder queryBuilder, String field1, String field2, String... indexs) throws IOException {
	    Map<String, Map<String, Long>> groupMap = new HashMap<>();
	    getClient();
	    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	    searchSourceBuilder.query(queryBuilder);
	    searchSourceBuilder.size(0);
	    
	    AggregationBuilder agg1 = AggregationBuilders.terms("agg1").field(field1);
	   // AggregationBuilders.count(name) 计数
	   // AggregationBuilders.sum(name) 聚合
	    AggregationBuilder agg2 = AggregationBuilders.terms("agg2").field(field2);
	    agg1.subAggregation(agg2);
	    searchSourceBuilder.aggregation(agg1);
	    
	    SearchRequest searchRequest = new SearchRequest(indexs).types(type);
	    searchRequest.source(searchSourceBuilder);
	    
	    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
	    Terms terms1 = searchResponse.getAggregations().get("agg1");
	    Terms terms2;
	    for (Terms.Bucket bucket1 : terms1.getBuckets()) {
	        terms2 = bucket1.getAggregations().get("agg2");
	        Map<String, Long> map2 = new HashMap<>();
	        for (Terms.Bucket bucket2 : terms2.getBuckets()) {
	            map2.put(bucket2.getKey().toString(), bucket2.getDocCount());
	        }
	        groupMap.put(bucket1.getKey().toString(), map2);
	    }
	    return groupMap;
	}

	public static void main(String[] args) throws IOException {
	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery(); //查询所有
	RestHighLevelClient client2 = EsRestUtils.getClient();
	SearchRequest searchRequest = new SearchRequest("dept");
	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	searchSourceBuilder.query(queryBuilder);
	searchRequest.source(searchSourceBuilder);
	SearchResponse searchResponse = client2.search(searchRequest, RequestOptions.DEFAULT);
	

	SearchHits hits = searchResponse.getHits();
	for (SearchHit hit : hits) {
		String sourceAsString = hit.getSourceAsString();
		System.out.println(sourceAsString);
	}
/**
 * {"code":"dept_1_1_2","name":"部门1_1_2","level":3,"path":"1,3,6","parentId":"3","status":null}
{"code":"dept_1_1_1","name":"部门1_1_1","level":3,"path":"1,3,5","parentId":"3","status":1}
{"code":"dept_1_2","name":"部门1_2","level":2,"path":"1,4","parentId":"1","status":0}
{"code":"dept_1_1","name":"部门1_1","level":2,"path":"1,3","parentId":"1","status":0}
{"code":"dept_2","name":"部门2","level":1,"path":"2","parentId":"","status":0}
{"code":"dept_1","name":"部门1","level":1,"path":"1","parentId":"","status":1}
 */
		
	QueryBuilder queryBuilder2 = QueryBuilders.wildcardQuery("name.keyword", "部门*");//模糊查询
	  try { Map<String, Long> groupMap =
	  EsRestUtils.getTermsAgg(queryBuilder2, "code", "dept");
	  groupMap.forEach((key, value) -> System.out.println(key + " %-> " +
	  value.toString())); 
	    } catch (IOException e) {
	    	e.printStackTrace(); 
	    	}
	  /**前提条件是     分组类型为  text
	   *  PUT /dept/_mapping
  {
  "properties":{
     "code":{
        "type": "text",
        "fielddata": true
      }
      }
   }
	   * dept_2 %-> 1
dept_1_1 %-> 1
dept_1_2 %-> 1
dept_1_1_1 %-> 1
dept_1_1_2 %-> 1
dept_1 %-> 1
	   */
	
	  Map<String, Object> byId = EsRestUtils.getById("dept","1");
	  byId.forEach((key, value) -> System.out.println(key + " @-> " + value.toString()));
	  /**
	   path @-> 1
code @-> dept_1
level @-> 1
name @-> 部门1
parentId @-> 
status @-> 1
	   */
	  List<Map<String, Object>> byId2 = EsRestUtils.getByIds("dept",Arrays.asList("1","2","3"));
	  QueryBuilder queryBuilder3 = QueryBuilders.wildcardQuery("name.keyword", "部门*");
	    try {
	        Map<String, Map<String, Long>> groupMap = EsRestUtils.getTermsAggTwoLevel(queryBuilder3, "level", "status", "dept");
	        groupMap.forEach((key, value) -> System.out.println(key + " -> " + value.toString()));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    /**
		   * 左边是level，右边是个数
		   * 1 -> 2
	     2 -> 2
	    3 -> 2
		   */
	    
	    

	}

}
/**
 * 字符串分组    "fielddata": true
 * PUT /dept/_mapping
  {
  "properties":{
     "code":{
        "type": "text",
        "fielddata": true
      }
      }
   }
 * 
 * 
 * PUT /dept/_doc/1 { "code": "dept_1", "name": "部门1", "level": 1, "path": "1",
 * "parentId": "", "status":1 }
 * 
 * PUT /dept/_doc/2 { "code": "dept_2", "name": "部门2", "level": 1, "path": "2",
 * "parentId": "", "status":0 }
 * 
 * PUT /dept/_doc/3 { "code": "dept_1_1", "name": "部门1_1", "level": 2, "path":
 * "1,3", "parentId": "1", "status":0 }
 * 
 * PUT /dept/_doc/4 { "code": "dept_1_2", "name": "部门1_2", "level": 2, "path":
 * "1,4", "parentId": "1", "status":0 }
 * 
 * PUT /dept/_doc/5 { "code": "dept_1_1_1", "name": "部门1_1_1", "level": 3,
 * "path": "1,3,5", "parentId": "3", "status":1 }
 * 
 * PUT /dept/_doc/6 { "code": "dept_1_1_2", "name": "部门1_1_2", "level": 3,
 * "path": "1,3,6", "parentId": "3", "status":null }
 * 
 */
