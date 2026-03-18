package com.example.demo.agent.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth/notion")
public class NotionAuthController {

    @GetMapping("/callback")
    public String notionCallback(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "error", required = false) String error,
                                 Model model) {
        if (error != null) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "인증에 실패했습니다: " + error);
            return "notion-auth-result";
        }

        if (code != null) {
            // TODO: 여기서 받은 code를 이용해 Notion API에 Access Token을 요청해야 합니다.
            // 지금은 페이지 테스트를 위해 성공 메시지만 전달합니다.
            model.addAttribute("status", "success");
            model.addAttribute("code", code);
            model.addAttribute("message", "인증 코드가 성공적으로 발급되었습니다!");
        }

        return "notion-auth-result";
    }
}