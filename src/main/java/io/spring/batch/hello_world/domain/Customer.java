package io.spring.batch.hello_world.domain;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
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

	private List<Transaction> transactions;

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
		StringBuilder output = new StringBuilder();

		output.append(firstName);
		output.append(" ");
		output.append(middleInitial);
		output.append(". ");
		output.append(lastName);

		if(transactions != null&& transactions.size() > 0) {
			output.append(" has ");
			output.append(transactions.size());
			output.append(" transactions.");
		} else {
			output.append(" has no transactions.");
		}

		return output.toString();
	}
}
