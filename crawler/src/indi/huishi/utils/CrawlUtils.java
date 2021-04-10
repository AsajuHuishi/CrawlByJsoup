package indi.huishi.utils;

import indi.huishi.service.Crawl;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CrawlUtils {

    public static String getPainterId(){
        System.out.println("请输入要查询的画师id");
        Scanner scanner = new Scanner(System.in);
        String s = scanner.next();
        return s;
    }
    /**
     * 根据url连接返回document
     * @param url
     * @return
     */
    public static Document getConnection(String url){
        Connection connect = Jsoup.connect(url);
        Document document = null;
        try {
            document = connect.get();
        } catch (IOException e) {
            throw new RuntimeException("连接失败");
        }
//        Element body = document.body();
//        System.out.println(body);
        System.out.println("连接成功,成功跳转至"+document.title());
        return document;
    }

    /**
     * 获取画师姓名
     * @param document
     * @param cssQuery
     * @param tagName
     * @return
     */
    public static String getPainterName(Document document, String cssQuery, String tagName){
        Elements elements = document.select(cssQuery).tagName(tagName);
        List<String> stringList = elements.stream().map(a -> a.text()).collect(Collectors.toList());
//        System.out.println(stringList);
        if(stringList.size()==0){
            throw new RuntimeException("获取画师姓名失败");
        }
        return stringList.get(0);
    }

    /**
     * 获取画师主页URL
     * @param document
     * @param cssQuery
     * @param attributeKey
     * @return
     */
    public static String getPainterHomePageURL(Document document, String cssQuery, String tagName, String attributeKey){
        Elements elements = document.select(cssQuery).tagName(tagName);//   画师58434088的路径：/painter/14040
        List<String> strings = elements.stream().map(a -> a.attr(attributeKey)).collect(Collectors.toList());
        String painterUrl;
        if(strings.size() == 0){
            throw new RuntimeException("很遗憾，未找到该ID对应的画师");
        }else{
            painterUrl = strings.get(0);
            System.out.println("已找到该画师，正在跳转至"+ Crawl.BasicUrl + painterUrl + "...");
        }
        System.out.println("正在跳转到该画师主页...");
        return painterUrl;
    }


    /**
     * 获取图片信息（链接、名称） 保存到List集合
     * @param cssQuery
     * @param tagName
     * @param attributeKey
     * @return
     */
    public static List<String> getInfoList(Document document, String cssQuery, String tagName, String attributeKey){
        Elements elements = document.select(cssQuery).tagName(tagName);
        // 利用stream获取图片链接
//        elements.stream().map(a -> a.attr(attributeKey)).forEach(System.out::println);
        List<String> stringList = elements.stream().map(a -> a.attr(attributeKey)).collect(Collectors.toList());
        if(stringList.size() == 0){
            throw new RuntimeException("图像获取数量为0，无法下载");
        }
        return stringList;
    }

    /**
     * 将两个list集合转为一个map集合
     * @param list1
     * @param list2
     * @param <T>
     * @return
     */
    public static <T> Map<T, T> listsToMap(List<T>list1, List<T>list2){
        Map<T, T> map = null;
        if(list1.size() != list2.size()){
            throw new RuntimeException("图像链接获取数量与图像名称获取数量不匹配，无法下载");
        }else{
            System.out.println("获得该画师图片资源数量为：" + list2.size() + "张");
            map = new HashMap<>();
            for (int i = 0; i < list1.size(); i++) {
                map.put(list1.get(i),list2.get(i));
            }
        }
        return map;
    }

    /**
     * 根据map集合中的图片链接和名称下载图片
     * @param map
     * @param baseFolder
     * @throws IOException
     */
    public static void downloadImages(Map<String, String>map, String baseFolder, String folderName, String suffix) throws IOException {
        // 生成文件夹
        File folder = new File(baseFolder + folderName);
        if(!folder.exists()){
            folder.mkdirs();
        }
        System.out.println("准备下载...");
        for(Map.Entry<String,String> entry : map.entrySet()){
            // 获取图片链接 下载
            URL url = new URL(entry.getValue());
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            System.out.println(folder.getAbsolutePath() + "//" + entry.getKey());
            File file = new File(baseFolder + folderName + "//" ,entry.getKey() + suffix);
            // 边读边写
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bytes = new byte[1024];
            int length;
            while((length = bufferedInputStream.read(bytes))!=-1) {
                bufferedOutputStream.write(bytes, 0, length);
            }
        }
    }

}
