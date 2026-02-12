package com.board.mail.service.impl;

import java.util.Date;
import java.util.Map;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.board.mail.domain.MailPO;
import com.board.mail.service.ifc.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailServiceImpl implements MailService {
  
  @Autowired
  private JavaMailSender javaMailSender;
  
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  @Override
  public void sendMail(Map<String, Object> map) throws Exception {
    MailPO po = objectMapper.convertValue(map, MailPO.class);
    sendMail(po);
  }
  
  @Override
  public void sendMail(MailPO po) throws Exception {
    MimeMessage msg = javaMailSender.createMimeMessage();

    // 메일 정보 가져오기 (null이면 빈 문자열로 설정)
    String from = po.getFrom();
    if (from == null) {
      from = "";
    }
    
    String fromName = po.getFromName();
    if (fromName == null) {
      fromName = "";
    }
    
    String to = po.getTo();
    if (to == null) {
      to = "";
    }
    
    String cc = po.getCc();
    if (cc == null) {
      cc = "";
    }
    
    String bcc = po.getBcc();
    if (bcc == null) {
      bcc = "";
    }
    
    String subject = po.getSubject();
    if (subject == null) {
      subject = "";
    }
    
    String content = po.getContent();
    if (content == null) {
      content = "";
    }
    
    String contentType = po.getContentType();
    if (contentType == null) {
      contentType = "text/html;charset=UTF-8";
    }
    Date now = new Date();
    
    log.debug("from: {}", from);
    log.debug("fromName: {}", fromName);
    log.debug("to: {}", to);
    log.debug("cc: {}", cc);
    log.debug("bcc: {}", bcc);
    log.debug("subject: {}", subject);
    log.debug("content: {}", content);
    log.debug("contentType: {}", contentType);
    log.debug("now: {}", now);

    if (!from.isEmpty()) {
      msg.setFrom(fromName.isEmpty() 
          ? new InternetAddress(from) 
          : new InternetAddress(from, fromName));
    }
    
    if (!to.isEmpty()) {
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    }
    
    if (!cc.isEmpty()) {
      msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
    }
    
    if (!bcc.isEmpty()) {
      msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
    }
    
    msg.setSubject(subject);
    msg.setContent(content, contentType);
    msg.setSentDate(now);
    
    javaMailSender.send(msg);
  }
}

