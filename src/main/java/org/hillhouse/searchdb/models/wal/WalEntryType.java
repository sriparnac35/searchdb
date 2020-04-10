package org.hillhouse.searchdb.models.wal;

public enum WalEntryType {
    INSERT_DOCUMENT, UPDATE_DOCUMENT, DELETE_DOCUMENT, COMMIT_SYNC_START, COMMIT_SYNC_END
}
