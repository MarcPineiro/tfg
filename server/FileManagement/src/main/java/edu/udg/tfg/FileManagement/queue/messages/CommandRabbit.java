package edu.udg.tfg.FileManagement.queue.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public record CommandRabbit(@JsonProperty("action") String action,
                            @JsonProperty("userId") String userId,
                            @JsonProperty("elementId") String elementId,
                            @JsonProperty("path") String path,
                            @JsonProperty("creationDate")Date creationDate,
                            @JsonProperty("lastModified") Date lastModified,
                            @JsonProperty("clientType") String client,
                            @JsonProperty("parent") String parent)
        implements Serializable {
}
