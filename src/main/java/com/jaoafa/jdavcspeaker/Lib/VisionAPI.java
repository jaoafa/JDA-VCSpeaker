package com.jaoafa.jdavcspeaker.Lib;

import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;
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
 *
 * @author Tomachi (book000)
 */
public class VisionAPI {
    final String apikey;
    final File file = new File("vision-api.json");

    public VisionAPI(String apikey) throws Exception {
        this.apikey = apikey;

        if (!file.exists()) {
            Files.write(file.toPath(), Collections.singleton(new JSONObject().toString()));
        }
    }

    @Nullable
    private static String getJapaneseDesc(String englishDesc) {
        try {
            File file = new File("vision-api-translate.json");
            JSONObject obj = new JSONObject();
            if (file.exists()) {
                obj = new JSONObject(String.join("\n", Files.readAllLines(file.toPath())));
            }
            if (!obj.has(englishDesc)) {
                obj.put(englishDesc, "");
                Files.write(file.toPath(), Collections.singleton(obj.toString()));
                return null;
            }
            if (obj.getString(englishDesc).isEmpty()) {
                return null;
            }
            return obj.getString(englishDesc);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 画像のラベル情報を取得します。<br />
     * 対象のファイルではない、もしくはリミッターに拒否された場合は null を返します。
     *
     * @param file ラベル情報を取得する画像ファイル
     *
     * @return ラベル情報のリスト
     *
     * @throws IOException IOExceptionが発生した場合
     */
    @Nullable
    public List<Result> getImageLabelOrText(File file) throws IOException {
        String hash = DigestUtils.md5Hex(Files.readAllBytes(file.toPath()));
        List<Result> cache = loadCache(hash);
        if (cache != null) {
            System.out.println("getImageLabel: Used cache");
            return cache;
        }

        if (isLimited()) {
            return null;
        }
        if (!isCheckTarget(file)) {
            return null;
        }

        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        // めんどくさいからJSONそのまま構築する
        String json = """
            {
               "requests": [
                  {
                     "image": {
                        "content": "%s"
                     },
                     "features": [{
                        "type": "LABEL_DETECTION",
                        "maxResults": 3
                     }, {
                        "type": "TEXT_DETECTION",
                        "maxResults": 1
                     }]
                  }
               ]
            }
            """.formatted(base64);

        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=UTF-8"));
        String apiUrl = String.format("https://vision.googleapis.com/v1/images:annotate?key=%s", apikey);
        OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
        Request request = new Request.Builder().url(apiUrl).post(requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            requested();
            ResponseBody body = response.body();
            assert body != null;
            if (response.code() != 200 && response.code() != 302) {
                System.out.println("error: " + response.code() + " -> " + body.string());
                return null;
            }

            JSONObject object = new JSONObject(body.string());

            LinkedList<Result> ret = new LinkedList<>();
            if (object
                .getJSONArray("responses")
                .getJSONObject(0).has("labelAnnotations")) {
                JSONArray labelResults = object
                    .getJSONArray("responses")
                    .getJSONObject(0)
                    .getJSONArray("labelAnnotations");

                for (int i = 0; i < labelResults.length(); i++) {
                    JSONObject result = labelResults.getJSONObject(i);
                    ret.add(new Result(result.getString("description"), result.getDouble("score"), ResultType.LABEL_DETECTION));
                }
            }

            if (object
                .getJSONArray("responses")
                .getJSONObject(0).has("textAnnotations")) {
                JSONArray textResults = object
                    .getJSONArray("responses")
                    .getJSONObject(0)
                    .getJSONArray("textAnnotations");

                if (textResults.length() > 0) {
                    JSONObject result = textResults.getJSONObject(0);
                    ret.add(new Result(result.getString("description"), 1, ResultType.TEXT_DETECTION));
                }
            }

            saveCache(hash, ret, object);
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
        int i = obj.optInt(date, 0) + 2;
        obj.put(date, i);
        Files.write(file.toPath(), Collections.singleton(obj.toString()));
    }

    @Nullable
    public List<Result> loadCache(String hash) throws IOException {
        File file = new File("vision-api-caches", hash);
        if (!file.exists()) {
            return null;
        }
        JSONArray array = new JSONArray(String.join("\n", Files.readAllLines(file.toPath())));
        List<Result> results = new LinkedList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject result = array.getJSONObject(i);
            results.add(new Result(
                result.getString("description"),
                result.getDouble("score"),
                result.optEnum(ResultType.class, "type", ResultType.LABEL_DETECTION)
            ));
        }
        return results;
    }

    public void saveCache(String hash, List<Result> results, JSONObject raw_object) throws IOException {
        File file = new File("vision-api-caches", hash);
        if (!file.getParentFile().exists()) {
            boolean bool = file.getParentFile().mkdirs();
            System.out.println("Created vision-api-caches directory. (" + (bool ? "successful" : "failed") + ")");
        }

        File file_result = new File("vision-api-results", hash);
        if (!file_result.getParentFile().exists()) {
            boolean bool = file_result.getParentFile().mkdirs();
            System.out.println("Created vision-api-results directory. (" + (bool ? "successful" : "failed") + ")");
        }

        JSONArray array = new JSONArray();
        for (Result result : results) {
            JSONObject object = new JSONObject();
            object.put("description", result.getDescription());
            object.put("score", result.getScore());
            object.put("type", result.getType());
            array.put(object);
        }
        Files.write(file.toPath(), Collections.singleton(array.toString()));

        Files.write(file_result.toPath(), Collections.singleton(raw_object.toString()));
    }

    public static List<String> getSupportedContentType() {
        return List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp"
        );
    }

    public boolean isCheckTarget(File file) {
        try {
            String mime = getMimeType(file);
            return getSupportedContentType().contains(mime);
        } catch (IOException e) {
            return false;
        }
    }

    public String getMimeType(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        return URLConnection.guessContentTypeFromStream(is);
    }

    public enum ResultType {
        LABEL_DETECTION,
        TEXT_DETECTION
    }

    public static class Result {
        final String description;
        final String jpDesc;
        final double score;
        final ResultType type;

        @Deprecated
        public Result(String description, double score) {
            this.description = description;
            this.score = score;
            this.jpDesc = getJapaneseDesc(description);
            this.type = ResultType.LABEL_DETECTION;
        }

        public Result(String description, double score, ResultType type) {
            this.description = description;
            this.score = score;
            this.jpDesc = getJapaneseDesc(description);
            this.type = type;
        }

        public String getDescription() {
            return jpDesc != null ? jpDesc : description;
        }

        public String getRawDescription() {
            return description;
        }

        public double getScore() {
            return score;
        }

        public ResultType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Result{" +
                "description='" + description + '\'' +
                ", jpDesc='" + jpDesc + '\'' +
                ", score=" + score +
                ", type=" + type +
                '}';
        }
    }
}
