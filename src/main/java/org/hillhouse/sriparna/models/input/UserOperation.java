package org.hillhouse.sriparna.models.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hillhouse.sriparna.models.OperationType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserOperation {
    private OperationType operationType;
    private Document document;
}
