package com.moneyCoreNews.security.repository;

import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends JpaRepository<Token,String> {
    public Token findByTokenValue(String tokenValue);
	/*
	@Query("select r from Response r where r.examen=:x and r.etudiant=:y ") //on utilise une requette Hql qui order les comptes by date
	public List <Response> findByExEt(@Param("x")Exam idExam,@Param("y")Candidate idEtudiant);
	*/
}
