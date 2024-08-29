package edu.udg.tfg.FileManagement.queue.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public record RecordMessage(@JsonProperty("userId") UUID userId,
                            @JsonProperty("action") String action,
                            @JsonProperty("elementType") String elementType,
                            @JsonProperty("elementName") String elementName,
                            @JsonProperty("elementId") UUID elementId,
                            @JsonProperty("parentId") UUID parentId,
                            @JsonProperty("path") String path,
                            @JsonProperty("actionDate") Date actionDate)
        implements Serializable {
}
