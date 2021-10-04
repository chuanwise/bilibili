package cn.chuanwise.bilibili.user.info;

import cn.chuanwise.api.ChineseConvertable;
import cn.chuanwise.bilibili.http.HttpUrl;
import cn.chuanwise.bilibili.object.RequestMessage;
import cn.chuanwise.bilibili.object.RequestResult;
import cn.chuanwise.toolkit.serialize.serializer.object.DeserializedObject;
import cn.chuanwise.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Data
public class UserInfo
        extends RequestResult {
    final int uid;

    String name;

    @AllArgsConstructor
    public enum Sex implements ChineseConvertable {
        MALE("男"),
        FMALE("女"),
        SECRECY("保密");

        final String chinese;

        @Override
        public String toChinese() {
            return chinese;
        }

        public static Optional<Sex> ofChinese(String input) {
            return Optional.ofNullable(ArrayUtil.first(values(), x -> Objects.equals(x.toChinese(), input)));
        }
    }
    Sex sex;

    String faceUrl;
    String sign;
    int rank;
    int level;

    Date joinTime;
    double coins;

    String topPhoto;

    int following;
    int whisper;
    int black;
    int follower;

    int totalVideoViewCount;
    int totalLikeCount;
    int totalArticleReadCount;

    UserInfo(int uid) throws IOException {
        this.uid = uid;
        flush();
    }

    public static UserInfo ofUid(int uid) throws IOException {
        return new UserInfo(uid);
    }

    protected void flushBaseInfo() throws IOException {
        final DeserializedObject result = HttpUrl.of("https://api.bilibili.com/x/space/acc/info")
                .withProperty("mid", uid)
                .getJsonObject();
        requestMessage = RequestMessage.of(result);
        final DeserializedObject data = result.getDeserializedObject("data");

        name = data.getString("name");
        sex = Sex.ofChinese(data.getString("sex")).orElseThrow();

        faceUrl = data.getString("face");
        sign = data.getString("sign");
        rank = data.getInteger("rank");
        level = data.getInteger("level");
        joinTime = new Date(data.getInteger("jointime"));
        try {
            coins = data.getDouble("coins");
        } catch (NoSuchElementException exception) {
            coins = data.getInteger("coins");
        }
        topPhoto = data.getString("top_photo");

        /*
        {
        "mid": uid,
        "name": "目标用户名称", //
        "sex": "性别",
        "face": "头像", //头像
        "sign": "自己的签名", //就是懒！
        "rank": 10000, //等级
        "level": 4,
        "jointime": 0,
        "moral": 0,
        "silence": 0,
        "birthday": "11-11", //放弃吧，这个是假的
        "coins": 975.1, //到底谁能告诉我这个.1是啥
        "fans_badge": false,
        "official": { //官方认证的那种玩意（可能我要进官方黑名单了）
            "role": 0,
            "title": "",
            "desc": "",
            "type": -1
        },
        "vip": {
            "type": 2,
            "status": 1,
            "theme_type": 0,
            "label": {
                "path": "",
                "text": "年度大会员",
                "label_theme": "annual_vip"
            },
            "avatar_subscript": 1,
            "nickname_color": "#FB7299"
        },
        "pendant": { //手机端的挂件
            "pid": 451,
            "name": "汉化日记",
            "image": "http://i2.hdslb.com/bfs/face/0f1f8ec045abd1fc572f537a6652207bcbf70a40.png",
            "expire": 0,
            "image_enhance": "http://i2.hdslb.com/bfs/face/0f1f8ec045abd1fc572f537a6652207bcbf70a40.png"
        },
        "nameplate": { //姓名版
            "nid": 0,
            "name": "",
            "image": "",
            "image_small": "",
            "level": "",
            "condition": "百大Up主啊什么的"
        },
        "is_followed": false,
        "top_photo": "http://i0.hdslb.com/bfs/space/cb1c3ef50e22b6096fde67febe863494caefebad.png", //顶部图片
        "theme": {},
        "sys_notice": {}
    }
         */
    }

    protected void flushFollowerInfo() throws IOException {
        final DeserializedObject data = HttpUrl.of("https://api.bilibili.com/x/relation/stat")
                .withProperty("vmid", uid)
                .getJsonObject()
                .getDeserializedObject("data");

        following = data.getInteger("following");
        whisper = data.getInteger("whisper");
        black = data.getInteger("black");
        follower = data.getInteger("follower");
        /*
        {
            "mid": 476720255,
            "following": 224,
            "whisper": 0,
            "black": 3,
            "follower": 14643
        }
         */
    }

    protected void flushUpperInfo() throws IOException {
        final DeserializedObject data = HttpUrl.of("https://api.bilibili.com/x/space/upstat")
                .withProperty("mid", uid)
                .getJsonObject()
                .getDeserializedObject("data");

        totalLikeCount = data.getInteger("likes");
        totalArticleReadCount = data.getInteger("article.view");
        totalVideoViewCount = data.getInteger("archive.view");

        /*
        {
            "archive": {
                "view": 281266
            },
            "article": {
                "view": 5174
            },
            "likes": 37488
        }
         */
    }

    @Override
    public void flush() throws IOException {
        flushBaseInfo();
        flushFollowerInfo();
        flushUpperInfo();
    }
}
