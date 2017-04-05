package cn.timd.Jelery.Utilities;

import java.net.URL;

class StringUtil {
    static String getRootPath(URL url) {
        String fileUrl = url.getFile();
        int pos = fileUrl.lastIndexOf('!');

        if (-1 == pos)
            return fileUrl;
        return fileUrl.substring(5, pos);
    }

    static String dotToSplash(String name) {
        return name.replaceAll("\\.", "/");
    }

    static String trimExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (-1 != pos)
            return name.substring(0, pos);
        return name;
    }
}

