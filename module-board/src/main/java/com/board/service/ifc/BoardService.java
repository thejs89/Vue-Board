package com.board.service.ifc;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.board.domain.Board;
import com.board.domain.BoardFile;
import com.board.domain.PageBoard;

public interface BoardService {
  List<PageBoard> getBoardList(Map<String,Object> map) throws Exception;
  void insertBoard(List<MultipartFile> fileList,Map<String, Object> map) throws Exception;
  Board getBoardView(Integer seq) throws Exception;
  List<BoardFile> getBoardFileList(Integer boardSeq) throws Exception;
  void insertReplyBoard(List<MultipartFile> fileList, Map<String,Object> map) throws Exception;
  void deleteBoard(Integer seq) throws Exception;
  Resource downloadFile(Map<String, Object> map) throws Exception;
  BoardFile getBoardFileInfo(Map<String, Object> map) throws Exception;
   
}

