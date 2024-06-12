package com.example.platform.jpa;

import com.example.platform.vo.Status;
import com.example.platform.vo.Location;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class TaxiInfo {
    @Id
    private String driverId;
    @Enumerated(EnumType.STRING)
    private Location areaCode;
    @Enumerated(EnumType.STRING)
    private Status status;
}
