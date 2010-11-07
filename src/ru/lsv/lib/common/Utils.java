package ru.lsv.lib.common;

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
}