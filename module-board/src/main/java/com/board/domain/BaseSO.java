package com.board.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.primitives.Ints;

public class BaseSO extends LinkedHashMap<String,Object> {
	// 사용할 변수명
	public static final String VAR_PAGE = "page";
	public static final String VAR_SIZE = "size";
	public static final String VAR_ORDER = "order";
	public static final String VAR_BY = "by";

  // 미정의시 기본값
	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_SIZE = 10;
	private static final String DEFAULT_ORDER_BY = "desc";

  public BaseSO() {
    init();
  }
  
  public BaseSO(Map<String, Object> map) {
    super(map);
		init();
	}

  private void init() {
		// 페이지 번호 설정 (기본값: 1)
		String pageStr = (String) this.get(VAR_PAGE);
		Integer page = DEFAULT_PAGE;
		if (pageStr != null && !pageStr.trim().isEmpty()) {
			Integer parsedPage = Ints.tryParse(pageStr);
			if (parsedPage != null) {
				page = parsedPage;
			}
		}
		this.put(VAR_PAGE, page);
		
		// 페이지 크기 설정 (기본값: 10)
		String sizeStr = (String) this.get(VAR_SIZE);
		Integer size = DEFAULT_SIZE;
		if (sizeStr != null && !sizeStr.trim().isEmpty()) {
			Integer parsedSize = Ints.tryParse(sizeStr);
			if (parsedSize != null) {
				size = parsedSize;
			}
		}
		this.put(VAR_SIZE, size);
		
		// 정렬 컬럼 설정 (기본값: 빈 문자열)
		String order = (String) this.get(VAR_ORDER);
		if (order != null) {
			this.put(VAR_ORDER, order.trim());
		} else {
			this.put(VAR_ORDER, "");
		}
		
		// 정렬 방향 설정 (기본값: desc)
		String by = (String) this.get(VAR_BY);
		if (by != null && (by.equalsIgnoreCase("asc") || by.equalsIgnoreCase("desc"))) {
			this.put(VAR_BY, by);
		} else {
			this.put(VAR_BY, DEFAULT_ORDER_BY);
		}
  }

  
}

