package cn.chuanwise.bilibili.object;

import cn.chuanwise.toolkit.serialize.serializer.object.DeserializedObject;
import lombok.Builder;
import lombok.Data;

@Data
public class RequestMessage {
    final int code;
    final String message;
    final int ttl;
    
    public static RequestMessage of(DeserializedObject object) {
        return new RequestMessage(object.getInteger("code"), object.getString("message"), object.getInteger("ttl"));
    }
}
