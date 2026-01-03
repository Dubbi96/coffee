# 변경 사항 메모 - Request ID: 30d26e78-ff34-404c-9898-f52d628de3fa

## 작업 개요
1. 이미지 URL 업데이트 및 삭제 로직 개선
2. 사용되지 않는 파일 정리

---

## 1. 영향받는 API 엔드포인트 상세

### 배경
Deprecated된 multipart API를 대체하는 `/url` 엔드포인트들에 이미지 업데이트 로직을 적용하고, approval 삭제 시 GCS 파일 삭제 기능을 추가했습니다.

---

### 6. `POST /app-user/sign-up/url` - 신규 계정 생성(URL 기반)

**변경 내용:**
- **메서드**: `AppUserService.signUpWithUrls`
- **설명**: Deprecated된 multipart 회원가입을 대체하는 URL 기반 회원가입
- **동작**: FE가 먼저 `/gcs/file` 또는 `/gcs/files`로 업로드해 URL을 획득한 뒤, 여기 API에는 파일이 아닌 URL(String)만 전달
- **파일 삭제 로직**: 없음 (생성 요청이므로)

**영향받는 필드:**
- VICE_ADMIN 계열: `idCardUrl` (identificationPhotoUrl)
- VILLAGE_HEAD: `identificationPhotoUrl`, `contractFileUrl`, `bankbookPhotoUrl`

**요청 예시:**
```json
{
  "userId": "hong123",
  "username": "홍길동",
  "password": "pw",
  "role": "VILLAGE_HEAD",
  "sectionId": 1,
  "bankName": "KB",
  "accountInfo": "123-45-6789",
  "identificationPhotoUrl": "https://storage.googleapis.com/...",
  "contractFileUrl": "https://storage.googleapis.com/...",
  "bankbookPhotoUrl": "https://storage.googleapis.com/..."
}
```

---

### 7. `POST /approval/village-head/url` - 면장 생성 승인 요청(URL 기반)

**변경 내용:**
- **메서드**: `ApprovalFacadeService.processVillageHeadCreation` → `AppUserService.requestApprovalToCreateVillageHead`
- **설명**: Deprecated된 multipart 승인 요청을 대체하는 URL 기반 승인 요청
- **동작**: FE가 먼저 `/gcs`로 업로드해 URL을 획득한 뒤, 여기 API에는 파일이 아닌 URL(String)만 전달
- **파일 삭제 로직**: 없음 (생성 요청이므로)

**영향받는 필드:**
- `identificationPhotoUrl`
- `contractFileUrl`
- `bankbookPhotoUrl`

**요청 예시:**
```
POST /approval/village-head/url?approverId=1
Content-Type: application/json

{
  "userId": "village_head_01",
  "password": "password",
  "username": "면장",
  "bankName": "국민은행",
  "accountInfo": "123-456-789",
  "sectionId": 1,
  "identificationPhotoUrl": "https://storage.googleapis.com/...",
  "contractFileUrl": "https://storage.googleapis.com/...",
  "bankbookPhotoUrl": "https://storage.googleapis.com/..."
}
```

---

### 8. `POST /approval/farmer/url` - 농부 생성 승인 요청(URL 기반)

**변경 내용:**
- **메서드**: `ApprovalFacadeService.processFarmerCreation` → `AppUserService.requestApprovalToCreateFarmer`
- **설명**: Deprecated된 multipart 승인 요청을 대체하는 URL 기반 승인 요청
- **동작**: FE가 먼저 `/gcs` API로 업로드해 URL을 획득한 뒤, 여기 API에는 URL(String)만 전달
- **파일 삭제 로직**: 없음 (생성 요청이므로)

**영향받는 필드:**
- `identificationPhotoUrl`

**요청 예시:**
```
POST /approval/farmer/url?approverId=1
Content-Type: application/json

{
  "name": "농부",
  "villageHeadId": 1,
  "identificationPhotoUrl": "https://storage.googleapis.com/..."
}
```

---

## 2. 사용되지 않는 파일 정리

### 삭제된 파일 (6개)

#### DTO 파일 (3개)
1. `src/main/java/com/coffee/atom/dto/appuser/AppUserStatusUpdateRequestDto.java`
   - Deprecated된 multipart API용 DTO
   - URL 기반 API로 대체되어 사용되지 않음

2. `src/main/java/com/coffee/atom/dto/appuser/AppUserResponseDto.java`
   - 어디서도 사용되지 않음

3. `src/main/java/com/coffee/atom/dto/file/FileDownloadResponseDto.java`
   - 어디서도 사용되지 않음

#### Common 파일 (2개)
4. `src/main/java/com/coffee/atom/common/FileVO.java`
   - `FileDto`를 사용하지만 `FileVO` 자체는 사용되지 않음

