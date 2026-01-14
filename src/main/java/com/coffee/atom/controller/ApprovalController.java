package com.coffee.atom.controller;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.approval.Status;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.approval.*;
import com.coffee.atom.service.approval.ApprovalFacadeService;
import com.coffee.atom.service.approval.ApprovalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
@Slf4j
public class ApprovalController {
    
    @Value("${debug.logging.enabled:false}")
    private boolean debugLoggingEnabled;
    
    @Value("${debug.logging.path:.cursor/debug.log}")
    private String debugLogPath;
    private final ApprovalFacadeService approvalFacadeService;
    private final ApprovalService approvalService;

    @GetMapping()
    @Operation(
        summary = "요청 목록 조회",
        description = "<b>승인 요청 목록을 상태 및 서비스 타입으로 필터링</b><br>" +
                "다중 선택 필터 및 페이지네이션 지원<br>" +
                "예: ?statuses=PENDING&statuses=APPROVED&serviceTypes=PURCHASE&page=0&size=10<br>" +
                "<b>⚠️ VILLAGE_HEAD는 조회 불가 (VIEWER 권한만 보유)</b><br>" +
                "<b>역할별 조회 범위:</b><br>" +
                "- <b>ADMIN</b>: 본인이 승인자로 지정된 요청 중, VICE_ADMIN_HEAD_OFFICER 또는 VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER가 요청한 것만 조회 (정책 1.1)<br>" +
                "- <b>VICE_ADMIN_HEAD_OFFICER</b>: 본인 또는 같은 Area의 VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER가 요청한 것만 조회<br>" +
                "- <b>VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER</b>: 본인이 요청한 것만 조회"
    )
    public Page<ApprovalResponseDto> getApprovals(
            @RequestParam(value = "statuses", required = false) List<Status> statuses,
            @RequestParam(value = "serviceTypes", required = false) List<ServiceType> serviceTypes,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            @Parameter(description = "페이지네이션과 정렬 정보",
                    example = "{\n" +
                            "  \"page\": 0,\n" +
                            "  \"size\": 1\n" +
                            "}")
            Pageable pageable,
            @LoginAppUser AppUser appUser
    ) {
        return approvalService.findApprovals(statuses, serviceTypes, pageable, appUser);
    }

    @PatchMapping("/approve/{approvalId}")
    @Operation(
        summary = "요청 승인 처리 1️⃣ 총 관리자",
        description = "<b>approvalId를 갖는 요청 승인 처리</b><br>" +
                "승인 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                "<b>⚠️ VILLAGE_HEAD는 승인 불가 (VIEWER 권한만 보유)</b><br>" +
                "본인이 승인자로 지정된 요청만 승인 가능<br>" +
                "승인 시 요청 유형(CREATE/UPDATE/DELETE)에 따라 해당 인스턴스가 실제로 생성/수정/삭제됨"
    )
    public void approveApproval(
            @PathVariable("approvalId") Long approvalId,
            @LoginAppUser AppUser appUser
    ) {
        approvalService.processApproval(approvalId, appUser);
    }

    @PatchMapping("/reject/{approvalId}")
    @Operation(
        summary = "요청 거절 처리 1️⃣ 총 관리자",
        description = "<b>approvalId를 갖는 요청 거절 처리</b><br>" +
                "거절 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                "<b>⚠️ VILLAGE_HEAD는 거절 불가 (VIEWER 권한만 보유)</b><br>" +
                "본인이 승인자로 지정된 요청만 거절 가능<br>" +
                "거절 사유(rejectedReason) 필수 입력<br>" +
                "<b>거절 처리 동작:</b><br>" +
                "- <b>CREATE</b> 요청: 생성 대기 중인 인스턴스 삭제<br>" +
                "- <b>UPDATE</b> 요청: DB 변경 없음<br>" +
                "- <b>DELETE</b> 요청: 삭제 대기 중이던 인스턴스 복구 (isApproved = true로 변경)"
    )
    public void rejectApproval(
            @PathVariable("approvalId") Long approvalId,
            @Valid @RequestBody RejectApprovalRequestDto rejectApprovalRequestDto,
            @LoginAppUser AppUser appUser
    ) {
        approvalService.rejectApproval(approvalId, rejectApprovalRequestDto.getRejectedReason(), appUser);
    }

