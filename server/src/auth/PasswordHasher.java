package auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Класс для хеширования паролей алгоритмом MD5 с солью и перцем.
 * ДОБАВЛЕНО: Новый класс для хеширования паролей согласно заданию (MD5 + соль + перец).
 */
public class PasswordHasher {

    // ДОБАВЛЕНО: Статический "перец" (общая секретная строка для всех паролей)
    private static final String PEPPER = "Lab7SecretPepper2024";

    /**
     * Генерация случайной соли.
     * @return соль в виде hex-строки
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Хеширование пароля алгоритмом MD5 с солью и перцем.
     * Формула: MD5(Пароль + Соль + Перец)
     * @param password исходный пароль
     * @param salt соль пользователя (уникальная для каждого)
     * @return хеш пароля в виде hex-строки
     */
    public static String hashPassword(String password, String salt) {
        try {
            String input = password + salt + PEPPER;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * Хеширование пароля без соли (только с перцем) - для обратной совместимости.
     * @param password исходный пароль
     * @return хеш пароля в виде hex-строки
     */
    public static String hashPassword(String password) {
        return hashPassword(password, "");
    }
}