package com.board.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.board.config.BoardMapper;
import com.board.domain.Board;
import com.board.domain.PageBoard;

@BoardMapper
@Repository
public interface BoardRepo {

  List<PageBoard> getBoardList(Map<String,Object> map);
  Integer insertBoard(Board board);
  Board getBoardView(Integer seq);
  Integer insertReplyBoard(Map<String,Object> map);
  Integer updateGroupOrd(Map<String,Object> map);
  Integer updateBoard(Board board);
  Integer deleteBoard(Integer seq);
  
}

