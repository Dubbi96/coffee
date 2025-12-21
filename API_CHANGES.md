## 2025-12-21

- `GET /village-heads` 응답 스키마 확장
  - `VillageHeadResponseDto`에 `areaInfo`(areaId, areaName, longitude, latitude)와 `sectionInfo`(sectionId, sectionName, longitude, latitude) 필드가 추가됨.
  - 기존 필드(id, appUserId, appUserName, sectionName, farmerCount)는 그대로 유지.
  - Admin, 부관리자 조회 모두 단일 JPQL로 Area/Section을 함께 조회하여 추가 쿼리 없이 반환.
