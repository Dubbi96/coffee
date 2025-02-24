package com.coffee.atom.util;

import com.coffee.atom.dto.thingplug.ParsedSensorLogDto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParsingUtil {

    private static final int MESSAGE_LENGTH = 102; // 메시지 전체 길이

    public static ParsedSensorLogDto parseMessage(String message) {
        ParsedSensorLogDto data = new ParsedSensorLogDto();
        try {
            if (message.length() != MESSAGE_LENGTH) {
                throw new IllegalArgumentException("Invalid message length: must be 102 characters.");
            }

            int index = 0;

            // 데이터 파싱
            data.setCmd(message.substring(index, index + 2));
            index += 2;

            data.setDeviceNumber(message.substring(index, index + 8));
            index += 8;

            data.setDeviceId(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setPositionSignalStrength(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setPositionSignalThreshold(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setCommSignalStrength(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setCommSignalThreshold(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setWireless235Strength(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setServerTime(Long.parseLong(message.substring(index, index + 8), 16));
            index += 8;

            int deviceSettings = Integer.parseInt(message.substring(index, index + 2), 16);
            data.setDeviceSettings(parseDeviceSettings(deviceSettings));
            index += 2;

            data.setVolumeSettings(parseVolumeSettings(message.substring(index, index + 14)));
            index += 14;

            data.setSilentSettings(parseSilentSettings(message.substring(index, index + 8)));
            index += 8;

            data.setCommInterval(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            int faultInfo = Integer.parseInt(message.substring(index, index + 2), 16);
            data.setFaultInformation(parseFaultInformation(faultInfo));
            index += 2;

            data.setSwVersion(Integer.parseInt(message.substring(index, index + 4), 16));
            index += 4;

            data.setHwVersion(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setButtonCount(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setPositionGuideCount(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setSignalGuideCount(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setGroupNumber(message.substring(index, index + 8));
            index += 8;

            data.setSignalsInGroup(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setGroupPositionNumber(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setDataType(Integer.parseInt(message.substring(index, index + 2), 16));
            index += 2;

            data.setSequenceNumber(Integer.parseInt(message.substring(index, index + 2), 16));

        } catch (Exception e) {
            data.setParsingError(true);
            data.setErrorMessage("Error parsing message: " + e.getMessage());
        }
        return data;
    }

    private static String parseDeviceSettings(int settings) {
        StringBuilder sb = new StringBuilder();
        if ((settings & 1) == 1) sb.append("Female, ");
        else sb.append("Male, ");
        if ((settings & 2) == 2) sb.append("Bird, ");
        else sb.append("Cricket, ");
        if ((settings & 4) == 4) sb.append("Intersection, ");
        else sb.append("Single Road, ");
        if ((settings & 8) == 8) sb.append("Close Proximity, ");
        else if ((settings & 16) == 16) sb.append("Single Proximity, ");
        else sb.append("General Proximity, ");
        if ((settings & 32) == 32) sb.append("Configured, ");
        else sb.append("Not Configured, ");
        if ((settings & 64) == 64) sb.append("Female Priority Broadcast");
        else sb.append("Male Priority Broadcast");
        return sb.toString();
    }

    private static Map<String, Integer> parseVolumeSettings(String message) {
        Map<String, Integer> volumeSettings = new LinkedHashMap<>();
        int index = 0;
        volumeSettings.put("Bird Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        volumeSettings.put("Cricket Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        volumeSettings.put("Dingdong Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        volumeSettings.put("Female Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        volumeSettings.put("Male Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        volumeSettings.put("Minuet Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        volumeSettings.put("System Volume", Integer.parseInt(message.substring(index, index + 2), 16));
        return volumeSettings;
    }

    private static Map<String, Integer> parseSilentSettings(String message) {
        Map<String, Integer> silentSettings = new LinkedHashMap<>();
        int index = 0;
        silentSettings.put("Female Mute 1", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        silentSettings.put("Female Mute 2", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        silentSettings.put("Male Mute 1", Integer.parseInt(message.substring(index, index + 2), 16));
        index += 2;
        silentSettings.put("Male Mute 2", Integer.parseInt(message.substring(index, index + 2), 16));
        return silentSettings;
    }

    private static Map<String, Boolean> parseFaultInformation(int faultInfo) {
        Map<String, Boolean> faultInformation = new LinkedHashMap<>();
        if ((faultInfo & 1) == 1) faultInformation.put("Front Cover Open", true); else faultInformation.put("Front Cover Open", false);
        if ((faultInfo & 2) == 2) faultInformation.put("235.3MHz Receiver Fault", true); else faultInformation.put("235.3MHz Receiver Fault", false);
        if ((faultInfo & 4) == 4) faultInformation.put("358.5MHz Receiver Fault", true); else faultInformation.put("358.5MHz Receiver Fault", false);
        if ((faultInfo & 8) == 8) faultInformation.put("User Button Fault", true); else faultInformation.put("User Button Fault", false);
        if ((faultInfo & 16) == 16) faultInformation.put("Speaker Fault", true); else faultInformation.put("Speaker Fault", false);
        if ((faultInfo & 32) == 32) faultInformation.put("Signal Light Residual Fault", true); else faultInformation.put("Signal Light Residual Fault", false);

        return faultInformation;
    }
}