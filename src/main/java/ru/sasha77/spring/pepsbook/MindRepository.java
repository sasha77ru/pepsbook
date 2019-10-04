package ru.sasha77.spring.pepsbook;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MindRepository extends CrudRepository<Mind, Integer> {
//  Old variants wo search in answers
//    @Query(value = "from Mind m inner join fetch m.user u WHERE u.name LIKE %:subs%")
//    @Query(value = "from #{#entityName} m where m.text LIKE %:subs% OR m.user.name LIKE %:subs% order by m.time desc")
    @Query(value = "SELECT * FROM minds m" +
            " JOIN users u ON m.user_id = u.id" +
            " WHERE m.text LIKE CONCAT('%',:subs,'%')" +
            "    OR u.name LIKE CONCAT('%',:subs,'%')" +
            "    OR EXISTS(SELECT * FROM answers a WHERE a.mind_id=m.id AND a.text LIKE CONCAT('%',:subs,'%'))"
            , nativeQuery=true)
    Iterable<Mind> findLike(@Param("subs") String name);
}
