package edu.udg.tfg.FileManagement.queue.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public record AddAccess(@JsonProperty("userId") UUID userId,
                        @JsonProperty("elementId") UUID elementId,
                        @JsonProperty("accessType") int accessType)
        implements Serializable {
}