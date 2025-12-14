# API 명세서

## 목차
1. [인증 및 계정 관리](#인증-및-계정-관리)
2. [승인 요청 관리](#승인-요청-관리)
3. [지역 관리](#지역-관리)
4. [섹션 관리](#섹션-관리)
5. [농부 관리](#농부-관리)
6. [수매 관리](#수매-관리)
7. [파일 관리](#파일-관리)
8. [파일 이벤트 로그](#파일-이벤트-로그)
9. [에러 코드 및 예외 처리](#에러-코드-및-예외-처리)

---

## 인증 및 계정 관리

### 1. 로그인
**Endpoint:** `POST /app-user/sign-in`

**설명:**
사용자 ID와 비밀번호를 통해 로그인하고 JWT 액세스 토큰을 발급받습니다.

**Request:**
```json
{
  "userId": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "appUserId": 1,
    "userId": "string",
    "accessToken": "string",
    "role": "ADMIN | VICE_ADMIN_HEAD_OFFICER | VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER | VILLAGE_HEAD"
  }
}
```

**Authorization:**
인증 불필요

**예외 처리:**
- `WRONG_PASSWORD` (code: `FAILURE_MODAL`): 올바르지 않은 아이디 및 비밀번호입니다.
- `ACCOUNT_NOT_FOUND` (code: `NO_USER_FROM_TOKEN`): 존재하지 않는 계정입니다.
- `TOKEN_EXPIRED` (code: `NO_TOKEN`): 토큰이 만료되었습니다.

---

### 2. 계정 생성
**Endpoint:** `POST /app-user/sign-up`

**설명:**
총 관리자(ADMIN)만 계정 생성이 가능합니다. 생성 가능한 역할은 VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER, VILLAGE_HEAD입니다. ADMIN 역할은 생성할 수 없습니다.

VICE_ADMIN 생성 시:
- 한 지역(Area)에는 각 권한(VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER)당 한 명씩만 할당 가능
- areaId 필수 입력

VILLAGE_HEAD 생성 시:
- sectionId 필수 입력
- 은행 정보(bankName, accountInfo) 선택 사항

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `idCardFile` (MultipartFile, optional): 신분증 파일
  - `identificationPhotoFile` (MultipartFile, optional): 신원 확인 사진 파일
  - `contractFile` (MultipartFile, optional): 계약서 파일
  - `bankbookFile` (MultipartFile, optional): 통장 사본 파일
  - `userId` (String, required): 사용자 ID
  - `username` (String, required): 사용자명
  - `password` (String, required): 비밀번호
  - `role` (String, required): 역할 (VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER, VILLAGE_HEAD)
  - `areaId` (Long, optional): 지역 ID (VICE_ADMIN 생성 시 필수)
  - `sectionId` (Long, optional): 섹션 ID (VILLAGE_HEAD 생성 시 필수)
  - `bankName` (String, optional): 은행명
  - `accountInfo` (String, optional): 계좌번호

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": 1
}
```
생성된 계정의 ID를 반환합니다.

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `ADMIN_CREATION_NOT_ALLOWED` (code: `FAILURE_MODAL`): ADMIN 권한으로 계정을 생성할 수 없습니다.
- `NICKNAME_ALREADY_EXISTS` (code: `ALREADY_APPLICANT_EXISTS`): 이미 존재하는 닉네임입니다.
- `VICE_ADMIN_ALREADY_EXISTS_IN_AREA` (code: `FAILURE_MODAL`): 해당 지역에는 이미 해당 권한의 부관리자가 할당되어 있습니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.
- `SECTION_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 섹션은 존재하지 않습니다.
- `SECTION_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인되지 않은 섹션입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `AREA_SECTION_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 섹션으로만 면장을 생성할 수 있습니다.
- `ID_CARD_UPLOAD_FAILED` (code: `FAILURE_MODAL`): ID 카드 업로드 실패
- `UNKNOWN_ERROR` (code: `FAILURE_MODAL`): 알 수 없는 에러입니다.

---

### 3. 내 정보 수정
**Endpoint:** `PATCH /app-user`

**설명:**
모든 사용자 역할에 관계없이 공통으로 사용 가능합니다. ADMIN, VILLAGE_HEAD의 경우 유저명과 비밀번호만 수정 가능하며, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER의 경우 신분증 파일까지 첨부 가능합니다.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `username` (String, required): 수정할 사용자명
  - `password` (String, required): 수정할 비밀번호
  - `idCardFile` (MultipartFile, optional): 신분증 파일 (VICE_ADMIN만 가능)

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `NICKNAME_ALREADY_EXISTS` (code: `ALREADY_APPLICANT_EXISTS`): 이미 존재하는 닉네임입니다.
- `ID_CARD_UPLOAD_FAILED` (code: `FAILURE_MODAL`): ID 카드 업로드 실패
- `UNKNOWN_ERROR` (code: `FAILURE_MODAL`): 알 수 없는 에러입니다.

---

### 4. 면장 목록 조회
**Endpoint:** `GET /app-user/village-heads`

**설명:**
총 관리자로 조회할 경우 면장 전체 목록을 조회하고, 부 관리자로 조회할 경우 해당 부 관리자가 관리하고 있는 지역의 면장들만 조회합니다. 각 면장의 농부 수가 포함됩니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "Id": 1,
      "appUserId": "string",
      "appUserName": "string",
      "sectionName": "string",
      "farmerCount": 5
    }
  ]
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.

---

### 5. 면장 상세 조회
**Endpoint:** `GET /app-user/village-head/{villageHeadId}`

**설명:**
면장 ID를 통해 해당 면장의 상세 정보를 조회합니다.

**Request:**
- Path Parameters:
  - `villageHeadId` (Long, required): 면장 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "userId": "string",
    "username": "string",
    "bankName": "string",
    "accountInfo": "string",
    "identificationPhotoUrl": "string",
    "contractFileUrl": "string",
    "bankbookPhotoUrl": "string",
    "areaInfo": {
      "areaId": 1,
      "longitude": 127.0,
      "latitude": 37.0,
      "areaName": "string"
    },
    "sectionInfo": {
      "sectionId": 1,
      "longitude": 127.0,
      "latitude": 37.0,
      "sectionName": "string"
    }
  }
}
```

**Authorization:**
인증 불필요

**예외 처리:**
- `ACCOUNT_NOT_FOUND` (code: `NO_USER_FROM_TOKEN`): 존재하지 않는 계정입니다.

---

### 6. 부 관리자 목록 조회
**Endpoint:** `GET /app-user/vice-admins`

**설명:**
총 관리자만 전체 부 관리자 목록을 조회할 수 있습니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "id": 1,
      "userName": "string",
      "userId": "string",
      "areaInfo": {
        "areaId": 1,
        "areaName": "string"
      }
    }
  ]
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.

---

### 7. 부 관리자 상세 조회
**Endpoint:** `GET /app-user/vice-admin/{viceAdminId}`

**설명:**
부 관리자 ID를 통해 해당 부 관리자의 상세 정보를 조회합니다.

**Request:**
- Path Parameters:
  - `viceAdminId` (Long, required): 부 관리자 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "id": 1,
    "userId": "string",
    "username": "string",
    "idCardUrl": "string",
    "areaInfo": {
      "areaId": 1,
      "areaName": "string",
      "latitude": 37.0,
      "longitude": 127.0
    }
  }
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 부 관리자입니다.

---

### 8. 부 관리자 정보 수정
**Endpoint:** `PATCH /app-user/vice-admin/{viceAdminId}`

**설명:**
총 관리자만 사용 가능합니다. 수정 가능한 정보는 이름, 유저아이디, 관리지역, 신분증 이미지입니다. 지역 변경 시 새로운 지역에 이미 같은 역할의 부 관리자가 배정되어 있으면 예외가 발생합니다.

**Request:**
- Content-Type: `multipart/form-data`
- Path Parameters:
  - `viceAdminId` (Long, required): 부 관리자 ID
- Parameters:
  - `username` (String, required): 수정할 이름
  - `userId` (String, required): 수정할 사용자 ID
  - `areaId` (Long, required): 관리 지역 ID
  - `idCardFile` (MultipartFile, optional): 신분증 파일

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `ACCOUNT_NOT_FOUND` (code: `NO_USER_FROM_TOKEN`): 존재하지 않는 계정입니다.
- `VICE_ADMIN_AREA_CHANGE_NOT_ALLOWED` (code: `FAILURE_MODAL`): 라오스 부관리자는 지역 변경이 불가능합니다.
- `VICE_ADMIN_ALREADY_EXISTS_IN_AREA` (code: `FAILURE_MODAL`): 해당 지역에는 이미 해당 권한의 부관리자가 할당되어 있습니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.
- `ID_CARD_UPLOAD_FAILED` (code: `FAILURE_MODAL`): ID 카드 업로드 실패
- `UNKNOWN_ERROR` (code: `FAILURE_MODAL`): 알 수 없는 에러입니다.

---

### 9. 내 정보 조회
**Endpoint:** `GET /app-user/my`

**설명:**
로그인한 사용자의 정보를 역할에 따라 조회합니다. AppUser 정보는 공통으로 포함되며, 역할에 따라 추가 정보가 포함됩니다.

**Request:**
없음

**Response:**
역할에 따라 다른 형태의 응답이 반환됩니다.

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `ACCOUNT_NOT_FOUND` (code: `NO_USER_FROM_TOKEN`): 존재하지 않는 계정입니다.

---

## 승인 요청 관리

### 1. 요청 목록 조회
**Endpoint:** `GET /approval`

**설명:**
승인 요청 목록을 상태 및 서비스 타입으로 필터링하여 조회합니다. 다중 선택 필터 및 페이지네이션을 지원합니다. VILLAGE_HEAD는 조회 불가합니다.

역할별 조회 범위:
- ADMIN: 본인이 승인자로 지정된 요청 중, VICE_ADMIN_HEAD_OFFICER 또는 VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER가 요청한 것만 조회
- VICE_ADMIN_HEAD_OFFICER: 본인 또는 같은 Area의 VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER가 요청한 것만 조회
- VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER: 본인이 요청한 것만 조회

**Request:**
- Query Parameters:
  - `statuses` (List<Status>, optional): 요청 상태 목록 (PENDING, APPROVED, REJECTED)
  - `serviceTypes` (List<ServiceType>, optional): 서비스 타입 목록 (VILLAGE_HEAD, FARMER, PURCHASE, SECTION)
  - `page` (Integer, optional): 페이지 번호 (기본값: 0)
  - `size` (Integer, optional): 페이지 크기
  - `sort` (String, optional): 정렬 기준 (기본값: id,DESC)

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "content": [
      {
        "id": 1,
        "requesterId": 2,
        "requesterName": "string",
        "approverName": "string",
        "status": "PENDING",
        "method": "CREATE",
        "serviceType": "VILLAGE_HEAD",
        "createdAt": "2025-12-12T12:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 100,
    "totalPages": 10
  }
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `ROLE_NOT_ALLOWED_APPROVAL_LIST` (code: `FAILURE_MODAL`): 해당 권한으로 요청 목록을 조회할 수 없습니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.

---

### 2. 요청 승인 처리
**Endpoint:** `PATCH /approval/approve/{approvalId}`

**설명:**
approvalId를 갖는 요청을 승인 처리합니다. 승인 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. VILLAGE_HEAD는 승인 불가합니다. 본인이 승인자로 지정된 요청만 승인 가능하며, 승인 시 요청 유형(CREATE/UPDATE/DELETE)에 따라 해당 인스턴스가 실제로 생성/수정/삭제됩니다.

**Request:**
- Path Parameters:
  - `approvalId` (Long, required): 승인 요청 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `SECTION_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인되지 않은 섹션입니다.
- `VILLAGE_HEAD_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인된 면장이 아닙니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 3. 요청 거절 처리
**Endpoint:** `PATCH /approval/reject/{approvalId}`

**설명:**
approvalId를 갖는 요청을 거절 처리합니다. 거절 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. VILLAGE_HEAD는 거절 불가합니다. 본인이 승인자로 지정된 요청만 거절 가능하며, 거절 사유(rejectedReason)는 필수 입력입니다.

거절 처리 동작:
- CREATE 요청: 생성 대기 중인 인스턴스 삭제
- UPDATE 요청: DB 변경 없음
- DELETE 요청: 삭제 대기 중이던 인스턴스 복구 (isApproved = true로 변경)

**Request:**
- Path Parameters:
  - `approvalId` (Long, required): 승인 요청 ID
- Body:
```json
{
  "rejectedReason": "string"
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 4. 요청 상세 조회
**Endpoint:** `GET /approval/{approvalId}`

**설명:**
approvalId에 해당하는 요청 상세 정보를 조회합니다. Approval 테이블의 요청 데이터를 기준으로 반환하며, 요청 유형(EntityType)에 따라 응답 형태(DTO)가 변경됩니다. 요청 상태(Status)는 PENDING, APPROVED, REJECTED입니다.

**Request:**
- Path Parameters:
  - `approvalId` (Long, required): 승인 요청 ID

**Response:**
요청 유형에 따라 다른 형태의 응답이 반환됩니다.

**Authorization:**
인증 불필요

**예외 처리:**
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.

---

### 5. 면장 생성 승인 요청
**Endpoint:** `POST /approval/village-head`

**설명:**
면장 계정 생성을 위한 승인 요청을 생성합니다. 요청 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. VICE_ADMIN의 경우 본인이 배정된 Area 내의 Section에만 면장 배정 가능하며, sectionId는 본인 Area 내의 Section이어야 합니다. 승인자는 approverId로 지정(ADMIN ID)합니다.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `identificationPhoto` (MultipartFile, optional): 면장 신원 확인 용 이미지
  - `contractFile` (MultipartFile, optional): 계약서 파일
  - `bankbookPhoto` (MultipartFile, optional): 통장 사본 이미지
  - `userId` (String, required): 면장 User ID
  - `username` (String, required): 면장 User명
  - `password` (String, required): 면장 비밀번호
  - `bankName` (String, optional): 은행 명
  - `accountInfo` (String, optional): 계좌번호
  - `sectionId` (Long, required): 배정 할 Section ID
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `AREA_SECTION_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 섹션으로만 면장을 생성할 수 있습니다.
- `SECTION_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 섹션은 존재하지 않습니다.
- `SECTION_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인되지 않은 섹션입니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 6. 농부 생성 승인 요청
**Endpoint:** `POST /approval/farmer`

**설명:**
농부 계정 생성을 위한 승인 요청을 생성합니다. 요청 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. VICE_ADMIN의 경우 villageHeadId는 본인이 배정된 Area 내의 면장이어야 하며, 본인 Area 외의 면장에게 농부를 배정할 수 없습니다. 승인자는 approverId로 지정(ADMIN ID)합니다.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `identificationPhoto` (MultipartFile, optional): 농부 신원 확인 용 이미지
  - `name` (String, required): 농부 이름
  - `villageHeadId` (Long, required): 농부가 소속된 면장 ID
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `FARMER_AREA_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 면장 하위에만 농부를 생성할 수 있습니다.
- `VILLAGE_HEAD_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 면장입니다.
- `VILLAGE_HEAD_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인된 면장이 아닙니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 7. 수매 승인 요청
**Endpoint:** `POST /approval/purchase`

**설명:**
수매 정보를 위한 승인 요청을 생성합니다. 요청 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. Purchase는 면장과 1:1 관계로 기록되며, villageHeadId는 필수 입력입니다. VICE_ADMIN의 경우 본인 Area 내의 면장만 지정 가능합니다. 승인자는 approverId로 지정(ADMIN ID)합니다.

**Request:**
- Parameters:
  - `approverId` (Long, required): 승인자 ADMIN ID
- Body:
```json
{
  "id": 0,
  "villageHeadId": 1,
  "deduction": 0,
  "paymentAmount": 0,
  "purchaseDate": "2025-12-12",
  "quantity": 0,
  "totalPrice": 0,
  "unitPrice": 0,
  "remarks": "string"
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `VILLAGE_HEAD_AREA_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 면장만 선택할 수 있습니다.
- `VILLAGE_HEAD_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 면장입니다.
- `VILLAGE_HEAD_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인된 면장이 아닙니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 8. 섹션 생성 승인 요청
**Endpoint:** `POST /approval/section`

**설명:**
섹션 생성 승인 요청을 생성합니다. 요청 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. 실제 섹션 생성은 ADMIN만 가능하며, VICE_ADMIN은 섹션 생성/삭제/수정을 위한 Approval 요청만 가능합니다. VICE_ADMIN의 경우 areaId는 본인이 배정된 Area만 사용 가능합니다(입력해도 무시됨). 승인자는 approverId로 지정(ADMIN ID)합니다.

**Request:**
- Parameters:
  - `approverId` (Long, required): 승인자 ADMIN ID
- Body:
```json
{
  "id": 0,
  "longitude": 127.0,
  "latitude": 37.0,
  "sectionName": "string",
  "areaId": 1
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 9. 면장 수정 승인 요청
**Endpoint:** `PATCH /approval/village-head`

**설명:**
면장 계정 수정을 위한 승인 요청을 생성합니다. 요청 가능한 역할은 ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER입니다. VICE_ADMIN의 경우 sectionId는 본인이 배정된 Area 내의 Section이어야 하며, 본인 Area 외의 Section에 면장을 배정할 수 없습니다. 승인자는 approverId로 지정(ADMIN ID)합니다.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `appUserId` (Long, required): 면장 ID
  - `identificationPhoto` (MultipartFile, optional): 면장 신원 확인 용 이미지
  - `contractFile` (MultipartFile, optional): 계약서 파일
  - `bankbookPhoto` (MultipartFile, optional): 통장 사본 이미지
  - `userId` (String, required): 면장 User ID
  - `username` (String, required): 면장 User명
  - `password` (String, required): 면장 비밀번호
  - `bankName` (String, optional): 은행 명
  - `accountInfo` (String, optional): 계좌번호
  - `sectionId` (Long, required): 배정 할 Section ID
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `ACCOUNT_NOT_FOUND` (code: `NO_USER_FROM_TOKEN`): 존재하지 않는 계정입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `VILLAGE_HEAD_UPDATE_AREA_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 면장만 수정할 수 있습니다.
- `VILLAGE_HEAD_SECTION_ASSIGN_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 섹션으로만 배정할 수 있습니다.
- `SECTION_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 섹션은 존재하지 않습니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 10. 농부 수정 승인 요청
**Endpoint:** `PATCH /approval/farmer/{farmerId}`

**설명:**
기존 농부 정보 수정을 위한 승인 요청을 생성합니다. 수정 대상 farmerId는 필수이며, 승인자는 approverId로 지정합니다.

**Request:**
- Content-Type: `multipart/form-data`
- Path Parameters:
  - `farmerId` (Long, required): 농부 ID
- Parameters:
  - `identificationPhoto` (MultipartFile, optional): 농부 신원 확인 용 이미지
  - `name` (String, required): 농부 이름
  - `villageHeadId` (Long, required): 농부가 소속된 면장 ID
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 11. 농부 삭제 승인 요청
**Endpoint:** `DELETE /approval/farmer/{farmerId}`

**설명:**
기존 농부 정보 삭제를 위한 승인 요청을 생성합니다. 삭제 대상 farmerId는 필수이며, 승인자는 approverId로 지정합니다.

**Request:**
- Path Parameters:
  - `farmerId` (Long, required): 농부 ID
- Query Parameters:
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `FARMER_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 농부는 존재하지 않습니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 12. 면장 삭제 승인 요청
**Endpoint:** `DELETE /approval/village-head/{villageHeadId}`

**설명:**
기존 면장 정보 삭제를 위한 승인 요청을 생성합니다. 삭제 대상 villageHeadId는 필수이며, 승인자는 approverId로 지정합니다.

**Request:**
- Path Parameters:
  - `villageHeadId` (Long, required): 면장 ID
- Query Parameters:
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `VILLAGE_HEAD_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 면장입니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 13. 섹션 삭제 승인 요청
**Endpoint:** `DELETE /approval/section/{sectionId}`

**설명:**
기존 섹션 정보 삭제를 위한 승인 요청을 생성합니다. 삭제 대상 sectionId는 필수이며, 승인자는 approverId로 지정합니다.

**Request:**
- Path Parameters:
  - `sectionId` (Long, required): 섹션 ID
- Query Parameters:
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `SECTION_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 섹션은 존재하지 않습니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 14. 구매 이력 삭제 승인 요청
**Endpoint:** `DELETE /approval/purchase/{purchaseId}`

**설명:**
기존 구매 이력 삭제를 위한 승인 요청을 생성합니다. 삭제 대상 purchaseId는 필수이며, 승인자는 approverId로 지정합니다.

**Request:**
- Path Parameters:
  - `purchaseId` (Long, required): 구매 이력 ID
- Query Parameters:
  - `approverId` (Long, required): 승인자 ADMIN ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.
- `PURCHASE_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 구매 이력입니다.
- `JSON_PROCESSING_ERROR` (code: `FAILURE_MODAL`): 요청 데이터를 파싱할 수 없습니다.

---

### 15. 요청 삭제
**Endpoint:** `DELETE /approval/{approvalId}`

**설명:**
본인이 요청한 경우에만 삭제 가능합니다. 타인이 요청한 경우 UNAUTHORIZED_SERVICE 에러가 발생합니다.

**Request:**
- Path Parameters:
  - `approvalId` (Long, required): 승인 요청 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `UNAUTHORIZED_SERVICE` (code: `ACCESS_DENIED`): 권한 외 요청입니다.
- `SUBJECT_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 요청입니다.

---

## 지역 관리

### 1. 지역 생성
**Endpoint:** `POST /area`

**설명:**
지역 명, 지역 위도, 경도로 신규 지역을 생성합니다. 신규 지역 생성은 ADMIN 권한만 사용 가능하며, 타 권한의 AppUser로 해당 서비스를 호출하면 UNAUTHORIZED 메시지가 반환됩니다.

**Request:**
- Body:
```json
{
  "areaName": "string",
  "latitude": 37.0,
  "longitude": 127.0
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.

---

### 2. 지역 및 섹션 조회
**Endpoint:** `GET /area/with-sections`

**설명:**
모든 지역과 해당 지역에 속한 섹션을 조회합니다. 지역은 areaName의 순서로 정렬되고, 섹션은 sectionName의 순서로 정렬됩니다. 모든 사용자가 조회 가능하며, 승인된 섹션만 조회됩니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "id": 1,
      "areaName": "string",
      "latitude": 37.0,
      "longitude": 127.0,
      "sections": [
        {
          "id": 1,
          "longitude": 127.0,
          "latitude": 37.0,
          "sectionName": "string"
        }
      ]
    }
  ]
}
```

**Authorization:**
인증 불필요

**예외 처리:**
없음

---

### 3. 지역만 조회
**Endpoint:** `GET /area`

**설명:**
역할(Role)에 따라 조회되는 지역 목록이 다릅니다. ADMIN은 모든 지역 목록을 조회하고, VICE_ADMIN_HEAD_OFFICER / VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER는 본인이 배정된 지역만 조회합니다. 지역은 areaName의 순서로 정렬됩니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "id": 1,
      "areaName": "string",
      "latitude": 37.0,
      "longitude": 127.0
    }
  ]
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.

---

### 4. 지역 내 섹션 조회
**Endpoint:** `GET /area/{areaId}/with-sections`

**설명:**
지정한 지역 ID에 해당하는 지역과 해당 지역에 속한 섹션을 조회합니다. 섹션은 sectionName의 순서로 정렬됩니다.

**Request:**
- Path Parameters:
  - `areaId` (Long, required): 지역 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "id": 1,
      "areaName": "string",
      "latitude": 37.0,
      "longitude": 127.0,
      "sections": [
        {
          "id": 1,
          "longitude": 127.0,
          "latitude": 37.0,
          "sectionName": "string"
        }
      ]
    }
  ]
}
```

**Authorization:**
인증 불필요

**예외 처리:**
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.

---

### 5. 내 지역 조회
**Endpoint:** `GET /area/my`

**설명:**
부 관리자의 배정된 지역을 조회합니다. VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한만 사용 가능하며, 본인이 배정된 지역(Area) 정보를 반환합니다. 배정되지 않은 경우 예외가 발생합니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "id": 1,
    "areaName": "string",
    "latitude": 37.0,
    "longitude": 127.0
  }
}
```

**Authorization:**
VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `UNAUTHORIZED_SERVICE` (code: `ACCESS_DENIED`): 권한 외 요청입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.

---

### 6. 지역 단건 조회
**Endpoint:** `GET /area/{areaId}`

**설명:**
지역 ID를 기준으로 해당 지역 정보를 조회합니다. 경도, 위도, 지역명 등을 포함한 정보를 반환합니다.

**Request:**
- Path Parameters:
  - `areaId` (Long, required): 지역 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "id": 1,
    "areaName": "string",
    "latitude": 37.0,
    "longitude": 127.0
  }
}
```

**Authorization:**
인증 불필요

**예외 처리:**
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.

---

### 7. 지역 삭제
**Endpoint:** `DELETE /area/{areaId}`

**설명:**
지정한 지역 ID에 해당하는 지역(Area)을 즉시 삭제합니다. ADMIN 권한을 가진 사용자만 요청할 수 있으며, 타 권한 사용자가 요청할 경우 UNAUTHORIZED 예외가 발생합니다.

**Request:**
- Path Parameters:
  - `areaId` (Long, required): 지역 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.
- `DATA_INTEGRITY_VIOLATION` (code: `E005`): 삭제할 수 없습니다. 해당 항목이 다른 데이터에서 참조되고 있습니다.

---

## 섹션 관리

### 1. 섹션 생성
**Endpoint:** `POST /section`

**설명:**
섹션 명, 섹션 위도, 경도로 신규 섹션을 생성합니다. 섹션 생성은 ADMIN 권한만 사용 가능하며, VICE_ADMIN은 섹션 생성/삭제/수정을 위한 Approval 요청만 가능합니다. 타 권한의 AppUser로 해당 서비스를 호출하면 UNAUTHORIZED 메시지가 반환됩니다.

**Request:**
- Body:
```json
{
  "id": 0,
  "longitude": 127.0,
  "latitude": 37.0,
  "sectionName": "string",
  "areaId": 1
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.

---

### 2. 섹션 삭제
**Endpoint:** `DELETE /section/{sectionId}`

**설명:**
지정한 섹션 ID에 해당하는 섹션을 즉시 삭제합니다. 섹션 삭제는 ADMIN 권한만 사용 가능하며, VICE_ADMIN은 섹션 삭제를 위한 Approval 요청만 가능합니다. 타 권한의 AppUser로 해당 서비스를 호출하면 UNAUTHORIZED 메시지가 반환됩니다.

**Request:**
- Path Parameters:
  - `sectionId` (Long, required): 섹션 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
ADMIN 권한 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `SECTION_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 섹션은 존재하지 않습니다.
- `DATA_INTEGRITY_VIOLATION` (code: `E005`): 삭제할 수 없습니다. 해당 항목이 다른 데이터에서 참조되고 있습니다.

---

### 3. 섹션 단건 조회
**Endpoint:** `GET /section/{sectionId}`

**설명:**
지정한 섹션 ID에 해당하는 섹션 정보를 조회합니다. 섹션명, 경도(longitude), 위도(latitude)를 포함한 정보를 반환합니다. 해당 ID가 존재하지 않을 경우 예외가 발생합니다.

**Request:**
- Path Parameters:
  - `sectionId` (Long, required): 섹션 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "id": 1,
    "longitude": 127.0,
    "latitude": 37.0,
    "sectionName": "string"
  }
}
```

**Authorization:**
인증 불필요

**예외 처리:**
- `SECTION_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 섹션은 존재하지 않습니다.

