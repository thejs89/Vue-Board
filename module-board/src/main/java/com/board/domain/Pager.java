package com.board.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Pager<T extends PageVO> {

  @Builder.Default
  private Integer totalCount = 0;
  @Builder.Default
  private Integer currentPage = 0;
  @Builder.Default
  private Integer totalPage = 0;
  @Builder.Default
  private List<T> contents = new ArrayList<>();

  public Pager(List<T> contents) {
    // 빈 리스트인 경우 기본값 설정
    if (contents == null || contents.size() == 0) {
      this.contents = new ArrayList<>();
      this.totalCount = 0;
      this.currentPage = 0;
      this.totalPage = 0; // 프론트엔드에서 계산하므로 0으로 설정
    } else {
      // 첫 번째 항목에서 페이지 정보 가져오기
      this.contents = contents;
      T item = contents.get(0);
      this.totalCount = item.getTotalCount();
      this.currentPage = item.getCurrentPage();
      // totalPage는 프론트엔드에서 계산하므로 0으로 설정
      this.totalPage = 0;
    }
  }
  public static <P extends PageVO> Pager<P> formList(List<P> contents) {
    return new Pager<P>(contents);
  }

}

