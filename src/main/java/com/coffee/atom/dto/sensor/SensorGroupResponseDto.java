package com.coffee.atom.dto.sensor;

import com.coffee.atom.domain.sensor.Sensor;
import com.coffee.atom.domain.sensor.SensorGroup;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entity To Dto로 설정하여 service 코드 간소화
 * */
@Data
public class SensorGroupResponseDto {

    private String sensorGroupId;
    private String groupKey;
    private Long sensorCount;
    private Long faultCount;
    private String ssid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SensorDto> sensors;

    @Data
    public static class SensorDto{
        private Long sensorId;
        private String deviceNumber;
        private Long groupPositionNumber;
        private Double deviceId;
        private Double longitude;
        private Double latitude;
        private Long positionSignalStrength;
        private Long positionSignalThreshold;
        private Long communicationSignalStrength;
        private Long communicationSignalThreshold;
        private Long wireless235Strength;
        private List<String> deviceSetting;
        private Long femaleMute1;
        private Long femaleMute2;
        private Long maleMute1;
        private Long maleMute2;
        private Long birdVolume;
        private Long cricketVolume;
        private Long dingdongVolume;
        private Long femaleVolume;
        private Long maleVolume;
        private Long systemVolume;
        private Long communicationInterval;
        private Map<String,Boolean> faultInformation;
        private Long swVersion;
        private Long hwVersion;
        private Long buttonCount;
        private Long positionGuideCount;
        private Long signalGuideCount;
        private String lastlyModifiedWith;
        private LocalDateTime updatedAt;

        public SensorDto(Sensor sensor) {
            this.sensorId = sensor.getId();
            this.deviceNumber = sensor.getDeviceNumber();
            this.deviceId = sensor.getDeviceId();
            this.longitude = sensor.getLongitude();
            this.latitude = sensor.getLatitude();
            this.positionSignalStrength = sensor.getPositionSignalStrength();
            this.positionSignalThreshold = sensor.getPositionSignalThreshold();
            this.communicationSignalStrength = sensor.getCommunicationSignalStrength();
            this.communicationSignalThreshold = sensor.getCommunicationSignalThreshold();
            this.wireless235Strength = sensor.getWireless235Strength();
            this.deviceSetting = sensor.getDeviceSetting();
            this.femaleMute1 = sensor.getFemaleMute1();
            this.femaleMute2 = sensor.getFemaleMute2();
            this.maleMute1 = sensor.getMaleMute1();
            this.maleMute2 = sensor.getMaleMute2();
            this.birdVolume = sensor.getBirdVolume();
            this.cricketVolume = sensor.getCricketVolume();
            this.dingdongVolume = sensor.getDingdongVolume();
            this.femaleVolume = sensor.getFemaleVolume();
            this.maleVolume = sensor.getMaleVolume();
            this.systemVolume = sensor.getSystemVolume();
            this.communicationInterval = sensor.getCommunicationInterval();
            this.faultInformation = sensor.getFaultInformation();
            this.swVersion = sensor.getSwVersion();
            this.hwVersion = sensor.getHwVersion();
            this.buttonCount = sensor.getButtonCount();
            this.positionGuideCount = sensor.getPositionGuideCount();
            this.signalGuideCount = sensor.getSignalGuideCount();
            this.groupPositionNumber = sensor.getGroupPositionNumber();
            this.lastlyModifiedWith = sensor.getLastlyModifiedWith();
            this.updatedAt = sensor.getUpdatedAt();
        }
    }

    public SensorGroupResponseDto(SensorGroup sensorGroup) {
        this.sensorGroupId = sensorGroup.getId();
        this.groupKey = sensorGroup.getGroupKey();
        this.sensorCount = sensorGroup.getSensorCount();
        this.faultCount = sensorGroup.getFaultCount();
        this.ssid = sensorGroup.getSsid();
        this.createdAt = sensorGroup.getCreatedAt();
        this.updatedAt = sensorGroup.getUpdatedAt();
        this.sensors = sensorGroup.getSensors().stream()
                .map(SensorDto::new)
                .collect(Collectors.toList());
    }
}
