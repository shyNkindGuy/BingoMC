package com.shyNkindguy.bingo.core.catalog;

// Error en los YAML del catálogo o al armar una cartilla. El mensaje va en español y dice el archivo y
// la tarea, para mostrarlo tal cual en la consola o en el chat.
public class CatalogException extends RuntimeException {

    public CatalogException(String message) {
        super(message);
    }

    public CatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
