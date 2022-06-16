package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "email_id")
    public List<UpdateAttachmentEntity> updateAttachmentEntities;

}
