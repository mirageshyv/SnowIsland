package com.example.snowisland.util;

import java.util.regex.Pattern;

/**
 * Defense-in-depth for persisted user text. SQL is still parameterized (JPA);
 * this strips control characters and caps length so import blobs cannot
 * smuggle NULs or unbounded payloads.
 */
public final class SafeText {

    public static final int PACK_NAME_MAX = 80;
    public static final int EVENT_NAME_MAX = 100;
    public static final int FIELD_MAX = 20000;
    public static final int NOTE_TITLE_MAX = 80;
    public static final int NOTE_BODY_MAX = 50000;
    public static final int NOTE_MAX_PAGES = 30;
    public static final int RAW_IMPORT_MAX = 200_000;
    public static final int MAX_IMPORT_EVENTS = 80;

    private static final Pattern PACK_NAME_OK = Pattern.compile("^[\\u4e00-\\u9fffA-Za-z0-9_\\-·（）()\\s]+$");

    private SafeText() {
    }

    /** Drop NUL and other C0 controls except newline/tab. */
    public static String clean(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == 0) {
                continue;
            }
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String cleanLimit(String raw, int max) {
        String cleaned = clean(raw);
        if (cleaned == null) {
            return null;
        }
        if (max > 0 && cleaned.length() > max) {
            return cleaned.substring(0, max);
        }
        return cleaned;
    }

    /**
     * @return trimmed pack name, or null if empty/invalid
     */
    public static String validatePackName(String raw) {
        String name = clean(raw);
        if (name == null) {
            return null;
        }
        name = name.trim();
        if (name.isEmpty() || name.length() > PACK_NAME_MAX) {
            return null;
        }
        if (!PACK_NAME_OK.matcher(name).matches()) {
            return null;
        }
        return name;
    }

    public static String packNameError(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "卡包名称不能为空";
        }
        if (raw.trim().length() > PACK_NAME_MAX) {
            return "卡包名称不能超过 " + PACK_NAME_MAX + " 个字符";
        }
        if (validatePackName(raw) == null) {
            return "卡包名称仅允许中文、字母、数字、空格与 ·_-（）";
        }
        return null;
    }
}
