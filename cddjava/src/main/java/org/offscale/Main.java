package org.offscale;


public class Main {
    public static void main(String[] args) {
        Create create = new Create("OpenAPISpec1/openapi.yaml");
        System.out.println(create.generateRoutesAndTests());
    }
}