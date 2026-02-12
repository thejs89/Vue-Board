package com.board.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.domain.Organization;
import com.board.domain.TreeNode;
import com.board.repository.OrganizationRepo;
import com.board.service.ifc.OrganizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class})
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationRepo organizationRepo;

  @Override
  public List<TreeNode> getTree() throws Exception {
    // 모든 조직 조회
    List<Organization> organizations = organizationRepo.findAll();
    
    if (organizations == null || organizations.isEmpty()) {
      return new ArrayList<>();
    }

    // 각 조직의 부모 조직 ID 조회 (조직 ID -> 부모 조직 ID 매핑)
    // 주의: 조직이 많을 경우 성능 이슈가 있을 수 있음 (N+1 쿼리)
    Map<Long, Long> parentMap = new HashMap<>();
    for (Organization org : organizations) {
      Long parentId = organizationRepo.findParentId(org.getOrgId());
      if (parentId != null) {
        parentMap.put(org.getOrgId(), parentId);
      }
    }

    // TreeNode 리스트 생성
    List<TreeNode> treeNodes = new ArrayList<>();
    for (Organization org : organizations) {
      TreeNode node = createTreeNode(org, parentMap);
      treeNodes.add(node);
    }

    return treeNodes;
  }

  @Override
  @Transactional(readOnly = false)
  public Long addOrganization(String orgName, Long parentOrgId) throws Exception {
    // 다음 조직 ID 가져오기
    Long newOrgId = organizationRepo.getNextOrgId();
    
    // 조직 정보 저장
    Organization organization = new Organization();
    organization.setOrgId(newOrgId);
    organization.setOrgName(orgName);
    organizationRepo.insertOrganization(organization);
    
    // 자기 자신과의 관계 추가 (depth=0)
    organizationRepo.insertSelfRelation(newOrgId);
    
    // 부모 조직이 있으면 부모와의 관계 추가
    if (parentOrgId != null) {
      organizationRepo.insertParentRelations(newOrgId, parentOrgId);
    }
    
    return newOrgId;
  }

  @Override
  @Transactional(readOnly = false)
  public void moveOrganization(Long orgId, Long newParentId) throws Exception {
    // 기존 부모 관계 삭제 (자기 자신 제외)
    organizationRepo.deleteParentRelations(orgId);
    
    // 새 부모가 있으면 새 부모와의 관계 추가
    if (newParentId != null) {
      organizationRepo.moveToNewParent(orgId, newParentId);
    }
  }

  /**
   * Organization을 TreeNode로 변환
   */
  private TreeNode createTreeNode(Organization org, Map<Long, Long> parentMap) {
    TreeNode node = new TreeNode();
    
    // 노드 ID 설정
    node.setId(String.valueOf(org.getOrgId()));
    
    // 부모 노드 설정 (부모가 없으면 "#"으로 루트 노드 표시)
    Long parentId = parentMap.get(org.getOrgId());
    if (parentId != null) {
      node.setParent(String.valueOf(parentId));
    } else {
      node.setParent("#");
    }
    
    // 노드 텍스트 설정 (조직명)
    node.setText(org.getOrgName());
    
    // 노드 데이터 설정
    TreeNode.TreeNodeData data = new TreeNode.TreeNodeData();
    data.setOrgId(org.getOrgId());
    data.setOrgName(org.getOrgName());
    node.setData(data);
    
    return node;
  }
}

