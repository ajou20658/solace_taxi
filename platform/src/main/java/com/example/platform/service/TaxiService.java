package com.example.platform.service;

import com.example.platform.vo.Status;
import com.example.platform.jpa.TaxiInfo;
import com.example.platform.jpa.TaxiRepository;
import com.example.platform.vo.DriverInfo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxiService {
    private final TaxiRepository taxiRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    public void addService(DriverInfo driverInfo){
//        TaxiInfo taxiInfo = modelMapper.map(driverInfo, TaxiInfo.class);
        TaxiInfo taxiInfo = new TaxiInfo();
        taxiInfo.setAreaCode(driverInfo.getLocation());
        taxiInfo.setStatus(driverInfo.getStatus());
        taxiInfo.setDriverId(driverInfo.getDriverId());
        taxiRepository.save(taxiInfo);
    }
    public List<TaxiInfo> getDriverIdList(){
        return taxiRepository.findAllByStatus(Status.IDLE);
    }
}
