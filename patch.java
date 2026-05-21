    @Test
    public void testWasiVirtualRoot() throws Exception {
        File wasiDir = Files.createTempDirectory("wasi").toFile();
        File absDir = new File(wasiDir, "absolute");
        absDir.mkdirs();
        File specFile = new File(absDir, "spec.json");
        Files.writeString(specFile.toPath(), "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");

        setEnv("CDD_WASI_VIRTUAL_ROOT", wasiDir.getAbsolutePath());
        try {
            runMain(new String[]{"from_openapi", "to_sdk", "-i", "/absolute/spec.json", "-o", "out"});
        } finally {
            setEnv("CDD_WASI_VIRTUAL_ROOT", null);
        }
    }
