package cn.chuanwise.bilibili.util;

import cn.chuanwise.bilibili.http.HttpUrl;
import cn.chuanwise.toolkit.serialize.serializer.object.DeserializedObject;
import cn.chuanwise.toolkit.util.del.JsonUtil;
import cn.chuanwise.util.StaticUtil;
import cn.chuanwise.util.StreamUtil;

import java.io.IOException;
import java.net.URL;

public class IdUtil extends StaticUtil {
    public static int bvidToCid(String bvid) throws IOException {
        return HttpUrl.of("https://api.bilibili.com/x/player/pagelist")
                .withProperty("bvid", bvid)
                .getJsonObject()
                .getDeserializedObjectList("data")
                .get(0)
                .getInteger("cid");
    }

    public static int bvidToAid(String bvid) throws IOException {
        return HttpUrl.of("https://api.bilibili.com/x/web-interface/view")
                .withProperty("cid", bvidToCid(bvid))
                .withProperty("bvid", bvid)
                .getJsonObject()
                .getInteger("data.aid");
    }
}
