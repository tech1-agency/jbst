package io.tech1.framework.iam.postgres.configs;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TestsApplicationRepositoriesRunner {

    @AfterEach
    void afterEach() {
        this.getJpaRepository().deleteAll();
    }

    public abstract JpaRepository<?, String> getJpaRepository();
}
