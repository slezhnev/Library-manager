package ru.lsv.lib.common;

import java.util.Arrays;

/**
 * Класс, содержащий вспомогательные методы
 */
public class Utils {
    /**
     * Проверка на равенство двух объектов с обработкой возможных null-значений
     *
     * @param aThis First compared item
     * @param aThat Second compared item
     * @return Result of comparation
     */
    static public boolean areEqual(Object aThis, Object aThat) {
        return aThis == null ? aThat == null : aThis.equals(aThat);
    }

    /**
     * Получение hash code объекта. В случае если объект равен null - возвращается 0
     *
     * @param hashObj Объект для получения hash code
     * @return Хэш или 0, если объект = null
     */
    static public int getHash(Object hashObj) {
        return hashObj == null ? 0 : hashObj.hashCode();
    }

    /**
     * Обработка и уделаление из имени файла всякой ненужной пакости. <br/>
     * Копипаст фром http://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars
     */
    final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
    static {
        Arrays.sort(illegalChars);
    }

    /**
     * Выкидывает из имени файла недопустустимые символы
     * @param badFileName Имя файла для обработки
     * @return Имя файла, из которого выкинуты все недопустимые символы
     */
    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int)badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char)c);
            }
        }
        // Дополнительно проверим на '..'
        while (cleanName.indexOf("..") > -1) {
            cleanName.replace(cleanName.indexOf(".."), cleanName.indexOf("..") + 2, "__");
        }
        return cleanName.toString();
    }

}