package com.swp.backend.service;

import com.swp.backend.repository.VehicleDAO;
import com.swp.backend.dto.vehicle.request.FilterVehicleRequest;
import com.swp.backend.dto.vehicle.request.SetVehicleRequest;
import com.swp.backend.dto.vehicle.response.FilterVehicleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleDAO vehicleDAO;

    public boolean setVehicle(SetVehicleRequest setVehicleRequest){
        return vehicleDAO.setVehicle(setVehicleRequest.id(), setVehicleRequest.state()) > 0;
    }

    public List<FilterVehicleResponse> filterVehicleByType(FilterVehicleRequest filterVehicleRequest){
        return vehicleDAO.filterVehicleByType(filterVehicleRequest.vehicle_type());
    }
}
