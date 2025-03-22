package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.approval.ApprovalFarmerRequestDto;
import com.coffee.atom.dto.approval.ApprovalTreesTransactionRequestDto;
import com.coffee.atom.dto.approval.ApprovalVillageHeadRequestDto;
import com.coffee.atom.service.AppUserService;
import com.coffee.atom.service.approval.ApprovalFacadeService;
import com.coffee.atom.service.approval.ApprovalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalFacadeService approvalFacadeService;

    @PostMapping(value = "/village-head", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "면장 생성 승인 요청",
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
            @Parameter(description = "배정 할 지역 ID")
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
        summary = "농부 생성 승인 요청",
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
        summary = "나무 수령 승인 요청",
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


}
