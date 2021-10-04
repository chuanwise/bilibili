package cn.chuanwise.bilibili.object;

import cn.chuanwise.bilibili.api.Flushable;
import lombok.Getter;

@Getter
public abstract class RequestResult 
        implements Flushable, BilibiliObject {
    protected RequestMessage requestMessage;
}
