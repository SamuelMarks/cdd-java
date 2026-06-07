#!/bin/bash
sed -i '' 's/public static class JSONRPCMessage {}/public static class JSONRPCMessage { @JsonProperty("jsonrpc") public String jsonrpc = "2.0"; }/g' src/main/java/classes/Emit.java
