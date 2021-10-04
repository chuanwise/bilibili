package cn.chuanwise.bilibili.example;

import cn.chuanwise.bilibili.video.info.VideoInfo;

import java.io.IOException;

public class BilibiliExample {
    void getVideoInfo() throws IOException {
        final VideoInfo videoInfo = VideoInfo.ofBvid("BV1PQ4y167ce");
    }

    public static void main(String[] args) {
        new BilibiliExample();
    }
}
