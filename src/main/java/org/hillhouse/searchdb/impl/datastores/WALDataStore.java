package org.hillhouse.searchdb.impl.datastores;

import com.google.inject.Inject;
import javafx.util.Pair;
import org.hillhouse.searchdb.constants.WalConstants;
import org.hillhouse.searchdb.interfaces.dao.DiskDao;
import org.hillhouse.searchdb.interfaces.processors.DataStore;
import org.hillhouse.searchdb.models.wal.*;
import org.hillhouse.searchdb.models.wal.entries.WalDataEntry;
import org.hillhouse.searchdb.models.wal.entries.WalEntry;
import org.hillhouse.searchdb.models.wal.entries.WalStateEntry;
import org.hillhouse.searchdb.models.wal.enums.WALOperationType;
import org.hillhouse.searchdb.models.wal.enums.WalCommitStatus;
import org.hillhouse.searchdb.models.wal.enums.WalEntryType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hillhouse.searchdb.constants.WalConstants.*;

public class WALDataStore implements DataStore<WalDataKey, WalValue, WalSearchKey, WalSearchValue> {
    private static final String FILE_NAME = WalConstants.WAL_DIRECTORY +  "wal";
    @Inject private DiskDao diskDao;

    @Override
    public void insert(WalDataKey key, WalValue value) throws IOException {
        switch (key.getEntryType()) {
            case DATA:
                writeDataToStore(key.getLogID(), key, (WalDataValue) value, WALOperationType.INSERT);
                break;
            case COMMIT_STATE:
                writeStateToStore(key.getLogID(), key, (WalStateValue) value);
                break;
        }
    }

    @Override
    public void update(WalDataKey key, WalValue value) throws IOException {
        switch (key.getEntryType()) {
            case DATA:
                writeDataToStore(key.getLogID(), key, (WalDataValue) value, WALOperationType.UPDATE);
            default:
                throw new UnsupportedOperationException("wal update supported only for data");
        }
    }

    @Override
    public void delete(WalDataKey key) throws IOException {
        switch (key.getEntryType()) {
            case DATA:
                writeDataToStore(key.getLogID(), key, null, WALOperationType.DELETE);
            default:
                throw new UnsupportedOperationException("wal delete item supported only for data");
        }
    }

    @Override
    public void initialize() throws Exception {
        diskDao.initialize();
        diskDao.createWithName(FILE_NAME);
        diskDao.makeCurrent(FILE_NAME);
    }

    @Override
    public void destroy() throws Exception {
        diskDao.destroy();
    }

    @Override
    public WalSearchValue search(WalSearchKey key) throws IOException {
        byte[] data = diskDao.getData(key.getWalID(), key.getStartOffset(), key.getEndOffset());
        int currentOffset = 0;
        List<WalEntry> walValues = new ArrayList<>();
        while (currentOffset < data.length) {
            WalEntryType entryType = deriveEntryTypeFromByte(data[currentOffset + WalConstants.OFFSET_ENTRY_TYPE]);
            Pair<WalEntry, Integer> extractedValue = (entryType == WalEntryType.DATA) ? extractDataValueFromByteArray(data, currentOffset) :
                    extractStateValueFromByteArray(data, currentOffset);
            walValues.add(extractedValue.getKey());
            currentOffset += extractedValue.getValue();
        }
        return new WalSearchValue(walValues);
    }

    private void writeDataToStore(int logID, WalDataKey key, WalDataValue value, WALOperationType operationType) throws IOException {
        byte[] valueBytes = (value != null) ? value.getValue().getBytes() : new byte[0];
        int valueLength = (operationType == WALOperationType.DELETE) ? 0 : valueBytes.length;
        byte[] bytes = new byte[WalConstants.HEADER_LENGTH_DATA + valueLength];

        byte[] rowKeyBytes = BigInteger.valueOf(logID).toByteArray();
        System.arraycopy(rowKeyBytes, 0, bytes, WalConstants.OFFSET_LOG_ID + LENGTH_LOG_ID - rowKeyBytes.length, rowKeyBytes.length);
        bytes[WalConstants.OFFSET_ENTRY_TYPE] = WalConstants.VALUE_ENTRY_TYPE_DATA;
        System.arraycopy(key.getId().getBytes(), 0, bytes, WalConstants.OFFSET_ROW_KEY - key.getId().length(), key.getId().length());
        bytes[WalConstants.OFFSET_OPERATION_TYPE] = deriveOperationTypeFlag(operationType);
        System.arraycopy(BigInteger.valueOf(valueLength).toByteArray(), 0, bytes, WalConstants.OFFSET_VALUE_LENGTH, WalConstants.LENGTH_VALUE_LENGTH);
        System.arraycopy(valueBytes, 0, bytes, WalConstants.OFFSET_VALUE, valueLength);

        diskDao.writeAndSyncToLatest(bytes);
    }

    private void writeStateToStore(int logID, WalDataKey key, WalStateValue value) throws IOException {
        byte[] bytes = new byte[WalConstants.HEADER_LENGTH_STATE];
        System.arraycopy(BigInteger.valueOf(logID).toByteArray(), 0, bytes, WalConstants.OFFSET_LOG_ID, WalConstants.LENGTH_LOG_ID);
        bytes[WalConstants.OFFSET_ENTRY_TYPE] = WalConstants.VALUE_ENTRY_TYPE_STATE;
        bytes[WalConstants.OFFSET_COMMIT_STATE] = deriveCommitState(value.getCommitStatus());
        System.arraycopy(BigInteger.valueOf(value.getBeginOffset()).toByteArray(), 0, bytes, WalConstants.OFFSET_BEGIN_OFFSET, WalConstants.LENGTH_BEGIN_OFFSET);
        System.arraycopy(BigInteger.valueOf(value.getEndOffset()).toByteArray(), 0, bytes, WalConstants.OFFSET_END_OFFSET, WalConstants.LENGTH_END_OFFSET);

        diskDao.writeAndSyncToLatest(bytes);
    }

