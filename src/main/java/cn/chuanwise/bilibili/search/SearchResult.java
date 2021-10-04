package cn.chuanwise.bilibili.search;

import cn.chuanwise.bilibili.api.Flushable;
import cn.chuanwise.bilibili.http.HttpUrl;
import cn.chuanwise.bilibili.object.BilibiliObject;
import cn.chuanwise.bilibili.object.RequestMessage;
import cn.chuanwise.toolkit.serialize.serializer.object.DeserializedObject;
import cn.chuanwise.util.ConditionUtil;
import lombok.Getter;

import java.io.IOException;
import java.util.*;

@Getter
public class SearchResult
        implements BilibiliObject, Flushable {
    final String keyword;
    final SearchType searchType;

    RequestMessage requestMessage;

    int pageSize = 20;
    int page = 1;
    
    List<SearchResult> results = new ArrayList<>();

    SearchResult(String keyword, SearchType searchType) throws IOException {
        this.keyword = keyword;
        this.searchType = searchType;

        flush();
    }

    public static SearchResult search(String keyword, SearchType searchType) throws IOException {
        return new SearchResult(keyword, searchType);
    }

    public void setPage(int page) throws IOException {
        ConditionUtil.checkArgument(page > 0, "page must be greater than 0!");
        this.page = page;
        flush();
    }

    public void setPageSize(int pageSize) throws IOException {
        ConditionUtil.checkArgument(page > 0, "page size must be greater than 0!");
        this.pageSize = pageSize;
        flush();
    }

    public List<SearchResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    @Override
    public void flush() throws IOException {
        // https://api.bilibili.com/x/web-interface/search/all/v2?keyword=太学&page=1&pagesize=20
        // https://api.bilibili.com/x/web-interface/search/type?keyword=太学&page=1&search_type=bili_user&order=totalrank&pagesize=20
        final HttpUrl url;
        final Optional<String> requestType = searchType.getRequestType();
        if (requestType.isPresent()) {
            url = HttpUrl.of("https://api.bilibili.com/x/web-interface/search/type/v2").withProperty("search_type", requestType.get());
        } else {
            url = HttpUrl.of("https://api.bilibili.com/x/web-interface/search/all/v2");
        }

        url.withProperty("keyword", keyword)
                .withProperty("page", page)
                .withProperty("pageSize", pageSize);

        // flush buffer
        final DeserializedObject result = url.getJsonObject();
        requestMessage = RequestMessage.of(result);
        final DeserializedObject data = result.getDeserializedObject("data");
    }
}