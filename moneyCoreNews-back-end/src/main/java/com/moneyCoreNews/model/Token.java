package com.moneyCoreNews.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE_Token", discriminatorType = DiscriminatorType.STRING, length = 35)
public class Token {
	
    @Id
	private String tokenValue;
	
	public Token() {
		super();
	}

	public Token(String tokenValue) {
		super();
		this.tokenValue = tokenValue;
	}

	public String getTokenValue() {
		return tokenValue;
	}

	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}
	
	public boolean checkValidToken() {
		return false;
	}
	public boolean checkValidToken1(LocalDateTime d1) {
		return false;
	}
}
