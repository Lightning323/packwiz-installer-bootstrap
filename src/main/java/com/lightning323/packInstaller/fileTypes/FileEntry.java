package com.lightning323.packInstaller.fileTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) // <--- Add this line!
public record FileEntry(
        String file,
        @JsonProperty("hash-format")
        String hashFormat,
        String hash
) {
}