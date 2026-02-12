package com.board.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.board.config.BoardMapper;
import com.board.domain.PasswordReset;

@BoardMapper
@Repository
public interface PasswordResetRepo {
  
  Integer getNextPasswordResetSeq();
  void insertPasswordReset(PasswordReset passwordReset);
  PasswordReset getPasswordResetByToken(Map<String, Object> map);
  void updatePasswordResetUsed(Integer seq);
  void invalidatePreviousTokens(String email);
  
}