    private Pair<WalEntry, Integer> extractDataValueFromByteArray(byte[] data, int currentOffset) {
        int logID = new BigInteger(Arrays.copyOfRange(data, currentOffset + WalConstants.OFFSET_LOG_ID, currentOffset + WalConstants.OFFSET_LOG_ID + WalConstants.LENGTH_LOG_ID)).intValue();
        WalCommitStatus commitStatus = deriveCommitStatusFromFlag(data[OFFSET_COMMIT_STATE]);
        int beginOffset = new BigInteger(Arrays.copyOfRange(data, currentOffset + OFFSET_BEGIN_OFFSET, currentOffset + WalConstants.OFFSET_BEGIN_OFFSET + LENGTH_BEGIN_OFFSET)).intValue();
        int endOffset = new BigInteger(Arrays.copyOfRange(data, currentOffset + OFFSET_END_OFFSET, currentOffset + WalConstants.OFFSET_END_OFFSET + WalConstants.LENGTH_END_OFFSET)).intValue();
        WalEntry value = WalStateEntry.builder().commitStatus(commitStatus).beginOffset(beginOffset).endOffset(endOffset)
                .logID(logID).entryType(WalEntryType.COMMIT_STATE).entryType(WalEntryType.COMMIT_STATE).walID(FILE_NAME).build();
        return new Pair<>(value, HEADER_LENGTH_STATE);
    }

    private Pair<WalEntry, Integer> extractStateValueFromByteArray(byte[] data, int currentOffset) {
        int logID = new BigInteger(Arrays.copyOfRange(data, currentOffset + WalConstants.OFFSET_LOG_ID, currentOffset + WalConstants.OFFSET_LOG_ID + WalConstants.LENGTH_LOG_ID)).intValue();
        String rowKey = new String(Arrays.copyOfRange(data, currentOffset + OFFSET_ROW_KEY, currentOffset + OFFSET_ROW_KEY + LENGTH_ROW_KEY));
        WALOperationType operationType = deriveOperationTypeFromFlag(data[currentOffset + OFFSET_OPERATION_TYPE]);
        int dataLength = new BigInteger(Arrays.copyOfRange(data, currentOffset + OFFSET_VALUE_LENGTH, currentOffset + OFFSET_VALUE_LENGTH + LENGTH_VALUE_LENGTH)).intValue();
        String dataValue = new String(Arrays.copyOfRange(data, currentOffset + OFFSET_VALUE, currentOffset + OFFSET_VALUE + dataLength));

        int bytesRead = HEADER_LENGTH_DATA + dataLength;
        WalEntry walEntry = WalDataEntry.builder().logID(logID).rowKey(rowKey).operationType(operationType)
                .entryType(WalEntryType.DATA).value(dataValue).walID(FILE_NAME).build();
        return new Pair<>(walEntry, bytesRead);
    }

    private WALOperationType deriveOperationTypeFromFlag(byte flag) {
        switch (flag) {
            case VALUE_OPERATION_TYPE_INSERT:
                return WALOperationType.INSERT;
            case VALUE_OPERATION_TYPE_UPDATE:
                return WALOperationType.UPDATE;
            case VALUE_OPERATION_TYPE_DELETE:
                return WALOperationType.DELETE;
            default:
                return null;
        }
    }

    private WalCommitStatus deriveCommitStatusFromFlag(byte status) {
        switch (status) {
            case VALUE_COMMIT_STATE_STARTED:
                return WalCommitStatus.COMMIT_BEGIN;
            case VALUE_COMMIT_STATE_ENDED:
                return WalCommitStatus.COMMIT_END;
            case VALUE_COMMIT_STATE_FAILED:
                return WalCommitStatus.COMMIT_FAILED;
            default:
                return null;
        }
    }

    private WalEntryType deriveEntryTypeFromByte(byte type) {
        switch (type) {
            case VALUE_ENTRY_TYPE_STATE:
                return WalEntryType.COMMIT_STATE;
            case VALUE_ENTRY_TYPE_DATA:
                return WalEntryType.DATA;
            default:
                return null;
        }
    }

    private byte deriveCommitState(WalCommitStatus commitStatus) {
        switch (commitStatus) {
            case COMMIT_BEGIN:
                return VALUE_COMMIT_STATE_STARTED;
            case COMMIT_FAILED:
                return WalConstants.VALUE_COMMIT_STATE_FAILED;
            case COMMIT_END:
                return WalConstants.VALUE_COMMIT_STATE_ENDED;
            default:
                return 0;
        }
    }

    private byte deriveOperationTypeFlag(WALOperationType operationType) {
        switch (operationType) {
            case INSERT:
                return WalConstants.VALUE_OPERATION_TYPE_INSERT;
            case UPDATE:
                return WalConstants.VALUE_OPERATION_TYPE_UPDATE;
            case DELETE:
                return WalConstants.VALUE_OPERATION_TYPE_DELETE;
            default:
                return 0;
        }
    }

}
