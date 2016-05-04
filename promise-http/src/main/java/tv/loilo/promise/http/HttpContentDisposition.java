package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

public final class HttpContentDisposition {
    @NonNull
    private final String mDispositionType;

    @NonNull
    private final Map<String, String> mParams;

    private HttpContentDisposition(final String dispositionType) {
        this(dispositionType, new ArrayMap<String, String>());
    }

    private HttpContentDisposition(@NonNull final String dispositionType, @NonNull final Map<String, String> params) {
        mDispositionType = dispositionType;
        mParams = params;
    }

    @Nullable
    public static HttpContentDisposition tryParse(final String contentDisposition) {

        if (contentDisposition == null) {
            return null;
        }

        final String trimContentDisposition = contentDisposition.trim();
        if (trimContentDisposition.length() <= 0) {
            return null;
        }

        final int dispositionTypeSeparatorIndex = trimContentDisposition.indexOf(";");
        if (dispositionTypeSeparatorIndex < 0) {
            return new HttpContentDisposition(trimContentDisposition.toLowerCase());
        }

        final String dispositionType = trimContentDisposition.substring(0, dispositionTypeSeparatorIndex).toLowerCase();
        //System.out.println("dispositionType = " + dispositionType);
        if (dispositionTypeSeparatorIndex >= trimContentDisposition.length() - 1) {
            return new HttpContentDisposition(dispositionType);
        }

        final String member = trimContentDisposition.substring(dispositionTypeSeparatorIndex + 1);

        final ArrayMap<String, String> params = new ArrayMap<>();

        for (int y = 0, length = member.length(); y < length; ++y) {
            //noinspection SuspiciousNameCombination
            int x = y;
            boolean inDoubleQuote = false;
            boolean inSingleQuote = false;
            boolean hasEscape = false;
            for (; x < length; ++x) {
                if (hasEscape) {
                    hasEscape = false;
                    continue;
                }

                final char c = member.charAt(x);

                if (inDoubleQuote) {
                    if (c == '"') {
                        inDoubleQuote = false;
                    } else if (c == '\\') {
                        hasEscape = true;
                    }
                    continue;
                }

                if (inSingleQuote) {
                    if (c == '\'') {
                        inSingleQuote = false;
                    } else if (c == '\\') {
                        hasEscape = true;
                    }
                    continue;
                }

                if (c == '"') {
                    inDoubleQuote = true;
                    continue;
                }

                if (c == '\'') {
                    inSingleQuote = true;
                    continue;
                }

                if (c == ';') {
                    break;
                }
            }
            if (x - y > 0) {
                final String record = member.substring(y, x);
                //System.out.println(record);

                final int keySeparatorIndex = record.indexOf('=');
                if (keySeparatorIndex >= 0 && keySeparatorIndex < record.length() - 1) {
                    final String key = record.substring(0, keySeparatorIndex).trim().toLowerCase();
                    if (key.length() > 0) {
                        final String value = record.substring(keySeparatorIndex + 1).trim();
                        if (value.length() > 0) {
                            params.put(key, value);
                        }
                    }
                }
            }

            //noinspection SuspiciousNameCombination
            y = x;
        }

        return new HttpContentDisposition(dispositionType, params);
    }

    @Nullable
    private static String unescapeValue(@Nullable final String value) {
        if (value == null) {
            return null;
        }

        String ret = value;
        if (ret.startsWith("\"") && ret.endsWith("\"")) {
            if (ret.length() <= 2) {
                return "";
            }
            ret = ret.substring(1, ret.length() - 1);
        } else if (ret.startsWith("'") && ret.endsWith("'")) {
            if (ret.length() <= 2) {
                return "";
            }
            ret = ret.substring(1, ret.length() - 1);
        }

        return ret.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    @Nullable
    private static String decodeValue(@Nullable final String value) {
        if (value == null) {
            return null;
        }

        final int encodeSeparatorIndex = value.indexOf("''");
        if (encodeSeparatorIndex < 0) {
            return value;
        }
        if (encodeSeparatorIndex + 2 > value.length() - 1) {
            return "";
        }

        final String encode = value.substring(0, encodeSeparatorIndex).trim();
        final String field = value.substring(encodeSeparatorIndex + 2).trim();
        if (encode.length() <= 0) {
            return field;
        }

        try {
            return URLDecoder.decode(field, encode);
        } catch (UnsupportedEncodingException e) {
            return field;
        }
    }

    @Nullable
    public String getFileName() {
        final String value = unescapeValue(mParams.get("filename"));
        if (value != null) {
            return value;
        }
        return unescapeValue(mParams.get("xfilename"));
    }

    @Nullable
    public String getFileNameStar() {
        return decodeValue(unescapeValue(mParams.get("filename*")));
    }

    @Nullable
    public String getName() {
        return unescapeValue(mParams.get("name"));
    }

    @NonNull
    public String getDispositionType() {
        return mDispositionType;
    }

    @NonNull
    public Map<String, String> getParams() {
        return mParams;
    }
}
