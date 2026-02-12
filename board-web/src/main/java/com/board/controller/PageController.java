package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class PageController {

	@GetMapping(value = { "", "index" })
	public String index() throws Exception {
		return "forward:index.html";
	}
}

