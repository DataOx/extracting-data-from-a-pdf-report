package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "email")
public class EmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long id;
    @Column(name = "attachment_name")
    public String attachmentName;
    public boolean isHandled;
    public boolean hasSendingError;
    public Date sendingTime;
    @Column(name = "receiving_time")
    public LocalDate receivingTime;

}
