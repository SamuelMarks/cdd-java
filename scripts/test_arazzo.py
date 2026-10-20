#!/usr/bin/env python3
"""End-to-end verification script for Arazzo 1.1.0 SDK, CLI, and Server generation and round-tripping."""

import glob
import json
import os
import subprocess
import sys
import tempfile


def main() -> int:
    """Verifies bidirectional Arazzo 1.1.0 <-> Java SDK, CLI, and Server generation."""
    jar_files = glob.glob("target/cdd-java-*-jar-with-dependencies.jar")
    if not jar_files:
        subprocess.run(
            ["mvn", "package", "-DskipTests", "-Djacoco.skip=true", "-B", "-q"],
            check=True,
        )
        jar_files = glob.glob("target/cdd-java-*-jar-with-dependencies.jar")
    jar_path = os.path.abspath(jar_files[0])

    arazzo_doc = {
        "arazzo": "1.1.0",
        "$self": "https://example.com/workflows/petstore.arazzo.json",
        "info": {
            "title": "Petstore Order Workflow",
            "summary": "Login, find pet, and place order",
            "description": "End-to-end pet adoption workflow",
            "version": "1.1.0",
        },
        "sourceDescriptions": [
            {"name": "petstore", "url": "../petstore_oas3.json", "type": "openapi"}
        ],
        "workflows": [
            {
                "workflowId": "placePetOrder",
                "summary": "Find an available pet and place an order",
                "inputs": {
                    "type": "object",
                    "properties": {
                        "status": {"type": "string"},
                        "quantity": {"type": "integer"},
                    },
                },
                "steps": [
                    {
                        "stepId": "findPets",
                        "operationId": "findPetsByStatus",
                        "parameters": [
                            {"name": "status", "in": "query", "value": "$inputs.status"}
                        ],
                        "successCriteria": [{"condition": "$statusCode == 200"}],
                        "outputs": {"petId": "$response.body#/0/id"},
                    },
                    {
                        "stepId": "orderPet",
                        "operationId": "placeOrder",
                        "dependsOn": ["findPets"],
                        "requestBody": {
                            "contentType": "application/json",
                            "payload": {
                                "petId": "$steps.findPets.outputs.petId",
                                "quantity": 1,
                            },
                        },
                        "successCriteria": [{"condition": "$statusCode == 200"}],
                        "outputs": {"orderId": "$response.body#/id"},
                    },
                ],
                "outputs": {"createdOrderId": "$steps.orderPet.outputs.orderId"},
            }
        ],
    }

    with tempfile.TemporaryDirectory() as tmpdir:
        spec_file = os.path.join(tmpdir, "petstore.arazzo.json")
        with open(spec_file, "w", encoding="utf-8") as f:
            json.dump(arazzo_doc, f, indent=2)

        for target in ("to_sdk", "to_sdk_cli", "to_server"):
            out_dir = os.path.join(tmpdir, target)
            subprocess.run(
                [
                    "java",
                    "-jar",
                    jar_path,
                    "from_arazzo",
                    target,
                    "-i",
                    spec_file,
                    "-o",
                    out_dir,
                    "--tests",
                ],
                check=True,
            )
            rt_file = os.path.join(tmpdir, f"rt_{target}.json")
            subprocess.run(
                ["java", "-jar", jar_path, "to_arazzo", "-i", out_dir, "-o", rt_file],
                check=True,
            )
            with open(rt_file, "r", encoding="utf-8") as rf:
                rt = json.load(rf)
                assert rt["arazzo"] == "1.1.0"
                assert rt["info"]["title"] == "Petstore Order Workflow"
                assert len(rt["workflows"]) == 1
                assert rt["workflows"][0]["workflowId"] == "placePetOrder"
                assert len(rt["workflows"][0]["steps"]) == 2

    print("Arazzo 1.1.0 SDK, CLI, and Server verification passed!")
    return 0


if __name__ == "__main__":
    sys.exit(main())
