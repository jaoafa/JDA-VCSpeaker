package com.jaoafa.jdavcspeaker.Lib;

import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Vision APIを使用した画像の文章化
 */
public class VisionAPI {
    String apikey;
    File file = new File("vision-api.json");
    public VisionAPI(String apikey) throws IOException {
        this.apikey = apikey;

        if(!file.exists()){
            Files.write(file.toPath(), Collections.singleton(new JSONObject().toString()));
        }
    }

    /**
     * 画像のラベル情報を取得します。<br />
     * 対象のファイルではない、もしくはリミッターに拒否された場合は null を返します。
     *
     * @param file ラベル情報を取得する画像ファイル
     * @return ラベル情報のリスト
     * @throws IOException IOExceptionが発生した場合
     */
    public List<Result> getImageLabel(File file) throws IOException {
        if(isLimited()){
            return null;
        }
        if(!isCheckTarget(file)){
            return null;
        }

        String hash = DigestUtils.md5Hex(Files.readAllBytes(file.toPath()));
        List<Result> cache = loadCache(hash);
        if(cache != null){
            System.out.println("getImageLabel: Used cache");
            return cache;
        }

        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        // めんどくさいからJSONそのまま構築する
        String json = "{\n" +
            "    \"requests\": [\n" +
            "        {\n" +
            "            \"image\": {\n" +
            "                \"content\": \"" + base64 + "\"\n" +
            "            },\n" +
            "            \"features\": {\n" +
            "                \"type\": \"LABEL_DETECTION\",\n" +
            "                \"maxResults\": 3\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=UTF-8"));
        String apiurl = String.format("https://vision.googleapis.com/v1/images:annotate?key=%s", apikey);
        OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
        Request request = new Request.Builder().url(apiurl).post(requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            requested();
            if (response.code() != 200 && response.code() != 302) {
                return null;
            }
            ResponseBody body = response.body();
            assert body != null;
            JSONArray results = new JSONObject(body.string())
                .getJSONArray("responses")
                .getJSONObject(0)
                .getJSONArray("labelAnnotations");

            LinkedList<Result> ret = new LinkedList<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                ret.add(new Result(result.getString("description"), result.getDouble("score")));
            }
            saveCache(hash, ret);
            System.out.println("getImageLabel: Saved cache");
            return ret;
        }
    }

    private boolean isLimited() throws IOException {
        return getRequestCount() >= 950; // 1000 units だけど余裕をもって
    }

    private int getRequestCount() throws IOException {
        JSONObject obj = new JSONObject(String.join("\n", Files.readAllLines(file.toPath())));
        return obj.optInt(new SimpleDateFormat("yyyy/MM").format(new Date()), 0);
    }

    private void requested() throws IOException {
        String date = new SimpleDateFormat("yyyy/MM").format(new Date());
        JSONObject obj = new JSONObject(String.join("\n", Files.readAllLines(file.toPath())));
        int i = obj.optInt(date, 0) + 1;
        obj.put(date, i);
        Files.write(file.toPath(), Collections.singleton(obj.toString()));
    }

    public List<Result> loadCache(String hash) throws IOException {
        File file = new File("vision-api-caches", hash);
        if(!file.exists()){
            return null;
        }
        JSONArray array = new JSONArray(String.join("\n", Files.readAllLines(file.toPath())));
        List<Result> results = new LinkedList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject result = array.getJSONObject(i);
            results.add(new Result(
                result.getString("description"),
                result.getDouble("score")
            ));
        }
        return results;
    }

    public void saveCache(String hash, List<Result> results) throws IOException {
        File file = new File("vision-api-caches", hash);
        if(!file.getParentFile().exists()){
            boolean bool = file.getParentFile().mkdirs();
            System.out.println("Created vision-api-caches directory. (" + (bool ? "successful" : "failed") + ")");
        }
        JSONArray array = new JSONArray();
        for(Result result : results){
            JSONObject object = new JSONObject();
            object.put("description", result.getDescription());
            object.put("score", result.getScore());
            array.put(object);
        }
        Files.write(file.toPath(), Collections.singleton(array.toString()));
    }

    public boolean isCheckTarget(File file){
        List<String> targets = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp"
        );
        try{
            String mime = getMimeType(file);
            return targets.contains(mime);
        }catch (IOException e){
            return false;
        }
    }

    public String getMimeType(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        return URLConnection.guessContentTypeFromStream(is);
    }

    public static class Result {
        String description;
        double score;

        public Result(String description, double score){
            this.description = description;
            this.score = score;
        }

        public String getDescription() {
            return description;
        }

        public double getScore() {
            return score;
        }
    }
}
