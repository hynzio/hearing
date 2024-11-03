package org.hdmd.hearingdemo.repository;

import org.hdmd.hearingdemo.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {


}
