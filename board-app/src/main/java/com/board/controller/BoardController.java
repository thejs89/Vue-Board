package com.board.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.board.domain.BaseSO;
import com.board.domain.Board;
import com.board.domain.BoardFile;
import com.board.domain.PageBoard;
import com.board.domain.Pager;
import com.board.service.ifc.BoardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/board")
@RestController
@RequiredArgsConstructor
public class BoardController {

	private final BoardService boardService;

  @GetMapping("/list")
  public Map<String, Object> getBoardList(@RequestParam(required = false) Map<String,Object> map) throws Exception {
    BaseSO so = new BaseSO(map);
		List<PageBoard> list = boardService.getBoardList(so);
    Pager<PageBoard> page = Pager.formList(list);

    Map<String, Object> result = new HashMap<>();
    result.put("page", page);
    return result;
  }

  @GetMapping("/{seq}")
  public Map<String, Object> getBoardView(@PathVariable Integer seq) throws Exception {
    Board board = boardService.getBoardView(seq);
    List<BoardFile> fileList = boardService.getBoardFileList(seq);
    
    Map<String, Object> result = new HashMap<>();
    result.put("board", board);
    result.put("fileList", fileList);
    return result;
  }

  @PostMapping("")
  public Map<String, Object> insertBoard(
      @RequestPart(value = "file", required = false) List<MultipartFile> fileList,
      @RequestParam(required = false) Map<String, Object> map) throws Exception {
    
    if (map == null) {
      map = new HashMap<>();
    }
    boardService.insertBoard(fileList, map);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "게시글이 등록되었습니다.");
    return result;
  }

  @PostMapping("/reply")
  public Map<String, Object> insertReplyBoard(
      @RequestPart(value = "file", required = false) List<MultipartFile> fileList,
      @RequestParam(required = false) Map<String, Object> map) throws Exception {
    
    if (map == null) {
      map = new HashMap<>();
    }
    boardService.insertReplyBoard(fileList, map);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "답글이 등록되었습니다.");
    return result;
  }

  @DeleteMapping("/{seq}")
  public Map<String, Object> deleteBoard(@PathVariable Integer seq) throws Exception {
    boardService.deleteBoard(seq);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "게시글이 삭제되었습니다.");
    return result;
  }

  @GetMapping("/file/download")
  public ResponseEntity<Resource> downloadFile(@RequestParam Integer fileSeq) throws Exception {
    Map<String, Object> searchMap = new HashMap<>();
    searchMap.put("fileSeq", fileSeq);
    
    // 파일 정보 조회
    BoardFile fileInfo = boardService.getBoardFileInfo(searchMap);
    if (fileInfo == null) {
      throw new RuntimeException("파일을 찾을 수 없습니다.");
    }
    
    // 파일 다운로드
    Resource resource = boardService.downloadFile(searchMap);
    
    String fileName = fileInfo.getFileName();
    String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
    
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"; filename*= UTF-8''%s", fileName, encodedFileName))
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }

}

