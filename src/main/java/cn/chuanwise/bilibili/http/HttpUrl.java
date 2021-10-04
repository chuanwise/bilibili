package cn.chuanwise.bilibili.http;

import cn.chuanwise.toolkit.serialize.serializer.object.DeserializedObject;
import cn.chuanwise.toolkit.util.del.JsonUtil;
import cn.chuanwise.toolkit.util.del.YamlUtil;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.ConditionUtil;
import cn.chuanwise.util.StreamUtil;
import cn.chuanwise.util.StringUtil;
import lombok.Getter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Getter
public class HttpUrl {
    final String prefix;
    final Map<String, Object> properties = new HashMap<>();

    HttpUrl(String url) {
        final int askPosition = url.indexOf('?');
        if (askPosition != -1) {
            // url can not be ends with '?'
            ConditionUtil.checkArgument(url.length() > askPosition + 1, "empty arguments!");

            for (String pair : url.substring(askPosition + 1).split(Pattern.quote("&"))) {
                final int equals = pair.indexOf('=');
                ConditionUtil.checkArgument(equals != -1, "no value for pair: " + pair);

                final String key = pair.substring(0, equals);
                final String value = pair.substring(equals + 1);

                ConditionUtil.checkArgument(key.length() > 0, "key length must be greater than 0!");
                ConditionUtil.checkArgument(value.length() > 0, "value length must be greater than 0!");

                // check if it's multiple key
                ConditionUtil.checkArgument(!properties.containsKey(key), "multiple key: " + key + ", first value is " + properties.get(key));

                properties.put(key, value);
            }
            this.prefix = url.substring(0, askPosition);
        } else {
            this.prefix = url;
        }
    }

    public static HttpUrl of(String prefix) {
        return new HttpUrl(prefix);
    }

    public static HttpUrl of(String prefix, Map<String, Object> arguments) {
        return new HttpUrl(prefix).withProperties(arguments);
    }

    public Optional<?> getProperty(String key) {
        synchronized (properties) {
            return Optional.ofNullable(properties.get(key));
        }
    }

    public boolean hasProperty(String key) {
        synchronized (properties) {
            return properties.containsKey(key);
        }
    }

    public HttpUrl withProperty(String key, Object value) {
        ConditionUtil.checkArgument(key.length() > 0, "key length must be greater than 0!");
        getProperty(key).ifPresent(firstValue -> {
            throw new IllegalArgumentException("multiple key: " + key + ", first value is " + StringUtil.doubleQuote(Objects.toString(firstValue)) + ", " +
                    "now: " + StringUtil.doubleQuote(Objects.toString(value)));
        });

        synchronized (properties) {
            properties.put(key, value);
        }
        return this;
    }

    public HttpUrl withProperties(Map<String, Object> arguments) {
        arguments.forEach((k, v) -> withProperty(k, v));
        return this;
    }

    @Override
    public String toString() {
        return toURLString();
    }

    public String toURLString() {
        synchronized (properties) {
            if (properties.isEmpty()) {
                return prefix;
            } else {
                return prefix + '?' + CollectionUtil.toString(properties.entrySet(), entry -> entry.getKey() + '=' + entry.getValue(), "&");
            }
        }
    }

    public URL toURL() throws MalformedURLException {
        return new URL(toURLString());
    }

    public String getString() throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) toURL().openConnection();
        if (200 == connection.getResponseCode()) {
            return StreamUtil.read(connection.getInputStream(), "UTF-8");
        } else {
            throw new IOException("connection failed");
        }
    }

    public DeserializedObject getJsonObject() throws IOException {
        return JsonUtil.SERIALIZER.deserialize(getString());
    }

    public <T> T getJsonObject(Class<T> clazz) throws IOException {
        return JsonUtil.SERIALIZER.deserialize(getString(), clazz);
    }
}