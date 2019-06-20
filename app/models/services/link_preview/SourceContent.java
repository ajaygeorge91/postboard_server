package models.services.link_preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SourceContent {

    private boolean success = false;
    private String htmlCode = "";
    private String raw = "";
    private String title = "";
    private String description = "";
    private String url = "";
    private String finalUrl = "";
    private String cannonicalUrl = "";
    private HashMap<String, String> metaTags = new HashMap<String, String>();

    private List<String> images = new ArrayList<String>();
    private String[] urlData = new String[2];

    public SourceContent() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        this.htmlCode = htmlCode;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public void setFinalUrl(String finalUrl) {
        this.finalUrl = finalUrl;
    }

    public String getCannonicalUrl() {
        return cannonicalUrl;
    }

    public void setCannonicalUrl(String cannonicalUrl) {
        this.cannonicalUrl = cannonicalUrl;
    }

    public HashMap<String, String> getMetaTags() {
        return metaTags;
    }

    public void setMetaTags(HashMap<String, String> metaTags) {
        this.metaTags = metaTags;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String[] getUrlData() {
        return urlData;
    }

    public void setUrlData(String[] urlData) {
        this.urlData = urlData;
    }

    @Override
    public String toString() {
        return "SourceContent{" +
                "success=" + success +
                ", htmlCode='" + "....truncated...." + '\'' +
                ", raw='" + raw + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", finalUrl='" + finalUrl + '\'' +
                ", cannonicalUrl='" + cannonicalUrl + '\'' +
                ", metaTags=" + metaTags +
                ", images=" + images +
                ", urlData=" + Arrays.toString(urlData) +
                '}';
    }
}
