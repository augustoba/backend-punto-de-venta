package com.pdv.common;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String what, Object id) { super(what + " no encontrado: " + id); }
}
