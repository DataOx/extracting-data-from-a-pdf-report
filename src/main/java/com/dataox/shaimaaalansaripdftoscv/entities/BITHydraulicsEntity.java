package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "bit_hydraulics")
public class BITHydraulicsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long id;
    public String BIT;
    public String size;
    public String model;
    public String jetSize;
    public String depthIn;
    public String depthOut;
    public String FTG;
    public String hours;
    public String FPH;
    public String serNo;
    public String manufacturer;
    public String RPM;
    public String WOB;
    public String I;
    public String O;
    public String D;
    public String L;
    public String B;
    public String G;
    public String Osecond;
    public String R;
    public String PSI;
    public String liner;
    public String SPM;
    public String GPM;
    public String PHHP;
    public String BHHP;
    public String TORQ;
    public String NVEL;
    public String AVEL;

}
