package com.dataox.shaimaaalansaripdftoscv.repositories;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmailRepository extends CrudRepository<EmailEntity, Long> {
    List<EmailEntity> findAllByIsHandledIsFalse();
    List<EmailEntity> findAllByHasSendingErrorIsTrue();
    List<EmailEntity> findAllBySendingTimeGreaterThan(LocalDate date);
    EmailEntity findTopByOrderByReceivingTimeDesc();

}
