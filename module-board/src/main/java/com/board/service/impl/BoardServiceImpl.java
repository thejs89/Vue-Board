package com.board.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.board.domain.Board;
import com.board.domain.BoardFile;
import com.board.domain.PageBoard;
import com.board.repository.BoardFileRepo;
import com.board.repository.BoardRepo;
import com.board.service.ifc.BoardService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class})
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

  private static final String DEFAULT_USER_ID = "jsjeon";
  private static final String DEFAULT_UPLOAD_PATH = "board";
  private static final String TEMP_FILE_PREFIX = "777";
  private static final int DEFAULT_GROUP_ORDER = 0;
  private static final int DEFAULT_DEPTH = 0;

  private final BoardRepo boardRepo;
  private final BoardFileRepo boardFileRepo;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${board.path.upload}")
  private String UPLOAD_PATH;

  @Transactional(readOnly = true)
  @Override
  public List<PageBoard> getBoardList(Map<String, Object> map) throws Exception {
    return boardRepo.getBoardList(map);
  }

  @Transactional(readOnly = false)
  @Override
  public void insertBoard(List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    Board board = convertToBoard(map);
    setDefaultBoardValues(board);
    boardRepo.insertBoard(board);

    uploadFiles(board.getSeq(), fileList, map);
  }

  @Transactional(readOnly = true)
  @Override
  public Board getBoardView(Integer seq) throws Exception {
    return boardRepo.getBoardView(seq);
  }

  @Transactional(readOnly = true)
  @Override
  public List<BoardFile> getBoardFileList(Integer boardSeq) throws Exception {
    return boardFileRepo.getBoardFileList(boardSeq);
  }

  @Transactional(readOnly = false)
  @Override
  public void insertReplyBoard(List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    String parentSeqStr = (String) map.get("parentSeq");
    if (parentSeqStr == null) {
      throw new IllegalArgumentException("parentSeq는 필수입니다.");
    }

    Board parentBoard = boardRepo.getBoardView(Integer.parseInt(parentSeqStr));
    if (parentBoard == null) {
      throw new IllegalArgumentException("부모 게시글이 존재하지 않습니다.");
    }

    updateGroupOrder(parentBoard.getGroupId(), parentBoard.getGroupOrder());
    setDefaultReplyBoardValues(map);
    boardRepo.insertReplyBoard(map);
    
    // insertReplyBoard 후 selectKey로 seq가 map에 설정됨
    Integer replySeq = (Integer) map.get("seq");
    if (replySeq != null) {
      uploadFiles(replySeq, fileList, map);
    }
  }

  @Transactional(readOnly = false)
  @Override
  public void updateBoard(List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    Board board = convertToBoard(map);
    setUpdateBoardValues(board);
    boardRepo.updateBoard(board);

    uploadFiles(board.getSeq(), fileList, map);
  }

  @Transactional(readOnly = false)
  @Override
  public void deleteBoard(Integer seq) throws Exception {
    boardRepo.deleteBoard(seq);
  }

  @Transactional(readOnly = true)
  @Override
  public org.springframework.core.io.Resource downloadFile(Map<String, Object> map) throws Exception {
    BoardFile file = boardFileRepo.getBoardFileInfo(map);
    if (file == null) {
      throw new RuntimeException("파일을 찾을 수 없습니다.");
    }

    String subPath = file.getUploadPath();
    java.nio.file.Path filePath = java.nio.file.Paths.get(UPLOAD_PATH, subPath, file.getUploadName()).normalize();
    org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

    if (!resource.exists()) {
      throw new RuntimeException("파일이 존재하지 않습니다.");
    }

    return resource;
  }

  @Transactional(readOnly = true)
  @Override
  public BoardFile getBoardFileInfo(Map<String, Object> map) throws Exception {
    return boardFileRepo.getBoardFileInfo(map);
  }

  /**
   * Map을 Board 객체로 변환
   */
  private Board convertToBoard(Map<String, Object> map) {
    return objectMapper.convertValue(map, Board.class);
  }

  /**
   * 게시글 기본값 설정
   */
  private void setDefaultBoardValues(Board board) {
    Date now = new Date();
    board.setGroupOrder(DEFAULT_GROUP_ORDER);
    board.setDepth(DEFAULT_DEPTH);
    board.setDeleteYn(false);
    board.setRegDate(now);
    board.setRegId(DEFAULT_USER_ID);
    board.setUpdDate(now);
    board.setUpdId(DEFAULT_USER_ID);
  }

  /**
   * 답글 게시글 기본값 설정
   */
  private void setDefaultReplyBoardValues(Map<String, Object> map) {
    Date now = new Date();
    map.put("deleteYn", false);
    map.put("regDate", now);
    map.put("regId", DEFAULT_USER_ID);
    map.put("updDate", now);
    map.put("updId", DEFAULT_USER_ID);
  }

  /**
   * 게시글 수정값 설정
   */
  private void setUpdateBoardValues(Board board) {
    Date now = new Date();
    board.setUpdDate(now);
    board.setUpdId(DEFAULT_USER_ID);
  }

  /**
   * 파일 업로드 처리
   */
  private void uploadFiles(Integer boardSeq, List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    // 삭제할 파일 처리
    removeFiles(map);
    
    // 신규 파일 업로드 처리
    List<MultipartFile> validFileList = filterValidFiles(fileList);
    if (validFileList.isEmpty()) { 
      return;
    }

    List<Map<String, Object>> fileInfoList = parseFileInfoList(map, validFileList.size());
    Map<String, Object> baseFileInfo = parseBaseFileInfo(map);

    // 파일 업로드 파라미터 생성
    List<Map<String, Object>> fileUploadParams = new ArrayList<>();
    for (int i = 0; i < validFileList.size(); i++) {
      MultipartFile file = validFileList.get(i);
      Map<String, Object> fileInfo = fileInfoList.get(i);
      
      Map<String, Object> params = new HashMap<>();
      params.putAll(baseFileInfo);
      params.putAll(fileInfo);
      params.put("seq", boardSeq);
      params.put("file", file);
      params.put("userId", DEFAULT_USER_ID);
      
      fileUploadParams.add(params);
    }
    
    processFileUploads(fileUploadParams);
  }

  /**
   * 삭제할 파일 처리
   */
  private void removeFiles(Map<String, Object> map) throws Exception {
    if (map == null) {
      return;
    }
    
    String removeFilesJson = (String) map.get("removeFiles");
    if (removeFilesJson == null || removeFilesJson.trim().isEmpty()) {
      return;
    }
    
    try {
      Map<String, Object> removeFilesMap = objectMapper.readValue(removeFilesJson, Map.class);
      List<String> keyList = (List<String>) removeFilesMap.get("key");
      if (keyList == null || keyList.isEmpty()) {
        return;
      }
      
      // key를 fileSeq로 변환하여 하나씩 삭제
      Date now = new Date();
      for (String key : keyList) {
        try {
          Integer fileSeq = Integer.parseInt(key);
          Map<String, Object> deleteParams = new HashMap<>();
          deleteParams.put("fileSeq", fileSeq);
          deleteParams.put("updDate", now);
          boardFileRepo.deleteBoardFile(deleteParams);
        } catch (NumberFormatException e) {
          // key가 숫자가 아닌 경우 무시 (신규 파일의 경우)
          log.warn("삭제할 파일 key가 숫자가 아닙니다: {}", key);
        }
      }
    } catch (Exception e) {
      log.error("파일 삭제 처리 실패: {}", e.getMessage(), e);
      throw new RuntimeException("파일 삭제 처리 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 유효한 파일만 필터링
   */
  private List<MultipartFile> filterValidFiles(List<MultipartFile> fileList) {
    if (fileList == null || fileList.isEmpty()) {
      return new ArrayList<>();
    }
    
    List<MultipartFile> validFiles = new ArrayList<>();
    for (MultipartFile file : fileList) {
      if (file != null && !file.isEmpty()) {
        validFiles.add(file);
      }
    }
    return validFiles;
  }

  /**
   * 파일 정보 리스트 파싱
   */
  private List<Map<String, Object>> parseFileInfoList(Map<String, Object> map, int fileCount) {
    if (map == null) {
      return createEmptyFileInfoList(fileCount);
    }
    
    Object fileInfoObj = map.get("fileInfo");
    if (fileInfoObj == null || !(fileInfoObj instanceof String)) {
      return createEmptyFileInfoList(fileCount);
    }
    
    String json = (String) fileInfoObj;
    List<Map<String, Object>> fileInfoList = parseJsonToList(json);
    
    if (fileInfoList == null || fileInfoList.size() != fileCount) {
      return createEmptyFileInfoList(fileCount);
    }
    
    return fileInfoList;
  }

  /**
   * JSON 문자열을 List로 파싱
   */
  private List<Map<String, Object>> parseJsonToList(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    } catch (Exception e) {
      log.warn("파일 정보 JSON 파싱 실패: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 빈 파일 정보 리스트 생성
   */
  private List<Map<String, Object>> createEmptyFileInfoList(int count) {
    List<Map<String, Object>> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(new HashMap<String, Object>());
    }
    return list;
  }

  /**
   * 기본 파일 정보 파싱
   */
  private Map<String, Object> parseBaseFileInfo(Map<String, Object> map) {
    if (map == null) {
      return new HashMap<>();
    }
    
    Object baseFileInfoObj = map.get("baseFileInfo");
    if (baseFileInfoObj == null || !(baseFileInfoObj instanceof String)) {
      return new HashMap<>();
    }
    
    String json = (String) baseFileInfoObj;
    Map<String, Object> baseFileInfo = parseJsonToMap(json);
    
    if (baseFileInfo == null) {
      return new HashMap<>();
    }
    
    return baseFileInfo;
  }

  /**
   * JSON 문자열을 Map으로 파싱
   */
  private Map<String, Object> parseJsonToMap(String json) {
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
      log.warn("기본 파일 정보 JSON 파싱 실패: {}", e.getMessage());
      return null;
    }
  }


  /**
   * 파일 업로드 처리 및 검증
   */
  private void processFileUploads(List<Map<String, Object>> fileUploadParams) throws Exception {
    for (Map<String, Object> params : fileUploadParams) {
      Map<String, Object> result = uploadFile(params);
      if (!Boolean.TRUE.equals(result.get("result"))) {
        // 실패한 파일명 가져오기
        Map<String, Object> fileInfo = (Map<String, Object>) result.get("file");
        String fileName = "알 수 없는 파일";
        if (fileInfo != null) {
          fileName = (String) fileInfo.get("fileName");
        }
        throw new RuntimeException("파일 업로드 실패: " + fileName);
      }
    }
  }


  /**
   * 단일 파일 업로드 처리
   */
  private Map<String, Object> uploadFile(Map<String, Object> params) {
    MultipartFile file = (MultipartFile) params.get("file");
    if (file == null) {
      return createFailureResult("파일이 null입니다.", null, null);
    }

    String fileName = file.getOriginalFilename();
    if (fileName == null || fileName.isEmpty()) {
      return createFailureResult("파일명이 없습니다.", null, null);
    }

    Integer fileSize = ((Long) file.getSize()).intValue();
    
    // 업로드 경로 설정 (기본값: "board")
    String uploadPath = (String) params.get("path");
    if (uploadPath == null || uploadPath.isEmpty()) {
      uploadPath = DEFAULT_UPLOAD_PATH;
    }

    try {
      String uploadName = saveFileToDisk(file, uploadPath, fileName);
      BoardFile boardFile = createBoardFile(params, fileName, fileSize, uploadPath, uploadName);
      boardFileRepo.insertBoardFile(boardFile);

      return createSuccessResult(fileName, fileSize, uploadPath, uploadName, boardFile.getFileSeq());
    } catch (Exception e) {
      log.error("파일 업로드 실패: {}", fileName, e);
      return createFailureResult(fileName, fileSize, uploadPath);
    }
  }

  /**
   * 파일을 디스크에 저장
   */
  private String saveFileToDisk(MultipartFile file, String relativePath, String fileName) throws Exception {
    String extension = com.google.common.io.Files.getFileExtension(fileName);
    String suffix = String.format(".%s", extension);

    Path targetPath = Paths.get(UPLOAD_PATH, relativePath);
    if (!java.nio.file.Files.exists(targetPath)) {
      java.nio.file.Files.createDirectories(targetPath);
    }

    File saveFile = File.createTempFile(
        TEMP_FILE_PREFIX,
        suffix,
        new File(UPLOAD_PATH + "/" + relativePath));
    file.transferTo(saveFile);

    return saveFile.getName();
  }

  /**
   * BoardFile 엔티티 생성
   */
  private BoardFile createBoardFile(
      Map<String, Object> params,
      String fileName,
      Integer fileSize,
      String uploadPath,
      String uploadName) {
    
    Integer boardSeq = (Integer) params.get("seq");
    
    // 사용자 ID 설정 (기본값: "jsjeon")
    String userId = (String) params.get("userId");
    if (userId == null) {
      userId = DEFAULT_USER_ID;
    }
    
    Date now = new Date();

    BoardFile boardFile = new BoardFile();
    boardFile.setBoardSeq(boardSeq);
    boardFile.setFileName(fileName);
    boardFile.setFileSize(fileSize);
    boardFile.setUploadName(uploadName);
    boardFile.setUploadPath(uploadPath);
    boardFile.setDeleteYn(false);
    boardFile.setRegDate(now);
    boardFile.setRegId(userId);
    boardFile.setUpdDate(now);
    boardFile.setUpdId(userId);

    return boardFile;
  }

  /**
   * 성공 결과 생성
   */
  private Map<String, Object> createSuccessResult(
      String fileName, Integer fileSize, String uploadPath, String uploadName, Integer fileSeq) {
    Map<String, Object> result = new HashMap<>();
    result.put("result", true);
    
    Map<String, Object> fileInfo = new HashMap<>();
    fileInfo.put("fileName", fileName);
    fileInfo.put("fileSize", fileSize);
    fileInfo.put("uploadPath", uploadPath);
    fileInfo.put("uploadName", uploadName);
    fileInfo.put("fileSeq", fileSeq);
    
    result.put("file", fileInfo);
    return result;
  }

  /**
   * 실패 결과 생성
   */
  private Map<String, Object> createFailureResult(String fileName, Integer fileSize, String uploadPath) {
    Map<String, Object> result = new HashMap<>();
    result.put("result", false);
    
    Map<String, Object> fileInfo = new HashMap<>();
    fileInfo.put("fileName", fileName != null ? fileName : "알 수 없는 파일");
    fileInfo.put("fileSize", fileSize != null ? fileSize : 0);
    fileInfo.put("uploadPath", uploadPath != null ? uploadPath : "");
    
    result.put("file", fileInfo);
    return result;
  }

  /**
   * 그룹 순서 업데이트
   */
  private void updateGroupOrder(Integer groupId, Integer groupOrder) {
    Map<String, Object> params = new HashMap<>();
    params.put("groupId", groupId);
    params.put("groupOrder", groupOrder);
    boardRepo.updateGroupOrd(params);
  }
}

