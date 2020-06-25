package com.quartz.cn.springbootquartzdemo.service.quartz.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.quartz.cn.springbootquartzdemo.bean.MovieIndex;
import com.quartz.cn.springbootquartzdemo.bean.MovieSearch;
import com.quartz.cn.springbootquartzdemo.dao.MovieIndexMapper;
import com.quartz.cn.springbootquartzdemo.service.quartz.MovieSearchService;
import com.quartz.cn.springbootquartzdemo.util.ResultUtil;
import com.quartz.cn.springbootquartzdemo.vo.MovieIndexTemplate;
import com.quartz.cn.springbootquartzdemo.vo.MovieVo;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.expression.Lists;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MovieSearchServiceImpl
 * @Description TODO
 * @Author simonsfan
 * @Date 2019/1/16
 * Version  1.0
 */
@Service
public class MovieSearchServiceImpl implements MovieSearchService {

    private static Logger logger = LoggerFactory.getLogger(MovieSearchServiceImpl.class);

    private static final String INDEX = "movie_index";
    private static final String TYPE = "info";

    @Autowired
    private RestHighLevelClient transportClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieIndexMapper movieIndexMapper;


    /**
     * 自动补全逻辑，注意索引中suggest字段类型要设置为 completion
     *
     * @param prefix
     * @return
     * @throws IOException 
     */
    public List<String> suggest(String prefix) throws IOException {
    	 SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
        //suggest是字段名称，返回匹配的5条
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete", suggestion);
     // 1、创建search请求
        SearchRequest searchRequest = new SearchRequest("mess"); 
        searchRequest.types("_doc");
        //将请求体加入到请求中
        searchRequest.source(sourceBuilder);
        SearchResponse requestBuilder =transportClient.search(searchRequest,RequestOptions.DEFAULT);
               
     
        Suggest suggest = requestBuilder.getSuggest();
        if (suggest == null) {
            return new ArrayList<>();
        }
        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");

        int maxSuggest = 0;
        Set<String> suggestSet = new HashSet<>();

        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                if (item.getOptions().isEmpty()) {
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }

            if (maxSuggest > 5) {
                break;
            }
        }
        List<String> suggests = Arrays.asList(suggestSet.toArray(new String[]{}));
        return suggests;
    }

    /**
     * 复合查询
     *
     * @param search
     * @return
     * @throws IOException 
     */
    @Override
    public List<MovieIndex> searchMovie(MovieSearch search) throws IOException {
        List<MovieIndex> movieVoList = new ArrayList<>();
        //如果搜索条件为空 默认进mysql查询
        if (StringUtils.isEmpty(search.getKeyword()) && StringUtils.isEmpty(search.getArea()) && StringUtils.isEmpty(search.getLabel()) && StringUtils.isEmpty(search.getRelease())) {
            List<MovieIndex> movieIndexList = movieIndexMapper.selectDefault();
            return movieIndexList;
        }
        String label = search.getLabel();
        String area = search.getArea();
        String release = search.getRelease();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(label) && !StringUtils.equals("*", label)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(MovieSearch.LABEL, label));
        }
        if (StringUtils.isNotEmpty(area) && !StringUtils.equals("*", area)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(MovieSearch.AREA, area));
        }
      /*  if (score > 0) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(MovieSearch.SCORE);
            rangeQueryBuilder.gte(score);
            boolQueryBuilder.filter(rangeQueryBuilder);
        }*/
        if (StringUtils.isNotEmpty(release) && !StringUtils.equals("*", release)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(MovieSearch.RELEASE, release));
        }

        if(StringUtils.isNotEmpty(search.getKeyword())){
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(search.getKeyword(),
                MovieSearch.NAME,
                MovieSearch.ACTORS,
                MovieSearch.ALIAS,
                MovieSearch.DIRECTORS,
                MovieSearch.INTRODUCTION,
                MovieSearch.AREA));
        }

        String[] includes = {MovieSearch.ID};
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.from(0);  ////设置确定结果要从哪个索引开始搜索的from选项，默认为0
        sourceBuilder.size(10); //分页查询    0-10  //设置确定搜素命中返回数的size选项，默认为10
       sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //设置一个可选的超时，控制允许搜索的时间。
       SearchRequest searchRequest = new SearchRequest("mess"); 
       searchRequest.types("_doc");
       //将请求体加入到请求中
       searchRequest.source(sourceBuilder);
       SearchResponse searchResponse = transportClient.search(searchRequest, RequestOptions.DEFAULT);
        
        if (searchResponse.status() != RestStatus.OK) {
            return movieVoList;
        }
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits.getHits()) {
        	
            String str =  hit.getSourceAsString();//   (MovieSearch.ID);
            JsonParser parser = new JsonParser();
            JsonElement el = parser.parse(str);
            JsonObject jsonObj = el.getAsJsonObject();
            String id=jsonObj.get(MovieSearch.ID).getAsString();
            MovieIndex movieIndex = movieIndexMapper.selectByPrimaryKey(Long.parseLong(id));
            movieVoList.add(movieIndex);
        }
        return movieVoList;
    }


    /**
     * 新增文档
     *
     * @param indexTemplate
     * @return
     * @throws IOException 
     */
    @Override
    public String addIndex(MovieIndexTemplate indexTemplate) throws IOException {
        if (indexTemplate == null) {
            return ResultUtil.fail();
        }
        IndexRequest request = new IndexRequest(
                "mess",   //索引
                "_doc",     // mapping type
                "3");     //文档id  
        
     
        request.source(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON); 
        	IndexResponse indexResponse = null;
        	
            try {
                // 同步方式
                indexResponse = transportClient.index(request,RequestOptions.DEFAULT);
        //    IndexRequestBuilder indexRequestBuilder = transportClient.prepareIndex(INDEX, TYPE).setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON);
           // IndexResponse indexResponse = indexRequestBuilder.get();
            if (indexResponse.status() == RestStatus.CREATED) {
                logger.info("name={},新增文档成功", indexTemplate.getName());
                return ResultUtil.success();
            }
        } catch (JsonProcessingException e) {
            logger.error("");
        }
        return ResultUtil.fail();
    }

    @Override
    public String getIndex(String id) throws IOException {
    	GetRequest getRequest = new GetRequest(INDEX, TYPE, id);
    	GetResponse getResponse2 = transportClient.get(getRequest,RequestOptions.DEFAULT);
    	
   
        String result = getResponse2.getSourceAsString();
        logger.info("get api result ={},", result);
        if (StringUtils.isEmpty(result)) {
            return ResultUtil.fail();
        }
        return ResultUtil.success(result);
    }

    //multi getindex
    public void getIndex2(String[] keyword) throws IOException {
    	// 创建查询父对象
        MultiGetRequest request = new MultiGetRequest();
        // 添加子查询
        request.add(new MultiGetRequest.Item("mess", "doc", "1"));
        request.add(new MultiGetRequest.Item("mess", "doc2", "2").fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE));
        String[] includes = new String[] {"user", "*r"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        request.add(new MultiGetRequest.Item("mess", "doc", "3").fetchSourceContext(fetchSourceContext));
//        // user必须在map集合中
//        request.add(new MultiGetRequest.Item("posts", "doc", "4")
//                .storedFields("user"));
//        MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
//        MultiGetItemResponse item = response.getResponses()[0];
//        // user必须在map集合中
//        String value = item.getResponse().getField("user").getValue();

        // 针对每个子请求分别设置，无法在主请求中设置
        // 指定去哪个分片上查询，如何指定分片上没有，不会再去其它分片查询，如果不指定，则依次轮询各个分片查询
        request.add(new MultiGetRequest.Item("posts", "doc", "with_routing")
                .routing("some_routing"));
    //    request.add(new MultiGetRequest.Item("index", "type", "with_parent")
    //            .parent("some_parent"));
        request.add(new MultiGetRequest.Item("index", "type", "with_version")
                .versionType(VersionType.EXTERNAL)
                .version(10123L));
        // preference, realtime and refresh 需要在主请求里设置，子请求中无法设置
        request.preference("some_preference");
        // realtime的值默认为true
        request.realtime(false);
        request.refresh(true);
        MultiGetResponse response = transportClient.mget(request, RequestOptions.DEFAULT);
        int count = 0;
        for(MultiGetItemResponse item: response.getResponses()) {
            String index = item.getIndex();
            String type = item.getType();
            String id = item.getId();
            System.out.println("第" + ++count + "条-》index:" + index + "; type:" + type + "; id:" + id);
            if(item.getFailure() != null) {
                Exception e = item.getFailure().getFailure();
                ElasticsearchException ee = (ElasticsearchException) e;
                if(ee.getMessage().contains("reason=no such index")) {
                    System.out.println("查询的文档库不存在！");
                }
            }

            GetResponse getResponse = item.getResponse();

            if (getResponse.isExists()) {
                long version = getResponse.getVersion();
                String sourceAsString = getResponse.getSourceAsString();
                System.out.println("查询的结果为：");
                System.out.println(sourceAsString);
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                byte[] sourceAsBytes = getResponse.getSourceAsBytes();
            } else {
                System.out.println("没有查询到相应文档");
            }
        }
//https://blog.csdn.net/qq_2300688967/java/article/details/83867140
    }

    @Override
    public String deleteIndex(String indexName) throws IOException {

    	  DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
          deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
          AcknowledgedResponse delete = transportClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
       boolean   acknowledged = delete.isAcknowledged();   
        if (acknowledged) {
            logger.info("");
            return ResultUtil.success();
        }
        return ResultUtil.fail();
//        
//        boolean isDelete = esUtil.deleteIndex(indexName);
//        if (isDelete) {
//            return new ResponseBean(200, "删除成功", null);
//        } else {
//            return new ResponseBean(10002, "删除失败", null);
//        }
    }

    @Override
    public String deleteByQueryAction(String indexName,String type, String id) throws IOException {
    	DeleteRequest deleteRequest = new DeleteRequest(indexName, type, id);
    	
//    	 DeleteRequest deleteRequest = new DeleteRequest(indexName);
//         deleteRequest.id(id);
         try {
             DeleteResponse deleteResponse = transportClient.delete(deleteRequest, RequestOptions.DEFAULT);
            
             if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                 //return new ResponseBean(1001, "删除失败", null);
                 return ResultUtil.fail();
             } else {
            	 return ResultUtil.success();
                // return new ResponseBean(200, "删除成功", null);
             }
         } catch (IOException e) {
             e.printStackTrace();
             return ResultUtil.fail();
            // return new ResponseBean(1003, "删除异常", null);
         }
     
