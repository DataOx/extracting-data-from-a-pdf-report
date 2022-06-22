package com.dataox.shaimaaalansaripdftoscv.domain;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ConvertData {
    private Map<String, byte[]> attachments;
    private List<EmailEntity> correctEmails;
    private List<EmailEntity> failedEmails;
}
