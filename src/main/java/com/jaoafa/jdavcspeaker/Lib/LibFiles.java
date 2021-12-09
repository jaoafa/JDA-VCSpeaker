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
            if (!Files.exists(path.getPath())) {
                continue;
            }

        }
    }

    enum VFile {
        CONFIG("config.json"),
        SERVERS("settings", "servers.json"),
        USER_DEFAULT_PARAMS("settings", "user-default-params.json"),
        ALIAS("settings", "alias.json"),
        IGNORE("settings", "ignore.json"),
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
         * このファイルにJSONObjectを書き込みます
         *
         * @param json 書き込むJSONObject
         *
         * @return 書き込めたかどうか
         */
        @CheckReturnValue
        public boolean write(@Nonnull JSONObject json) {
            try {
                Files.writeString(path, json.toString());
                return true;
            } catch (IOException e) {
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

    enum VDirectory {
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
        FILE_VISION_API(Path.of("vision-api.json"), VFile.VISION_API),
        FILE_VISION_API_TRANSLATE(Path.of("vision-api-translate.json"), VFile.VISION_API_TRANSLATE),
        // external_scripts: 変更なし
        DIR_VOICETEXT_CACHES(Path.of("Temp"), VDirectory.VOICETEXT_CACHES),
        DIR_VISION_API_CACHES(Path.of("tmp"), VDirectory.VISION_API_TEMP),
        VISION_API_CACHES(Path.of("vision-api-caches"), VDirectory.VISION_API_CACHES),
        DIR_VISION_API_RESULTS(Path.of("vision-api-results"), VDirectory.VISION_API_RESULTS);

        private final Path path;
        private final boolean isFile;
        private final VFile file;
        private final VDirectory directory;

        VersionUpgradePaths(Path path, VFile file) {
            this.path = path;
            this.isFile = true;
            this.file = file;
            this.directory = null;
        }

        VersionUpgradePaths(Path path, VDirectory directory) {
            this.path = path;
            this.isFile = false;
            this.file = null;
            this.directory = directory;
        }

        public Path getPath() {
            return path;
        }

        public boolean isFile() {
            return isFile;
        }

        public VFile getFile() {
            return file;
        }

        public VDirectory getDirectory() {
            return directory;
        }
    }
}
