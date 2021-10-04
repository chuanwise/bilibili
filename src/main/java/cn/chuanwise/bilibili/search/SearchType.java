package cn.chuanwise.bilibili.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum SearchType {
    ALL(Optional.empty()),
//    VIDEO(Optional.of("")),
    DRAMA(Optional.of("media_bangumi")),
    FILMS(Optional.of("media_ft")),
//    LIVE(Optional.of("")),
//    COLUMN(Optional.of("")),
//    TOPIC(Optional.of("")),
    USER(Optional.of("bili_user"));

    final Optional<String> requestType;
}