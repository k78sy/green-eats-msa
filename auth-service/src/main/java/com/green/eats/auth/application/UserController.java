package com.green.eats.auth.application;

import com.green.eats.auth.application.model.UserSigninReq;
import com.green.eats.auth.application.model.UserSigninRes;
import com.green.eats.auth.application.model.UserSignupReq;
import com.green.eats.auth.application.model.UserUpdateReq;
import com.green.eats.auth.entity.User;
import com.green.eats.common.auth.UserContext;
import com.green.eats.common.model.JwtUser;
import com.green.eats.common.model.ResultResponse;
import com.green.eats.common.model.UserDto;
import com.green.eats.common.model.UserPrincipal;
import com.green.eats.common.security.JwtTokenManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenManager jwtTokenManager;

    @PostMapping("/signup")
    public ResultResponse<?> signup(@RequestBody UserSignupReq req){
        log.info("req: {}", req);
        userService.signup(req);
        return ResultResponse.builder()
                .resultMessage("회원가입 성공")
                .resultData(1)
                .build();
    }

    @PostMapping("/signin")
    public ResultResponse<?> signin( HttpServletResponse res, @RequestBody UserSigninReq req ){
        log.info("req: {}", req);
        User signedUser = userService.signin(req);

        // 보안 쿠키 처리
        JwtUser jwtUser = new JwtUser( signedUser.getId(), signedUser.getName(), signedUser.getEnumUserRole() );
        jwtTokenManager.issue( res, jwtUser );

        UserSigninRes userSigninRes = UserSigninRes.builder()
                .id( signedUser.getId() )
                .name( signedUser.getName() )
                .build();

        return ResultResponse.builder()
                .resultMessage("로그인 성공")
                .resultData( userSigninRes ) // userSigninRes 내용을 그대로 넣어도 됨
                .build();
    }

    @PutMapping
    public ResultResponse<?> update(@Valid @RequestBody UserUpdateReq req){
        UserDto userDto = UserContext.get(); // 토큰에서 로그인 유저의 정보를 가져오는거...

        Long id = userDto.id();
        log.info("signedUserId: {}", id);
        userService.update(id, req);

        return ResultResponse.builder()
                .resultMessage("수정 성공")
                .resultData( req ) // userSigninRes 내용을 그대로 넣어도 됨
                .build();
    }
}
