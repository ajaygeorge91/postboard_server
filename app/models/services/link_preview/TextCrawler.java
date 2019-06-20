package models.services.link_preview;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.LinkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextCrawler {

    public static final int ALL = -1;
    public static final int NONE = -2;


    public TextCrawler() {
    }

    public static SourceContent scrape(String url, int imageQty) {
        SourceContent sourceContent = new TextCrawler().new GetCode(imageQty).doInBackground(url);
        return sourceContent;
    }

    /**
     * Get html code
     */
    public class GetCode {

        private SourceContent sourceContent = new SourceContent();
        private int imageQuantity;
        private ArrayList<String> urls;

        public GetCode(int imageQuantity) {
            this.imageQuantity = imageQuantity;
        }

        protected SourceContent doInBackground(String url) {
            // Don't forget the http:// or https://
            urls = SearchUrls.matches(url);

            if (urls.size() > 0)
                sourceContent
                        .setFinalUrl(unshortenUrl(extendedTrim(urls.get(0))));
            else
                sourceContent.setFinalUrl("");

            if (!sourceContent.getFinalUrl().equals("")) {
                if (isImage(sourceContent.getFinalUrl())
                        && !sourceContent.getFinalUrl().contains("dropbox")) {
                    sourceContent.setSuccess(true);

                    sourceContent.getImages().add(sourceContent.getFinalUrl());

                    sourceContent.setTitle("");
                    sourceContent.setDescription("");

                } else {
                    try {
                        Document doc = Jsoup
                                .connect(sourceContent.getFinalUrl())
                                .userAgent("Mozilla").get();

                        sourceContent.setHtmlCode(extendedTrim(doc.toString()));

                        HashMap<String, String> metaTags = getMetaTags(sourceContent
                                .getHtmlCode());

                        sourceContent.setMetaTags(metaTags);

                        sourceContent.setTitle(metaTags.get("title"));
                        sourceContent.setDescription(metaTags
                                .get("description"));

                        if (sourceContent.getTitle().equals("")) {
                            String matchTitle = Regex.pregMatch(
                                    sourceContent.getHtmlCode(),
                                    Regex.TITLE_PATTERN, 2);

                            if (!matchTitle.equals(""))
                                sourceContent.setTitle(htmlDecode(matchTitle));
                        }

                        if (sourceContent.getDescription().equals(""))
                            sourceContent
                                    .setDescription(crawlCode(sourceContent
                                            .getHtmlCode()));

                        sourceContent.setDescription(sourceContent
                                .getDescription().replaceAll(
                                        Regex.SCRIPT_PATTERN, ""));

                        if (imageQuantity != NONE) {
                            if (!metaTags.get("image").equals(""))
                                sourceContent.getImages().add(
                                        metaTags.get("image"));
                            else {
                                sourceContent.setImages(getImages(doc,
                                        imageQuantity));
                            }
                        }

                        sourceContent.setSuccess(true);
                    } catch (Exception e) {
                        sourceContent.setSuccess(false);
                    }
                }
            }

            String[] finalLinkSet = sourceContent.getFinalUrl().split("&");
            sourceContent.setUrl(finalLinkSet[0]);

            sourceContent.setCannonicalUrl(LinkUtils.cannonicalPage(sourceContent
                    .getFinalUrl()));
            sourceContent.setDescription(stripTags(sourceContent
                    .getDescription()));

            return sourceContent;
        }

        /**
         * Verifies if the content could not be retrieved
         */
        public boolean isNull() {
            return !sourceContent.isSuccess() &&
                    extendedTrim(sourceContent.getHtmlCode()).equals("") &&
                    !isImage(sourceContent.getFinalUrl());
        }

    }

    /**
     * Gets content from a html tag
     */
    private String getTagContent(String tag, String content) {

        String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
        String result = "", currentMatch = "";

        List<String> matches = Regex.pregMatchAll(content, pattern, 2);

        int matchesSize = matches.size();
        for (int i = 0; i < matchesSize; i++) {
            currentMatch = stripTags(matches.get(i));
            if (currentMatch.length() >= 120) {
                result = extendedTrim(currentMatch);
                break;
            }
        }

        if (result.equals("")) {
            String matchFinal = Regex.pregMatch(content, pattern, 2);
            result = extendedTrim(matchFinal);
        }

        result = result.replaceAll("&nbsp;", "");

        return htmlDecode(result);
    }

    /**
     * Gets images from the html code
     */
    public List<String> getImages(Document document, int imageQuantity) {
        List<String> matches = new ArrayList<String>();

        Elements media = document.select("[src]");

        for (Element srcElement : media) {
            if (srcElement.tagName().equals("img")) {
                matches.add(srcElement.attr("abs:src"));
            }
        }

        if (imageQuantity != ALL)
            matches = matches.subList(0, imageQuantity);

        return matches;
    }

    /**
     * Transforms from html to normal string
     */
    private String htmlDecode(String content) {
        return Jsoup.parse(content).text();
    }

    /**
     * Crawls the code looking for relevant information
     */
    private String crawlCode(String content) {
        String result = "";
        String resultSpan = "";
        String resultParagraph = "";
        String resultDiv = "";

        resultSpan = getTagContent("span", content);
        resultParagraph = getTagContent("p", content);
        resultDiv = getTagContent("div", content);

        result = resultSpan;

        if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() >= resultDiv.length())
            result = resultParagraph;
        else if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() < resultDiv.length())
            result = resultDiv;
        else
            result = resultParagraph;

        return htmlDecode(result);
    }

    /**
     * Strips the tags from an element
     */
    private String stripTags(String content) {
        return Jsoup.parse(content).text();
    }

    /**
     * Verifies if the url is an image
     */
    private boolean isImage(String url) {
        return url.matches(Regex.IMAGE_PATTERN);
    }

    /**
     * Returns meta tags from html code
     */
    private HashMap<String, String> getMetaTags(String content) {

        HashMap<String, String> metaTags = new HashMap<String, String>();
        metaTags.put("url", "");
        metaTags.put("title", "");
        metaTags.put("description", "");
        metaTags.put("image", "");

        List<String> matches = Regex.pregMatchAll(content,
                Regex.METATAG_PATTERN, 1);

        for (String match : matches) {
            if (match.toLowerCase().contains("property=\"og:url\"")
                    || match.toLowerCase().contains("property='og:url'")
                    || match.toLowerCase().contains("name=\"url\"")
                    || match.toLowerCase().contains("name='url'"))
                metaTags.put("url", separeMetaTagsContent(match));
            else if (match.toLowerCase().contains("property=\"og:title\"")
                    || match.toLowerCase().contains("property='og:title'")
                    || match.toLowerCase().contains("name=\"title\"")
                    || match.toLowerCase().contains("name='title'"))
                metaTags.put("title", separeMetaTagsContent(match));
            else if (match.toLowerCase()
                    .contains("property=\"og:description\"")
                    || match.toLowerCase()
                    .contains("property='og:description'")
                    || match.toLowerCase().contains("name=\"description\"")
                    || match.toLowerCase().contains("name='description'"))
                metaTags.put("description", separeMetaTagsContent(match));
            else if (match.toLowerCase().contains("property=\"og:image\"")
                    || match.toLowerCase().contains("property='og:image'")
                    || match.toLowerCase().contains("name=\"image\"")
                    || match.toLowerCase().contains("name='image'"))
                metaTags.put("image", separeMetaTagsContent(match));
        }

        return metaTags;
    }

    /**
     * Gets content from metatag
     */
    private String separeMetaTagsContent(String content) {
        String result = Regex.pregMatch(content, Regex.METATAG_CONTENT_PATTERN,
                1);
        return htmlDecode(result);
    }

    /**
     * Unshortens a short url
     */
    public static String unshortenUrl(String shortURL) {
        if (!shortURL.startsWith(LinkUtils.HTTP_PROTOCOL())
                && !shortURL.startsWith(LinkUtils.HTTPS_PROTOCOL()))
            return "";

        URLConnection urlConn = connectURL(shortURL);
        urlConn.getHeaderFields();

        String finalResult = urlConn.getURL().toString();

        urlConn = connectURL(finalResult);
        urlConn.getHeaderFields();

        shortURL = urlConn.getURL().toString();

        while (!shortURL.equals(finalResult)) {
            finalResult = unshortenUrl(finalResult);
        }

        return finalResult;
    }

    /**
     * Takes a valid url and return a URL object representing the url address.
     */
    private static URLConnection connectURL(String strURL) {
        URLConnection conn = null;
        try {
            URL inputURL = new URL(strURL);
            conn = inputURL.openConnection();
        } catch (MalformedURLException e) {
            System.out.println("Please input a valid URL");
        } catch (IOException ioe) {
            System.out.println("Can not connect to the URL");
        }
        return conn;
    }

    /**
     * Removes extra spaces and trim the string
     */
    public static String extendedTrim(String content) {
        return content.replaceAll("\\s+", " ").replace("\n", " ")
                .replace("\r", " ").trim();
    }



    /**
     * Returns the cannoncial url
     */
    public static String cannonicalPage (String url) {

        String cannonical = "";
        if (url.startsWith(LinkUtils.HTTP_PROTOCOL())) {
            url = url.substring(LinkUtils.HTTP_PROTOCOL().length());
        } else if (url.startsWith(LinkUtils.HTTPS_PROTOCOL())) {
            url = url.substring(LinkUtils.HTTPS_PROTOCOL().length());
        }

        int urlLength = url.length();
        for (int i = 0; i < urlLength; i ++ ) {
            if (url.charAt(i) != '/')
                cannonical += url.charAt(i);
            else
                break;
        }

        return cannonical;

    }



}
