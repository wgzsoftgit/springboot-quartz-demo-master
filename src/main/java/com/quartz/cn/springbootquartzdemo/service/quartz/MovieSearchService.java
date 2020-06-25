package com.quartz.cn.springbootquartzdemo.service.quartz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quartz.cn.springbootquartzdemo.bean.MovieIndex;
import com.quartz.cn.springbootquartzdemo.bean.MovieSearch;
import com.quartz.cn.springbootquartzdemo.vo.MovieIndexTemplate;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName MovieSearchService
 * @Description TODO
 * @Author simonsfan
 * @Date 2019/1/16
 * Version  1.0
 */
public interface MovieSearchService {
/**
 * 复合查询
 * @param search
 * @return
 */
    List<MovieIndex> searchMovie(MovieSearch search)throws IOException;
    String addIndex(MovieIndexTemplate indexTemplate)throws IOException;
    String getIndex(String id)throws IOException;
    String deleteIndex(String id) throws IOException;
    public String deleteByQueryAction(String indexName,String type, String id) throws IOException;
    void buldOption(MovieIndexTemplate indexTemplate, String id) throws JsonProcessingException,IOException;
}
