package com.green.eats.store.application;

import com.green.eats.common.auth.UserContext;
import com.green.eats.common.model.MenuGetClientRes;
import com.green.eats.common.model.ResultResponse;
import com.green.eats.common.model.UserDto;
import com.green.eats.store.application.model.MenuGetRes;
import com.green.eats.store.application.model.MenuPostReq;
import com.green.eats.store.entity.Menu;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @PostMapping("/menu")
    public ResultResponse<?> addMenu(@Valid @RequestBody MenuPostReq req) {
        log.info("menuPostReq: {}", req);
        storeService.addMenu(req);
        return ResultResponse.builder()
                .resultMessage( "success" )
                .build();
    }

    @GetMapping("/menu")
    public ResultResponse<?> getAllMenus(){
        UserDto userDto = UserContext.get(); // 토큰에서 로그인 유저 정보 담기
        log.info("userDto: {}", userDto);

        List<MenuGetRes> menus = storeService.getAllMenus();
        return ResultResponse.builder()
                .resultMessage( String.format("%d rows", menus.size()) )
                .resultData( menus )
                .build();
    }

    @GetMapping("/menu/list")
    public Map<Long, MenuGetClientRes> getAllMenus(@RequestParam List<Long> menuIds) {
        log.info("menuIds: {}, size: {}", menuIds, menuIds.size());
        return storeService.getMenuListByIds(menuIds);
    }
}
