package cn.chuanwise.bilibili.object;

import cn.chuanwise.bilibili.Bilibili;

public interface BilibiliObject {
    default Bilibili getBilibili() {
        return Bilibili.getInstance();
    }
}