//        BulkByScrollResponse bulkByScrollResponse =  transportClient.filter(QueryBuilders.matchQuery(MovieSearch.NAME, keyword)).source(INDEX).get();
//        long deleted = bulkByScrollResponse.getDeleted();
//        logger.info("根据条件keyword={}删除result={}", keyword, deleted);
//        if (deleted < 1) {
//            logger.info("根据条件keyword={}删除失败", keyword);
//            return ResultUtil.fail();
//        }
//        logger.info("根据条件keyword={}删除成功", keyword);
     //   return ResultUtil.success();
    }

    //异步执行delete by query
    public void deleteByQueryAction1(String keyword) {
    	
        DeleteRequest deleteRequest = new DeleteRequest("index", "type", "ID");
     //   ActionFuture<DeleteResponse> delete = transportClient.delete(deleteRequest);
     //  
    }

    //批量bulk操作
    @Override
    public void buldOption(MovieIndexTemplate indexTemplate, String id) throws JsonProcessingException {
//        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
//
//        bulkRequestBuilder.add(transportClient.prepareDelete(INDEX, TYPE, id));
//
//        bulkRequestBuilder.add(transportClient.prepareIndex(INDEX, TYPE).setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON));
//
//        BulkResponse bulkItemResponses = bulkRequestBuilder.get();
//        RestStatus status = bulkItemResponses.status();
//        logger.info("bulk option result status={}", status);
    }

    public void queryDsl() {
        //Match Query
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(MovieSearch.NAME, "毒液");

        //Muitl Match Query
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("", MovieSearch.NAME, MovieSearch.AREA, MovieSearch.INTRODUCTION, MovieSearch.ACTORS, MovieSearch.DIRECTORS);

        //Common Terms Query
        CommonTermsQueryBuilder commonTermsQueryBuilder = QueryBuilders.commonTermsQuery(MovieSearch.NAME, "毒液");

        //Query String Query
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("毒液").field(MovieSearch.NAME).defaultOperator(Operator.OR);

        //Simple Query String Query
        SimpleQueryStringBuilder simpleQueryStringBuilder = QueryBuilders.simpleQueryStringQuery("毒液").field(MovieSearch.NAME);


        //Term Query
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(MovieSearch.NAME, "毒液");

        //Terms Query
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(MovieSearch.NAME, "毒液", "我不是药神");

        //Range Query    筛选评分在7~10分之间的数据集，includeLower(false)表示from是gt，反之；includeUpper(false)表示to是lt，反之
        QueryBuilders.rangeQuery(MovieSearch.SCORE).from(7).to(10).includeLower(false).includeUpper(false);

    }

    public void boolDsl() {
        //Bool Query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //电影名称必须包含我不是药神经过分词后的文本，比如我、不、是、药、神
        boolQueryBuilder.must(QueryBuilders.matchQuery(MovieSearch.NAME, "我不是药神"));

        //排除导演是张三的电影信息
        boolQueryBuilder.mustNot(QueryBuilders.termQuery(MovieSearch.DIRECTORS, "张三"));

        //别名中应该包含药神经过分词后的文本，比如药、神
        boolQueryBuilder.should(QueryBuilders.matchQuery(MovieSearch.ALIAS, "药神"));

        //评分必须大于9（因为es对filter会有智能缓存，推荐使用）
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(MovieSearch.SCORE).gt(9));

        //name、actors、introduction、alias、label 多字段匹配"药神"，或的关系
        boolQueryBuilder.filter(QueryBuilders.multiMatchQuery("药神", MovieSearch.NAME, MovieSearch.ACTORS, MovieSearch.INTRODUCTION, MovieSearch.ALIAS, MovieSearch.LABEL));

        String[] includes = {MovieSearch.NAME, MovieSearch.ALIAS, MovieSearch.SCORE, MovieSearch.ACTORS, MovieSearch.DIRECTORS, MovieSearch.INTRODUCTION};
//        SearchRequestBuilder searchRequestBuilder = transportClient.Search(INDEX).setTypes(TYPE).setQuery(boolQueryBuilder).addSort(MovieSearch.SCORE, SortOrder.DESC).setFrom(0).setSize(10).setFetchSource(includes, null);
//        SearchResponse searchResponse = searchRequestBuilder.get();
//        if (!RestStatus.OK.equals(searchResponse.status())) {
//            return;
//        }
//        for (SearchHit searchHit : searchResponse.getHits()) {
//        	System.err.println(searchHit);
//         //   String name = (String) searchHit.getSource().get(MovieSearch.NAME);
//            //TODO
//        }
    }


}
