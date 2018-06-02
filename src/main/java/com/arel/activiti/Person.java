package com.arel.activiti;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Person {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
 
	private String name;
 
	private Date birthDate;
 
	public Person() {
	}
 
	public Person(String name, Date birthDate) {
		this.name = name;
		this.birthDate = birthDate;
	}
 
	public Long getId() {
		return id;
	}
 
	public void setId(Long id) {
		this.id = id;
	}
 
	public String getName() {
		return name;
	}
 
	public void setName(String name) {
		this.name = name;
	}
 
	public Date getBirthDate() {
		return birthDate;
	}
 
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
 
}