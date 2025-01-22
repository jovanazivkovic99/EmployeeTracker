package com.example.employeetracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Getter
public class ResourceNotFoundException extends RuntimeException{

    private String resourceName;
    private String resourceKey;

    public ResourceNotFoundException(String resourceName, Object resourceKey) {
        super(String.format("%s with '%s' not found", resourceName, resourceKey));
        this.resourceName = resourceName;
        this.resourceKey = resourceKey.toString();
    }
}
