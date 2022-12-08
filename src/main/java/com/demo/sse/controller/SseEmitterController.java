package com.demo.sse.controller;

import com.demo.sse.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/sseEmitter")
public class SseEmitterController {
    private static final Map<String, SseEmitter> emitterMap = new HashMap<>();

    /**
     * 服务端不使用SseEmitter时使用
     *
     * @param response
     * @throws IOException
     */
    @GetMapping(value = "/data")
    public void getData(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream;charset=UTF-8");
        response.getWriter().write("retry: 5000\n");
        response.getWriter().write("data: hahahaha\n\n");
        response.getWriter().flush();
        System.in.read();
    }


    /**
     * 服务端使用SseEmitter时使用
     *
     * @param username
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/connect/{username}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter connect(@PathVariable String username) throws IOException, InterruptedException {
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(() -> {
            System.out.println(username + "连接结束！");
            emitterMap.remove(username);
        });
        sseEmitter.onError((t) -> {
            System.out.println(username + "连接出错！错误信息：" + t.getMessage());
            emitterMap.remove(username);
        });
        sseEmitter.onTimeout(() -> {
            System.out.println(username + "连接超时！");
            emitterMap.remove(username);
        });
        emitterMap.put(username, sseEmitter);

        sseEmitter.send("连接建立成功");
        return sseEmitter;
    }

    /**
     * 服务端使用SseEmitter时使用
     * <p>
     * 点对点
     *
     * @param username
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/send/{username}")
    public String send(@PathVariable String username) throws IOException {
        SseEmitter sseEmitter = emitterMap.get(username);
        if (sseEmitter == null) {
            return "没查询到该用户的连接！";
        }
        for (int i = 0; i < 30; i++) {
            User user = new User("用户" + i, "密码" + i, "昵称" + i);
            sseEmitter.send(SseEmitter.event().name("psh").data(user));
        }

        return "发送成功～";
    }

    /**
     * 服务端使用SseEmitter时使用
     * <p>
     * 广播方式
     *
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/sendAll")
    public String sendAll() throws IOException, InterruptedException {
        for (SseEmitter sseEmitter : emitterMap.values()) {
            for (int i = 0; i < 30; i++) {
                User user = new User("用户" + i, "密码" + i, "昵称" + i);
                sseEmitter.send(SseEmitter.event().name("psh").data(user));
            }
        }
        return "发送完成～";
    }

    /**
     * 生成测试数据
     *
     * @throws Exception
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void ZsScanningProcess() throws Exception {
        SseEmitter sseEmitter = emitterMap.get("zhangsan");
        if (sseEmitter != null) {
            for (int i = 0; i < 10; i++) {
                User user = new User("zhangsan" + i, "密码" + i, "昵称" + i);
                sseEmitter.send(SseEmitter.event().name("psh").data(user));
            }
            //完成
            sseEmitter.complete();
        }

    }

    /**
     * 生成测试数据
     *
     * @throws Exception
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void LsScanningProcess() throws Exception {
        SseEmitter sseEmitter = emitterMap.get("lisi");
        if (sseEmitter != null) {
            for (int i = 0; i < 10; i++) {
                User user = new User("lisi" + i, "密码" + i, "昵称" + i);
                sseEmitter.send(SseEmitter.event().name("psh").data(user));
                Thread.sleep(500);
            }
            //完成
            sseEmitter.complete();
        }
    }


}
