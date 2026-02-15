package com.board.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.board.config.BoardMapper;
import com.board.domain.BoardFile;

@BoardMapper
@Repository
public interface BoardFileRepo {

  Integer insertBoardFile(BoardFile boardFile);
  List<BoardFile> getBoardFileList(Integer boardSeq);
  BoardFile getBoardFileInfo(Map<String, Object> map);
  Integer deleteBoardFile(Map<String, Object> map);
  
}

