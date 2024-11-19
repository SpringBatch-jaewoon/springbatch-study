package io.spring.batch.hello_world.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "customer")
public class Customer {

	@Id
	private Long id;

	private String firstName;
    private String middleInitial;
    private String lastName;
//    private String addressNumber;
//	private String street;
	private String address;
    private String city;
    private String state;
    private String zipCode;

    public Customer(){
    }
    public Customer(String firstName, String middleInitial, String lastName, String city, String state, String zipCode) {
		this.firstName = firstName;
		this.middleInitial = middleInitial;
		this.lastName = lastName;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}
}
