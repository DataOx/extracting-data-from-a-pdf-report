package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email")
@Entity
@Builder
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
    public Date receivingTime;

}
