package com.scheduler.szz.helpers;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scheduler.szz.model.DBEntry;

@Repository
@Transactional
public interface DBEntryDao extends MongoRepository<DBEntry, String> {
    public DBEntry findByToken(String token);
    public DBEntry deleteByToken(String token);

}
