package com.mdadzerkin.lambda.repository;

import com.mdadzerkin.lambda.model.Entity;

import java.util.Collection;

public interface DynamoDbRepository<T extends Entity> {
    void save(T entity);

    Collection<T> findAll();
}
