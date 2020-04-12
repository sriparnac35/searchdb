package org.hillhouse.searchdb.constants;

public class WalConstants {
    public static final int OFFSET_LOG_ID = 0;
    public static final int LENGTH_LOG_ID = 4;

    public static final int OFFSET_ENTRY_TYPE = 4;
    public static final int LENGTH_ENTRY_TYPE = 1;
    public static final byte VALUE_ENTRY_TYPE_DATA = 1 << 1;
    public static final byte VALUE_ENTRY_TYPE_STATE = 1 << 2;

    // constants for entry type state :
    public static final int OFFSET_COMMIT_STATE = 5;
    public static final int LENGTH_COMMIT_STATE = 1;
    public static final byte VALUE_COMMIT_STATE_STARTED = 1 << 1;
    public static final byte VALUE_COMMIT_STATE_ENDED = 1 << 2;
    public static final byte VALUE_COMMIT_STATE_FAILED = 1 << 3;

    public static final int OFFSET_BEGIN_OFFSET = 6;
    public static final int LENGTH_BEGIN_OFFSET = 4;

    public static final int OFFSET_END_OFFSET = 10;
    public static final int LENGTH_END_OFFSET = 4;
    
    public static final int HEADER_LENGTH_STATE = LENGTH_LOG_ID + LENGTH_ENTRY_TYPE + LENGTH_COMMIT_STATE +
            LENGTH_BEGIN_OFFSET + LENGTH_END_OFFSET;
    

    // constants for entry type data :
    public static final int OFFSET_ROW_KEY = 5;
    public static final int LENGTH_ROW_KEY = 256;

    public static final int OFFSET_OPERATION_TYPE = 261;
    public static final int LENGTH_OPERATION_TYPE = 1;
    public static final byte VALUE_OPERATION_TYPE_INSERT = 1 << 1;
    public static final byte VALUE_OPERATION_TYPE_UPDATE = 1 << 2;
    public static final byte VALUE_OPERATION_TYPE_DELETE = 1 << 3;

    public static final int OFFSET_VALUE_LENGTH = 262;
    public static final int LENGTH_VALUE_LENGTH = 4;

    public static final int OFFSET_VALUE = OFFSET_VALUE_LENGTH + LENGTH_VALUE_LENGTH;

    public static final int HEADER_LENGTH_DATA = LENGTH_LOG_ID + LENGTH_ENTRY_TYPE + LENGTH_ROW_KEY +
            LENGTH_OPERATION_TYPE + LENGTH_VALUE_LENGTH;

}
