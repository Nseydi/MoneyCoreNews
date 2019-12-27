package com.moneyCoreNews.security.repository;

import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AUserRepository extends CrudRepository<AUser, Long>, JpaSpecificationExecutor<AUser> {

    AUser findByEmail(String email);

    AUser findById(Long id);

    AUser findByUsername(String userName);

    AUser findByConfirmationToken(String confirmationToken);

    @Query("select u.roles from  AUser u where u.email =:email")
    List<AppRole> findAllAUserRoles(@Param("email") String email);

    @Query("select u from  AUser u where u.enabled =false")
    List<AUser> findUnActivatedAccounts();

    @Query("select u from  AUser u join u.roles  d where d = ?1")
    List<AUser> findAllAdmin(AppRole role);
}