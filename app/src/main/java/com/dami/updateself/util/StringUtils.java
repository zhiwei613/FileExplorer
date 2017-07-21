package com.dami.updateself.util;

import java.util.regex.Pattern;

public class StringUtils {
    public static final String EMPTY = "";
    public static final int INDEX_NOT_FOUND = -1;
    public static final String SPACE = " ";

    private StringUtils() {
    }

    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence charSequence) {
        return !isEmpty(charSequence);
    }

    public static <T> String join(T... tArr) {
        return join((Object[]) tArr, null);
    }

    public static String join(Object[] objArr, char c) {
        return objArr == null ? null : join(objArr, c, 0, objArr.length);
    }

    public static String join(Object[] objArr, char c, int i, int i2) {
        if (objArr == null) {
            return null;
        }
        int i3 = i2 - i;
        if (i3 <= 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(i3 * 16);
        for (i3 = i; i3 < i2; i3++) {
            if (i3 > i) {
                stringBuilder.append(c);
            }
            if (objArr[i3] != null) {
                stringBuilder.append(objArr[i3]);
            }
        }
        return stringBuilder.toString();
    }

    public static String join(Object[] objArr, String str) {
        return objArr == null ? null : join(objArr, str, 0, objArr.length);
    }

    public static String join(Object[] objArr, String str, int i, int i2) {
        if (objArr == null) {
            return null;
        }
        if (str == null) {
            str = "";
        }
        int i3 = i2 - i;
        if (i3 <= 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(i3 * 16);
        for (i3 = i; i3 < i2; i3++) {
            if (i3 > i) {
                stringBuilder.append(str);
            }
            if (objArr[i3] != null) {
                stringBuilder.append(objArr[i3]);
            }
        }
        return stringBuilder.toString();
    }

    public static String removePattern(String str, String str2) {
        return replacePattern(str, str2, "");
    }

    public static String replace(String str, String str2, String str3) {
        return replace(str, str2, str3, -1);
    }

    public static String replace(String str, String str2, String str3, int i) {
        int i2 = 64;
        if (isEmpty(str) || isEmpty(str2) || str3 == null || i == 0) {
            return str;
        }
        int indexOf = str.indexOf(str2, 0);
        if (indexOf == -1) {
            return str;
        }
        int length = str2.length();
        int length2 = str3.length() - length;
        if (length2 < 0) {
            length2 = 0;
        }
        if (i < 0) {
            i2 = 16;
        } else if (i <= 64) {
            i2 = i;
        }
        StringBuilder stringBuilder = new StringBuilder((i2 * length2) + str.length());
        i2 = 0;
        while (indexOf != -1) {
            stringBuilder.append(str.substring(i2, indexOf)).append(str3);
            i2 = indexOf + length;
            i--;
            if (i == 0) {
                break;
            }
            indexOf = str.indexOf(str2, i2);
        }
        stringBuilder.append(str.substring(i2));
        return stringBuilder.toString();
    }

    public static String replaceChars(String str, char c, char c2) {
        return str == null ? null : str.replace(c, c2);
    }

    public static String replaceChars(String str, String str2, String str3) {
        Object obj = null;
        if (isEmpty(str) || isEmpty(str2)) {
            return str;
        }
        if (str3 == null) {
            str3 = "";
        }
        int length = str3.length();
        int length2 = str.length();
        StringBuilder stringBuilder = new StringBuilder(length2);
        for (int i = 0; i < length2; i++) {
            char charAt = str.charAt(i);
            int indexOf = str2.indexOf(charAt);
            if (indexOf >= 0) {
                obj = 1;
                if (indexOf < length) {
                    stringBuilder.append(str3.charAt(indexOf));
                }
            } else {
                stringBuilder.append(charAt);
            }
        }
        return obj != null ? stringBuilder.toString() : str;
    }

    public static String replaceEach(String str, String[] strArr, String[] strArr2) {
        return replaceEach(str, strArr, strArr2, false, 0);
    }

    private static String replaceEach(String str, String[] strArr, String[] strArr2, boolean z, int i) {
        if (str == null || str.isEmpty() || strArr == null || strArr.length == 0 || strArr2 == null || strArr2.length == 0) {
            return str;
        }
        if (i < 0) {
            throw new IllegalStateException("Aborting to protect against StackOverflowError - output of one loop is the input of another");
        }
        int length = strArr.length;
        int length2 = strArr2.length;
        if (length != length2) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: " + length + " vs " + length2);
        }
        boolean[] zArr = new boolean[length];
        int i2 = 0;
        int i3 = -1;
        int i4 = -1;
		int i5 = 0;
        while (i2 < length) {
            if (!(zArr[i2] || strArr[i2] == null || strArr[i2].isEmpty())) {
                if (strArr2[i2] == null) {
                    length2 = i4;
                    i5 = i3;
                } else {
                    length2 = str.indexOf(strArr[i2]);
                    if (length2 == -1) {
                        zArr[i2] = true;
                        length2 = i4;
                        i5 = i3;
                    } else if (i4 == -1 || length2 < i4) {
                        i5 = i2;
                    }
                }
                i2++;
                i3 = i5;
                i4 = length2;
            }
            length2 = i4;
            i5 = i3;
            i2++;
            i3 = i5;
            i4 = length2;
        }
        if (i4 == -1) {
            return str;
        }
        length2 = 0;
        i5 = 0;
        while (length2 < strArr.length) {
            if (!(strArr[length2] == null || strArr2[length2] == null)) {
                i2 = strArr2[length2].length() - strArr[length2].length();
                if (i2 > 0) {
                    i5 += i2 * 3;
                }
            }
            length2++;
        }
        StringBuilder stringBuilder = new StringBuilder(Math.min(i5, str.length() / 5) + str.length());
        length2 = 0;
        while (i4 != -1) {
            while (length2 < i4) {
                stringBuilder.append(str.charAt(length2));
                length2++;
            }
            stringBuilder.append(strArr2[i3]);
            i2 = i4 + strArr[i3].length();
            i4 = -1;
            i3 = 0;
            length2 = -1;
            while (i3 < length) {
                if (!(zArr[i3] || strArr[i3] == null || strArr[i3].isEmpty())) {
                    if (strArr2[i3] == null) {
                        i5 = i4;
                    } else {
                        i5 = str.indexOf(strArr[i3], i2);
                        if (i5 == -1) {
                            zArr[i3] = true;
                            i5 = i4;
                        } else if (length2 == -1 || i5 < length2) {
                            length2 = i5;
                            i5 = i3;
                        }
                    }
                    i3++;
                    i4 = i5;
                }
                i5 = i4;
                i3++;
                i4 = i5;
            }
            i3 = i4;
            i4 = length2;
            length2 = i2;
        }
        int length3 = str.length();
        while (length2 < length3) {
            stringBuilder.append(str.charAt(length2));
            length2++;
        }
        str = stringBuilder.toString();
        return z ? replaceEach(str, strArr, strArr2, z, i - 1) : str;
    }

    public static String replaceEachRepeatedly(String str, String[] strArr, String[] strArr2) {
        return replaceEach(str, strArr, strArr2, true, strArr == null ? 0 : strArr.length);
    }

    public static String replaceOnce(String str, String str2, String str3) {
        return replace(str, str2, str3, 1);
    }

    public static String replacePattern(String str, String str2, String str3) {
        return Pattern.compile(str2, 32).matcher(str).replaceAll(str3);
    }

    public static String strip(String str) {
        return strip(str, null);
    }

    public static String strip(String str, String str2) {
        return isEmpty(str) ? str : stripEnd(stripStart(str, str2), str2);
    }

    public static String stripEnd(String str, String str2) {
        if (str == null) {
            return str;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        if (str2 == null) {
            while (length != 0 && Character.isWhitespace(str.charAt(length - 1))) {
                length--;
            }
        } else if (str2.isEmpty()) {
            return str;
        } else {
            while (length != 0 && str2.indexOf(str.charAt(length - 1)) != -1) {
                length--;
            }
        }
        return str.substring(0, length);
    }

    public static String stripStart(String str, String str2) {
        int i = 0;
        if (str == null) {
            return str;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        if (str2 == null) {
            while (i != length && Character.isWhitespace(str.charAt(i))) {
                i++;
            }
        } else if (str2.isEmpty()) {
            return str;
        } else {
            while (i != length && str2.indexOf(str.charAt(i)) != -1) {
                i++;
            }
        }
        return str.substring(i);
    }

    public static String stripToEmpty(String str) {
        return str == null ? "" : strip(str, null);
    }

    public static String stripToNull(String str) {
        if (str == null) {
            return null;
        }
        String strip = strip(str, null);
        return !strip.isEmpty() ? strip : null;
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    public static String trimToNull(String str) {
        Object trim = trim(str);
        return (String) (isEmpty((CharSequence) trim) ? null : trim);
    }
}
