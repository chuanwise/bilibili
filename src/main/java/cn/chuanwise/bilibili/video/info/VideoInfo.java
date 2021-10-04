package cn.chuanwise.bilibili.video.info;

import cn.chuanwise.bilibili.http.HttpUrl;
import cn.chuanwise.bilibili.object.RequestMessage;
import cn.chuanwise.bilibili.object.RequestResult;
import cn.chuanwise.bilibili.user.info.UserInfo;
import cn.chuanwise.exception.UnsupportedVersionException;
import cn.chuanwise.toolkit.serialize.serializer.object.DeserializedObject;
import cn.chuanwise.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class VideoInfo
        extends RequestResult {
    final String bvid;

    String title;
    String description;
    String coverUrl;

    boolean unionVideo;
    int authorUid;
    int aid;

    Date publishDate;
    Date createDate;

    String dynamicContent;

    @Data
    @Builder
    public static class Statistic {
        final int view;
        final int barrage;
        final int comment;
        final int collection;
        final int coin;
        final int share;
        final int like;
        final int dislike;
        final int nowRank;
        final int hisRank;
        final String evaluation;
        final String argueMessage;
    }
    Statistic statistic;

    @Data
    @Builder
    public static class Page {
        final int cid;
        final int page;
        final String from;
        final String part;
        final long length;
        final String vid;
        final String weblink;
        final Dimension dimension;
        final String firstFrameUrl;

        @Data
        @Builder
        public static class Dimension {
            final int width;
            final int height;
            final int rotate;
        }
    }
    List<Page> pages;

    @Data
    public static class Staff {
        @AllArgsConstructor
        public enum Type {
            UPPER("UP主"),
            ACTOR("参演");

            final String chinese;

            @Override
            public String toString() {
                return chinese;
            }

            public static Optional<Type> ofChinese(String chinese) {
                return Optional.ofNullable(ArrayUtil.first(Type.values(), x -> Objects.equals(x.chinese, chinese)));
            }
        }

        final Type type;
        final int uid;

        public UserInfo getUserInfo() throws IOException {
            return UserInfo.ofUid(uid);
        }
    }
    List<Staff> staffs;

    VideoInfo(String bvid) throws IOException {
        this.bvid = bvid;

        flush();
    }

    public UserInfo getAuthorInfo() throws IOException {
        return UserInfo.ofUid(authorUid);
    }

    public boolean isUnionVideo() {
        return staffs.size() > 1;
    }

    @Override
    public void flush() throws IOException {
        // https://api.bilibili.com/x/web-interface/view?bvid=BV1PQ4y167ce
        final DeserializedObject result = HttpUrl.of("https://api.bilibili.com/x/web-interface/view")
                .withProperty("bvid", this.bvid)
                .getJsonObject();
        requestMessage = RequestMessage.of(result);
        final DeserializedObject data = result.getDeserializedObject("data");

        aid = data.getInteger("aid");
        coverUrl = data.getString("pic");
        title = data.getString("title");

        publishDate = new Date(data.getInteger("pubdate"));
        createDate = new Date(data.getInteger("ctime"));
        description = data.getString("desc");

        authorUid = data.getInteger("owner.mid");

        final DeserializedObject statFields = data.getDeserializedObject("stat");
        statistic = Statistic.builder()
                .view(statFields.getInteger("view"))
                .barrage(statFields.getInteger("danmaku"))
                .comment(statFields.getInteger("reply"))
                .collection(statFields.getInteger("favorite"))
                .coin(statFields.getInteger("coin"))
                .share(statFields.getInteger("share"))
                .like(statFields.getInteger("like"))
                .dislike(statFields.getInteger("dislike"))
                .evaluation(statFields.getString("evaluation"))
                .argueMessage(statFields.getString("argue_msg"))
                .nowRank(statFields.getInteger("now_rank"))
                .hisRank(statFields.getInteger("his_rank"))
                .build();

        dynamicContent = data.getString("dynamic");

        /*
        {
                "cid": 417898238,
                "page": 1,
                "from": "vupload",
                "part": "发布版",
                "duration": 82,
                "vid": "",
                "weblink": "",
                "dimension": {
                    "width": 1920,
                    "height": 1080,
                    "rotate": 0
                },
                "first_frame": "http://i0.hdslb.com/bfs/storyff/n211002qn2p9a45lyk915n1catpu2l1s_firsti.jpg"
            }
         */
        pages = data.getDeserializedObjectList("pages").stream()
                .sorted(Comparator.comparingInt(l -> l.getInteger("page")))
                .map(object -> {
                    final DeserializedObject dimensionFields = object.getDeserializedObject("dimension");
                    final Page.Dimension dimension = Page.Dimension.builder()
                            .height(dimensionFields.getInteger("height"))
                            .width(dimensionFields.getInteger("width"))
                            .rotate(dimensionFields.getInteger("rotate"))
                            .build();
                    return Page.builder()
                            .cid(object.getInteger("cid"))
                            .from(object.getString("from"))
                            .part(object.getString("part"))
                            .length(object.getInteger("duration"))
                            .vid(object.getString("vid"))
                            .weblink(object.getString("weblink"))
//                            .firstFrameUrl(object.getString("first_frame"))
                            .dimension(dimension)
                            .build();
                })
                .collect(Collectors.toUnmodifiableList());

        try {
            staffs = data.getDeserializedObjectList("staff")
                    .stream()
                    .map(object -> {
                        final Staff.Type type = Staff.Type.ofChinese(object.getString("title")).orElseThrow(UnsupportedVersionException::new);
                        final int uid = object.getInteger("mid");
                        return new Staff(type, uid);
                    })
                    .collect(Collectors.toUnmodifiableList());
        } catch (NoSuchElementException exception) {
            staffs = Collections.emptyList();
        }
    }

    public static VideoInfo ofBvid(String bvid) throws IOException {
        return new VideoInfo(bvid);
    }
}
