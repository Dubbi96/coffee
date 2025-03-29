package com.coffee.atom.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalFacadeService approvalFacadeService;
    private final ApprovalService approvalService;

    @GetMapping()
    @Operation(
        summary = "요청 목록 조회",
        description = "<b>승인 요청 목록을 상태 및 서비스 타입으로 필터링</b><br>" +
                      "다중 선택 필터 및 페이지네이션 지원<br>" +
                      "예: ?statuses=PENDING&statuses=APPROVED&serviceTypes=PURCHASE&page=0&size=10"
    )
    public Page<ApprovalResponseDto> getApprovals(
            @RequestParam(value = "statuses", required = false) List<Status> statuses,
            @RequestParam(value = "serviceTypes", required = false) List<ServiceType> serviceTypes,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            @Parameter(description = "페이지네이션과 정렬 정보",
                    example = "{\n" +
                            "  \"page\": 0,\n" +
                            "  \"size\": 1,\n" +
                            "  \"sort\": \"id\"\n" +
                            "}")
            Pageable pageable,
            @LoginAppUser AppUser appUser
    ) {
        return approvalService.findApprovals(statuses, serviceTypes, pageable, appUser);
    }

    @PostMapping(value = "/village-head", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "면장 생성 승인 요청 2️⃣ 부 관리자",
        description = "<b>면장 계정 생성을 위한 승인 요청</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
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
            throw new IllegalArgumentException("요청 JsonProcessing 중 에러 발생하였습니다.");
        }
    }

    @PostMapping(value = "/farmer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "농부 생성 승인 요청 2️⃣ 부 관리자 ",
        description = "<b>농부 계정 생성을 위한 승인 요청 생성</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>" +
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
            throw new IllegalArgumentException("요청 JsonProcessing 중 에러 발생하였습니다.");
        }
    }

    @PostMapping(value = "/trees-transaction")
    @Operation(
        summary = "나무 수령 승인 요청 2️⃣ 부 관리자 ",
        description = "<b>나무 수령 정보를 위한 승인 요청 생성</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>"
    )
    public void requestApprovalToCreateTreesTransaction(
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수령할 나무 정보<br>" +
                            "- <b>quantity</b>: 수량<br>" +
                            "- <b>receivedDate</b>: 수령 일자<br>" +
                            "- <b>species</b>: 나무 종<br>" +
                            "- <b>farmerId</b>: 수령 농부 ID",
                    required = true
            )
            @RequestBody ApprovalTreesTransactionRequestDto approvalTreesTransactionRequestDto,
            @LoginAppUser AppUser appUser
    ){
        try {
            approvalFacadeService.processTreesTransactionCreation(appUser, approverId, approvalTreesTransactionRequestDto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("요청 JsonProcessing 중 에러 발생하였습니다.");
        }
    }

    @PostMapping(value = "/purchase")
    @Operation(
        summary = "수매 승인 요청 2️⃣ 부 관리자 (한국지사) ",
        description = "<b>수매 정보를 위한 승인 요청 생성</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>"
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
            @RequestBody ApprovalPurchaseRequestDto approvalPurchaseRequestDto,
            @LoginAppUser AppUser appUser
    ){
        try {
            approvalFacadeService.processPurchaseCreation(appUser, approverId, approvalPurchaseRequestDto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("요청 JsonProcessing 중 에러 발생하였습니다.");
        }
    }

    @PostMapping(value = "/section")
    @Operation(
        summary = "섹션 생성 승인 요청 2️⃣ 부 관리자 ",
        description = "<b>섹션 생성 승인 요청 생성</b><br>" +
                      "요청자는 로그인된 사용자이며, 승인자는 approverId로 지정<br>"
    )
    public void requestApprovalToCreateSection(
            @Parameter(description = "승인자 ADMIN ID")
            @RequestParam("approverId") Long approverId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수매 내역 등록 정보<br>" +
                            "- <b>id</b>: ⚠️타 서비스에 사용할 필드로 해당 서비스에서는 사용하지 않음<br>" +
                            "- <b>longitude</b>: 섹션의 경도<br>" +
                            "- <b>latitude</b>: 섹션의 위도<br>" +
                            "- <b>sectionName</b>: 섹션 명<br>" +
                            "- <b>areaId</b>: 지역 ID<br>",
                    required = true
            )
            @RequestBody ApprovalSectionRequestDto approvalSectionRequestDto,
            @LoginAppUser AppUser appUser
    ){
        try {
            approvalFacadeService.processSectionCreation(appUser, approverId, approvalSectionRequestDto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("요청 JsonProcessing 중 에러 발생하였습니다.");
        }
    }

}
