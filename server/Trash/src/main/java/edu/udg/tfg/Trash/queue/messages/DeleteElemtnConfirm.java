package edu.udg.tfg.Trash.queue.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public record DeleteElemtnConfirm(@JsonProperty("userId") UUID userId,
                                  @JsonProperty("elementId") UUID elementId,
                                  @JsonProperty("service") String service)
        implements Serializable {
}
