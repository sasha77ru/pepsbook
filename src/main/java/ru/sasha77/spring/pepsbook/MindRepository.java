package ru.sasha77.spring.pepsbook;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface MindRepository extends CrudRepository<Mind, Integer> {
//    @Query(value = "SELECT * FROM minds " +
//            " JOIN users ON minds.user_id = users.id" +
//            " WHERE text LIKE CONCAT('%',:subs,'%')" +
//            "    OR name LIKE CONCAT('%',:subs,'%')", nativeQuery=true)
//    @Query(value = "from Mind m inner join fetch m.user u"+
//            " WHERE u.name LIKE %:subs%")
    @Query(value = "from #{#entityName} m where m.text LIKE %:subs% OR m.user.name LIKE %:subs% order by m.time desc")
    Iterable<Mind> findLike(@Param("subs") String name);
}
