package indi.huishi.service;

import indi.huishi.utils.CrawlUtils;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Crawl {

    public static final String Proto = "https://";
    public static final String BasicUrl = "www.huashi6.com";
    static final String ImageUrlCssQuery = ".p-painter-detail .painter-page-body .hot-work-list .hot-work-page .cover-img .img-vec,.img-hor";
    static final String ImageTitleCssQuery = ".p-painter-detail .painter-page-body .hot-work-list .hot-work-page .work-info .name";
    static final String PainterHomePageCssQuery = ".search-container .container .painter .classify-painter .c-painter-item .painter .painter-info";
    static final String PainterNameCssQuery = ".search-container .container .painter .classify-painter .c-painter-item .painter .painter-name";
    static final String BaseFolder = "crawler//saveImage//";
    static String painterName = "未命名画师";
    static String suffix = ".png";

    public static void main(String[] args) throws IOException {
        // 输入画师id
        String painterId = CrawlUtils.getPainterId();
//        String s ="58434088";
//        String s = "22222232"; //错误id
        StringBuffer buffer = new StringBuffer(Proto + BasicUrl + "/search?searchText=");
        // 1.根据url连接返回document
        Document document = null;
        try {
            document = CrawlUtils.getConnection(buffer.append(painterId).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        // 2.根据层级元素查找画师的主页url并跳转
        String painterHomePageUrl = null;
        try {
            painterHomePageUrl = CrawlUtils.getPainterHomePageURL(document, PainterHomePageCssQuery, "a", "href");
        } catch (Exception e){
            e.printStackTrace();
        }
        // 获取画师姓名
        try {
            painterName = CrawlUtils.getPainterName(document, PainterNameCssQuery, "span");
        } catch(Exception e){
            e.printStackTrace();
        }
        // 根据画师主页 连接返回document
        Document documentPainterHomePage = null;
        try {
            documentPainterHomePage = CrawlUtils.getConnection(Proto + BasicUrl + painterHomePageUrl);
        } catch (Exception e){
            e.printStackTrace();
        }
        // 3.获取图片信息，包括图片链接和图片名称
        List<String> imgURLList = CrawlUtils.getInfoList(documentPainterHomePage, ImageUrlCssQuery, "img", "src");
        List<String> imgTitleList = CrawlUtils.getInfoList(documentPainterHomePage, ImageTitleCssQuery, "div", "title");
        // 保存为map
        Map<String, String> map = CrawlUtils.listsToMap(imgTitleList, imgURLList);

        // 4.下载到 画师姓名的文件夹
        try {
            CrawlUtils.downloadImages(map, BaseFolder, painterName, suffix);
        }catch (Exception e){
            throw new RuntimeException("下载失败");
        }
    }
}
