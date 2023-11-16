package ua.nure.vovk.task3.core;

public enum MessageCode {
    BYE (-1),
    OK (200),
    BAD_REQUEST (400),
    UNAUTHORIZED (401),
    FORBIDDEN (403),
    AUTHORIZE (1000),
    READ_ONLY (1101),
    READ_WRITE (1102),
    EXIT (1900),
    ERROR (5000)
    ;
    private final int code;
    MessageCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.code + " " + this.name();
    }

    public boolean equals(int code) {
        return this.code == code;
    }
}
