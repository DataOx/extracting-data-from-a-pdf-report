package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "update_attachment")
public class UpdateAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long id;
    public String wellNo;
    public String tgTarget;
    public String profile;
    public String area;
    public String team;
    public String kocTeamLeader;
    public String RIG;
    public String drillingBHA;
    public String presentActivity;
    public String formation;
    public Date date;
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "email_id")
    public List<BITHydraulicsEntity> BITHydraulics;
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "email_id")
    public List<NonProductiveTimeEntity> nonProductiveTime;

}