---

## 농부 관리

### 1. 농부 목록 조회
**Endpoint:** `GET /farmer`

**설명:**
역할(Role)에 따라 조회되는 농부 목록이 다릅니다. ADMIN은 모든 농부 목록을 조회하고, VICE_ADMIN_HEAD_OFFICER / VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER는 자신의 Area 내 VillageHead에 속한 농부 목록을 조회하며, VILLAGE_HEAD는 본인에게 직접 소속된 농부 목록을 조회합니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "id": 1,
      "farmerName": "string",
      "villageHeadName": "string",
      "sectionName": "string"
    }
  ]
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `AREA_NOT_FOUND` (code: `FAILURE_MODAL`): 존재하지 않는 지역입니다.
- `ROLE_NOT_ALLOWED_FARMER_LIST` (code: `FAILURE_MODAL`): 해당 역할은 농부 목록을 조회할 수 없습니다.

---

### 2. 단일 농부 정보 조회
**Endpoint:** `GET /farmer/{farmerId}`

**설명:**
farmerId에 해당하는 농부의 정보를 조회합니다. 응답에는 농부 이름과 해당 섹션(section) 이름, 신분증 사진 URL이 포함됩니다. TreesTransaction 관련 기능은 제거되었으며, 나무 수령 이력 조회 기능은 더 이상 제공되지 않습니다.

