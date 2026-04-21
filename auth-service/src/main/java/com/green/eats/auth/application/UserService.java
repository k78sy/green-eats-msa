package com.green.eats.auth.application;

import com.green.eats.auth.application.model.UserSigninReq;
import com.green.eats.auth.application.model.UserSignupReq;
import com.green.eats.auth.application.model.UserUpdateReq;
import com.green.eats.auth.entity.User;
import com.green.eats.auth.exception.UserErrorCode;
import com.green.eats.common.constants.UserEventType;
import com.green.eats.common.exception.BusinessException;
import com.green.eats.common.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void signup(UserSignupReq req) {
        // 비밀번호 암호화
        String hashedPw = passwordEncoder.encode( req.getPassword() );

        //회원가입시켜주세요
        User newUser = new User();
        newUser.setEmail( req.getEmail() );
        newUser.setPassword( hashedPw );
        newUser.setName( req.getName() );
        newUser.setAddress( req.getAddress() );
        newUser.setIsDel( false );
        newUser.setEnumUserRole( req.getUserRole() );

        userRepository.save(newUser);

        // 카프카(집배원)에게 신호를 보내는 셈...
        UserEvent userEvent = UserEvent.builder()
                .userId( newUser.getId() )
                .name( newUser.getName() )
                .eventType( UserEventType.CREATE )
                .build();

        // 카프카..에게  이 통신의 이름.unique.보내는값 담아 콜백 함수
        kafkaTemplate(newUser, userEvent);

    }

    public User signin(UserSigninReq req){
        User signedUser = userRepository.findByEmail( req.getEmail() );
        log.info("signedUser: {}", signedUser);

        if(signedUser == null || !passwordEncoder.matches( req.getPassword(), signedUser.getPassword() ) ){
            notFoundUserAndNotMatchedPassword();
        }
        return signedUser;
    }

    private void notFoundUserAndNotMatchedPassword() {
        throw new BusinessException(UserErrorCode.CHECK_EMAIL_PASSWORD);
    }

    @Transactional
    public void update(Long userId, UserUpdateReq req){
        User res = userRepository.findById( userId ).orElseThrow();
        res.setName( req.getName() );
        res.setAddress( req.getAddress() );
        userRepository.save( res );

        UserEvent userEvent = UserEvent.builder()
                .userId( res.getId() )
                .name( res.getName() )
                .eventType( UserEventType.UPDATE )
                .build();

        kafkaTemplate(res, userEvent);
    }

    @Transactional
    public void delete(Long signedUserId){
        User res = userRepository.findById( signedUserId ).orElseThrow();
        res.setIsDel( true );
        userRepository.save( res );

        UserEvent userEvent = UserEvent.builder()
                .userId( res.getId() )
                .name( res.getName() )
                .eventType( UserEventType.DELETE )
                .build();

        kafkaTemplate(res, userEvent);
    }



    private void kafkaTemplate(User user, UserEvent userEvent){

        kafkaTemplate.send("user-topic", String.valueOf(user.getId()), userEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // 성공 시 로그
                        log.info("✅ [Kafka Success] Topic: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    } else {
                        // 실패 시 로그
                        log.error("❌ [Kafka Failure] 원인: {}", ex.getMessage());
                    }
                });

    }
}
