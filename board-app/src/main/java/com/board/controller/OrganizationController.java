package com.board.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.board.domain.TreeNode;
import com.board.service.ifc.OrganizationService;
import java.util.HashMap;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/organization")
@RestController
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  /**
   * 조직 트리 데이터 조회 API
   */
  @GetMapping("/tree")
  public Map<String, Object> getTreeData() throws Exception {
    List<TreeNode> tree = organizationService.getTree();
    
    Map<String, Object> result = new HashMap<>();
    result.put("tree", tree);
    return result;
  }

  /**
   * 새 조직 추가 API
   */
  @PostMapping("/add")
  public Map<String, Object> addOrganization(
      @RequestParam String orgName,
      @RequestParam(required = false) Long parentOrgId) throws Exception {
    Long newOrgId = organizationService.addOrganization(orgName, parentOrgId);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("orgId", newOrgId);
    result.put("message", "조직이 추가되었습니다.");
    return result;
  }

  /**
   * 조직 이동 API
   */
  @PostMapping("/move")
  public Map<String, Object> moveOrganization(
      @RequestParam Long orgId,
      @RequestParam(required = false) Long newParentId) throws Exception {
    organizationService.moveOrganization(orgId, newParentId);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "조직이 이동되었습니다.");
    return result;
  }

}

