package com.dataox.shaimaaalansaripdftoscv.repositories;

import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateAttachmentRepository extends CrudRepository<UpdateAttachmentEntity, Long> {

    List<UpdateAttachmentEntity> findAllByOrderByIdAsc();

}