5. `src/main/java/com/coffee/atom/common/FileType.java`
   - enum이지만 어디서도 사용되지 않음

#### Service 파일 (1개)
6. `src/main/java/com/coffee/atom/service/approval/ApprovalProcessingService.java`
   - 빈 서비스 클래스 (필드만 있고 메서드 없음)
   - 어디서도 사용되지 않음

### 수정된 파일

#### ApprovalService.java
- `requestedInstanceRepository` 필드 제거
  - 선언만 되어 있고 실제로 사용되지 않았음

---

## 영향받는 API 엔드포인트 상세

### 1. `PATCH /app-user/url` - 내 정보 수정(URL 기반)

**변경 내용:**
- **메서드**: `AppUserService.updateAppUserStatusWithUrl`
- **변경 전**: URL이 제공되면 무조건 기존 파일 삭제 후 새 URL 저장
- **변경 후**: 기존 URL과 새 URL을 비교하여 **다를 때만** 기존 파일 삭제
  - 새 이미지로 변경 시: 기존 파일 삭제 (GCS) → 새 URL 저장
  - 이미지 미변경 시: 삭제하지 않음 → 기존 URL 유지

**영향받는 필드:**
- `idCardUrl` (VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER만)

**요청 예시:**
```json
{
  "username": "홍길동",
  "password": "newPassword",
  "idCardUrl": "https://storage.googleapis.com/..." // 새 URL 또는 기존 URL
}
```

---

### 2. `PATCH /app-user/vice-admin/{viceAdminId}/url` - 부 관리자 정보 수정(URL 기반)

**변경 내용:**
- **메서드**: `AppUserService.updateViceAdminWithUrl`
- **변경 전**: URL이 제공되면 무조건 기존 파일 삭제 후 새 URL 저장
- **변경 후**: 기존 URL과 새 URL을 비교하여 **다를 때만** 기존 파일 삭제
  - 새 이미지로 변경 시: 기존 파일 삭제 (GCS) → 새 URL 저장
  - 이미지 미변경 시: 삭제하지 않음 → 기존 URL 유지

**영향받는 필드:**
- `idCardUrl`

**요청 예시:**
```json
{
  "username": "부관리자",
  "userId": "vice_admin_01",
  "areaId": 1,
  "idCardUrl": "https://storage.googleapis.com/..." // 새 URL 또는 기존 URL
}
```

---

### 3. `PATCH /approval/village-head/url` - 면장 수정 승인 요청(URL 기반)

**변경 내용:**
- **메서드**: `AppUserService.requestApprovalToUpdateVillageHead`
- **변경 전**: URL 기반 요청 시 무조건 기존 파일 삭제
- **변경 후**: 각 이미지 URL별로 기존 URL과 새 URL을 비교하여 **다를 때만** 기존 파일 삭제
  - `identificationPhotoUrl`: 변경 시에만 삭제
  - `contractFileUrl`: 변경 시에만 삭제
  - `bankbookPhotoUrl`: 변경 시에만 삭제

**영향받는 필드:**
- `identificationPhotoUrl`
- `contractFileUrl`
- `bankbookPhotoUrl`

**요청 예시:**
```json
{
  "id": 1,
  "userId": "village_head_01",
  "password": "newPassword",
  "username": "면장",
  "bankName": "국민은행",
  "accountInfo": "123-456-789",
  "sectionId": 1,
  "identificationPhotoUrl": "https://storage.googleapis.com/...", // 새 URL 또는 기존 URL
  "contractFileUrl": "https://storage.googleapis.com/...", // 새 URL 또는 기존 URL
  "bankbookPhotoUrl": "https://storage.googleapis.com/..." // 새 URL 또는 기존 URL
}
```

---

### 4. `PATCH /approval/farmer/{farmerId}/url` - 농부 수정 승인 요청(URL 기반)

**변경 내용:**
- **메서드**: `AppUserService.requestApprovalToUpdateFarmer`
- **변경 전**: URL 기반 요청 시 기존 파일 삭제 로직 없음
- **변경 후**: 기존 URL과 새 URL을 비교하여 **다를 때만** 기존 파일 삭제
  - 새 이미지로 변경 시: 기존 파일 삭제 (GCS) → 새 URL 저장
  - 이미지 미변경 시: 삭제하지 않음 → 기존 URL 유지

**영향받는 필드:**
- `identificationPhotoUrl`

**요청 예시:**
```json
{
  "name": "농부",
  "villageHeadId": 1,
  "identificationPhotoUrl": "https://storage.googleapis.com/..." // 새 URL 또는 기존 URL
}
```

---

### 5. `PATCH /approval/approve/{approvalId}` - 요청 승인 처리

