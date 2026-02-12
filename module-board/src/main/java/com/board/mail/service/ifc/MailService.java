package com.board.mail.service.ifc;

import java.util.Map;

import com.board.mail.domain.MailPO;

public interface MailService {
  void sendMail(Map<String, Object> map) throws Exception;
  void sendMail(MailPO po) throws Exception;
}

