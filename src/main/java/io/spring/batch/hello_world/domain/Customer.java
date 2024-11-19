package io.spring.batch.hello_world.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@XStreamAlias("customer")
public class Customer {

	@XStreamAlias("firstName")
    private String firstName;
	@XStreamAlias("middleInitial")
    private String middleInitial;
	@XStreamAlias("lastName")
    private String lastName;
	@XStreamAlias("addressNumber")
    private String addressNumber;
	@XStreamAlias("street")
	private String street;
	@XStreamAlias("address")
	private String address;
	@XStreamAlias("city")
    private String city;
	@XStreamAlias("state")
    private String state;
	@XStreamAlias("zipCode")
    private String zipCode;

	@XStreamImplicit
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
