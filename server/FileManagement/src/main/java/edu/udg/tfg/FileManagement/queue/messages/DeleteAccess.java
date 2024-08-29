package edu.udg.tfg.FileManagement.queue.messages;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeleteAccess(@JsonProperty("userId") UUID userId,
                           @JsonProperty("elementId") UUID elementId)
        implements Serializable {
}