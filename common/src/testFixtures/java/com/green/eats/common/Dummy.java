package com.green.eats.common;

import net.datafaker.Faker;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;

import java.util.Locale;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 테스트 DB가 아닌 기존 DB
@Rollback(false) // 테스트 끝나면 되돌리는 롤백 막기
public abstract class Dummy {
    protected Faker koFaker = new Faker(Locale.KOREA);
    protected Faker enFaker = new Faker(Locale.ENGLISH);
}
