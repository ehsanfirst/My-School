package org.example.my_school.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ProjectCodeAggregator {

    // --- تنظیمات ---
    // پسوندهای فایل‌هایی که می‌خواهید شامل شوند (با نقطه شروع کنید)
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Set.of(
            ".java"    // فایل‌های کد جاوا
//            ".properties",// فایل‌های Properties
//            ".yml",       // فایل‌های YAML (مانند application.yml)
//            ".yaml",      // فایل‌های YAML
//            ".html"    // فایل‌های HTML
//            ".css",       // فایل‌های CSS
//            ".js",        // فایل‌های JavaScript
//            ".sql",       // فایل‌های SQL (مثلا schema.sql, data.sql)
//            ".md",        // فایل‌های Markdown (مانند README.md)
//            ".txt"        // فایل‌های متنی ساده
            // در صورت نیاز پسوندهای بیشتری اضافه کنید
    ));

    // نام پوشه‌هایی که می‌خواهید نادیده گرفته شوند
    private static final Set<String> IGNORED_DIRECTORIES = new HashSet<>(Set.of(
            ".git",       // پوشه Git
            "target",     // پوشه خروجی Maven/Gradle
            "build",      // پوشه خروجی Gradle/Build
            "node_modules",// پوشه ماژول‌های Node.js
            ".idea",      // پوشه تنظیمات IntelliJ IDEA
            ".vscode",    // پوشه تنظیمات VS Code
            ".settings"   // پوشه تنظیمات Eclipse
            // در صورت نیاز پوشه‌های بیشتری اضافه کنید
    ));
    // --- پایان تنظیمات ---


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name()); // استفاده از UTF-8 برای ورودی فارسی

        System.out.println("--- برنامه جمع‌آوری کد پروژه ---");

        Path projectPath = null;
        boolean validPath = false;
        while (!validPath) {
            System.out.println("لطفا مسیر کامل پوشه ریشه پروژه خود را وارد کنید:");
            String projectDirStr = scanner.nextLine();
            projectPath = Paths.get(projectDirStr);

            if (Files.isDirectory(projectPath)) {
                validPath = true;
            } else {
                System.err.println("خطا: مسیر وارد شده یک پوشه معتبر نیست یا وجود ندارد. لطفا دوباره تلاش کنید.");
            }
        }

        System.out.println("لطفا نام فایل خروجی را وارد کنید (مثال: aggregated_code.txt):");
        String outputFileName = scanner.nextLine();

        // فایل خروجی را در کنار پوشه پروژه یا در مسیر دیگری ایجاد کنید
        // Path outputPath = projectPath.resolveSibling(outputFileName); // کنار پوشه پروژه
        Path outputPath = Paths.get(outputFileName); // در پوشه جاری که برنامه اجرا می‌شود

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            System.out.println("\nشروع پردازش فایل‌ها در پوشه: " + projectPath.toAbsolutePath());
            System.out.println("فایل خروجی در مسیر زیر ایجاد خواهد شد: " + outputPath.toAbsolutePath());
            System.out.println("پسوندهای مجاز: " + ALLOWED_EXTENSIONS);
            System.out.println("پوشه‌های نادیده گرفته شده: " + IGNORED_DIRECTORIES);
            System.out.println("--------------------------------------------------");

            CodeFileVisitor visitor = new CodeFileVisitor(writer, projectPath);
            Files.walkFileTree(projectPath, visitor); // شروع پیمایش

            System.out.println("\n--------------------------------------------------");
            System.out.println("عملیات با موفقیت انجام شد.");
            System.out.println("تعداد " + visitor.getFilesProcessed() + " فایل در فایل خروجی (" + outputPath.getFileName() + ") ذخیره شد.");

        } catch (IOException e) {
            System.err.println("\nخطا در هنگام پردازش فایل‌ها یا نوشتن فایل خروجی:");
            e.printStackTrace(); // نمایش جزئیات خطا
        } finally {
            scanner.close(); // بستن اسکنر در هر صورت
        }
    }

    // کلاس داخلی برای پیمایش فایل‌ها
    private static class CodeFileVisitor extends SimpleFileVisitor<Path> {
        private final BufferedWriter writer;
        private final Path rootPath;
        private int filesProcessed = 0;

        public CodeFileVisitor(BufferedWriter writer, Path rootPath) {
            this.writer = writer;
            this.rootPath = rootPath;
        }

        // این متد قبل از ورود به هر پوشه اجرا می‌شود
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            String dirName = dir.getFileName().toString();
            // اگر نام پوشه در لیست نادیده گرفته شده‌ها بود، از پیمایش آن و زیرپوشه‌هایش صرف نظر کن
            if (IGNORED_DIRECTORIES.contains(dirName)) {
                System.out.println("نادیده گرفتن پوشه: " + rootPath.relativize(dir));
                return FileVisitResult.SKIP_SUBTREE; // این پوشه و محتویاتش را رد کن
            }
            return FileVisitResult.CONTINUE; // ادامه بده
        }

        // این متد برای هر فایل پیدا شده اجرا می‌شود
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // بررسی کن آیا پسوند فایل مجاز است
            String fileName = file.getFileName().toString().toLowerCase();
            boolean isAllowed = ALLOWED_EXTENSIONS.stream().anyMatch(fileName::endsWith);

            if (attrs.isRegularFile() && isAllowed) {
                Path relativePath = rootPath.relativize(file); // مسیر نسبی فایل نسبت به ریشه پروژه
                System.out.println("پردازش فایل: " + relativePath);

                try {
                    // نوشتن جداکننده و نام فایل در فایل خروجی
                    writer.write("==================================================");
                    writer.newLine();
                    // مسیر را با '/' نمایش بده برای سازگاری بین سیستم‌عامل‌ها
                    writer.write("=== File: " + relativePath.toString().replace('\\', '/'));
                    writer.newLine();
                    writer.write("==================================================");
                    writer.newLine();
                    writer.newLine(); // یک خط خالی برای جداسازی

                    // خواندن تمام خطوط فایل با انکودینگ UTF-8
                    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    for (String line : lines) {
                        writer.write(line); // نوشتن هر خط در فایل خروجی
                        writer.newLine(); // اضافه کردن خط جدید
                    }

                    writer.newLine(); // یک خط خالی بعد از محتوای فایل
                    writer.newLine(); // یک خط خالی دیگر برای جداسازی بیشتر
                    filesProcessed++; // افزایش شمارنده فایل‌های پردازش شده

                } catch (IOException e) {
                    System.err.println(" >> خطا در خواندن یا نوشتن فایل: " + relativePath + " - " + e.getMessage());
                    // می‌توانید تصمیم بگیرید که آیا در صورت خطا برنامه متوقف شود یا ادامه دهد
                    // در اینجا ما فقط خطا را چاپ می‌کنیم و ادامه می‌دهیم
                } catch (Exception e) {
                    // گرفتن خطاهای احتمالی دیگر مثل مشکلات انکودینگ
                    System.err.println(" >> خطای غیرمنتظره هنگام پردازش فایل: " + relativePath + " - " + e.getMessage());
                }
            }
            return FileVisitResult.CONTINUE; // ادامه پیمایش به فایل بعدی
        }

        // این متد در صورت بروز خطا هنگام دسترسی به یک فایل اجرا می‌شود
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.err.println("خطا: امکان دسترسی به فایل وجود ندارد: " + file + " (" + (exc != null ? exc.getMessage() : "دلیل نامشخص") + ")");
            return FileVisitResult.CONTINUE; // ادامه بده و این فایل را نادیده بگیر
        }

        public int getFilesProcessed() {
            return filesProcessed;
        }
    }
}
