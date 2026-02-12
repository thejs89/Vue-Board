package com.board.service.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.domain.PasswordReset;
import com.board.mail.service.ifc.MailService;
import com.board.repository.PasswordResetRepo;
import com.board.service.ifc.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Base64Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class})
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

  private static final String TOKEN_PREFIX = "PWRESET_";
  private static final String TOKEN_SUFFIX = "_PWRESET";
  private static final String DEFAULT_FROM_EMAIL = "noreply@example.com";
  private static final String DEFAULT_FROM_NAME = "게시판 관리자";
  private static final String MAIL_SUBJECT = "비밀번호 재설정을 위한 인증메일입니다.";
  private static final String MAIL_CONTENT_TYPE = "text/html;charset=UTF-8";
  private static final String MAIL_TEMPLATE_PATH = "templates/mail/password-reset-form.html";
  private static final int TOKEN_EXPIRATION_HOURS = 1;
  private static final String PARAM_EMAIL = "email";
  private static final String PARAM_TOKEN = "token";
  private static final String PARAM_NEW_PASSWORD = "newPassword";
  private static final String PARAM_ACCESS_IP = "accessIp";
  private static final String RESPONSE_RESULT = "result";
  private static final String RESPONSE_EMAIL = "email";
  private static final String RESPONSE_SEQ = "seq";
  private static final String TEMPLATE_RESET_URL = "##RESET_URL##";

  private final PasswordResetRepo passwordResetRepo;
  private final MailService mailService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${web.url:http://localhost:8080}")
  private String webUrl;

  @Override
  public Boolean sendPasswordResetMail(Map<String, Object> requestMap) throws Exception {
    String email = extractEmail(requestMap);
    String accessIp = extractAccessIp(requestMap);
    
    if (!isValidEmail(email)) {
      log.error("이메일이 없거나 유효하지 않습니다: {}", email);
      return false;
    }

    passwordResetRepo.invalidatePreviousTokens(email);
    
    Integer resetSeq = passwordResetRepo.getNextPasswordResetSeq();
    Date currentTime = new Date();
    Date expirationTime = calculateExpirationTime(currentTime);
    
    String encodedToken = createAndEncodeToken(resetSeq, email);
    String resetUrl = createResetUrl(encodedToken);
    String mailContent = createMailContent(resetUrl);
    
    try {
      sendMail(email, mailContent);
      savePasswordResetInfo(resetSeq, email, encodedToken, expirationTime, accessIp, currentTime);
      
      log.info("비밀번호 재설정 메일 발송 성공: email={}", email);
      return true;
      
    } catch (Exception e) {
      log.error("비밀번호 재설정 메일 발송 실패: email={}", email, e);
      return false;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> verifyPasswordResetToken(Map<String, Object> requestMap) throws Exception {
    String token = extractToken(requestMap);
    validateTokenNotEmpty(token);

    try {
      Map<String, Object> decodedData = decodeToken(token);
      String email = (String) decodedData.get(RESPONSE_EMAIL);
      
      PasswordReset passwordReset = findPasswordResetByToken(token);
      validatePasswordResetExists(passwordReset);
      validateTokenNotExpired(passwordReset);
      validateTokenMatches(token, passwordReset);
      validateEmailMatches(email, passwordReset);
      
      return createSuccessResponse(passwordReset);
      
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      log.error("토큰 검증 중 예상치 못한 오류 발생: token={}", token, e);
      throw new IllegalArgumentException("토큰 검증 중 오류가 발생했습니다.");
    }
  }

  @Override
  public Boolean resetPassword(Map<String, Object> requestMap) throws Exception {
    String token = extractToken(requestMap);
    String newPassword = extractNewPassword(requestMap);
    
    validateTokenNotEmpty(token);
    validatePasswordNotEmpty(newPassword);
    
    Map<String, Object> verifyResult = verifyPasswordResetToken(requestMap);
    if (!Boolean.TRUE.equals(verifyResult.get(RESPONSE_RESULT))) {
      log.warn("토큰 검증 실패로 비밀번호 재설정 불가");
      return false;
    }
    
    Integer seq = (Integer) verifyResult.get(RESPONSE_SEQ);
    markTokenAsUsed(seq);
    
    log.info("비밀번호 재설정 완료: seq={}", seq);
    return true;
  }

  private String extractEmail(Map<String, Object> map) {
    if (map == null) {
      return "";
    }
    String email = (String) map.get(PARAM_EMAIL);
    if (email == null) {
      return "";
    }
    return email.trim();
  }

  private String extractAccessIp(Map<String, Object> map) {
    if (map == null) {
      return "";
    }
    String ip = (String) map.get(PARAM_ACCESS_IP);
    if (ip == null) {
      return "";
    }
    return ip;
  }

  private String extractToken(Map<String, Object> map) {
    if (map == null) {
      return "";
    }
    String token = (String) map.get(PARAM_TOKEN);
    if (token == null) {
      return "";
    }
    return token.trim();
  }

  private String extractNewPassword(Map<String, Object> map) {
    if (map == null) {
      return "";
    }
    String password = (String) map.get(PARAM_NEW_PASSWORD);
    if (password == null) {
      return "";
    }
    return password.trim();
  }

  private boolean isValidEmail(String email) {
    return email != null && !email.trim().isEmpty();
  }

  private void validateTokenNotEmpty(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("토큰이 없습니다.");
    }
  }

  private void validatePasswordNotEmpty(String password) {
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("새 비밀번호가 없습니다.");
    }
  }

  private void validatePasswordResetExists(PasswordReset passwordReset) {
    if (passwordReset == null) {
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
    }
  }

  private void validateTokenNotExpired(PasswordReset passwordReset) {
    Date now = new Date();
    if (passwordReset.getExpDate().compareTo(now) < 0) {
      throw new IllegalArgumentException("만료된 토큰입니다. 다시 요청해주세요.");
    }
  }

  private void validateTokenMatches(String token, PasswordReset passwordReset) {
    if (!token.equals(passwordReset.getToken())) {
      throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
    }
  }

  private void validateEmailMatches(String email, PasswordReset passwordReset) {
    if (!email.equals(passwordReset.getEmail())) {
      throw new IllegalArgumentException("이메일이 일치하지 않습니다.");
    }
  }

  private String createAndEncodeToken(Integer seq, String email) throws Exception {
    // 토큰 데이터 생성
    Map<String, Object> tokenData = new HashMap<>();
    tokenData.put(RESPONSE_SEQ, seq);
    tokenData.put(RESPONSE_EMAIL, email);
    
    // JSON 문자열로 변환
    String jsonString = objectMapper.writeValueAsString(tokenData);
    
    // 접두사와 접미사 추가
    String tokenWithPrefix = TOKEN_PREFIX + jsonString + TOKEN_SUFFIX;
    
    // Base64 인코딩 (URL 안전 형식)
    return Base64Utils.encodeToUrlSafeString(
        tokenWithPrefix.getBytes(StandardCharsets.UTF_8)
    );
  }

  private Map<String, Object> decodeToken(String encodedToken) throws Exception {
    byte[] decodedBytes = Base64Utils.decodeFromUrlSafeString(encodedToken);
    String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
    
    String jsonString = decodedString
        .replaceFirst("^" + TOKEN_PREFIX, "")
        .replaceFirst(TOKEN_SUFFIX + "$", "");
    
    return objectMapper.readValue(jsonString, Map.class);
  }

  private Date calculateExpirationTime(Date currentTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentTime);
    calendar.add(Calendar.HOUR_OF_DAY, TOKEN_EXPIRATION_HOURS);
    return calendar.getTime();
  }

  private String createResetUrl(String token) {
    return webUrl + "/api/password/reset?token=" + token;
  }

  private String createMailContent(String resetUrl) {
    try {
      InputStream templateStream = new ClassPathResource(MAIL_TEMPLATE_PATH).getInputStream();
      String templateHtml = new String(IOUtils.toByteArray(templateStream), StandardCharsets.UTF_8);
      return templateHtml.replaceAll(TEMPLATE_RESET_URL, resetUrl);
          
    } catch (Exception e) {
      log.error("메일 템플릿 파일을 읽을 수 없습니다: {}", MAIL_TEMPLATE_PATH, e);
      return createDefaultMailContent(resetUrl);
    }
  }

  private String createDefaultMailContent(String resetUrl) {
    return "<html><body>" +
        "<h2>비밀번호 재설정</h2>" +
        "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>" +
        "<p><a href=\"" + resetUrl + "\">비밀번호 재설정하기</a></p>" +
        "<p>링크는 " + TOKEN_EXPIRATION_HOURS + "시간 동안만 유효합니다.</p>" +
        "</body></html>";
  }

  private void sendMail(String email, String content) throws Exception {
    // 메일 발송 데이터 생성
    Map<String, Object> mailData = new HashMap<>();
    mailData.put("from", DEFAULT_FROM_EMAIL);
    mailData.put("fromName", DEFAULT_FROM_NAME);
    mailData.put("to", email);
    mailData.put("subject", MAIL_SUBJECT);
    mailData.put("content", content);
    mailData.put("contentType", MAIL_CONTENT_TYPE);
    
    mailService.sendMail(mailData);
  }

  private void savePasswordResetInfo(
      Integer seq,
      String email,
      String token,
      Date expirationTime,
      String accessIp,
      Date currentTime) {
    
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setSeq(seq);
    passwordReset.setEmail(email);
    passwordReset.setToken(token);
    passwordReset.setExpDate(expirationTime);
    passwordReset.setUsedYn(false);
    passwordReset.setRegDate(currentTime);
    passwordReset.setRegIp(accessIp);
    
    passwordResetRepo.insertPasswordReset(passwordReset);
  }

  private PasswordReset findPasswordResetByToken(String token) {
    Map<String, Object> searchMap = new HashMap<>();
    searchMap.put(PARAM_TOKEN, token);
    return passwordResetRepo.getPasswordResetByToken(searchMap);
  }

  private void markTokenAsUsed(Integer seq) {
    passwordResetRepo.updatePasswordResetUsed(seq);
  }

  private Map<String, Object> createSuccessResponse(PasswordReset passwordReset) {
    Map<String, Object> result = new HashMap<>();
    result.put(RESPONSE_RESULT, true);
    result.put(RESPONSE_EMAIL, passwordReset.getEmail());
    result.put(RESPONSE_SEQ, passwordReset.getSeq());
    return result;
  }
}

