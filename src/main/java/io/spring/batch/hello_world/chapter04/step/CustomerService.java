package io.spring.batch.hello_world.chapter04.step;

import org.springframework.stereotype.Component;

//@Component
public class CustomerService {

    public void serviceMethod(){
        System.out.println("Service method was called");
    }
    public void serviceMethod2(String message){
        System.out.println(message);
    }
}
