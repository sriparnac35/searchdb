package org.hillhouse.searchdb.models.wal.enums;

import java.io.Serializable;

public enum WalEntryType implements Serializable {
    DATA, COMMIT_STATE
}
