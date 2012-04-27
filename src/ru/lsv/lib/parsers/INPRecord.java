package ru.lsv.lib.parsers;

/**
 * Парсер одной строки в INP-файле, который содержится в INPX
 */
public class INPRecord {

    /**
     * Парсит строку в INP-файле
     * @param inpString Входная строка, которую надо будет распарсить
     */
    public INPRecord (String inpString) {
        // AUTHOR     ;    GENRE     ;     TITLE           ; SERIES ; SERNO ; FILE ;    SIZE   ;  LIBID    ;    DEL   ;    EXT     ;       DATE        ;    LANG    ; LIBRATE  ; KEYWORDS ;
        // static char* dummy = "dummy:" "\x04" "other:" "\x04" "dummy record" "\x04"   "\x04"  "\x04" "\x04" "1" "\x04" "%d" "\x04" "1" "\x04" "EXT" "\x04" "2000-01-01" "\x04" "en" "\x04" "0" "\x04"     "\x04" "\r\n";
        // from http://www.assembla.com/code/myhomelib/subversion/nodes/Utils/InpxCreator/trunk/lib2inpx/main.cpp

    }

}
