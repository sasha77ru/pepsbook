package ru.sasha77.spring.pepsbook.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ru.sasha77.spring.pepsbook.models.Mind;

public interface MindRepository extends PagingAndSortingRepository<Mind, Integer> {
//  Old variants wo search in answers
//    @Query(value = "from Mind m inner join fetch m.user u WHERE u.name LIKE %:subs%")
//    @Query(value = "from #{#entityName} m where m.text LIKE %:subs% OR m.user.name LIKE %:subs% order by m.time desc")
    @Query(value = "SELECT * FROM minds m" +
            " JOIN users u ON m.user_id = u.id" +
            " WHERE (EXISTS(SELECT * FROM friendship f WHERE f.user_id = :user_id AND f.friend_id = u.id)" +
            "    OR m.user_id = :user_id" +
            " ) AND (" +
            "       m.text LIKE CONCAT('%',:subs,'%')" +
            "    OR u.name LIKE CONCAT('%',:subs,'%')" +
            "    OR EXISTS(SELECT * FROM answers a WHERE a.mind_id=m.id AND a.text LIKE CONCAT('%',:subs,'%')))" +
            " ORDER BY m.time DESC"
            , nativeQuery=true)
    Page<Mind> findLike(@Param("subs") String subs, @Param("user_id") int userId, Pageable pageable);

    Mind findByTextContaining(@Param("subs") String name);
}
