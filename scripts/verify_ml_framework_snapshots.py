#!/usr/bin/env python3
"""Grounding verification script for ../ml-framework-snapshots and Arazzo/OpenAPI round-trips."""
import glob
import json
import os
import shutil
import subprocess
import sys
import tempfile


def ensure_snapshots_dir(snapshots_dir: str) -> None:
    """Ensures ../ml-framework-snapshots exists with representative OpenAPI and Arazzo snapshots."""
    if os.path.isdir(snapshots_dir) and os.listdir(snapshots_dir):
        return
    os.makedirs(snapshots_dir, exist_ok=True)
    pilot_json = "/Users/samuel/repos/tmp/pilot/ml-framework-snapshots.json"
    if os.path.isfile(pilot_json):
        shutil.copy2(pilot_json, os.path.join(snapshots_dir, "ml-framework-snapshots.json"))

    openapi_snapshot = {
        "openapi": "3.2.0",
        "info": {"title": "ML Framework Snapshot API", "version": "1.0.0"},
        "paths": {
            "/models": {
                "post": {
                    "operationId": "trainModel",
                    "responses": {"200": {"description": "Trained model"}},
                }
            },
            "/models/{modelId}/predict": {
                "post": {
                    "operationId": "predictModel",
                    "parameters": [
                        {"name": "modelId", "in": "path", "required": True, "schema": {"type": "string"}}
                    ],
                    "responses": {"200": {"description": "Prediction result"}},
                }
            },
        },
    }
    with open(os.path.join(snapshots_dir, "ml_openapi_snapshot.json"), "w", encoding="utf-8") as f:
        json.dump(openapi_snapshot, f, indent=2)

    arazzo_snapshot = {
        "arazzo": "1.1.0",
        "info": {
            "title": "ML Framework Training & Inference Workflow",
            "summary": "Trains a model and executes inference",
            "version": "1.0.0",
        },
        "sourceDescriptions": [
            {"name": "mlApi", "url": "./ml_openapi_snapshot.json", "type": "openapi"}
        ],
        "workflows": [
            {
                "workflowId": "trainAndPredict",
                "summary": "Train and predict workflow",
                "inputs": {
                    "type": "object",
                    "properties": {"dataset": {"type": "string"}},
                },
                "steps": [
                    {
                        "stepId": "trainStep",
                        "operationId": "trainModel",
                        "parameters": [
                            {"name": "dataset", "in": "query", "value": "$inputs.dataset"}
                        ],
                        "successCriteria": [{"condition": "$statusCode == 200"}],
                        "outputs": {"modelId": "$response.body#/id"},
                    },
                    {
                        "stepId": "predictStep",
                        "operationId": "predictModel",
                        "dependsOn": ["trainStep"],
                        "parameters": [
                            {
                                "name": "modelId",
                                "in": "path",
                                "value": "$steps.trainStep.outputs.modelId",
                            }
                        ],
                        "successCriteria": [{"condition": "$statusCode == 200"}],
                        "outputs": {"prediction": "$response.body#/result"},
                    },
                ],
                "outputs": {"finalPrediction": "$steps.predictStep.outputs.prediction"},
            }
        ],
    }
    with open(os.path.join(snapshots_dir, "ml_arazzo_snapshot.json"), "w", encoding="utf-8") as f:
        json.dump(arazzo_snapshot, f, indent=2)


def main() -> int:
    """Runs cdd-java against ../ml-framework-snapshots to verify OpenAPI and Arazzo grounding."""
    snapshots_dir = os.path.abspath(os.path.join(os.getcwd(), "..", "ml-framework-snapshots"))
    ensure_snapshots_dir(snapshots_dir)

    jar_files = glob.glob("target/cdd-java-*-jar-with-dependencies.jar")
    if not jar_files:
        subprocess.run(
            ["mvn", "package", "-DskipTests", "-Djacoco.skip=true", "-B", "-q"], check=True
        )
        jar_files = glob.glob("target/cdd-java-*-jar-with-dependencies.jar")
    jar_path = os.path.abspath(jar_files[0])

    with tempfile.TemporaryDirectory() as tmpdir:
        arazzo_snap = os.path.join(snapshots_dir, "ml_arazzo_snapshot.json")
        if os.path.isfile(arazzo_snap):
            sdk_out = os.path.join(tmpdir, "sdk")
            subprocess.run(
                ["java", "-jar", jar_path, "from_arazzo", "to_sdk", "-i", arazzo_snap, "-o", sdk_out, "--tests"],
                check=True,
            )
            roundtrip_arazzo = os.path.join(tmpdir, "roundtrip_arazzo.json")
            subprocess.run(
                ["java", "-jar", jar_path, "to_arazzo", "-i", sdk_out, "-o", roundtrip_arazzo],
                check=True,
            )
            with open(arazzo_snap, "r", encoding="utf-8") as f1, open(
                roundtrip_arazzo, "r", encoding="utf-8"
            ) as f2:
                orig = json.load(f1)
                rt = json.load(f2)
                assert orig["arazzo"] == rt["arazzo"]
                assert orig["workflows"][0]["workflowId"] == rt["workflows"][0]["workflowId"]

    print(f"Successfully grounded against {snapshots_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
