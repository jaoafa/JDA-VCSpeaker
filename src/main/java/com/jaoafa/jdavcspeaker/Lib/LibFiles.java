package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LibFiles {
    public static void moveDirFiles() {
        for (VersionUpgradePaths path : VersionUpgradePaths.values()) {
            if (!Files.exists(path.getOldPath())) {
                continue;
            }
            Path oldPath = path.getOldPath();
            try {
                new LibFlow("LibFiles.moveDirFiles")
                    .action("Moving " + path.name());
                if (path.getNewPath().getParent() != null && !Files.exists(path.getNewPath().getParent())) {
                    Files.createDirectories(path.getNewPath().getParent());
                }
                Files.move(oldPath, path.getNewPath());
                new LibFlow("LibFiles.moveDirFiles")
                    .action("Moved " + path.name());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public enum VFile {
        CONFIG("config.json"),
        SERVERS("settings", "servers.json"),
        USER_DEFAULT_PARAMS("settings", "user-default-params.json"),
        ALIAS("settings", "alias.json"),
        IGNORE("settings", "ignore.json"),
        TITLE("settings", "title.json"),
        VISION_API("settings", "vision-api.json"),
        VISION_API_TRANSLATE("settings", "vision-api-translate.json");

        private final Path path;

        VFile(String first, String... more) {
            path = Path.of(first, more);
        }

        /**
         * このファイルのPathを返します
         *
         * @return このファイルのPath
         */
        public Path getPath() {
            return path;
        }

        /**
         * このファイルが存在するかを返します
         *
         * @return このファイルが存在すれば true
         */
        public boolean exists() {
            return Files.exists(path);
        }

        /**
         * このファイルにJSONObjectを書き込みます
         *
         * @param json 書き込むJSONObject
         *
         * @return 書き込めたかどうか
         */
        public boolean write(@Nonnull JSONObject json) {
            try {
                Files.writeString(path, json.toString());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * このファイルにJSONArrayを書き込みます
         *
         * @param json 書き込むJSONArray
         *
         * @return 書き込めたかどうか
         */
        @CheckReturnValue
        public boolean write(@Nonnull JSONArray json) {
            try {
                Files.writeString(path, json.toString());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * ファイルの内容をJSONObjectとして返します
         *
         * @return ファイルの内容 (読み込めない場合はnull)
         */
        @CheckReturnValue
        @Nullable
        public JSONObject readJSONObject() {
            try {
                return new JSONObject(Files.readString(path));
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * ファイルの内容をJSONObjectとして返します。読み込めない場合はdefaultValueを返します
         *
         * @param defaultValue 読み込めない場合に使用するデフォルト値
         *
         * @return ファイルの内容 (読み込めない場合はdefaultValueの値)
         */
        @CheckReturnValue
        @Nonnull
        public JSONObject readJSONObject(@Nonnull JSONObject defaultValue) {
            try {
                return new JSONObject(Files.readString(path));
            } catch (IOException e) {
                return defaultValue;
            }
        }

        /**
         * ファイルの内容をJSONArrayとして返します
         *
         * @return ファイルの内容 (読み込めない場合はnull)
         */
        @CheckReturnValue
        @Nullable
        public JSONArray readJSONArray() {
            try {
                return new JSONArray(Files.readString(path));
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * ファイルの内容をJSONArrayとして返します。読み込めない場合はdefaultValueを返します
         *
         * @param defaultValue 読み込めない場合に使用するデフォルト値
         *
         * @return ファイルの内容 (読み込めない場合はdefaultValueの値)
         */
        @CheckReturnValue
        @Nonnull
        public JSONArray readJSONArray(@Nonnull JSONArray defaultValue) {
            try {
                return new JSONArray(Files.readString(path));
            } catch (IOException e) {
                return defaultValue;
            }
        }
    }

    public enum VDirectory {
        EXTERNAL_SCRIPTS("external_scripts"),
        VOICETEXT_CACHES("voicetext-caches"),
        VISION_API_TEMP("vision-api", "temp"),
        VISION_API_CACHES("vision-api", "caches"),
        VISION_API_RESULTS("vision-api", "results");

        private final Path path;

        VDirectory(String first, String... more) {
            path = Path.of(first, more);
        }

        /**
         * このディレクトリのPathを返します
         *
         * @return このディレクトリのPath
         */
        public Path getPath() {
            return path;
        }

        /**
         * 指定されたパスを使ってパスを解決し返します
         *
         * @param p 解決に使うパス
         *
         * @return 解決されたパス
         */
        public Path resolve(Path p) {
            return path.resolve(p);
        }

        /**
         * このディレクトリが存在するかを返します
         *
         * @return このディレクトリが存在すれば true
         */
        public boolean exists() {
            return Files.exists(path);
        }

        /**
         * このディレクトリに指定したファイルパスが存在するかを返します
         *
         * @return このディレクトリ指定したファイルパスが存在すれば true
         */
        public boolean exists(Path filePath) {
            return Files.exists(path.resolve(filePath));
        }

        /**
         * ディレクトリを作成します。(ディレクトリが存在する場合は作成せず true を返します)
         */
        public void mkdirs() {
            if (Files.exists(path)) {
                return;
            }
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * このディレクトリにファイルを作成し、JSONObjectを書き込みます。既存のファイルが存在する場合は上書きします
         *
         * @param file 作成・書き込むファイルパス
         * @param json 書き込むJSONObject
         *
         * @return 書き込めたかどうか
         */
        @CheckReturnValue
        public boolean writeFile(@Nonnull Path file, @Nonnull JSONObject json) {
            try {
                Path filePath = path.resolve(file);
                Files.writeString(filePath, json.toString());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * このディレクトリにファイルを作成し、JSONArrayを書き込みます。既存のファイルが存在する場合は上書きします
         *
         * @param file 作成・書き込むファイルパス
         * @param json 書き込むJSONArray
         *
         * @return 書き込めたかどうか
         */
        @CheckReturnValue
        public boolean writeFile(@Nonnull Path file, @Nonnull JSONArray json) {
            try {
                Path filePath = path.resolve(file);
                Files.writeString(filePath, json.toString());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * このディレクトリにあるファイルの内容をJSONObjectとして返します
         *
         * @param file 読み込むファイルパス
         *
         * @return ファイルの内容 (読み込めない場合はnull)
         */
        @CheckReturnValue
        @Nullable
        public JSONObject readJSONObject(@Nonnull Path file) {
            try {
                Path filePath = path.resolve(file);
                return new JSONObject(Files.readString(filePath));
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * このディレクトリにあるファイルの内容をJSONObjectとして返します。読み込めない場合はdefaultValueを返します
         *
         * @param file         読み込むファイルパス
         * @param defaultValue 読み込めない場合に使用するデフォルト値
         *
         * @return ファイルの内容 (読み込めない場合はdefaultValueの値)
         */
        @CheckReturnValue
        @Nonnull
        public JSONObject readJSONObject(@Nonnull Path file, @Nonnull JSONObject defaultValue) {
            try {
                Path filePath = path.resolve(file);
                return new JSONObject(Files.readString(filePath));
            } catch (IOException e) {
                return defaultValue;
            }
        }

        /**
         * このディレクトリにあるファイルの内容をJSONArrayとして返します
         *
         * @param file 読み込むファイルパス
         *
         * @return ファイルの内容 (読み込めない場合はnull)
         */
        @CheckReturnValue
        @Nullable
        public JSONArray readJSONArray(@Nonnull Path file) {
            try {
                Path filePath = path.resolve(file);
                return new JSONArray(Files.readString(filePath));
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * このディレクトリにあるファイルの内容をJSONArrayとして返します。読み込めない場合はdefaultValueを返します
         *
         * @param file         読み込むファイルパス
         * @param defaultValue 読み込めない場合に使用するデフォルト値
         *
         * @return ファイルの内容 (読み込めない場合はdefaultValueの値)
         */
        @CheckReturnValue
        @Nonnull
        public JSONArray readJSONArray(@Nonnull Path file, JSONArray defaultValue) {
            try {
                Path filePath = path.resolve(file);
                return new JSONArray(Files.readString(filePath));
            } catch (IOException e) {
                return defaultValue;
            }
        }
    }

    enum VersionUpgradePaths {
        FILE_CONFIG(Path.of("VCSpeaker.json"), VFile.CONFIG),
        FILE_SERVERS(Path.of("servers.json"), VFile.SERVERS),
        FILE_USER_DEFAULT_PARAMS(Path.of("user-default-params.json"), VFile.USER_DEFAULT_PARAMS),
        FILE_ALIAS(Path.of("alias.json"), VFile.ALIAS),
        FILE_IGNORE(Path.of("ignore.json"), VFile.IGNORE),
        FILE_TITLE(Path.of("title.json"), VFile.TITLE),
        FILE_VISION_API(Path.of("vision-api.json"), VFile.VISION_API),
        FILE_VISION_API_TRANSLATE(Path.of("vision-api-translate.json"), VFile.VISION_API_TRANSLATE),
        // external_scripts: 変更なし
        DIR_VOICETEXT_CACHES(Path.of("Temp"), VDirectory.VOICETEXT_CACHES),
        DIR_VISION_API_CACHES(Path.of("tmp"), VDirectory.VISION_API_TEMP),
        VISION_API_CACHES(Path.of("vision-api-caches"), VDirectory.VISION_API_CACHES),
        DIR_VISION_API_RESULTS(Path.of("vision-api-results"), VDirectory.VISION_API_RESULTS);

        private final Path oldPath;
        private final boolean isFile;
        private final Path newPath;

        VersionUpgradePaths(Path oldPath, VFile file) {
            this.oldPath = oldPath;
            this.isFile = true;
            this.newPath = file.getPath();
        }

        VersionUpgradePaths(Path oldPath, VDirectory directory) {
            this.oldPath = oldPath;
            this.isFile = false;
            this.newPath = directory.getPath();
        }

        /**
         * 古いパスを返します
         *
         * @return 古いパス
         */
        public Path getOldPath() {
            return oldPath;
        }

        /**
         * ファイルかどうかを返します
         *
         * @return ファイルかどうか
         */
        public boolean isFile() {
            return isFile;
        }

        /**
         * 新しいパスを返します
         *
         * @return 新しいパス
         */
        public Path getNewPath() {
            return newPath;
        }
    }
}
