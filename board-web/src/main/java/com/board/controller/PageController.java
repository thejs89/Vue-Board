package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping(value = {
		"/",
		"/{p1:[^\\.]*}",
		"/{p1:[^\\.]*}/{p2:[^\\.]*}",
		"/{p1:[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}"
	})
	public String index() throws Exception {
		return "forward:/index.html";
	}
}
