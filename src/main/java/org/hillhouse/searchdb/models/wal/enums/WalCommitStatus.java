package org.hillhouse.searchdb.models.wal.enums;

import java.io.Serializable;

public enum WalCommitStatus implements Serializable {
    COMMIT_BEGIN, COMMIT_END, COMMIT_FAILED
}
