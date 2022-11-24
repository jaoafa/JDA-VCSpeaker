package com.jaoafa.jdavcspeaker.Lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibTextImg {
    public static File getTempimgPath(String mediaUrl) throws IOException {
        File tmp = File.createTempFile("textimg", ".png");
        Process p;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(List.of("php", "external_scripts/image-text.php", mediaUrl, tmp.getAbsolutePath()));
            builder.redirectErrorStream(true);
            builder.directory(new File("."));
            p = builder.start();
            boolean bool = p.waitFor(3, TimeUnit.MINUTES);
            if (!bool) {
                return null;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(p.getInputStream());
            Stream<String> streamOfString = new BufferedReader(inputStreamReader).lines();
            String streamToString = streamOfString.collect(Collectors.joining("\n"));
            System.out.println(streamToString);
            if (p.exitValue() != 0) {
                return null;
            }
        } catch (InterruptedException e) {
            return null;
        }
        return tmp;
    }
}
