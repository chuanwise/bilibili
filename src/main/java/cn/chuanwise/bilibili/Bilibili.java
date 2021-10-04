package cn.chuanwise.bilibili;

import cn.chuanwise.toolkit.serialize.serializer.json.JackJsonSerializer;
import cn.chuanwise.toolkit.serialize.serializer.json.JsonSerializer;
import cn.chuanwise.util.StaticUtil;

/**
 * bilibili api
 */
public final class Bilibili {
    private static class InstanceContainer extends StaticUtil {
        protected static final Bilibili INSTANCE = new Bilibili();
    }
    private Bilibili() {}

    public static Bilibili getInstance() {
        return InstanceContainer.INSTANCE;
    }
}