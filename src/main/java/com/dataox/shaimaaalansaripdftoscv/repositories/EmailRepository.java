package com.dataox.shaimaaalansaripdftoscv.repositories;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends CrudRepository<EmailEntity, Long> {

}
