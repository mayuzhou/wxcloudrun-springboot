package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.service.impl.ChatGPTClient;
import com.tencent.wxcloudrun.service.impl.CmdClient;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.CounterRequest;
import com.tencent.wxcloudrun.model.Counter;
import com.tencent.wxcloudrun.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * counter控制器
 */
@RestController

public class CounterController {

  final CounterService counterService;
  final Logger logger;

  @Autowired
  ChatGPTClient chatGPTClient;

  @Autowired
  CmdClient cmdClient;

  public CounterController(@Autowired CounterService counterService) {
    this.counterService = counterService;
    this.logger = LoggerFactory.getLogger(CounterController.class);
  }


  /**
   * 获取当前计数
   * @return API response json
   */
  @GetMapping(value = "/api/count")
  ApiResponse get() {
    logger.info("/api/count get request");
    Optional<Counter> counter = counterService.getCounter(1);
    Integer count = 0;
    if (counter.isPresent()) {
      count = counter.get().getCount();
    }

    return ApiResponse.ok(count);
  }


  @GetMapping(value = "/api/question")
  public ApiResponse getResult(String content) throws Exception {
    logger.info("/api/question get request");
    return ApiResponse.ok(chatGPTClient.getCompletion(content));
  }

  @GetMapping(value = "/api/cmd")
  public ApiResponse getCmd(@RequestParam String content, HttpServletRequest request) {
    logger.info("/api/cmd get request");
    HttpSession session = request.getSession();
    return ApiResponse.ok(cmdClient.exec(content,session));
  }

  /**
   * 更新计数，自增或者清零
   * @param request {@link CounterRequest}
   * @return API response json
   */
  @PostMapping(value = "/api/count")
  public ApiResponse create(@RequestBody CounterRequest request) {
    logger.info("/api/count post request, action: {}", request.getAction());

    Optional<Counter> curCounter = counterService.getCounter(1);
    if (request.getAction().equals("inc")) {
      Integer count = 1;
      if (curCounter.isPresent()) {
        count += curCounter.get().getCount();
      }
      Counter counter = new Counter();
      counter.setId(1);
      counter.setCount(count);
      counterService.upsertCount(counter);
      return ApiResponse.ok(count);
    } else if (request.getAction().equals("clear")) {
      if (!curCounter.isPresent()) {
        return ApiResponse.ok(0);
      }
      counterService.clearCount(1);
      return ApiResponse.ok(0);
    } else {
      return ApiResponse.error("参数action错误");
    }
  }
  
}