**변경 내용:**
- **메서드**: `ApprovalService.handleDeleteApproval` (DELETE 메서드인 경우만)
- **변경 전**: 엔티티 삭제만 수행, GCS 파일 삭제 없음
- **변경 후**: 엔티티 삭제 전에 관련 GCS 파일을 먼저 삭제

**영향받는 케이스:**

#### 5.1 FARMER 삭제 승인
- **삭제되는 파일**: `identificationPhotoUrl`
- **동작**: Farmer 엔티티 삭제 전에 `identificationPhotoUrl`이 GCS에서 삭제됨

#### 5.2 VILLAGE_HEAD 삭제 승인
- **삭제되는 파일**: 
  - `identificationPhotoUrl`
  - `contractUrl`
  - `bankbookUrl`
- **동작**: AppUser 엔티티 삭제 전에 모든 관련 이미지 파일이 GCS에서 삭제됨

#### 5.3 VICE_ADMIN 삭제 승인
- **삭제되는 파일**: `idCardUrl`
- **동작**: AppUser 엔티티 삭제 전에 `idCardUrl`이 GCS에서 삭제됨

#### 5.4 SECTION, PURCHASE 삭제 승인
- **변경 없음**: 파일이 없으므로 변경 사항 없음

**요청 예시:**
```
PATCH /approval/approve/123
(헤더에 access-token 필요)
```

**주의사항:**
- Approval의 `method`가 `DELETE`인 경우에만 파일 삭제 로직 실행
- `CREATE`, `UPDATE` 메서드인 경우 변경 없음
- 승인 처리 시점에 파일 삭제 (요청 생성 시점이 아님)
- 거절 시에는 파일 삭제하지 않음

---

## 변경 사항 없음

### Deprecated API (변경 없음)
- `PATCH /app-user` (multipart) - Deprecated
- `PATCH /app-user/vice-admin/{viceAdminId}` (multipart) - Deprecated
- `POST /approval/village-head` (multipart) - Deprecated
- `PATCH /approval/village-head` (multipart) - Deprecated
- `POST /approval/farmer` (multipart) - Deprecated
- `PATCH /approval/farmer/{farmerId}` (multipart) - Deprecated

**참고**: Deprecated된 multipart API들은 기존 동작 유지, 변경 사항 없음

---

## 테스트 권장 사항

### 이미지 업데이트 로직 테스트
1. **새 이미지로 변경하는 경우**
   - 기존 파일이 GCS에서 삭제되는지 확인
   - 새 URL이 저장되는지 확인

2. **이미지 미변경하는 경우**
   - 기존 파일이 삭제되지 않는지 확인
   - 기존 URL이 유지되는지 확인

3. **null URL 전달하는 경우**
   - 삭제되지 않는지 확인

### Approval 삭제 시 파일 삭제 테스트
1. **FARMER 삭제 승인**
   - `identificationPhotoUrl`이 GCS에서 삭제되는지 확인

2. **VILLAGE_HEAD 삭제 승인**
   - `identificationPhotoUrl`, `contractUrl`, `bankbookUrl`이 모두 GCS에서 삭제되는지 확인

3. **VICE_ADMIN 삭제 승인**
   - `idCardUrl`이 GCS에서 삭제되는지 확인

---

## 주의사항

1. **기존 multipart API는 변경 없음**
   - Deprecated된 API들은 기존 동작 유지
   - URL 기반 API만 개선됨

2. **GCS 파일 삭제는 승인 시점에 실행**
   - Approval 요청 생성 시점이 아닌 승인 처리 시점에 파일 삭제
   - 거절 시에는 파일 삭제하지 않음

3. **파일 삭제 실패 시**
   - `GCSUtil.deleteFileFromGCS()`에서 예외 발생 가능
   - 엔티티 삭제는 트랜잭션 내에서 수행되므로 롤백 가능

---

## 파일 변경 내역

### 수정된 파일
- `src/main/java/com/coffee/atom/service/AppUserService.java`
- `src/main/java/com/coffee/atom/service/approval/ApprovalService.java`

### 삭제된 파일
- `src/main/java/com/coffee/atom/dto/appuser/AppUserStatusUpdateRequestDto.java`
- `src/main/java/com/coffee/atom/dto/appuser/AppUserResponseDto.java`
- `src/main/java/com/coffee/atom/dto/file/FileDownloadResponseDto.java`
- `src/main/java/com/coffee/atom/common/FileVO.java`
- `src/main/java/com/coffee/atom/common/FileType.java`
- `src/main/java/com/coffee/atom/service/approval/ApprovalProcessingService.java`

---

작성일: 2025-01-XX
Request ID: 30d26e78-ff34-404c-9898-f52d628de3fa

