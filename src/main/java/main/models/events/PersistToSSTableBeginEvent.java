package main.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PersistToSSTableBeginEvent extends Event{
    private String walID;
    private AtomicInteger beginLogID;
    private AtomicInteger endLogID;
}
