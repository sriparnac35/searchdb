package org.hillhouse.searchdb.impl.eventHandlers;

import com.google.inject.Inject;
import org.hillhouse.searchdb.impl.datastores.WALDataStore;
import org.hillhouse.searchdb.interfaces.processors.WalProcessor;
import org.hillhouse.searchdb.interfaces.utilities.DocumentQueue;
import org.hillhouse.searchdb.models.wal.WalSearchKey;
import org.hillhouse.searchdb.models.wal.WalSearchValue;
import org.hillhouse.searchdb.models.wal.entries.WalDataEntry;
import org.hillhouse.searchdb.models.wal.entries.WalEntry;
import org.hillhouse.searchdb.models.wal.entries.WalStateEntry;
import org.hillhouse.searchdb.models.wal.enums.WalCommitStatus;

import java.util.ArrayDeque;
import java.util.Iterator;


public class WalRecoveryWrapper implements WalProcessor {
    @Inject
    private DocumentQueue<WalDataEntry> documentQueue;
    @Inject
    private WALDataStore dataStore;

    @Override
    public void recoverWal() throws Exception {
        WalSearchKey searchKey = WalSearchKey.builder().startOffset(0).endOffset(-1).build();
        WalSearchValue searchValue = dataStore.search(searchKey);
        ArrayDeque<WalDataEntry> entriesToPush = new ArrayDeque<>();

        for (int i = searchValue.getDataValues().size() - 1; i >= 0; i--) {
            WalEntry entry = searchValue.getDataValues().get(i);
            if (entry instanceof WalStateEntry && ((WalStateEntry) entry).getCommitStatus() == WalCommitStatus.COMMIT_END) {
                break;
            } else if (entry instanceof WalDataEntry) {
                entriesToPush.addFirst((WalDataEntry) entry);
            }
        }
        for (Iterator<WalDataEntry> iterator = entriesToPush.iterator(); iterator.hasNext(); ) {
            documentQueue.push(iterator.next());
        }
    }
}
