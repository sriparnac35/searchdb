package main.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.interfaces.dao.IDDao;
import main.models.memory.Memtable;

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
        currentMemtable = new Memtable(idDao.getNextID());
    }
}
