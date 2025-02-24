package com.coffee.atom.dto.sensor;

import com.coffee.atom.domain.sensor.SensorGroup;
import lombok.Data;

@Data
public class UnregisteredSensorGroupResponseDto {
    private String sensorGroupId;
    private Double longitude;
    private Double latitude;

    public UnregisteredSensorGroupResponseDto(SensorGroup sensorGroup, Double longitude, Double latitude) {
        this.sensorGroupId = sensorGroup.getId();
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
