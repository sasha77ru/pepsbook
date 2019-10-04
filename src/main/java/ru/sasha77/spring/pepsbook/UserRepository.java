package ru.sasha77.spring.pepsbook;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Integer> {
    //UNUSED. THE SAME LIKE NEXT ONE
//    @Query(value = "SELECT * FROM users WHERE name LIKE CONCAT('%',:subs,'%') OR country LIKE CONCAT('%',:subs,'%')", nativeQuery=true)
    @Query(value = "from #{#entityName} where (name LIKE %:subs% OR country LIKE %:subs%)AND(id <> :currUserId) order by name")
    Iterable<User> findLike(@Param("subs") String name,@Param("currUserId") Integer currUserId);

//    Iterable<User> findUsersByNameContainsAndIdIsNotOrCountryContainsAndIdIsNot(String subs1, Integer id1, String subs2, Integer id2);

    User findByUsername(String username);
    User findByName(String username);
    User findByEmail(String name);
}