**Request:**
- Path Parameters:
  - `farmerId` (Long, required): 농부 ID

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": {
    "sectionName": "string",
    "farmerName": "string",
    "identificationPhotoUrl": "string"
  }
}
```

**Authorization:**
인증 불필요

**예외 처리:**
- `FARMER_NOT_FOUND` (code: `FAILURE_MODAL`): 해당 농부는 존재하지 않습니다.

---

## 수매 관리

### 1. 수매 목록 조회
**Endpoint:** `GET /purchase`

**설명:**
역할(Role)에 따라 수매 목록을 조회합니다. ADMIN은 승인된 전체 수매 목록을 조회하고, VICE_ADMIN_HEAD_OFFICER / VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER는 본인이 관리자(manager)로 지정된 수매 목록만 조회하며, VILLAGE_HEAD는 본인과 1:1 관계인 수매 목록만 조회합니다. Purchase는 면장과 1:1 관계로 기록되며, 각 면장당 하나의 Purchase 기록만 존재합니다. 정렬은 구매일자(purchaseDate) 기준 내림차순입니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "id": 1,
      "managerId": 1,
      "managerName": "string",
      "villageHeadId": 1,
      "villageHeadName": "string",
      "purchaseDate": "2025-12-12",
      "quantity": 100,
      "unitPrice": 1000,
      "totalPrice": 100000,
      "deduction": 5000,
      "paymentAmount": 95000,
      "remarks": "string"
    }
  ]
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `VICE_ADMIN_INFO_NOT_FOUND` (code: `FAILURE_MODAL`): 부 관리자 정보가 존재하지 않습니다.
- `VILLAGE_HEAD_NOT_APPROVED` (code: `FAILURE_MODAL`): 승인된 면장이 아닙니다.
- `VILLAGE_HEAD_AREA_MISMATCH` (code: `FAILURE_MODAL`): 본인이 배정된 지역의 면장만 선택할 수 있습니다.

---

## 파일 관리

### 1. 단일 파일 업로드
**Endpoint:** `POST /gcs/file`

**설명:**
GCS 버킷에 단일 파일을 업로드합니다. 업로드한 파일은 UUID 기반 고유파일 명으로 저장하며, 업로드 성공 여부에 따라 FileEventLog에 로그가 기록됩니다. 업로드 경로는 선택적으로 지정 가능하며, 지정하지 않으면 루트 디렉토리에 저장됩니다.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `directory` (String, optional): 업로드할 디렉토리 경로
  - `file` (MultipartFile, required): 업로드할 파일

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": "https://storage.googleapis.com/bucket-name/path/to/file.jpg"
}
```
업로드된 파일의 GCS URL을 반환합니다.

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `FILE_EMPTY` (code: `FAILURE_MODAL`): 파일이 비어있습니다.
- `UNKNOWN_ERROR` (code: `FAILURE_MODAL`): 알 수 없는 에러입니다.

