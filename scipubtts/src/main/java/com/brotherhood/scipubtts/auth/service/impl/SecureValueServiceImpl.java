package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.service.SecureValueService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
//Tận dụng tối đa các thư viện core cấu trúc mạnh mẽ của Java (SecureRandom, MessageDigest) để tạo ra các giá trị ngẫu nhiên có độ an toàn mã hóa cao (Cryptographically Secure)
public class SecureValueServiceImpl implements SecureValueService {

    //SECURE_RANDOM: Khác với lớp Random thông thường (vốn có thể bị đoán trước thuật toán), SecureRandom tạo ra các chuỗi byte ngẫu nhiên đạt tiêu chuẩn bảo mật cao, hacker không thể dùng toán học để dò ra quy luật.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    //BASE64_URL: Công cụ mã hóa chuỗi theo chuẩn Base64 nhưng biến đổi các ký tự đặc biệt (+, /) thành (-, _) và bỏ dấu bằng (=). Mục đích là để chuỗi này có thể truyền an toàn trên URL hoặc lưu vào Cookie mà không bị lỗi hệ thống.
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    //OTP_CHARS: Danh sách các ký tự dùng để tạo mã OTP. Bạn có thể thấy tác giả đã cố tình loại bỏ các ký tự dễ gây nhầm lẫn khi nhìn bằng mắt thường như: số 0 và chữ O, số 1 và chữ I.
    private static final char[] OTP_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();


    //Chức năng:
    //Tạo ra một Opaque Token ngẫu nhiên, có độ dài lớn và độ phức tạp cao. Thường được sử dụng làm Refresh Token lưu vào Database hoặc Session ID, API Key.
    //Flow code:
    //Khởi tạo mảng: Tạo một mảng byte trống có kích thước 48 phần tử (byte[] bytes = new byte[48];).
    //Đổ dữ liệu ngẫu nhiên: Gọi SECURE_RANDOM.nextBytes(bytes); để nhét đầy 48 byte này bằng các ký tự ngẫu nhiên bảo mật cao.
    //Mã hóa chuỗi: Chuyển mảng 48 byte ngẫu nhiên đó thành một chuỗi String đọc được bằng BASE64_URL.encodeToString(bytes).
    //Kết quả: Trả về một chuỗi dạng chữ và số ngẫu nhiên dài khoảng 64 ký tự an toàn tuyệt đối trên trình duyệt và URL.
    @Override
    public String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_URL.encodeToString(bytes);
    }

    //2. Hàm sha256(String raw)
    //Chức năng:
    //Mã hóa một chiều chuỗi văn bản thuần túy (Raw String) thành một chuỗi băm SHA-256.
    //Ứng dụng trong case Token của bạn: Khi bạn lưu Refresh Token vào Database, bạn không nên lưu trực tiếp chuỗi token thô (vì nếu lộ DB, hacker sẽ lấy được token đi phá hoại). Bạn cần chạy hàm sha256(token_tho) này rồi mới lưu chuỗi băm vào DB. Khi User gửi token lên, bạn lại băm ra rồi so sánh 2 chuỗi băm với nhau.
    //Flow code:
    //Nạp thuật toán: Gọi MessageDigest.getInstance("SHA-256") để yêu cầu Java cung cấp bộ máy băm SHA-256.
    //Băm chuỗi: Chuyển chuỗi raw thành mảng byte dựa trên bảng mã UTF_8, sau đó đưa vào máy băm md.digest(...) để nhận về một mảng byte đã băm (luôn có độ dài cố định là 32 bytes).
    //Chuyển sang dạng Hex: Định dạng mảng 32 bytes kết quả đó thành chuỗi ký tự dạng Hex (hệ thập lục phân từ 0-9 và a-f) thông qua HexFormat.of().formatHex(digest).
    //Kết quả: Trả về một chuỗi Hex dài cố định 64 ký tự. Nếu hệ thống thiếu thuật toán SHA-256 (gần như không thể), nó sẽ ném ra lỗi IllegalStateException.
    @Override
    public String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    @Override
    public String generateOtpCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(OTP_CHARS[SECURE_RANDOM.nextInt(OTP_CHARS.length)]);
        }
        return sb.toString();
    }
}