    @GetMapping("/{approvalId}")
    @Operation(
        summary = "요청 상세 조회 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>approvalId에 해당하는 요청 상세 정보를 조회</b><br>" +
                  "<b>Approval</b> 테이블의 요청 데이터를 기준으로 반환<br>" +
                  "<b>요청 유형(EntityType)</b>에 따라 응답 형태(DTO) 변경<br>" +
                  "요청 상태(<b>Status</b>)는 <code>PENDING</code>, <code>APPROVED</code>, <code>REJECTED</code>"
    )
    public ApprovalDetailResponse getApprovalDetail(@PathVariable("approvalId") Long approvalId) {
        return approvalService.getApprovalDetail(approvalId);
    }

    @PostMapping(value = "/village-head", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated(since = "2026. 01. 15")
    @Operation(
        summary = "면장 생성 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>면장 계정 생성을 위한 승인 요청</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ VICE_ADMIN의 경우 정책:</b><br>" +
                      "- 본인이 배정된 Area 내의 Section에만 면장 배정 가능 (정책 2.1)<br>" +
                      "- sectionId는 본인 Area 내의 Section이어야 함<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)<br>" +
                      "파일은 Multipart 형식으로 전송되며, 일부 항목은 생략 가능"
    )
    public void requestApprovalToCreateVillageHead(
            @Parameter(description = "면장 신원 확인 용 이미지")
            @RequestPart(value = "identificationPhoto", required = false) MultipartFile identificationPhoto,
            @Parameter(description = "계약서 파일")
            @RequestPart(value = "contractFile", required = false) MultipartFile contractFile,
            @Parameter(description = "통장 사본 이미지")
            @RequestPart(value = "bankbookPhoto", required = false) MultipartFile bankbookPhoto,
            @Parameter(description = "면장 User ID")
            @RequestParam("userId") String userId,
            @Parameter(description = "면장 User명")
            @RequestParam("username") String username,
            @Parameter(description = "면장 비밀번호")
            @RequestParam("password") String password,
            @Parameter(description = "은행 명")
            @RequestParam(value = "bankName", required = false) String bankName,
            @Parameter(description = "계좌번호")
            @RequestParam(value = "accountInfo", required = false) String accountInfo,
            @Parameter(description = "배정 할 Section ID")
            @RequestParam("sectionId") Long sectionId,
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ){
        ApprovalVillageHeadRequestDto approvalVillageHeadRequestDto =
                new ApprovalVillageHeadRequestDto(userId,password,username,bankName,accountInfo,identificationPhoto,contractFile,bankbookPhoto,sectionId);
        try {
            approvalFacadeService.processVillageHeadCreation(appUser, approverId, approvalVillageHeadRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PostMapping(value = "/village-head/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "면장 생성 승인 요청(URL 기반) 1️⃣ 총 관리자 2️⃣ 부 관리자",
            description = "<b>Deprecated된 multipart 승인 요청을 대체하는 URL 기반 승인 요청</b><br>" +
                    "FE는 먼저 <code>/gcs</code>로 업로드해 URL을 획득한 뒤,<br>" +
                    "여기 API에는 파일이 아닌 URL(String)만 전달합니다."
    )
    public void requestApprovalToCreateVillageHeadWithUrl(
            @RequestParam("approverId") Long approverId,
            @Valid @RequestBody ApprovalVillageHeadRequestDto dto,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processVillageHeadCreation(appUser, approverId, dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PostMapping(value = "/farmer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated(since = "2026. 01. 15")
    @Operation(
        summary = "농부 생성 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자 ",
        description = "<b>농부 계정 생성을 위한 승인 요청 생성</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ VICE_ADMIN의 경우 정책:</b><br>" +
                      "- villageHeadId는 본인이 배정된 Area 내의 면장이어야 함 (정책 2.5)<br>" +
                      "- 본인 Area 외의 면장에게 농부를 배정할 수 없음<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)<br>" +
                      "파일은 Multipart 형식으로 전송되며, 신분증 이미지는 선택 사항"
    )
    public void requestApprovalToCreateFarmer(
            @Parameter(description = "농부 신원 확인 용 이미지")
            @RequestPart(value = "identificationPhoto", required = false) MultipartFile identificationPhoto,
            @Parameter(description = "농부 이름")
            @RequestParam("name") String name,
            @Parameter(description = "농부가 소속된 면장 ID")
            @RequestParam("villageHeadId") Long villageHeadId,
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ){
        ApprovalFarmerRequestDto approvalVillageHeadRequestDto =
                new ApprovalFarmerRequestDto(identificationPhoto,name,villageHeadId);
        try {
            approvalFacadeService.processFarmerCreation(appUser, approverId, approvalVillageHeadRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PostMapping(value = "/farmer/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "농부 생성 승인 요청(URL 기반) 1️⃣ 총 관리자 2️⃣ 부 관리자",
            description = "<b>Deprecated된 multipart 승인 요청을 대체하는 URL 기반 승인 요청</b><br>" +
                    "FE는 먼저 /gcs API로 업로드해 URL을 획득한 뒤, 여기 API에는 URL(String)만 전달합니다."
    )
    public void requestApprovalToCreateFarmerWithUrl(
            @RequestParam("approverId") Long approverId,
            @Valid @RequestBody ApprovalFarmerRequestDto dto,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processFarmerCreation(appUser, approverId, dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PostMapping(value = "/purchase")
    @Operation(
        summary = "수매 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>수매 정보를 위한 승인 요청 생성</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ 정책 변경사항:</b><br>" +
                      "- Purchase는 면장과 1:1 관계로 기록됨 (정책 2.2)<br>" +
                      "- villageHeadId 필수 입력 (각 면장당 하나의 Purchase 기록)<br>" +
                      "- VICE_ADMIN의 경우 본인 Area 내의 면장만 지정 가능<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)<br>" +
                      "승인 후 특정 기간 내 생성된 기록을 합산하여 조회 가능"
    )
    public void requestApprovalToCreatePurchase(
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수매 내역 등록 정보<br>" +
                            "- <b>id</b>: ⚠️수정에 사용할 필드로 해당 서비스에서는 사용하지 않음<br>" +
                            "- <b>deduction</b>: 차감액<br>" +
                            "- <b>paymentAmount</b>: 지급액<br>" +
                            "- <b>purchaseDate</b>: 거래 일자<br>" +
                            "- <b>quantity</b>: 수량<br>" +
                            "- <b>totalPrice</b>: 총액<br>" +
                            "- <b>unitPrice</b>: 단가<br>",
                    required = true
            )
            @Valid @RequestBody ApprovalPurchaseRequestDto approvalPurchaseRequestDto,
            @LoginAppUser AppUser appUser
    ){
        try {
            approvalFacadeService.processPurchaseCreation(appUser, approverId, approvalPurchaseRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PostMapping(value = "/section")
    @Operation(
        summary = "섹션 생성 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자 ",
        description = "<b>섹션 생성 승인 요청 생성</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ 정책 변경사항:</b><br>" +
                      "- 실제 섹션 생성은 ADMIN만 가능 (정책 1.7)<br>" +
                      "- VICE_ADMIN은 섹션 생성/삭제/수정을 위한 Approval 요청만 가능 (정책 2.3)<br>" +
                      "- VICE_ADMIN의 경우 areaId는 본인이 배정된 Area만 사용 가능 (입력해도 무시됨)<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)"
    )
    public void requestApprovalToCreateSection(
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "섹션 등록 정보<br>" +
                            "- <b>id</b>: ⚠️타 서비스에 사용할 필드로 해당 서비스에서는 사용하지 않음<br>" +
                            "- <b>longitude</b>: 섹션의 경도<br>" +
                            "- <b>latitude</b>: 섹션의 위도<br>" +
                            "- <b>sectionName</b>: 섹션 명<br>" +
                            "- <b>areaId</b>: ⚠️부 관리자의 경우 지역 ID 입력 하더라도 본인이 배정된 Area만 사용 가능<br>",
                    required = true
            )
            @Valid @RequestBody ApprovalSectionRequestDto approvalSectionRequestDto,
            @LoginAppUser AppUser appUser
    ){
        try {
            approvalFacadeService.processSectionCreation(appUser, approverId, approvalSectionRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PatchMapping(value = "/village-head", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated(since = "2026. 01. 15")
    @Operation(
        summary = "면장 수정 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>면장 계정 수정을 위한 승인 요청</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ VICE_ADMIN의 경우 정책:</b><br>" +
                      "- sectionId는 본인이 배정된 Area 내의 Section이어야 함 (정책 2.4)<br>" +
                      "- 본인 Area 외의 Section에 면장을 배정할 수 없음<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)<br>" +
                      "파일은 Multipart 형식으로 전송되며, 일부 항목은 생략 가능"
    )
    public void requestApprovalToUpdateVillageHead(
            @Parameter(description = "면장 신원 확인 용 이미지")
            @RequestPart(value = "identificationPhoto", required = false) MultipartFile identificationPhoto,
            @Parameter(description = "계약서 파일")
            @RequestPart(value = "contractFile", required = false) MultipartFile contractFile,
            @Parameter(description = "통장 사본 이미지")
            @RequestPart(value = "bankbookPhoto", required = false) MultipartFile bankbookPhoto,
            @Parameter(description = "면장 ID")
            @RequestParam("appUserId") Long id,
            @Parameter(description = "면장 User ID")
            @RequestParam("userId") String userId,
            @Parameter(description = "면장 User명")
            @RequestParam("username") String username,
            @Parameter(description = "면장 비밀번호")
            @RequestParam("password") String password,
            @Parameter(description = "은행 명")
            @RequestParam(value = "bankName", required = false) String bankName,
            @Parameter(description = "계좌번호")
            @RequestParam(value = "accountInfo", required = false) String accountInfo,
            @Parameter(description = "배정 할 Section ID")
            @RequestParam("sectionId") Long sectionId,
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ){
        ApprovalVillageHeadRequestDto approvalVillageHeadRequestDto =
                new ApprovalVillageHeadRequestDto(id,userId,password,username,bankName,accountInfo,identificationPhoto,contractFile,bankbookPhoto,sectionId);
        try {
            approvalFacadeService.processVillageHeadUpdate(appUser, approverId, approvalVillageHeadRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PatchMapping(value = "/village-head/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "면장 수정 승인 요청(URL 기반) 1️⃣ 총 관리자 2️⃣ 부 관리자",
            description = "<b>Deprecated된 multipart 승인 요청을 대체하는 URL 기반 승인 요청</b><br>" +
                    "FE는 먼저 /gcs API로 업로드해 URL을 획득한 뒤, 여기 API에는 URL(String)만 전달합니다."
    )
    public void requestApprovalToUpdateVillageHeadWithUrl(
            @RequestParam("approverId") Long approverId,
            @Valid @RequestBody ApprovalVillageHeadRequestDto dto,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processVillageHeadUpdate(appUser, approverId, dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @DeleteMapping(value = "/{approvalId}")
    @Operation(
        summary = "요청 삭제 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>요청 삭제</b><br>" +
                      "본인이 요청한 경우에만 삭제 가능<br>" +
                      "타인이 요청한 경우 UNAUTHORIZED_SERVICE 에러 발생"
    )
    public void deleteApproval(@PathVariable("approvalId") Long approvalId, @LoginAppUser AppUser appUser){
        approvalService.deleteApproval(approvalId, appUser);
    }

    @PatchMapping(value = "/farmer/{farmerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated(since = "2026. 01. 15")
    @Operation(
        summary = "농부 수정 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자 ",
        description = "<b>기존 농부 정보 수정을 위한 승인 요청</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
                      "수정 대상 farmerId는 필수<br>" +
                      "파일은 Multipart 형식으로 전송되며, 신분증 이미지는 선택 사항"
    )
    public void requestApprovalToUpdateFarmer(
            @Parameter(description = "농부 신원 확인 용 이미지")
            @RequestPart(value = "identificationPhoto", required = false) MultipartFile identificationPhoto,
            @Parameter(description = "농부 이름")
            @RequestParam("name") String name,
            @Parameter(description = "농부가 소속된 면장 ID")
            @RequestParam("villageHeadId") Long villageHeadId,
            @PathVariable("farmerId") Long farmerId,
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ) {
        ApprovalFarmerRequestDto dto =
                new ApprovalFarmerRequestDto(identificationPhoto, name, villageHeadId);
        dto.setId(farmerId); // 기존 농부 ID 설정

        try {
            approvalFacadeService.processFarmerUpdate(appUser, approverId, dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PatchMapping(value = "/farmer/{farmerId}/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "농부 수정 승인 요청(URL 기반) 1️⃣ 총 관리자 2️⃣ 부 관리자",
            description = "<b>Deprecated된 multipart 승인 요청을 대체하는 URL 기반 승인 요청</b><br>" +
                    "FE는 먼저 /gcs API로 업로드해 URL을 획득한 뒤, 여기 API에는 URL(String)만 전달합니다."
    )
    public void requestApprovalToUpdateFarmerWithUrl(
            @PathVariable("farmerId") Long farmerId,
            @RequestParam("approverId") Long approverId,
            @Valid @RequestBody ApprovalFarmerRequestDto dto,
            @LoginAppUser AppUser appUser
    ) {
        dto.setId(farmerId);
        try {
            approvalFacadeService.processFarmerUpdate(appUser, approverId, dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @DeleteMapping("/farmer/{farmerId}")
    @Operation(
        summary = "농부 삭제 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>기존 농부 정보 삭제를 위한 승인 요청</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
                      "삭제 대상 farmerId는 필수"
    )
    public void requestApprovalToDeleteFarmer(
            @PathVariable("farmerId") Long farmerId,
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processFarmerDelete(appUser, approverId, farmerId);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @DeleteMapping("/village-head/{villageHeadId}")
    @Operation(
        summary = "면장 삭제 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>기존 면장 정보 삭제를 위한 승인 요청</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
                      "삭제 대상 villageHeadId는 필수"
    )
    public void requestApprovalToDeleteVillageHead(
            @PathVariable("villageHeadId") Long villageHeadId,
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processVillageHeadDelete(appUser, approverId, villageHeadId);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @DeleteMapping("/section/{sectionId}")
    @Operation(
        summary = "섹션 삭제 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>기존 섹션 정보 삭제를 위한 승인 요청</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
                      "삭제 대상 sectionId는 필수"
    )
    public void requestApprovalToDeleteSection(
            @PathVariable("sectionId") Long sectionId,
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processSectionDelete(appUser, approverId, sectionId);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    @PatchMapping(value = "/purchase/{purchaseId}")
    @Operation(
        summary = "수매 수정 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>기존 수매 정보 수정을 위한 승인 요청</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ 정책:</b><br>" +
                      "- Purchase는 면장과 1:1 관계로 기록됨 (정책 2.2)<br>" +
                      "- villageHeadId 필수 입력 (각 면장당 하나의 Purchase 기록)<br>" +
                      "- VICE_ADMIN의 경우 본인이 관리하는 Purchase만 수정 가능<br>" +
                      "- VICE_ADMIN의 경우 본인 Area 내의 면장만 지정 가능<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)<br>" +
                      "수정 대상 purchaseId는 필수"
    )
    public void requestApprovalToUpdatePurchase(
            @PathVariable("purchaseId") Long purchaseId,
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수매 내역 수정 정보<br>" +
                            "- <b>id</b>: ⚠️수정에 사용할 필드로 해당 서비스에서는 사용하지 않음<br>" +
                            "- <b>villageHeadId</b>: 면장 ID (1:1 관계)<br>" +
                            "- <b>deduction</b>: 차감액<br>" +
                            "- <b>paymentAmount</b>: 지급액<br>" +
                            "- <b>purchaseDate</b>: 거래 일자<br>" +
                            "- <b>quantity</b>: 수량<br>" +
                            "- <b>totalPrice</b>: 총액<br>" +
                            "- <b>unitPrice</b>: 단가<br>" +
                            "- <b>remarks</b>: 비고<br>",
                    required = true
            )
            @Valid @RequestBody ApprovalPurchaseRequestDto approvalPurchaseRequestDto,
            @LoginAppUser AppUser appUser
    ){
        // #region agent log
        if (debugLoggingEnabled) {
            try {
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"ApprovalController.requestApprovalToUpdatePurchase:509\",\"message\":\"Controller method entry\",\"data\":{\"purchaseId\":%d,\"approverId\":%d,\"appUser\":\"%s\"},\"timestamp\":%d}%n", 
                    purchaseId, approverId, appUser != null ? (appUser.getId() + "/" + (appUser.getRole() != null ? appUser.getRole().name() : "null")) : "null", System.currentTimeMillis());
                Files.write(Paths.get(debugLogPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {}
        }
        // #endregion
        approvalPurchaseRequestDto.setId(purchaseId);
        try {
            approvalFacadeService.processPurchaseUpdate(appUser, approverId, approvalPurchaseRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

    // ============================================
    // [NEW] Query Parameter 지원 엔드포인트 추가 (2026-01-03)
    // 기존 경로: /approval/purchase/{purchaseId} (Path Variable)
    // 새 경로: /approval/purchase?purchaseId=xxx&approverId=xxx (Query Parameter)
    // 롤백 시: 아래 주석 처리된 코드를 활성화하고 이 메서드를 주석 처리
    // ============================================
    @PatchMapping(value = "/purchase", params = "purchaseId")
    @Operation(
        summary = "수매 수정 승인 요청 (Query Parameter 지원) 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>기존 수매 정보 수정을 위한 승인 요청 (purchaseId를 query parameter로 받음)</b><br>" +
                      "요청 가능한 역할: ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER<br>" +
                      "<b>⚠️ 정책:</b><br>" +
                      "- Purchase는 면장과 1:1 관계로 기록됨 (정책 2.2)<br>" +
                      "- villageHeadId 필수 입력 (각 면장당 하나의 Purchase 기록)<br>" +
                      "- VICE_ADMIN의 경우 본인이 관리하는 Purchase만 수정 가능<br>" +
                      "- VICE_ADMIN의 경우 본인 Area 내의 면장만 지정 가능<br>" +
                      "승인자는 approverId로 지정 (ADMIN ID)<br>" +
                      "수정 대상 purchaseId는 query parameter로 필수"
    )
    public void requestApprovalToUpdatePurchaseWithQueryParam(
            @Parameter(description = "수정 대상 Purchase ID")
            @RequestParam("purchaseId") Long purchaseId,
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수매 내역 수정 정보<br>" +
                            "- <b>id</b>: ⚠️수정에 사용할 필드로 해당 서비스에서는 사용하지 않음<br>" +
                            "- <b>villageHeadId</b>: 면장 ID (1:1 관계)<br>" +
                            "- <b>deduction</b>: 차감액<br>" +
                            "- <b>paymentAmount</b>: 지급액<br>" +
                            "- <b>purchaseDate</b>: 거래 일자<br>" +
                            "- <b>quantity</b>: 수량<br>" +
                            "- <b>totalPrice</b>: 총액<br>" +
                            "- <b>unitPrice</b>: 단가<br>" +
                            "- <b>remarks</b>: 비고<br>",
                    required = true
            )
            @Valid @RequestBody ApprovalPurchaseRequestDto approvalPurchaseRequestDto,
            @LoginAppUser AppUser appUser
    ){
        // #region agent log
        if (debugLoggingEnabled) {
            try {
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"ApprovalController.requestApprovalToUpdatePurchaseWithQueryParam:509\",\"message\":\"Controller method entry\",\"data\":{\"purchaseId\":%d,\"approverId\":%d,\"appUser\":\"%s\"},\"timestamp\":%d}%n", 
                    purchaseId, approverId, appUser != null ? (appUser.getId() + "/" + (appUser.getRole() != null ? appUser.getRole().name() : "null")) : "null", System.currentTimeMillis());
                Files.write(Paths.get(debugLogPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {}
        }
        // #endregion
        approvalPurchaseRequestDto.setId(purchaseId);
        try {
            approvalFacadeService.processPurchaseUpdate(appUser, approverId, approvalPurchaseRequestDto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }
    
    // ============================================
    // [ROLLBACK] 기존 코드 (Path Variable 방식)
    // 롤백 시: 위의 requestApprovalToUpdatePurchaseWithQueryParam 메서드를 주석 처리하고
    // 아래 주석을 해제하여 기존 방식만 사용
    // ============================================
    /*
    // 기존에는 Path Variable 방식만 지원했음
    // 경로: /approval/purchase/{purchaseId}?approverId=xxx
    // 롤백 시 이 주석을 해제하고 위의 query parameter 메서드를 주석 처리
    */

    @DeleteMapping("/purchase/{purchaseId}")
    @Operation(
        summary = "구매 이력 삭제 승인 요청 1️⃣ 총 관리자 2️⃣ 부 관리자",
        description = "<b>기존 구매 이력 삭제를 위한 승인 요청</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
                      "삭제 대상 purchaseId는 필수"
    )
    public void requestApprovalToDeletePurchase(
            @PathVariable("purchaseId") Long purchaseId,
            @RequestParam("approverId") Long approverId,
            @LoginAppUser AppUser appUser
    ) {
        try {
            approvalFacadeService.processPurchaseDelete(appUser, approverId, purchaseId);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
    }

}
