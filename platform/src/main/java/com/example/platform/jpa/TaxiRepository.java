package com.example.platform.jpa;

import com.example.platform.vo.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxiRepository extends JpaRepository<TaxiInfo,String> {
    List<TaxiInfo> findAllByStatus(Status status);
}
