package app.giftify.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthTestController {

    @GetMapping("/")
    public String publicPage() {
        return "여기는 아무나 볼 수 있는 페이지입니다.";
    }

    @GetMapping("/private")
    public String privatePage(@AuthenticationPrincipal OAuth2User principal) {
        return "로그인 성공! 환영합니다, " + principal.getAttribute("name") + "님.";
    }
}