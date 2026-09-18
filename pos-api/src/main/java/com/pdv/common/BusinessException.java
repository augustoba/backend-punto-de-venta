package com.pdv.common;

/** Regla de negocio incumplida (se responde 400 con el mensaje). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
