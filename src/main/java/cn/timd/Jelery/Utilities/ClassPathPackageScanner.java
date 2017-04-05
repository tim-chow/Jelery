package cn.timd.Jelery.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassPathPackageScanner {
    private String basePackage;
    private ClassLoader classLoader;

    public ClassPathPackageScanner(String basePackage) {
        this.basePackage = basePackage;
        this.classLoader = getClass().getClassLoader();
    }

    public List<String> getFullyQualifiedClassNameList() throws IOException {
        return doScan(basePackage, new ArrayList<String>());
    }

    private List<String> doScan(String basePackage, List<String> classNameList) throws IOException {
        String splashPath = StringUtil.dotToSplash(basePackage);
        Enumeration<URL> urls = classLoader.getResources(splashPath);
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            String filePath = StringUtil.getRootPath(url);
            List<String> names;
            if (isJarFile(filePath))
                names = readFromJarFile(filePath, splashPath);
            else
                names = readFromDirectory(filePath);

            for (String name : names) {
                if (isClassFile(name))
                    classNameList.add(toFullyQualifiedName(name, basePackage));
                else
                    doScan(basePackage + "." + name, classNameList);
            }
        }
        return classNameList;
    }

    private String toFullyQualifiedName(String shortName, String basePackage) {
        StringBuilder sb = new StringBuilder(basePackage);
        sb.append('.');
        sb.append(StringUtil.trimExtension(shortName));

        return sb.toString();
    }

    private List<String> readFromJarFile(String jarPath, String splashedPackageName) throws IOException {
        JarInputStream jarIn = new JarInputStream(new FileInputStream(jarPath));
        JarEntry entry = jarIn.getNextJarEntry();

        List<String> nameList = new ArrayList<String>();
        while (null != entry) {
            String name = entry.getName();
            if (name.startsWith(splashedPackageName) && isClassFile(name)) {
                nameList.add(name);
            }

            entry = jarIn.getNextJarEntry();
        }

        return nameList;
    }

    private List<String> readFromDirectory(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory() || !file.canRead())
            return new ArrayList<String>();

        String[] names = file.list();
        if (null == names)
            return new ArrayList<String>();
        return Arrays.asList(names);
    }

    private boolean isClassFile(String name) {
        return name.endsWith(".class");
    }

    private boolean isJarFile(String name) {
        File file = new File(name);
        return file.exists() && file.isFile() && file.canRead() && name.endsWith(".jar");
    }
}
