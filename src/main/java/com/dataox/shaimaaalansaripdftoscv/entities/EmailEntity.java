package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

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
    public boolean isHandled;
    public boolean hasSendingError;
    public LocalDate sendingTime;
    @Column(name = "receiving_time")
    public LocalDate receivingTime;
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "update_attachment_id")
    public UpdateAttachmentEntity updateAttachment;

}
