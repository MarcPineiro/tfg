package edu.udg.tfg.Trash.queue.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public record DeleteElemtn(@JsonProperty("userId") UUID userId,
                           @JsonProperty("elementId") UUID elementId)
        implements Serializable {
}
