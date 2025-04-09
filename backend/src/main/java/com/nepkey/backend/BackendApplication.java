package com.nepkey.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	private int egy = 1;

    public int getEgy() {
        return egy;
    }

    public void setEgy(int egy) {
        this.egy = egy;
    }
}
