package org.hillhouse.searchdb.models.wrappers;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.interfaces.dao.IDDao;
import org.hillhouse.searchdb.models.memory.Memtable;

import java.util.ArrayDeque;
import java.util.Deque;

@AllArgsConstructor
@NoArgsConstructor
public class CurrentMemtableWrapper {
    @Inject private IDDao idDao;
    @Getter private Memtable currentMemtable;
    @Getter private final Deque<Memtable> oldTables = new ArrayDeque<>();

    public void createNewMemtable(){
        if (currentMemtable != null){
            oldTables.addLast(currentMemtable);
        }
        currentMemtable = new Memtable(idDao.getNextID().get());
    }
}
