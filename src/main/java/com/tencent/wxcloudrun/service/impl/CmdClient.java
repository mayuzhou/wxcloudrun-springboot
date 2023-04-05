package com.tencent.wxcloudrun.service.impl;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author mayuzhou
 * @classname CmdClient
 * @description TODO
 * @date 2023/4/5 6:11 下午
 */
@Component
public class CmdClient {
    public String exec(String cmd) {
        try {
            // 创建一个进程并执行命令
            Process process = Runtime.getRuntime().exec(cmd);

            // 读取进程的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder ret = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                ret.append(line);
            }

            // 等待进程结束
            process.waitFor();
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
