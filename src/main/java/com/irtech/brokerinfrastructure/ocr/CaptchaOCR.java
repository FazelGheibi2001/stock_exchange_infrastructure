package com.irtech.brokerinfrastructure.ocr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class CaptchaOCR {

    /*
     * برای تصاویر خیلی کوچک مثل نمونه شما،
     * بزرگ‌نمایی زیاد باعث عملکرد بهتر Tesseract می‌شود.
     */
    private static final int IMAGE_SCALE = 5;

    private static final int BORDER_SIZE = 25;

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

    private CaptchaOCR() {
    }

    /**
     * مثال:
     * <p>
     * long number = CaptchaOCR.readNumber(imageAddress, 5);
     * <p>
     * imageAddress:
     * <p>
     * data:image/jpeg;base64,...
     * <p>
     * یا:
     * <p>
     * https://example.com/image.jpg
     * <p>
     * expectedDigits:
     * <p>
     * 5
     */
    public static int readNumber(
            String imageAddress,
            int expectedDigits
    ) {

        if (imageAddress == null || imageAddress.isBlank()) {
            throw new IllegalArgumentException(
                    "imageAddress نمی‌تواند خالی باشد"
            );
        }

        if (expectedDigits <= 0 || expectedDigits > 18) {
            throw new IllegalArgumentException(
                    "تعداد رقم نامعتبر است: " + expectedDigits
            );
        }

        List<Path> tempFiles = new ArrayList<>();

        try {

            /*
             * =================================
             * 1. Load image
             * =================================
             */

            BufferedImage original =
                    loadImage(imageAddress.trim());

            if (original == null) {
                throw new IllegalArgumentException(
                        "تصویر قابل خواندن نیست"
                );
            }

            /*
             * =================================
             * 2. حذف border مشکی
             * =================================
             */

            BufferedImage cropped =
                    trimDarkBorder(original);

            /*
             * =================================
             * 3. grayscale
             * =================================
             */

            BufferedImage gray =
                    toGray(cropped);

            /*
             * =================================
             * 4. تشخیص زاویه متن
             * =================================
             */

            double skewAngle =
                    detectSkewAngle(
                            gray,
                            expectedDigits
                    );

            /*
             * =================================
             * 5. صاف کردن تصویر
             * =================================
             */

            BufferedImage deskewed =
                    rotateAndAddBorder(
                            gray,
                            -skewAngle
                    );

            /*
             * =================================
             * 6. بزرگ کردن تصویر
             * =================================
             */

            BufferedImage scaled =
                    scaleImage(
                            deskewed,
                            IMAGE_SCALE
                    );

            /*
             * =================================
             * 7. پیدا کردن threshold مناسب
             * =================================
             */

            int otsu =
                    calculateOtsuThreshold(scaled);

            /*
             * چند حالت مختلف امتحان می‌کنیم.
             *
             * این قسمت دقت OCR را روی تصاویر
             * بی‌کیفیت خیلی بهتر می‌کند.
             */
            int[] thresholdOffsets = {
                    -10,
                    0,
                    8,
                    15,
                    22
            };

            List<OcrCandidate> candidates =
                    new ArrayList<>();

            for (int offset : thresholdOffsets) {

                int threshold =
                        clamp(
                                otsu + offset,
                                0,
                                255
                        );

                BufferedImage binary =
                        threshold(
                                scaled,
                                threshold
                        );

                Path file =
                        Files.createTempFile(
                                "number-ocr-",
                                ".png"
                        );

                tempFiles.add(file);

                ImageIO.write(
                        binary,
                        "png",
                        file.toFile()
                );

                /*
                 * PSM 7:
                 *
                 * تصویر یک خط متن دارد.
                 */
                OcrCandidate candidate =
                        executeTesseract(
                                file,
                                expectedDigits,
                                7
                        );

                if (candidate != null) {
                    candidates.add(candidate);
                }
            }

            /*
             * =================================
             * 8. اگر چیزی پیدا نشد
             *
             * یک حالت اضافی بدون binarize
             * امتحان می‌کنیم.
             * =================================
             */

            if (candidates.isEmpty()) {

                Path grayFile =
                        Files.createTempFile(
                                "number-ocr-gray-",
                                ".png"
                        );

                tempFiles.add(grayFile);

                ImageIO.write(
                        scaled,
                        "png",
                        grayFile.toFile()
                );

                OcrCandidate candidate =
                        executeTesseract(
                                grayFile,
                                expectedDigits,
                                7
                        );

                if (candidate != null) {
                    candidates.add(candidate);
                }
            }

            /*
             * =================================
             * 9. انتخاب بهترین نتیجه
             * =================================
             */

            if (candidates.isEmpty()) {

                throw new IllegalStateException(
                        "هیچ عدد " +
                                expectedDigits +
                                " رقمی در تصویر تشخیص داده نشد"
                );
            }

            OcrCandidate best =
                    candidates.stream()
                            .max(
                                    Comparator.comparingDouble(
                                            OcrCandidate::confidence
                                    )
                            )
                            .orElseThrow();

            /*
             * برای debug می‌توانی این را موقتاً فعال کنی:
             *
             * System.out.println(
             *     "OCR = " + best.number()
             *     + ", confidence = "
             *     + best.confidence()
             *     + ", angle = "
             *     + skewAngle
             * );
             */

            return Integer.parseInt(
                    best.number()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "خطا در تشخیص عدد از تصویر: "
                            + e.getMessage(),
                    e
            );

        } finally {

            /*
             * حذف فایل‌های موقت
             */
            for (Path path : tempFiles) {

                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                }
            }
        }
    }


    /**
     * ==========================================
     * اجرای Tesseract و گرفتن confidence
     * ==========================================
     */
    private static OcrCandidate executeTesseract(
            Path imagePath,
            int expectedDigits,
            int psm
    ) throws Exception {

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "tesseract",

                        imagePath
                                .toAbsolutePath()
                                .toString(),

                        "stdout",

                        /*
                         * چون تصاویر نمونه فقط
                         * رقم انگلیسی دارند،
                         * eng دقیق‌تر از fas+eng است.
                         */
                        "-l",
                        "eng",

                        "--psm",
                        String.valueOf(psm),

                        "-c",
                        "tessedit_char_whitelist=0123456789",

                        /*
                         * خروجی TSV برای گرفتن confidence
                         */
                        "tsv"
                );

        Process process;

        try {

            process =
                    processBuilder.start();

        } catch (IOException e) {

            throw new IllegalStateException(
                    """
                            دستور tesseract روی سیستم پیدا نشد.
                            
                            Ubuntu/Debian:
                            
                            sudo apt update
                            sudo apt install -y tesseract-ocr tesseract-ocr-eng
                            """,
                    e
            );
        }

        CompletableFuture<String> stdout =
                CompletableFuture.supplyAsync(
                        () -> readStream(
                                process.getInputStream()
                        )
                );

        CompletableFuture<String> stderr =
                CompletableFuture.supplyAsync(
                        () -> readStream(
                                process.getErrorStream()
                        )
                );

        boolean finished =
                process.waitFor(
                        20,
                        TimeUnit.SECONDS
                );

        if (!finished) {

            process.destroyForcibly();

            throw new IllegalStateException(
                    "زمان اجرای Tesseract بیش از حد مجاز شد"
            );
        }

        String output =
                stdout.join();

        String error =
                stderr.join();

        if (process.exitValue() != 0) {

            throw new IllegalStateException(
                    "Tesseract error: " + error
            );
        }

        return parseTsvResult(
                output,
                expectedDigits
        );
    }


    /**
     * ==========================================
     * خواندن خروجی TSV
     * ==========================================
     */
    private static OcrCandidate parseTsvResult(
            String tsv,
            int expectedDigits
    ) {

        if (tsv == null || tsv.isBlank()) {
            return null;
        }

        OcrCandidate best = null;

        String[] lines =
                tsv.split("\\R");

        for (String line : lines) {

            String[] columns =
                    line.split(
                            "\t",
                            -1
                    );

            /*
             * TSV Tesseract:
             *
             * column 10 = confidence
             * column 11 = text
             */
            if (columns.length < 12) {
                continue;
            }

            String text =
                    normalizeDigits(
                            columns[11]
                    );

            if (text.length() != expectedDigits) {
                continue;
            }

            if (!text.matches("\\d+")) {
                continue;
            }

            double confidence;

            try {

                confidence =
                        Double.parseDouble(
                                columns[10]
                        );

            } catch (NumberFormatException e) {

                continue;
            }

            if (confidence < 0) {
                continue;
            }

            OcrCandidate candidate =
                    new OcrCandidate(
                            text,
                            confidence
                    );

            if (best == null
                    || candidate.confidence()
                    > best.confidence()) {

                best = candidate;
            }
        }

        return best;
    }


    /**
     * ==========================================
     * تشخیص زاویه عدد
     * ==========================================
     * <p>
     * مثلاً تصویر نمونه حدود 15 درجه
     * چرخیده است.
     */
    private static double detectSkewAngle(
            BufferedImage image,
            int expectedDigits
    ) {

        int threshold =
                calculateOtsuThreshold(image);

        int width =
                image.getWidth();

        int height =
                image.getHeight();

        boolean[][] foreground =
                new boolean[height][width];

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int gray =
                        getGray(
                                image,
                                x,
                                y
                        );

                foreground[y][x] =
                        gray < threshold;
            }
        }

        boolean[][] visited =
                new boolean[height][width];

        List<Component> components =
                new ArrayList<>();

        int[] dx = {
                -1, 0, 1,
                -1, 1,
                -1, 0, 1
        };

        int[] dy = {
                -1, -1, -1,
                0, 0,
                1, 1, 1
        };

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                if (!foreground[y][x]
                        || visited[y][x]) {

                    continue;
                }

                ArrayDeque<Point> queue =
                        new ArrayDeque<>();

                queue.add(
                        new Point(x, y)
                );

                visited[y][x] = true;

                int area = 0;

                long sumX = 0;
                long sumY = 0;

                int minX = x;
                int maxX = x;

                int minY = y;
                int maxY = y;

                while (!queue.isEmpty()) {

                    Point point =
                            queue.removeFirst();

                    area++;

                    sumX += point.x;
                    sumY += point.y;

                    minX =
                            Math.min(
                                    minX,
                                    point.x
                            );

                    maxX =
                            Math.max(
                                    maxX,
                                    point.x
                            );

                    minY =
                            Math.min(
                                    minY,
                                    point.y
                            );

                    maxY =
                            Math.max(
                                    maxY,
                                    point.y
                            );

                    for (int i = 0; i < 8; i++) {

                        int nx =
                                point.x + dx[i];

                        int ny =
                                point.y + dy[i];

                        if (nx < 0
                                || ny < 0
                                || nx >= width
                                || ny >= height) {

                            continue;
                        }

                        if (!visited[ny][nx]
                                && foreground[ny][nx]) {

                            visited[ny][nx] = true;

                            queue.add(
                                    new Point(
                                            nx,
                                            ny
                                    )
                            );
                        }
                    }
                }

                int componentWidth =
                        maxX - minX + 1;

                int componentHeight =
                        maxY - minY + 1;

                /*
                 * حذف noiseهای ریز
                 */
                if (area >= 15
                        && componentWidth >= 3
                        && componentHeight >= 7) {

                    components.add(
                            new Component(
                                    area,
                                    (double) sumX / area,
                                    (double) sumY / area
                            )
                    );
                }
            }
        }

        if (components.size() < 2) {
            return 0;
        }

        /*
         * اگر noise اضافی وجود داشت،
         * بزرگ‌ترین componentها را نگه می‌داریم.
         */
        components.sort(
                Comparator.comparingInt(
                        Component::area
                ).reversed()
        );

        if (components.size() > expectedDigits) {

            components =
                    new ArrayList<>(
                            components.subList(
                                    0,
                                    expectedDigits
                            )
                    );
        }

        /*
         * ترتیب از چپ به راست
         */
        components.sort(
                Comparator.comparingDouble(
                        Component::centerX
                )
        );

        /*
         * Linear regression:
         *
         * y = slope * x + b
         */
        double sumX = 0;
        double sumY = 0;

        for (Component component : components) {

            sumX +=
                    component.centerX();

            sumY +=
                    component.centerY();
        }

        double meanX =
                sumX / components.size();

        double meanY =
                sumY / components.size();

        double numerator = 0;
        double denominator = 0;

        for (Component component : components) {

            double x =
                    component.centerX()
                            - meanX;

            double y =
                    component.centerY()
                            - meanY;

            numerator += x * y;
            denominator += x * x;
        }

        if (denominator == 0) {
            return 0;
        }

        double slope =
                numerator / denominator;

        double angle =
                Math.toDegrees(
                        Math.atan(slope)
                );

        /*
         * زاویه‌های غیرعادی را قبول نمی‌کنیم.
         */
        if (Math.abs(angle) > 30) {
            return 0;
        }

        return angle;
    }


    /**
     * ==========================================
     * OTSU Threshold
     * ==========================================
     */
    private static int calculateOtsuThreshold(
            BufferedImage image
    ) {

        int[] histogram =
                new int[256];

        int width =
                image.getWidth();

        int height =
                image.getHeight();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                histogram[
                        getGray(
                                image,
                                x,
                                y
                        )
                        ]++;
            }
        }

        int total =
                width * height;

        double sum = 0;

        for (int i = 0; i < 256; i++) {

            sum +=
                    i * histogram[i];
        }

        double sumBackground = 0;

        int weightBackground = 0;

        int weightForeground;

        double maxVariance = 0;

        int threshold = 127;

        for (int i = 0; i < 256; i++) {

            weightBackground +=
                    histogram[i];

            if (weightBackground == 0) {
                continue;
            }

            weightForeground =
                    total
                            - weightBackground;

            if (weightForeground == 0) {
                break;
            }

            sumBackground +=
                    (double) i
                            * histogram[i];

            double meanBackground =
                    sumBackground
                            / weightBackground;

            double meanForeground =
                    (sum - sumBackground)
                            / weightForeground;

            double variance =
                    (double)
                            weightBackground
                            * weightForeground
                            * Math.pow(
                            meanBackground
                                    - meanForeground,
                            2
                    );

            if (variance > maxVariance) {

                maxVariance =
                        variance;

                threshold =
                        i;
            }
        }

        return threshold;
    }


    /**
     * ==========================================
     * Binary threshold
     * ==========================================
     */
    private static BufferedImage threshold(
            BufferedImage source,
            int threshold
    ) {

        BufferedImage result =
                new BufferedImage(
                        source.getWidth(),
                        source.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );

        for (int y = 0;
             y < source.getHeight();
             y++) {

            for (int x = 0;
                 x < source.getWidth();
                 x++) {

                int gray =
                        getGray(
                                source,
                                x,
                                y
                        );

                int value =
                        gray < threshold
                                ? 0
                                : 255;

                int rgb =
                        new Color(
                                value,
                                value,
                                value
                        ).getRGB();

                result.setRGB(
                        x,
                        y,
                        rgb
                );
            }
        }

        return result;
    }


    /**
     * ==========================================
     * حذف border تیره
     * ==========================================
     */
    private static BufferedImage trimDarkBorder(
            BufferedImage source
    ) {

        int left = 0;

        int right =
                source.getWidth() - 1;

        int top = 0;

        int bottom =
                source.getHeight() - 1;

        while (right > left
                && columnAverage(
                source,
                right
        ) < 80) {

            right--;
        }

        while (left < right
                && columnAverage(
                source,
                left
        ) < 80) {

            left++;
        }

        while (bottom > top
                && rowAverage(
                source,
                bottom
        ) < 80) {

            bottom--;
        }

        while (top < bottom
                && rowAverage(
                source,
                top
        ) < 80) {

            top++;
        }

        return source.getSubimage(
                left,
                top,
                right - left + 1,
                bottom - top + 1
        );
    }


    private static int columnAverage(
            BufferedImage image,
            int x
    ) {

        long sum = 0;

        for (int y = 0;
             y < image.getHeight();
             y++) {

            sum +=
                    getGray(
                            image,
                            x,
                            y
                    );
        }

        return (int)
                (sum / image.getHeight());
    }


    private static int rowAverage(
            BufferedImage image,
            int y
    ) {

        long sum = 0;

        for (int x = 0;
             x < image.getWidth();
             x++) {

            sum +=
                    getGray(
                            image,
                            x,
                            y
                    );
        }

        return (int)
                (sum / image.getWidth());
    }


    /**
     * ==========================================
     * Grayscale
     * ==========================================
     */
    private static BufferedImage toGray(
            BufferedImage source
    ) {

        BufferedImage gray =
                new BufferedImage(
                        source.getWidth(),
                        source.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                gray.createGraphics();

        try {

            graphics.drawImage(
                    source,
                    0,
                    0,
                    null
            );

        } finally {

            graphics.dispose();
        }

        return gray;
    }


    /**
     * ==========================================
     * Rotate + white border
     * ==========================================
     */
    private static BufferedImage rotateAndAddBorder(
            BufferedImage source,
            double angle
    ) {

        int width =
                source.getWidth()
                        + BORDER_SIZE * 2;

        int height =
                source.getHeight()
                        + BORDER_SIZE * 2;

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                result.createGraphics();

        try {

            graphics.setColor(
                    Color.WHITE
            );

            graphics.fillRect(
                    0,
                    0,
                    width,
                    height
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            AffineTransform transform =
                    new AffineTransform();

            transform.translate(
                    width / 2.0,
                    height / 2.0
            );

            transform.rotate(
                    Math.toRadians(angle)
            );

            transform.translate(
                    -source.getWidth() / 2.0,
                    -source.getHeight() / 2.0
            );

            graphics.drawImage(
                    source,
                    transform,
                    null
            );

        } finally {

            graphics.dispose();
        }

        return result;
    }


    /**
     * ==========================================
     * Resize
     * ==========================================
     */
    private static BufferedImage scaleImage(
            BufferedImage source,
            int scale
    ) {

        int width =
                source.getWidth()
                        * scale;

        int height =
                source.getHeight()
                        * scale;

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                result.createGraphics();

        try {

            graphics.setColor(
                    Color.WHITE
            );

            graphics.fillRect(
                    0,
                    0,
                    width,
                    height
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            graphics.drawImage(
                    source,
                    0,
                    0,
                    width,
                    height,
                    null
            );

        } finally {

            graphics.dispose();
        }

        return result;
    }


    /**
     * ==========================================
     * Load image
     * ==========================================
     */
    private static BufferedImage loadImage(
            String address
    ) throws Exception {

        /*
         * Base64 Data URL
         */
        if (address.startsWith("data:image")) {

            int commaIndex =
                    address.indexOf(',');

            if (commaIndex < 0) {

                throw new IllegalArgumentException(
                        "Base64 نامعتبر است"
                );
            }

            String base64 =
                    address.substring(
                            commaIndex + 1
                    );

            byte[] bytes =
                    Base64
                            .getMimeDecoder()
                            .decode(base64);

            return ImageIO.read(
                    new ByteArrayInputStream(
                            bytes
                    )
            );
        }

        /*
         * HTTP
         */
        if (address.startsWith("http://")
                || address.startsWith("https://")) {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(address)
                            )
                            .timeout(
                                    Duration.ofSeconds(20)
                            )
                            .GET()
                            .build();

            HttpResponse<byte[]> response =
                    HTTP_CLIENT.send(
                            request,
                            HttpResponse
                                    .BodyHandlers
                                    .ofByteArray()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "HTTP Status: "
                                + response.statusCode()
                );
            }

            return ImageIO.read(
                    new ByteArrayInputStream(
                            response.body()
                    )
            );
        }

        /*
         * Local file
         */
        Path path =
                Path.of(address);

        if (!Files.exists(path)) {

            throw new IllegalArgumentException(
                    "فایل پیدا نشد: "
                            + address
            );
        }

        return ImageIO.read(
                path.toFile()
        );
    }


    private static String readStream(
            InputStream stream
    ) {

        try {

            return new String(
                    stream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }


    private static String normalizeDigits(
            String text
    ) {

        if (text == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (char c : text.toCharArray()) {

            if (c >= '0' && c <= '9') {

                result.append(c);

            } else if (c >= '۰' && c <= '۹') {

                result.append(
                        (char) (
                                '0'
                                        + c
                                        - '۰'
                        )
                );

            } else if (c >= '٠' && c <= '٩') {

                result.append(
                        (char) (
                                '0'
                                        + c
                                        - '٠'
                        )
                );
            }
        }

        return result.toString();
    }


    private static int getGray(
            BufferedImage image,
            int x,
            int y
    ) {

        Color color =
                new Color(
                        image.getRGB(
                                x,
                                y
                        ),
                        true
                );

        return (int) (
                0.299 * color.getRed()
                        + 0.587 * color.getGreen()
                        + 0.114 * color.getBlue()
        );
    }


    private static int clamp(
            int value,
            int min,
            int max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }


    private record Component(
            int area,
            double centerX,
            double centerY
    ) {
    }


    private record OcrCandidate(
            String number,
            double confidence
    ) {
    }
}