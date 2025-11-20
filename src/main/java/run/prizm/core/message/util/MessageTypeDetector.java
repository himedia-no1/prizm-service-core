package run.prizm.core.message.util;

import run.prizm.core.message.constraint.MessageType;

import java.util.Set;
import java.util.regex.Pattern;

public class MessageTypeDetector {

    // URL 패턴 (http:// 또는 https:// + 도메인)
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:[0-9]+)?(/.*)?",
            Pattern.CASE_INSENSITIVE
    );

    // MEDIA 타입 확장자
    private static final Set<String> MEDIA_EXTENSIONS = Set.of(
            // 이미지
            "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "ico",
            "tiff", "tif", "heic", "heif", "avif",
            // 비디오
            "mp4", "avi", "mov", "mkv", "webm", "flv", "wmv", "m4v",
            "mpg", "mpeg", "3gp", "ogv",
            // 오디오
            "mp3", "wav", "ogg", "m4a", "flac", "aac", "wma", "opus",
            "oga", "mid", "midi"
    );

    // DOCUMENT 타입 확장자
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            // MS Office
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            // 한컴 오피스
            "hwp", "hwpx",
            // 문서
            "pdf", "txt", "rtf", "odt", "ods", "odp",
            // 데이터
            "csv", "json", "xml", "yaml", "yml",
            // 마크다운/로그
            "md", "markdown", "log"
    );

    // FILE 타입 확장자 (압축)
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz"
    );

    // 차단할 확장자 (보안)
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "dll", "bat", "cmd", "com", "scr", "vbs", "jar",
            "php", "jsp", "asp", "aspx",
            "html", "htm",
            "sh", "bash", "ps1"
    );

    /**
     * 메시지 타입 판별
     *
     * @param content 메시지 내용
     * @param fileExtension 파일 확장자 (파일이 없으면 null)
     * @return MessageType
     */
    public static MessageType determineType(String content, String fileExtension) {
        // 파일이 있으면 확장자로 판별
        if (fileExtension != null && !fileExtension.isEmpty()) {
            return getTypeByExtension(fileExtension);
        }

        // 파일이 없으면 content 검사
        if (content != null && containsUrl(content)) {
            return MessageType.LINK;
        }

        return MessageType.TEXT;
    }

    /**
     * 확장자로 타입 판별
     */
    private static MessageType getTypeByExtension(String extension) {
        String ext = extension.toLowerCase();

        if (MEDIA_EXTENSIONS.contains(ext)) {
            return MessageType.MEDIA;
        }

        if (DOCUMENT_EXTENSIONS.contains(ext)) {
            return MessageType.DOCUMENT;
        }

        if (ARCHIVE_EXTENSIONS.contains(ext)) {
            return MessageType.FILE;
        }

        // 기본값
        return MessageType.FILE;
    }

    /**
     * URL 포함 여부 검사
     */
    private static boolean containsUrl(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(text).find();
    }

    /**
     * 확장자 검증 (허용 여부)
     *
     * @param extension 확장자
     * @return 허용되면 true
     * @throws IllegalArgumentException 차단된 확장자인 경우
     */
    public static boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        String ext = extension.toLowerCase();

        if (BLOCKED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Blocked file extension: " + extension);
        }

        return MEDIA_EXTENSIONS.contains(ext)
                || DOCUMENT_EXTENSIONS.contains(ext)
                || ARCHIVE_EXTENSIONS.contains(ext);
    }

    /**
     * Content-Type과 확장자가 일치하는지 검증
     */
    public static boolean matchesContentType(String extension, String contentType) {
        if (extension == null || contentType == null) {
            return false;
        }

        String ext = extension.toLowerCase();
        String type = contentType.toLowerCase();

        // 간단한 매칭 (확장 가능)
        if (type.contains("image") && MEDIA_EXTENSIONS.contains(ext)) {
            return true;
        }
        if (type.contains("video") && MEDIA_EXTENSIONS.contains(ext)) {
            return true;
        }
        if (type.contains("audio") && MEDIA_EXTENSIONS.contains(ext)) {
            return true;
        }
        if (type.contains("pdf") && ext.equals("pdf")) {
            return true;
        }
        if (type.contains("msword") || type.contains("document")) {
            return DOCUMENT_EXTENSIONS.contains(ext);
        }
        if (type.contains("spreadsheet") || type.contains("excel")) {
            return DOCUMENT_EXTENSIONS.contains(ext);
        }
        if (type.contains("presentation") || type.contains("powerpoint")) {
            return DOCUMENT_EXTENSIONS.contains(ext);
        }
        if (type.contains("text")) {
            return DOCUMENT_EXTENSIONS.contains(ext);
        }
        if (type.contains("json") || type.contains("xml") || type.contains("yaml")) {
            return DOCUMENT_EXTENSIONS.contains(ext);
        }
        if (type.contains("zip") || type.contains("compressed")) {
            return ARCHIVE_EXTENSIONS.contains(ext);
        }

        // 알 수 없는 타입은 허용 (S3가 검증함)
        return true;
    }
}
