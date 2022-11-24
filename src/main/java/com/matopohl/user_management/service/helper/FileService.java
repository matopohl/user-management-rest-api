package com.matopohl.user_management.service.helper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Component
public class FileService {

    Lock lock = new ReentrantLock();

    @Value("${my.cache.profile-images.treshold}")
    private int cacheProfileImagesTreshold;

    @Value("${my.cache.profile-images.download}")
    private int cacheProfileImagesDownload;

    private static final String SUCCESS = "SUCCESS-";
    private static final String ACTIVE = "ACTIVE-";

    public Optional<Path> getActiveContentFromDir(String dir) throws IOException {
        Path dirPath = Paths.get(dir);

        Files.createDirectories(dirPath);

        Optional<Path> file;

        try(Stream<Path> stream = Files.list(dirPath).filter(f -> f.getFileName().toString().startsWith(ACTIVE))) {
            file = stream.findFirst();
        }

        return file;
    }

    public void saveFile(String dir, byte[] content) throws IOException {
        Optional<Path> activeFile = getActiveContentFromDir(dir);

        if(activeFile.isPresent()) {
            Path findPath = activeFile.get().toAbsolutePath();
            Path sourcePath = Paths.get(dir + findPath.getFileName().toString().substring(ACTIVE.length()));

            Files.move(findPath, sourcePath);
        }

        InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));
        String mimeType = URLConnection.guessContentTypeFromStream(is);
        String extension = mimeType.substring(mimeType.indexOf("/") + 1);
        String newFileName = ACTIVE + UUID.randomUUID() + "." + extension;

        String filePath = dir + newFileName;

        Files.createDirectories(Paths.get(dir));

        Path path = Paths.get(filePath);

        Files.write(path, content);
    }

    public byte[] generateFile(String dir, String cacheDir, String sourceUrl, String extension, boolean cache) throws IOException {
        Path dirPath = Paths.get(dir);
        Path cacheDirPath = Paths.get(cacheDir);

        Files.createDirectories(dirPath);
        Files.createDirectories(cacheDirPath);

        byte[] result;

        lock.lock();

        try (Stream<Path> stream = Files.list(cacheDirPath).filter(f -> f.getFileName().toString().startsWith(SUCCESS))) {
            Optional<Path> file = stream.findFirst();

            if (file.isPresent()) {
                Path findPath = file.get().toAbsolutePath();
                Path sourcePath = Paths.get(dir + ACTIVE + findPath.getFileName().toString().substring(SUCCESS.length()));

                Files.move(findPath, sourcePath);

                result = Files.readAllBytes(sourcePath);
            } else {
                result = downloadFile(dir, ACTIVE + UUID.randomUUID(), sourceUrl, extension).getContent();
            }
        }

        if (cache) {
            try (Stream<Path> stream = Files.list(cacheDirPath).filter(f -> f.getFileName().toString().startsWith(SUCCESS))) {
                if (stream.count() < cacheProfileImagesTreshold) {
                    cacheFiles(cacheDir, sourceUrl, extension);
                }
            }
        }

        lock.unlock();

        return result;
    }

    private DownloadedFile downloadFile(String dir, String fileName, String sourceUrl, String extension) throws IOException {
        URL url = new URL(sourceUrl + fileName);

        try(InputStream reader = url.openConnection().getInputStream();
            FileOutputStream fileWriter = new FileOutputStream(dir + "/" + fileName + "." + extension);
            ByteArrayOutputStream byteWriter = new ByteArrayOutputStream()) {

            final byte[] buffer = new byte[1024];

            for (int count; (count = reader.read(buffer)) > 0;) {
                fileWriter.write(buffer, 0, count);
                byteWriter.write(buffer, 0, count);
            }

            return new DownloadedFile().setContent(byteWriter.toByteArray()).setName(fileName + "." + extension).setDir(dir);
        }
    }

    private void cacheFiles(String cacheDir, String sourceUrl, String extension) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1));

        for (int i = 0; i < cacheProfileImagesDownload; i++) {
            executor.submit(new DownloadFileThread(cacheDir, sourceUrl, extension));
        }

        executor.shutdown();
    }

    private class DownloadFileThread implements Callable<DownloadedFile> {

        private final String cacheDir;
        private final String sourceUrl;
        private final String extension;

        public DownloadFileThread(String cacheDir, String sourceUrl, String extension) {
            this.cacheDir = cacheDir;
            this.sourceUrl = sourceUrl;
            this.extension = extension;
        }

        @Override
        public DownloadedFile call() throws Exception {
            String fileName = UUID.randomUUID().toString();

            DownloadedFile result = downloadFile(cacheDir, fileName, sourceUrl, extension);

            Files.move(Paths.get(cacheDir + result.getName()), Paths.get(cacheDir + SUCCESS + result.getName()));

            return result;
        }

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    private static class DownloadedFile {

        private byte[] content;
        private String name;
        private String dir;

    }

}
