package main.models.diskDS;

public class SSTableConstants {
    public static final int MAX_ROW_ID_LENGTH = 256;

    public static final int OFFSET_ROW_ID = 0;
    public static final int SIZE_BYTE_ROW_ID = 512;

    public static final int OFFSET_ROW_FLAG = 514;
    public static final int SIZE_BYTE_FLAG = 1;

    public static final int OFFSET_ROW_VALUE_LENGTH = 516;
    public static final int SIZE_BYTE_VALUE_LENGTH = 4;

    public static final int OFFSET_ROW_VALUE_DATA = 523;

    public static final byte FLAG_DELETED = 1;
    public static final byte FLAG_UPDATED = 1 << 1;


    public static final byte START_BYTE = 121;
    public static final byte END_BYTE = 85;
    public static final int HEADER_SIZE_BYTES = 523;
    public static final int OFFSET_DATA_START_BYTE = 1;
}
