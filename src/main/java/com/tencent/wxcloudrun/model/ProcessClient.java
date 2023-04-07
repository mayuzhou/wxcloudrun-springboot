package com.tencent.wxcloudrun.model;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author mayuzhou
 * @classname ProcessClient
 * @description TODO
 * @date 2023/4/7 10:01 下午
 */
@Slf4j
public class ProcessClient {

    Thread thread;
    String id;
    BlockingDeque<String> commandQueue = new LinkedBlockingDeque<>();
    BlockingDeque<String> resultQueue = new LinkedBlockingDeque<>();

    Map<String, ProcessClient> processClientMap = new HashMap<>();
    String path;

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public String invokeCommand(String id, String command) throws InterruptedException {
        if (!processClientMap.containsKey(id)) {
            ProcessClient client = new ProcessClient();
            client.id = id;
            client.thread = new Thread(client::run);
            processClientMap.put(id, client);
            client.thread.start();
        }
        ProcessClient client = processClientMap.get(id);
        client.commandQueue.push(command);
        return client.resultQueue.take();
    }


    public void run(){
        log.info("invoke cmd");
        StringBuilder sb = new StringBuilder();
        try {
            /// 创建一个进程并执行命令
            Process process = Runtime.getRuntime().exec("bash");

            // 获取进程的输入输出流
            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            init(process);
            // 获取命令的输出流
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }
                String command = commandQueue.take();
                writer.write(command + "\n");
                writer.flush();

                while (!reader.ready() && !errorReader.ready()) {
                    Thread.sleep(1000);
                }

                String line;
                while (errorReader.ready() && (line = errorReader.readLine()) != null) {
                    sb.append(line);
                }
                if(!sb.toString().isEmpty()){
                    resultQueue.push(sb.toString());
                    continue;
                }
                while (reader.ready() && (line = reader.readLine()) != null) {
                    sb.append(line);
                }
                resultQueue.push(sb.toString());
            }
            // 输出命令执行结果

        } catch (IOException | InterruptedException e) {
            log.info("{} process stop", id);
            log.info(e.getMessage(), e);
        }
    }

    private void init(Process process){
        OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
        try {
            writer.write("useradd -m user-" + id + "\n");
            writer.write("passwd user-" + id + "\n");
            writer.write("user-" + id + "\n");
            writer.write("user-" + id + "\n");
            writer.write("su user-" + id + "\n");
            writer.write("user-" + id + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
