package com.moneyCoreNews.security.repository;

import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    public AppRole findByRole(String role);

    public AppRole findById(Long id);

    @Query("select r from AppRole r  where r.role like :x")
    public AppRole findRole(@Param("x") String role);
}
