package io.spring.batch.hello_world.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {

    private String firstName;
    private String middleInitial;
    private String lastName;
    private String addressNumber;
	private String street;
	private String address;
    private String city;
    private String state;
    private String zipCode;

    public Customer(){
    }
    public Customer(String firstName, String middleInitial, String lastName, String addressNumber, String street, String city, String state, String zipCode) {
		this.firstName = firstName;
		this.middleInitial = middleInitial;
		this.lastName = lastName;
		this.addressNumber = addressNumber;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}

    @Override
	public String toString() {
		return "Customer{" +
				"firstName='" + firstName + '\'' +
				", middleInitial='" + middleInitial + '\'' +
				", lastName='" + lastName + '\'' +
				", address='" + address + '\'' +
				", addressNumber='" + addressNumber + '\'' +
				", street='" + street + '\'' +
				", city='" + city + '\'' +
				", state='" + state + '\'' +
				", zipCode='" + zipCode + '\'' +
				'}';
	}


}