---

### 2. 복수 파일 업로드
**Endpoint:** `POST /gcs/files`

**설명:**
GCS 버킷에 복수의 파일을 업로드합니다. 각 파일은 UUID 기반 고유 파일명으로 저장되며, 업로드 성공 여부에 따라 FileEventLog에 로그가 기록됩니다. 업로드 경로는 선택적으로 지정 가능하며, 지정하지 않으면 루트 디렉토리에 저장됩니다.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `directory` (String, optional): 업로드할 디렉토리 경로
  - `files` (List<MultipartFile>, required): 업로드할 파일 목록

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    "https://storage.googleapis.com/bucket-name/path/to/file1.jpg",
    "https://storage.googleapis.com/bucket-name/path/to/file2.jpg"
  ]
}
```
업로드된 파일들의 GCS URL 목록을 반환합니다.

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `FILES_EMPTY` (code: `FAILURE_MODAL`): 파일 목록이 비어있습니다.
- `UNKNOWN_ERROR` (code: `FAILURE_MODAL`): 알 수 없는 에러입니다.

---

### 3. 복수 파일 삭제
**Endpoint:** `DELETE /gcs/files`

**설명:**
GCS 버킷에서 복수 파일을 삭제합니다. 파일 URL 리스트를 전달하면 해당 파일들을 GCS에서 삭제하며, 삭제된 파일들은 FileEventLog에 DELETE 타입으로 로그가 기록됩니다.

**Request:**
- Body:
```json
{
  "fileUrls": [
    "https://storage.googleapis.com/bucket-name/path/to/file1.jpg",
    "https://storage.googleapis.com/bucket-name/path/to/file2.jpg"
  ]
}
```

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": null
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `GCS_URL_INVALID` (code: `FAILURE_MODAL`): 올바르지 않은 GCS URL입니다.

---

### 4. 파일 다운로드
**Endpoint:** `GET /gcs/download`

**설명:**
GCS 버킷에서 파일을 다운로드합니다. 요청된 파일 URL을 통해 GCS에서 파일을 스트림으로 읽어와 응답하며, InputStreamResource로 응답하기 위해 ResponseEntity를 예외적으로 사용합니다. 파일 다운로드 결과는 FileEventLog에 DOWNLOAD 타입으로 로그가 기록됩니다.

**Request:**
- Query Parameters:
  - `fileUrl` (String, required): 다운로드할 파일의 GCS URL

**Response:**
- Content-Type: `application/octet-stream`
- Body: 파일 바이너리 데이터

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `GCS_URL_INVALID` (code: `FAILURE_MODAL`): 올바르지 않은 GCS URL입니다.

---

### 5. 이미지 미리보기
**Endpoint:** `GET /gcs/image`

**설명:**
GCS 버킷에 저장된 이미지 파일을 스트림으로 반환합니다. 파일 URL을 통해 이미지 스트림을 반환하며, InputStreamResource로 응답하기 위해 ResponseEntity를 예외적으로 사용합니다. 파일 확장자에 따라 MediaType을 설정해 응답하며, 이미지에 한정된 API이므로 PNG, JPEG, GIF 형식만 지원 가능합니다.

**Request:**
- Query Parameters:
  - `fileUrl` (String, required): 미리보기할 이미지 파일의 GCS URL

**Response:**
- Content-Type: `image/png` 또는 `image/jpeg` 또는 `image/gif`
- Body: 이미지 바이너리 데이터

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.
- `GCS_URL_INVALID` (code: `FAILURE_MODAL`): 올바르지 않은 GCS URL입니다.

---

## 파일 이벤트 로그

### 1. 파일 이벤트 로그 조회
**Endpoint:** `GET /file-event/logs`

**설명:**
파일 업로드/다운로드/삭제 로그를 조회합니다. 타입 없이 전체 조회도 가능합니다.

**Request:**
- Query Parameters:
  - `type` (FileEventLogType, optional): 이벤트 타입 (UPLOAD, DOWNLOAD, DELETE)

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "fileUrl": "https://storage.googleapis.com/bucket-name/path/to/file.jpg",
      "fileName": "file.jpg",
      "number": 1,
      "createdAt": "2025-12-12T12:00:00",
      "type": "UPLOAD",
      "appUserId": 1,
      "size": "1024 KB",
      "status": true
    }
  ]
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.

---

### 2. 존재하는 파일 조회
**Endpoint:** `GET /file-event/existing-files`

**설명:**
현재 버킷에 존재하는 파일만 조회합니다. 로그를 기반으로 버킷에 존재하는 파일만 조회합니다.

**Request:**
없음

**Response:**
```json
{
  "message": "SUCCESS",
  "code": "SUCCESS",
  "response": [
    {
      "fileUrl": "https://storage.googleapis.com/bucket-name/path/to/file.jpg",
      "fileName": "file.jpg",
      "number": 1,
      "createdAt": "2025-12-12T12:00:00",
      "type": "UPLOAD",
      "appUserId": 1,
      "size": "1024 KB",
      "status": true
    }
  ]
}
```

**Authorization:**
로그인 필요

**예외 처리:**
- `UNAUTHORIZED` (code: `ACCESS_DENIED`): 인증되지 않은 사용자입니다.

---

## 에러 코드 및 예외 처리

### 에러 응답 형식

모든 에러 응답은 다음 형식을 따릅니다:

```json
{
  "message": "에러 메시지",
  "code": "에러 코드",
  "response": null
}
```

### HTTP 상태 코드

- `200 OK`: 정상 응답 (에러도 200으로 반환되며 code 필드로 구분)
- `401 UNAUTHORIZED`: 인증 실패 (토큰 없음, 토큰 만료 등)

### 에러 코드 (CodeValue)

| 코드 | 값 | 설명 |
|------|-----|------|
| SUCCESS | SUCCESS | 정상 처리 |
| FAILURE_MODAL | DIALOGUE | 다이얼로그로 표시할 에러 |
| BAD_REQUEST | BR001 | 잘못된 요청 |
| NO_TOKEN_IN_REQUEST | A001 | 요청 헤더에 토큰이 없는 경우 |
| NO_TOKEN | A002 | 레디스에 토큰이 없거나 만료된 경우 |
| ACCESS_DENIED | A003 | 권한이 충분하지 않은 경우 |
| NO_USER_FROM_TOKEN | U002 | 토큰으로 파싱한 userId가 DB에 존재하지 않는 경우 |
| INTERNAL_ERROR | E001 | 내부 서버 오류 |
| DATA_INTEGRITY_VIOLATION | E005 | 데이터 무결성 제약 위반 (다른 데이터에서 참조 중) |

### 에러 메시지 (ErrorValue)

#### 인증/권한 관련
- `ACCESS_DENIED`: 허용되지 않은 접근입니다.
- `UNAUTHORIZED`: 인증되지 않은 사용자입니다.
- `UNAUTHORIZED_SERVICE`: 권한 외 요청입니다.
- `TOKEN_NOT_FOUND`: 토큰이 존재하지 않습니다.
- `TOKEN_EXPIRED`: 토큰이 만료되었습니다.

#### 리소스 조회 관련
- `ACCOUNT_NOT_FOUND`: 존재하지 않는 계정입니다.
- `AREA_NOT_FOUND`: 존재하지 않는 지역입니다.
- `FARMER_NOT_FOUND`: 해당 농부는 존재하지 않습니다.
- `SECTION_NOT_FOUND`: 해당 섹션은 존재하지 않습니다.
- `SUBJECT_NOT_FOUND`: 존재하지 않는 요청입니다.
- `PURCHASE_NOT_FOUND`: 존재하지 않는 구매 이력입니다.
- `VILLAGE_HEAD_NOT_FOUND`: 존재하지 않는 면장입니다.
- `VICE_ADMIN_NOT_FOUND`: 존재하지 않는 부 관리자입니다.
- `APP_USER_NOT_FOUND`: 존재하지 않는 사용자입니다.

#### 승인 상태 관련
- `SECTION_NOT_APPROVED`: 승인되지 않은 섹션입니다.
- `VILLAGE_HEAD_NOT_APPROVED`: 승인된 면장이 아닙니다.

#### 계정 생성/수정 관련
- `USER_KEY_ALREADY_EXISTS`: 이미 존재하는 외부 유저입니다.
- `NICKNAME_ALREADY_EXISTS`: 이미 존재하는 닉네임입니다.
- `ADMIN_CREATION_NOT_ALLOWED`: ADMIN 권한으로 계정을 생성할 수 없습니다.
- `VICE_ADMIN_ALREADY_EXISTS_IN_AREA`: 해당 지역에는 이미 해당 권한의 부관리자가 할당되어 있습니다.
- `VICE_ADMIN_INFO_NOT_FOUND`: 부 관리자 정보가 존재하지 않습니다.
- `VILLAGE_HEAD_DETAIL_NOT_FOUND`: 면장 세부정보를 찾을 수 없습니다.

#### 지역/섹션 관련
- `AREA_SECTION_MISMATCH`: 본인이 배정된 지역의 섹션으로만 면장을 생성할 수 있습니다.
- `VILLAGE_HEAD_AREA_MISMATCH`: 본인이 배정된 지역의 면장만 선택할 수 있습니다.
- `VILLAGE_HEAD_SECTION_MISMATCH`: 본인이 배정된 지역의 Section에만 면장을 배정할 수 있습니다.
- `FARMER_AREA_MISMATCH`: 본인이 배정된 지역의 면장 하위에만 농부를 생성할 수 있습니다.
- `VILLAGE_HEAD_UPDATE_AREA_MISMATCH`: 본인이 배정된 지역의 면장만 수정할 수 있습니다.
- `VILLAGE_HEAD_SECTION_ASSIGN_MISMATCH`: 본인이 배정된 지역의 섹션으로만 배정할 수 있습니다.

#### 역할 관련
- `ROLE_NOT_ALLOWED_FARMER_LIST`: 해당 역할은 농부 목록을 조회할 수 없습니다.
- `ROLE_NOT_ALLOWED_APPROVAL_LIST`: 해당 권한으로 요청 목록을 조회할 수 없습니다.

#### 파일 관련
- `FILE_EMPTY`: 파일이 비어있습니다.
- `FILES_EMPTY`: 파일 목록이 비어있습니다.
- `FILE_NAME_INVALID`: 파일명이 유효하지 않습니다.
- `GCS_URL_INVALID`: 올바르지 않은 GCS URL입니다.

#### 처리 관련
- `JSON_PROCESSING_ERROR`: 요청 데이터를 파싱할 수 없습니다.
- `JWT_PARSING_ERROR`: JWT 토큰 파싱 중 에러가 발생했습니다.
- `UNSUPPORTED_OPERATION`: 지원하지 않는 작업입니다.
- `WRONG_PASSWORD`: 올바르지 않은 아이디 및 비밀번호입니다.
- `VICE_ADMIN_AREA_CHANGE_NOT_ALLOWED`: 라오스 부관리자는 지역 변경이 불가능합니다.
- `ID_CARD_UPLOAD_FAILED`: ID 카드 업로드 실패
- `UNKNOWN_ERROR`: 알 수 없는 에러입니다.

### 예외 처리 흐름

1. **CustomException**: 비즈니스 로직 예외
   - ErrorValue를 통해 에러 메시지 결정
   - CodeValue를 통해 프론트엔드 처리 방식 결정
   - GlobalExceptionHandler에서 처리

2. **IllegalArgumentException**: 잘못된 인자 예외
   - CodeValue.BAD_REQUEST로 처리
   - HTTP 400 상태 코드 반환

3. **IllegalStateException**: 잘못된 상태 예외
   - CodeValue.BAD_REQUEST로 처리
   - HTTP 400 상태 코드 반환

4. **DataIntegrityViolationException**: 데이터 무결성 제약 위반
   - CodeValue.DATA_INTEGRITY_VIOLATION으로 처리
   - HTTP 400 상태 코드 반환
   - 메시지: "삭제할 수 없습니다. 해당 항목이 다른 데이터에서 참조되고 있습니다."

5. **RuntimeException**: 기타 런타임 예외
   - CodeValue.INTERNAL_ERROR로 처리
   - HTTP 500 상태 코드 반환

6. **MethodArgumentNotValidException**: 요청 파라미터 검증 실패
   - CodeValue.BAD_REQUEST로 처리

7. **SessionAuthenticationException**: 세션 인증 실패
   - CodeValue.NO_TOKEN_IN_REQUEST로 처리
   - HTTP 401 상태 코드 반환

### 프론트엔드 처리 가이드

- `code: "SUCCESS"`: 정상 처리
- `code: "DIALOGUE"`: 다이얼로그로 에러 메시지 표시
- `code: "A001"`, `code: "A002"`: 인증 관련 에러, 로그인 페이지로 리다이렉트
- `code: "A003"`: 권한 부족 에러, 접근 불가 메시지 표시
- `code: "BR001"`: 잘못된 요청, 입력값 검증 메시지 표시
- `code: "E001"`: 서버 오류, 재시도 안내
- `code: "E005"`: 데이터 참조 오류, 삭제 불가 안내

---

## 역할(Role) 설명

- **ADMIN**: 총 관리자. 모든 권한을 가진 최고 관리자
- **VICE_ADMIN_HEAD_OFFICER**: 부 관리자 (본청 담당)
- **VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER**: 부 관리자 (농업부 담당)
- **VILLAGE_HEAD**: 면장. VIEWER 권한만 보유

## 상태(Status) 설명

- **PENDING**: 승인 대기 중
- **APPROVED**: 승인됨
- **REJECTED**: 거절됨

## 요청 유형(Method) 설명

- **CREATE**: 생성 요청
- **UPDATE**: 수정 요청
- **DELETE**: 삭제 요청

## 서비스 타입(ServiceType) 설명

- **VILLAGE_HEAD**: 면장 관련
- **FARMER**: 농부 관련
- **PURCHASE**: 수매 관련
- **SECTION**: 섹션 관련

## 파일 이벤트 타입(FileEventLogType) 설명

- **UPLOAD**: 파일 업로드
- **DOWNLOAD**: 파일 다운로드
- **DELETE**: 파일 삭제

