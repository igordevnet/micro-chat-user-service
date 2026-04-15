package com.microservice.microchatuserservice.infrastructure.persistence;

import com.microservice.microchatuserservice.infrastructure.persistence.entities.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, String> {
    @Query(value = """
      select t from TokenEntity t inner join UserEntity u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<TokenEntity> findAllValidTokenByUser(Long id);

    Optional<TokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenEntity t WHERE t.expired = true OR t.revoked = true")
    void deleteTokensWhereExpiredOrRevoked();
}
