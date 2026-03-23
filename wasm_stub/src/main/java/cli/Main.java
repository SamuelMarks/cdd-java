package cli;
public class Main {
    public static void main(String[] args) {
        System.err.println("cdd-java WASM natively is unsupported due to heavy Reflection, java.nio, and Sockets usage.");
        System.err.println("The Java ecosystem currently lacks a production-ready standalone WASI compiler for complex applications.");
        System.err.println("Please run the provided Docker image or the .jar directly.");
        
    }
}
