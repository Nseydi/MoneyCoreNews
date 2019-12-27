package com.moneyCoreNews.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Entity
@DiscriminatorValue("SimpleToken")
public class SimpleToken extends Token {

	private LocalDate startDate;
	private int EXPIRATION_TIME = 172800;
	
	public SimpleToken() {
		super();
	}
	
	public SimpleToken(String tokenValue, LocalDate startDate) {
		super(tokenValue);
		this.startDate = startDate;
	}

	public SimpleToken(String tokenValue, LocalDate startDate, int eXPIRATION_TIME) {
		super(tokenValue);
		this.startDate = startDate;
		EXPIRATION_TIME = eXPIRATION_TIME;
	}


	public int getEXPIRATION_TIME() {
		return EXPIRATION_TIME;
	}

	public void setEXPIRATION_TIME(int eXPIRATION_TIME) {
		EXPIRATION_TIME = eXPIRATION_TIME;
	}
	
    @Override
	public boolean checkValidToken() {
		// LocalDateTime oldDate = LocalDateTime.of(2018, Month.MAY, 6, 10, 20, 55);
		Period period = Period.between(this.startDate, LocalDate.now());
		Date date = Date.from(this.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()); //convert LocalDate to Date
		LocalDateTime cd = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();  //convert Date to LocalDateTime
		long seconds = ChronoUnit.SECONDS.between(cd, LocalDateTime.now());
		
		if(period.getYears() == 0 && period.getMonths() == 0 && period.getDays() == 0 && seconds < this.getEXPIRATION_TIME()) {
			return true;
		}
		
		return false;
	}
	
}
