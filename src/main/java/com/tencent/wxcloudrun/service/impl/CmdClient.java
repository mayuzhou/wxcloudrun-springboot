package com.tencent.wxcloudrun.service.impl;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mayuzhou
 * @classname CmdClient
 * @description TODO
 * @date 2023/4/5 6:11 下午
 */
@Component
public class CmdClient {

    AtomicInteger count = new AtomicInteger();

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public String exec(String cmd, HttpSession session) {
        // 创建一个 bash 进程
        String ret = "";
        String path = (String) session.getAttribute("path");
        if (path == null){
            try {
                execProcess("sudo useradd -m -s /bin/sh user-"+count.getAndIncrement(), "/app");
            } catch (Exception e){
                return e.getMessage();
            }

            session.setAttribute("user", "user-"+count.get());
        }
        String user = (String) session.getAttribute("user");
        path = execProcess("su " + user + "&&echo $HOME\n","/app");
        ret = execProcess(cmd, path);
        return ret;
    }

    private String execProcess(String cmd, String path){
        StringBuilder sb = new StringBuilder();
        try {
            // 指定执行的目录
            File workingDirectory = new File(path);
            // 构建命令和参数列表
            ProcessBuilder processBuilder = new ProcessBuilder(cmd.split(" "));
            // 设置执行的目录
            processBuilder.directory(workingDirectory);
            // 开始执行命令
            Process process = processBuilder.start();
            // 获取命令的输出流
            InputStream inputStream = process.getInputStream();
            // 将输出流包装成BufferedReader以便读取命令的输出结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            // 读取命令的输出结果
            // 等待命令执行完成
            int exitCode = process.waitFor();
            String line;
            while ((line = errorReader.readLine()) != null) {
                sb.append(line);
            }
            if(!sb.toString().isEmpty()){
                throw new RuntimeException(sb.toString());
            }
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 输出命令执行结果
            System.out.println("Command exited with code " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static void main(String[] args) throws IOException {
        try {
            // 创建一个进程并执行命令
            Process process = Runtime.getRuntime().exec("bash");

            // 获取进程的输入输出流
            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // 发送命令并读取输出
            writer.write("ll\n");
            writer.flush();

            String error;

            String line;
            while (errorReader.ready()&&(line = errorReader.readLine()) != null) {
                System.out.println(line);
            }

            while (reader.ready()&&(line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 关闭输入输出流
            writer.close();
            reader.close();

            // 等待进程结束
//            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
