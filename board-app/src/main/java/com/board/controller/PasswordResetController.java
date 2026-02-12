package com.board.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.board.service.ifc.PasswordResetService;
import java.util.HashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 재설정 컨트롤러 (API)
 */
@Slf4j
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {

  /** 요청 파라미터 키 */
  private static final String PARAM_EMAIL = "email";
  private static final String PARAM_TOKEN = "token";
  private static final String PARAM_NEW_PASSWORD = "newPassword";
  
  /** 응답 키 */
  private static final String RESPONSE_SUCCESS = "success";
  private static final String RESPONSE_MESSAGE = "message";
  
  /** 성공/실패 메시지 */
  private static final String MSG_MAIL_SENT = "비밀번호 재설정 메일이 발송되었습니다.";
  private static final String MSG_MAIL_FAILED = "메일 발송에 실패했습니다.";
  private static final String MSG_PASSWORD_CHANGED = "비밀번호가 성공적으로 변경되었습니다.";
  private static final String MSG_PASSWORD_CHANGE_FAILED = "비밀번호 변경에 실패했습니다.";
  private static final String MSG_PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다.";
  private static final String MSG_INVALID_TOKEN = "유효하지 않은 토큰입니다.";
  
  private final PasswordResetService passwordResetService;

  /**
   * 비밀번호 재설정 메일 발송 요청 처리
   */
  @PostMapping("/reset-request")
  public Map<String, Object> handleResetRequest(
      @RequestParam String email,
      HttpServletRequest request) {
    
    try {
      String accessIp = extractClientIp(request);
      
      Map<String, Object> requestData = new HashMap<>();
      requestData.put(PARAM_EMAIL, email);
      requestData.put("accessIp", accessIp);
      
      Boolean isSuccess = passwordResetService.sendPasswordResetMail(requestData);
      
      if (Boolean.TRUE.equals(isSuccess)) {
        return createSuccessResponse(MSG_MAIL_SENT);
      } else {
        return createFailureResponse(MSG_MAIL_FAILED);
      }
      
    } catch (Exception e) {
      log.error("비밀번호 재설정 메일 발송 실패: email={}", email, e);
      return createFailureResponse("메일 발송 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /**
   * 토큰 검증
   */
  @GetMapping("/verify")
  public Map<String, Object> verifyToken(@RequestParam String token) {
    try {
      Map<String, Object> requestData = new HashMap<>();
      requestData.put(PARAM_TOKEN, token);
      Map<String, Object> verifyResult = passwordResetService.verifyPasswordResetToken(requestData);
      
      if (Boolean.TRUE.equals(verifyResult.get("result"))) {
        return createSuccessResponse("유효한 토큰입니다.");
      } else {
        return createFailureResponse(MSG_INVALID_TOKEN);
      }
      
    } catch (Exception e) {
      log.error("토큰 검증 실패: token={}", token, e);
      return createFailureResponse(e.getMessage());
    }
  }

  /**
   * 비밀번호 재설정 처리
   */
  @PostMapping("/reset")
  public Map<String, Object> handlePasswordReset(
      @RequestParam String token,
      @RequestParam String newPassword,
      @RequestParam(required = false) String confirmPassword) {
    
    try {
      if (!newPassword.equals(confirmPassword)) {
        return createFailureResponse(MSG_PASSWORD_MISMATCH);
      }
      
      Map<String, Object> requestData = new HashMap<>();
      requestData.put(PARAM_TOKEN, token);
      requestData.put(PARAM_NEW_PASSWORD, newPassword);
      
      Boolean isSuccess = passwordResetService.resetPassword(requestData);
      
      if (Boolean.TRUE.equals(isSuccess)) {
        return createSuccessResponse(MSG_PASSWORD_CHANGED);
      } else {
        return createFailureResponse(MSG_PASSWORD_CHANGE_FAILED);
      }
      
    } catch (Exception e) {
      log.error("비밀번호 재설정 실패: token={}", token, e);
      return createFailureResponse("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  private String extractClientIp(HttpServletRequest request) {
    // 프록시를 통한 요청인 경우 실제 IP 확인
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null) {
      ip = request.getHeader("X-Real-IP");
    }
    
    // IP가 없거나 유효하지 않은 경우 기본 IP 사용
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    
    return ip;
  }

  private Map<String, Object> createSuccessResponse(String message) {
    Map<String, Object> result = new HashMap<>();
    result.put(RESPONSE_SUCCESS, true);
    result.put(RESPONSE_MESSAGE, message);
    return result;
  }

  private Map<String, Object> createFailureResponse(String message) {
    Map<String, Object> result = new HashMap<>();
    result.put(RESPONSE_SUCCESS, false);
    result.put(RESPONSE_MESSAGE, message);
    return result;
  }
}

