package cn.chuanwise.bilibili.user;

import cn.chuanwise.bilibili.object.BilibiliObject;

public class User
        implements BilibiliObject {
    final int uid;

    User(int uid) {
        this.uid = uid;
    }

    User(String name) {
        uid = 0;
    }

    public static User fromUid(int uid) {
        return new User(uid);
    }
}
