package com.campito.backend.common.exception;

public record ExceptionInfo(String message, String path, String timestamp, int status) {}
