package com.coffee.atom.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    EXCEL("xlsx"),
    PDF("pdf"),
    ZIP("zip");
    private final String extension;
}
