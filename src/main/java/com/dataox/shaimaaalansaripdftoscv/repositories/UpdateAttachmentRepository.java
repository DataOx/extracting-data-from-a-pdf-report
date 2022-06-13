package com.dataox.shaimaaalansaripdftoscv.repositories;

import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdateAttachmentRepository extends CrudRepository<UpdateAttachmentEntity, Long> {

}